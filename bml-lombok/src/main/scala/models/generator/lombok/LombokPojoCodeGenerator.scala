package models.generator.lombok

import java.lang.IllegalArgumentException

import bml.util.AnotationUtil.singular
import bml.util.java.ClassNames.{builder, _}
import bml.util.java.{ClassNames, JavaDataTypes, JavaEnums, JavaPojoUtil, JavaPojos}
import bml.util.{AnotationUtil, FieldUtil, NameSpaces, SpecValidation}
import bml.util.GeneratorFSUtil.makeFile
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.squareup.javapoet.{ClassName, TypeSpec, _}
import io.apibuilder.generator.v0.models.{File, InvocationForm}
import io.apibuilder.spec.v0.models.{Attribute, Enum, Field, Model, Service, Union}
import javax.lang.model.element.Modifier._
import lib.generator.CodeGenerator
import lombok.experimental.Accessors
import org.checkerframework.checker.units.qual.min
import play.api.{Logger, PlayException, UsefulException}
import lib.Text._
import org.apache.commons.lang3.StringUtils
import views.html.defaultpages
import views.html.defaultpages.error

import scala.collection.JavaConverters._

trait LombokPojoCodeGenerator extends CodeGenerator with JavaPojoUtil {

  val logger: Logger = Logger.apply(this.getClass())

  def getJavaDocFileHeader(): String //abstract

  override def invoke(form: InvocationForm): Either[Seq[String], Seq[File]] = invoke(form, addHeader = true)

  def invoke(form: InvocationForm, addHeader: Boolean = false): Either[Seq[String], Seq[File]] = generateCode(form, addHeader)

  private def generateCode(form: InvocationForm, addHeader: Boolean = true): Either[Seq[String], Seq[File]] = {
    val header =
      if (addHeader) Some(new ApidocComments(form.service.version, form.userAgent).forClassFile)
      else None
    new Generator(form.service, header).generateSourceFiles()
  }


  class Generator(service: Service, header: Option[String]) {

    private val nameSpaces = new NameSpaces(service)

    private val nameSpace = makeNameSpace(service.namespace)
    private val modelsNameSpace = nameSpace + ".models"
    private val modelsDirectoryPath = createDirectoryPath(modelsNameSpace)

    private val apiDocComments = {
      val s = getJavaDocFileHeader + "\n"
      header.fold(s)(_ + "\n" + s)
    }

    val T = "$T" //this is a hack for substituting types, as "$" is used by scala to do string substitution, and $T is used by javapoet to handle types

    def createDirectoryPath(namespace: String) = namespace.replace('.', '/')

    def generateSourceFiles(): Either[Seq[String], Seq[File]] = {
      val errors = SpecValidation.validate(service: Service, header: Option[String])
      if (errors.isDefined) {
        return Left(errors.get)
      }

      Right(generateEnums(service) ++
        generateUnionTypes(service) ++
        generateModels(service))
    }

    def generateEnums(service: Service): Seq[File] = {
      service.enums.map(generateEnum)
    }

    def generateModels(service: Service): Seq[File] = {
      service.models.map { model =>
        val relatedUnions = service.unions.filter(_.types.exists(_.`type` == model.name))
        generateModel(model, relatedUnions)
      }
    }

    def generateUnionTypes(service: Service): Seq[File] = {
      service.unions.map(generateUnionType)
    }


    def generateEnum(enum: Enum): File = {
      val className = toClassName(enum.name)
      val builder = JavaEnums.standardEnumBuilder(enum, apiDocComments)
      enum.values.foreach(value => {
        val enumValBuilder = TypeSpec.anonymousClassBuilder("$S", value.name)
        if (value.description.isDefined) enumValBuilder.addJavadoc(value.description.get)
        builder.addEnumConstant(toEnumName(value.name), enumValBuilder.build())
      })
      makeFile(className, nameSpaces.model, builder)
    }

    def generateUnionType(union: Union): File = {
      val className = toClassName(union.name)
      val builder = TypeSpec.interfaceBuilder(className)
        .addModifiers(PUBLIC)
        .addJavadoc(apiDocComments)
      union.description.map(builder.addJavadoc(_))
      makeFile(className, nameSpaces.model, builder)
    }

    def generateModel(model: Model, relatedUnions: Seq[Union]): File = {
      val className = toClassName(model.name)
      logger.info(s"Generating Model Class ${className}")

      val classBuilder = TypeSpec.classBuilder(className)
        .addModifiers(PUBLIC)
        .addJavadoc(apiDocComments)
        .addJavadoc("\n")
        .addJavadoc(model.description.getOrElse(""))
        .addAnnotation(AnnotationSpec.builder(classOf[Accessors])
          .addMember("fluent", CodeBlock.builder().add("true").build).build())
        .addAnnotations(
          Seq(builder, allArgsConstructor, noArgsConstructor, fieldNameConstants)
            .map(AnnotationSpec.builder(_).build()).asJava
        )
        .addAnnotation(
          AnnotationSpec.builder(classOf[JsonIgnoreProperties])
            .addMember("ignoreUnknown", CodeBlock.builder().add("true").build).build()
        )
      // Add in static booleans for each field to tell if the field is required.
      model.fields.foreach(JavaPojos.handleRequiredFieldAddition(classBuilder, _))

      val constructorWithParams = MethodSpec.constructorBuilder().addModifiers(PUBLIC)
      val constructorWithoutParams = MethodSpec.constructorBuilder().addModifiers(PUBLIC)
      val unionClassTypeNames = relatedUnions.map { u => ClassName.get(modelsNameSpace, toClassName(u.name)) }
      classBuilder.addSuperinterfaces(unionClassTypeNames.asJava)
      //      val shouldAnnotateAsDyanmoDb = isDynamoDbModel(model)

      model.fields.foreach(field => {
        val javaDataType = dataTypeFromField(field.`type`, modelsNameSpace)

        val fieldBuilder = FieldSpec.builder(javaDataType, toParamName(field.name, true))
          .addModifiers(PROTECTED)
          .addAnnotation(AnotationUtil.jsonProperty(field.name, field.required))
          .addAnnotation(getter)

        if (isParameterArray(field.`type`) || isParameterMap(field.`type`)) {
          fieldBuilder.addAnnotation(singular)
        }


        if (field.required) {
          fieldBuilder.addAnnotation(AnotationUtil.notNull)
        }
        if (field.minimum.isDefined || field.maximum.isDefined) {
          try {
            fieldBuilder.addAnnotation(JavaPojos.handleSizeAttribute(classBuilder, field))
          } catch {
            case x: IllegalArgumentException => {
              throw new PlayException("Validation Issue", x.getMessage, x)
            }
          }
        }


        ///////////////////////////////////////
        //Deal with javadocs
        val docString = FieldUtil.javaDoc(field)
        if (docString.isDefined) {
          fieldBuilder.addJavadoc(docString.get)
        }
        ///////////////////////////////////////

        field.attributes.foreach(attribute => {
          logger.info(s"Working on Attribute ${attribute.name}")
          attribute.name match {
            case "pattern" => {
              fieldBuilder.addAnnotation(AnotationUtil.pattern(attribute))
            }
            case "email" => {
              fieldBuilder.addAnnotation(ClassNames.email)
            }
            case _ =>
          }
        })
        classBuilder.addField(fieldBuilder.build)
      })
      makeFile(className, nameSpaces.model, classBuilder)
    }


    private def toMap(cc: AnyRef): Map[String, Any] =
      (Map[String, Any]() /: cc.getClass.getDeclaredFields) { (a, f) =>
        f.setAccessible(true)
        a + (f.getName -> f.get(cc))
      }

    private def commentFromOpt(opt: Option[String]) = {
      opt.fold("") { s => textToComment(s) + "\n" }
    }

    def camelToUnderscores(name: String): String = "[A-Z\\d]".r.replaceAllIn(name, { m =>
      "_" + m.group(0).toLowerCase()
    })


    def underscoreToCamel(name: String): String = "_([a-z\\d])".r.replaceAllIn(name, { m =>
      m.group(1).toUpperCase()
    })

    //def makeFile(name: String, builder: TypeSpec.Builder): File = {
    //      makeFile(name, nameSpaces.model, builder)
    //}

  }

}
