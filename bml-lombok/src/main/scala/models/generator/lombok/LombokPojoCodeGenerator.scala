package models.generator.lombok

import bml.util.AnotationUtil.JavaxAnnotations.{JavaxPersistanceAnnotations, JavaxValidationAnnotations}
import bml.util.AnotationUtil.{JacksonAnno, LombokAnno}
import bml.util.GeneratorFSUtil.makeFile
import bml.util.attribute.Hibernate
import bml.util.java.ClassNames.JavaxTypes.JavaxValidationTypes
import bml.util.java.ClassNames._
import bml.util.java.{JavaEnums, JavaPojoUtil, JavaPojos}
import bml.util.jpa.JPA
import bml.util.{FieldUtil, NameSpaces, SpecValidation}
import com.squareup.javapoet.{ClassName, TypeSpec, _}
import io.apibuilder.generator.v0.models.{File, InvocationForm}
import io.apibuilder.spec.v0.models.{Enum, Model, Service, Union}
import javax.lang.model.element.Modifier._
import javax.persistence.{EnumType, Enumerated}
import lib.generator.CodeGenerator
import play.api.Logger

import scala.collection.JavaConverters._

/**
 * Generator for Lombok based pojos for models.
 */
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

    private val apiDocComments = {
      val s = getJavaDocFileHeader + "\n"
      header.fold(s)(_ + "\n" + s)
    }

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
        val enumValBuilder = TypeSpec.anonymousClassBuilder("$S,$S", value.name, value.description.getOrElse(""))
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

      //      union.types.foreach(
      //        `type` => {
      //          var foo = service.models.find(
      //            model => model.name == `type`.`type`
      //          )
      //          if (foo.isDefined) {
      //            val model = foo.get
      //
      //            //          model.fields.foreach(
      //            //          field =>
      //            //field
      //
      //            //            )
      //
      //
      //          }
      //
      //        }
      //      )
      makeFile(className, nameSpaces.model, builder)
    }

    def generateModel(model: Model, relatedUnions: Seq[Union]): File = {
      //Should we add in hibernate
      val useHibernate = Hibernate.fromModel(model).use;

      val className = toClassName(nameSpaces.model, model)
      //logger.info(s"Generating Model Class ${className}")


      val baseAnnotations = Seq(
        LombokAnno.Data,
        LombokAnno.Builder,
        LombokAnno.AccessorFluent,
        LombokAnno.EqualsAndHashCode,
        LombokAnno.AllArgsConstructor,
        LombokAnno.NoArgsConstructor,
        LombokAnno.FieldNameConstants,
        JacksonAnno.JsonIncludeNON_EMPTY,
        JacksonAnno.JsonIgnoreProperties_Ignore_unknown
      ).asJava


      val classJavadoc = Seq(apiDocComments, model.description).mkString("\n")


      val classBuilder = TypeSpec.classBuilder(className)
        .addModifiers(PUBLIC)
        .addJavadoc(classJavadoc)
        .addAnnotations(baseAnnotations)
        .addField(FieldUtil.serialVersionUID(service))
        .addField(JavaPojos.makeRequiredFieldsField(model))

      val unionClassTypeNames = relatedUnions.map { u => ClassName.get(nameSpaces.model.nameSpace, toClassName(u.name)) }
      classBuilder.addSuperinterfaces(unionClassTypeNames.asJava)

      JavaPojos.getStringValueLengthStaticFields(model)
        .foreach(classBuilder.addField(_))

      JavaPojos.getListSizeStaticFields(model)
        .foreach(classBuilder.addField(_))

      JavaPojos.getSizeStaticFields(model)
        .foreach(classBuilder.addField(_))


      if (useHibernate) {
        classBuilder.addAnnotation(JavaxPersistanceAnnotations.Entity)
        classBuilder.addAnnotation(JavaxPersistanceAnnotations.Table(model))

      }

      model.fields.foreach(field => {
        val javaDataType = dataTypeFromFieldArraySupport(service, field, nameSpaces.model.nameSpace)


        val fieldBuilder = FieldSpec.builder(javaDataType, toParamName(field.name, true))
          .addModifiers(PROTECTED)
          .addAnnotation(LombokAnno.Getter)

        if (useHibernate) {
          if (JavaPojoUtil.isEnumType(service, field)) {
            fieldBuilder.addAnnotation(
              AnnotationSpec.builder(classOf[Enumerated])
                .addMember("value", "$T.STRING", classOf[EnumType])
                .build()
            )
          }
        }


        if (field.`type` == "integer" && field.default.isDefined) {

          fieldBuilder.addAnnotation(LombokTypes.BuilderDefault)
            .initializer("$L", field.default.get)


        }

        if (isParameterArray(field.`type`) || isParameterMap(field.`type`)) {
          fieldBuilder.addAnnotation(LombokAnno.Singular)

        }
        if (field.required) {
          fieldBuilder.addAnnotation(JavaxValidationAnnotations.NotNull)

        }
        //        if (JavaPojoUtil.isParameterArray(field.`type`)) {
        //          if (field.required) {
        //            fieldBuilder.addAnnotation(JavaxValidationAnnotations.NotEmpty)
        //          }
        //        }

        val sizeAnnotation = JavaPojos.handleSizeAttribute(className, field)
        if (sizeAnnotation.isDefined) {
          fieldBuilder.addAnnotation(sizeAnnotation.get)
        }

        if (useHibernate) {
          JavaPojos.handlePersisitanceAnnontations(service, className, field).foreach(fieldBuilder.addAnnotation(_))
        }
        fieldBuilder.addAnnotation(JacksonAnno.JsonProperty(field.name, field.required))
        if (field.required) {
          fieldBuilder.addAnnotation(JacksonAnno.JsonIncludeALLWAYS)
        }

        if (
          field.`type`.equals("boolean")
            &&
            field.default.isDefined
        ) {

          val default = field.default.get
          if (default.equals("true") || default.equals("false"))
            fieldBuilder.addAnnotation(LombokTypes.BuilderDefault)
          fieldBuilder.initializer("$L", default)


        }


        ///////////////////////////////////////
        //Deal with javadocs
        val docString = FieldUtil.javaDoc(field)
        if (docString.isDefined) {
          fieldBuilder.addJavadoc(docString.get)
        }
        ///////////////////////////////////////

        field.attributes.foreach(attribute => {
          //logger.info(s"Working on Attribute ${attribute.name}")
          attribute.name match {
            case "pattern" => {
              fieldBuilder.addAnnotation(JavaxValidationAnnotations.Pattern(attribute))
            }
            case "email" => {
              fieldBuilder.addAnnotation(JavaxValidationTypes.Email)
            }
            case _ =>
          }
        })
        classBuilder.addField(fieldBuilder.build)
      })

      if (useHibernate) {
        JPA.addJPAStandardFields(model).foreach(classBuilder.addField)
      }
      makeFile(className.simpleName(), nameSpaces.model, classBuilder)
    }

  }

}
