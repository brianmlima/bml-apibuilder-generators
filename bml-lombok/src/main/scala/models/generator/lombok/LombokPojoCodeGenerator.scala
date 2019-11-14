package models.generator.lombok

import bml.util.java.JavaPojoUtil
import bml.util.{AnotationUtil, FieldUtil}
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.squareup.javapoet.{TypeSpec, _}
import io.apibuilder.generator.v0.models.{File, InvocationForm}
import io.apibuilder.spec.v0.models.{Enum, Model, Operation, Resource, Service, Union}
import javax.lang.model.element.Modifier
import javax.validation.constraints.Email
import lib.generator.{CodeGenerator, GeneratorUtil}
import lombok.experimental.Accessors
import play.api.Logger

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

      val generatedResolvers = generateResources(service.resources)

      generatedEnums ++
        generatedUnionTypes ++
        generatedModels ++
        generatedResolvers
    }

    def generateResources(resources: Seq[Resource]): Seq[File] = {
      //Resolves data types for built in types and models
      val datatypeResolver = GeneratorUtil.datatypeResolver(service)

      def generateOperation(resource: Resource, operation: Operation): Option[MethodSpec] = {
        //val gqlMethodModel = GqlMethodModel(datatypeResolver,)
        Option.empty
      }

      val methodSpecs = resources.map(resource => resource.operations.map(generateOperation(resource, _))).flatten.flatten
      val interfaceName = toClassName("query_resolver")
      var builder = TypeSpec.interfaceBuilder(interfaceName)
      methodSpecs.foreach(builder.addMethod)
      Seq(makeFile(interfaceName, builder))
    }


    def generateEnum(enum: Enum): File = {

      val className = toClassName(enum.name)

      val builder =
        TypeSpec.enumBuilder(className)
          .addModifiers(Modifier.PUBLIC)
          .addJavadoc(apiDocComments)

      enum.attributes.foreach(attribute => {
        attribute.name match {
          case _ => {}
        }

      })

      enum.description.map(builder.addJavadoc(_))

      enum.values.foreach(value => {

        logger.warn(value.name)
        builder.addEnumConstant(toEnumName(value.name))
      })

      val nameFieldType = classOf[String]

      val constructorWithParams = MethodSpec.constructorBuilder()
      builder.addMethod(constructorWithParams.build())

      makeFile(className, builder)

    }

    def generateUnionType(union: Union): File = {
      val className = toClassName(union.name)
      val builder = TypeSpec.interfaceBuilder(className)
        .addModifiers(Modifier.PUBLIC)
        .addJavadoc(apiDocComments)
      union.description.map(builder.addJavadoc(_))
      makeFile(className, builder)
    }

    def generateModel(model: Model, relatedUnions: Seq[Union]): File = {
      val className = toClassName(model.name)
      logger.info("Generating Model Class ${className}")

      def addDataClassAnnotations(builder: TypeSpec.Builder) {
        builder.addAnnotation(AnnotationSpec.builder(classOf[Accessors])
          .addMember("fluent", CodeBlock.builder().add("true").build).build())
          .addAnnotation(classOf[lombok.Builder])
          .addAnnotation(classOf[lombok.AllArgsConstructor])
          .addAnnotation(classOf[lombok.NoArgsConstructor])
          .addAnnotation(
            AnnotationSpec.builder(classOf[JsonIgnoreProperties])
              .addMember("ignoreUnknown", CodeBlock.builder().add("true").build).build()
          )

      }

      val builder = TypeSpec.classBuilder(className)
        .addModifiers(Modifier.PUBLIC)
        .addJavadoc(apiDocComments)

      model.description.map(builder.addJavadoc(_))

      addDataClassAnnotations(builder)




      //Eventually do something with the model attributes.
      model.attributes.foreach(attribute => {
        attribute.name match {
          case _ => {}
        }
      })

      val constructorWithParams = MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC)

      val constructorWithoutParams = MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC)

      val unionClassTypeNames = relatedUnions.map { u => ClassName.get(modelsNameSpace, toClassName(u.name)) }
      builder.addSuperinterfaces(unionClassTypeNames.asJava)
      //      val shouldAnnotateAsDyanmoDb = isDynamoDbModel(model)

      model.fields.foreach(field => {
        val javaDataType = dataTypeFromField(field.`type`, modelsNameSpace)

        val fieldBuilder = FieldSpec.builder(
          javaDataType,
          toParamName(field.name, true)
        ).addModifiers(Modifier.PROTECTED)

        if (field.required) {
          fieldBuilder.addAnnotation(AnotationUtil.notNull)
        }
        if (field.minimum.isDefined || field.maximum.isDefined) {
          fieldBuilder.addAnnotation(AnotationUtil.size(field.minimum, field.maximum))
        }


        ///////////////////////////////////////
        //Deal with javadocs
        val docString = FieldUtil.javaDoc(field)
        if (docString.isDefined) {
          fieldBuilder.addJavadoc(docString.get)
        }
        ///////////////////////////////////////

        field.attributes.foreach(attribute => {
          attribute.name match {
            case "size" => {
              fieldBuilder.addAnnotation(AnotationUtil.size(attribute))
            }
            case "pattern" => {
              fieldBuilder.addAnnotation(AnotationUtil.pattern(attribute))
            }
            case "email" => {
              fieldBuilder.addAnnotation(classOf[Email])
            }
            case _ =>
          }
        })
        builder.addField(fieldBuilder.build)
      })
      makeFile(className, builder)
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

    def makeFile(name: String, builder: TypeSpec.Builder): File = {
      File(s"${name}.java", Some(modelsDirectoryPath), JavaFile.builder(modelsNameSpace, builder.build).build.toString)
    }

  }

}
