package models.generator.lombok

import bml.util.java.ClassNames.{builder, _}
import bml.util.java.{ClassNames, JavaDataTypes, JavaEnums, JavaPojoUtil}
import bml.util.{AnotationUtil, FieldUtil, NameSpaces}
import bml.util.GeneratorFSUtil.makeFile
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.squareup.javapoet.{ClassName, TypeSpec, _}
import io.apibuilder.generator.v0.models.{File, InvocationForm}
import io.apibuilder.spec.v0.models.{Attribute, Enum, Field, Model, Service, Union}
import javax.lang.model.element.Modifier._
import lib.generator.CodeGenerator
import lombok.experimental.Accessors
import org.checkerframework.checker.units.qual.min
import play.api.Logger
import lib.Text._
import org.apache.commons.lang3.StringUtils

import scala.collection.JavaConverters._

trait LombokPojoCodeGenerator extends CodeGenerator with JavaPojoUtil {

  val logger: Logger = Logger.apply(this.getClass())

  def getJavaDocFileHeader(): String //abstract


  override def invoke(form: InvocationForm): Either[Seq[String], Seq[File]] = invoke(form, addHeader = true)

  def invoke(form: InvocationForm, addHeader: Boolean = false): Either[Seq[String], Seq[File]] = Right(generateCode(form, addHeader))

  private def generateCode(form: InvocationForm, addHeader: Boolean = true): Seq[File] = {
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

    def generateSourceFiles() = {

      //Build enum classes
      val generatedEnums = service.enums.map {
        generateEnum
      }

      //Build Union Types
      val generatedUnionTypes = service.unions.map {
        generateUnionType
      }

      val generatedModels = service.models.map { model =>
        val relatedUnions = service.unions.filter(_.types.exists(_.`type` == model.name))
        generateModel(model, relatedUnions)
      }

      //      val generatedResolvers = generateResources(service.resources)

      generatedEnums ++
        generatedUnionTypes ++
        generatedModels
      //        generatedResolvers
    }

    //    def generateResources(resources: Seq[Resource]): Seq[File] = {
    //      //Resolves data types for built in types and models
    //      val datatypeResolver = GeneratorUtil.datatypeResolver(service)
    //
    //      def generateOperation(resource: Resource, operation: Operation): Option[MethodSpec] = {
    //        //val gqlMethodModel = GqlMethodModel(datatypeResolver,)
    //        Option.empty
    //      }
    //
    //      val methodSpecs = resources.map(resource => resource.operations.map(generateOperation(resource, _))).flatten.flatten
    //      val interfaceName = toClassName("query_resolver")
    //      var builder = TypeSpec.interfaceBuilder(interfaceName)
    //      methodSpecs.foreach(builder.addMethod)
    //      Seq(makeFile(interfaceName, builder))
    //    }


    def generateEnum(enum: Enum): File = {

      val className = toClassName(enum.name)

      //val stringValueParam = "value"


      val builder = JavaEnums.standardEnumBuilder(enum, apiDocComments)

      enum.attributes.foreach(attribute => {
        attribute.name match {
          case _ => {}
        }
      })

      enum.values.foreach(value => {
        val enumValBuilder = TypeSpec.anonymousClassBuilder("$S", value.name)
        if (value.description.isDefined) enumValBuilder.addJavadoc(value.description.get)
        builder.addEnumConstant(toEnumName(value.name), enumValBuilder.build())
      })

      makeFile(className, nameSpaces.model, builder)

      //makeFile(className, builder)

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

      def addDataClassAnnotations(classBuilder: TypeSpec.Builder) {
        classBuilder.addAnnotation(AnnotationSpec.builder(classOf[Accessors])
          .addMember("fluent", CodeBlock.builder().add("true").build).build())
          .addAnnotation(builder)
          .addAnnotation(allArgsConstructor)
          .addAnnotation(noArgsConstructor)
          .addAnnotation(fieldNameConstants)
          .addAnnotation(
            AnnotationSpec.builder(classOf[JsonIgnoreProperties])
              .addMember("ignoreUnknown", CodeBlock.builder().add("true").build).build()
          )
      }

      val classBuilder = TypeSpec.classBuilder(className)
        .addModifiers(PUBLIC)
        .addJavadoc(apiDocComments)

      model.description.map(classBuilder.addJavadoc(_))
      addDataClassAnnotations(classBuilder)
      //Eventually do something with the model attributes.
      model.attributes.foreach(attribute => {
        attribute.name match {
          case _ => {}
        }
      })

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
          .addAnnotation(ClassNames.getter)

        if (isParameterArray(field.`type`) || isParameterMap(field.`type`)) {
          fieldBuilder.addAnnotation(AnotationUtil.singular)
        }


        if (field.required) {
          fieldBuilder.addAnnotation(AnotationUtil.notNull)
        }
        if (field.minimum.isDefined || field.maximum.isDefined) {

          def handleSizeAttribute(field: Field) = {
            logger.info("handleSizeAttribute")
            val isString = (field.`type` == "string")
            val minStaticParamName = toStaticFieldName(field.name) + "_MIN" + (if (isString) "_LENGTH" else "_SIZE")
            val maxStaticParamName = toStaticFieldName(field.name) + "_MAX" + (if (isString) "_LENGTH" else "_SIZE")
            val spec = AnnotationSpec.builder(ClassNames.size)
            if (field.minimum.isDefined) {
              spec.addMember("min", "$L", minStaticParamName)
              classBuilder.addField(
                FieldSpec.builder(TypeName.INT, minStaticParamName, PUBLIC, STATIC, FINAL)
                  .initializer("$L", field.minimum.get.toInt.toString)
                  .build()
              )
            }
            if (field.maximum.isDefined) {
              spec.addMember("max", "$L", maxStaticParamName)
              classBuilder.addField(
                FieldSpec.builder(TypeName.INT, maxStaticParamName, PUBLIC, STATIC, FINAL)
                  .initializer("$L", field.maximum.get.toString)
                  .build()
              )
            }
            spec.build()
          }

          fieldBuilder.addAnnotation(handleSizeAttribute(field))
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

  def handleSizeAttribute(classSpec: TypeSpec.Builder, field: Field) = {
    logger.info("handleSizeAttribute")
    val isString = (field.`type` == "string")
    val minStaticParamName = toStaticFieldName(field.name) + "_MIN" + (if (isString) "_LENGTH" else "_SIZE")
    val maxStaticParamName = toStaticFieldName(field.name) + "_MAX" + (if (isString) "_LENGTH" else "_SIZE")
    val spec = AnnotationSpec.builder(ClassNames.size)
    if (field.minimum.isDefined) {
      spec.addMember("min", "$L", minStaticParamName)
      classSpec.addField(
        FieldSpec.builder(TypeName.INT, minStaticParamName, PUBLIC, STATIC, FINAL)
          .initializer("$L", field.minimum.get.toInt.toString)
          .build()
      )
    }
    if (field.maximum.isDefined) {
      spec.addMember("max", "$L", maxStaticParamName)
      classSpec.addField(
        FieldSpec.builder(TypeName.INT, maxStaticParamName, PUBLIC, STATIC, FINAL)
          .initializer("$L", field.maximum.get.toString)
          .build()
      )
    }
    spec.build()
  }


}
