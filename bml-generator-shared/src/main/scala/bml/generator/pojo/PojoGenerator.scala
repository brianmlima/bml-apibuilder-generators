package bml.generator.pojo

import akka.actor.FSM.->
import bml.util.AnotationUtil.{JacksonAnno, LombokAnno}
import bml.util.GeneratorFSUtil
import bml.util.{FieldUtil, GeneratorFSUtil, JavaNameSpace, NameSpaces, SpecValidation}
import bml.util.attribute.{Converters, Hibernate, JsonName, Singular, SnakeCase}
import bml.util.java.ClassNames.{AtlasTypes, JacksonTypes, JavaTypes, LombokTypes}
import bml.util.java.poet.StaticImport
import bml.util.java.{JavaEnums, JavaPojoUtil, JavaPojos}
import bml.util.jpa.JPA
import bml.util.persist.SpringVariableTypes.{PersistenceAnnotations, PersistenceTypes, ValidationAnnotations}
import bml.util.persist.{SpringVariableTypes, UUIDIfNullGenerator}
import bml.util.spring.SpringVersion.SpringVersion
import com.squareup.javapoet.{AnnotationSpec, ClassName, CodeBlock, FieldSpec, MethodSpec, ParameterSpec, ParameterizedTypeName, TypeSpec}
import io.apibuilder.generator.v0.models.File
import io.apibuilder.spec.v0.models.{Enum, Model, Service, Union}
import javax.lang.model.element.Modifier.{FINAL, PROTECTED, PUBLIC}
import javax.persistence.{EnumType, Enumerated}
import play.api.Logger
import play.api.libs.json.Json

class PojoGenerator(
                     springVersion: SpringVersion,
                     apiDocComments: String,
                     service: Service,
                     header: Option[String]
                   ) extends JavaPojoUtil {

  import scala.collection.JavaConverters._

  val logger: Logger = Logger.apply(this.getClass())

  private val nameSpaces = new NameSpaces(service)

  private val javaFileUtil = new GeneratorFSUtil(
    springVersion,
    "bml_lombok",
    "bml_lombok_spring_6",
    service
  )


  private val useJpa = service.models.seq
    .map(Hibernate.fromModel(_).use)
    .find(_ == true).isDefined

  def genIdGenerators(): Seq[File] = {
    if (!useJpa) {
      return Seq[File]()
    }
    return Seq[File](UUIDIfNullGenerator.get(springVersion, nameSpaces))
  }


  def generateSourceFiles(): Either[Seq[String], Seq[File]] = {
    val errors = SpecValidation.validate(service: Service, header: Option[String])
    if (errors.isDefined) {
      return Left(errors.get)
    }

    Right(generateEnums(service) ++
      generateUnionTypes(service) ++
      generateModels(service) ++
      genIdGenerators()
    )
  }

  def generateEnums(service: Service): Seq[File] = {
    service.enums.map(generateEnum) ++
      generateEnumJpaConverters(service)
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


  def generateEnumJpaConverters(service: Service): Seq[File] = {
    service.enums
      .filter(Converters.fromEnum(_).map(_.useJpaConverter).getOrElse(false))
      .map(generateEnumJpaConverter(_))
  }


  def generateEnumJpaConverter(enum: Enum): File = {
    val enumClassName = JavaPojoUtil.toClassName(nameSpaces.model, enum.name)
    val convertToClassName = JavaTypes.String
    val className = AtlasTypes.enumJpaConverterClassName(enum.name, nameSpaces)

    val builder = TypeSpec.classBuilder(className)
      .addModifiers(PUBLIC)
      .addAnnotation(PersistenceAnnotations.Converter(springVersion, true))
      .addSuperinterface(
        ParameterizedTypeName.get(
          PersistenceTypes.AttributeConverter.toClassName(springVersion),
          //              JavaxPersistanceTypes.AttributeConverter,
          enumClassName,
          convertToClassName)
      )
      .addMethod(
        MethodSpec.methodBuilder("convertToDatabaseColumn")
          .addAnnotation(JavaTypes.Override)
          .addModifiers(PUBLIC)
          .addParameter(ParameterSpec.builder(enumClassName, "attribute", FINAL).build())
          .addCode(
            CodeBlock.builder()
              .beginControlFlow("if (attribute == null)")
              .addStatement("return null")
              .endControlFlow()
              .addStatement("return attribute.apiValue()")
              .build()
          )
          .returns(JavaTypes.String)
          .build()
      )
      .addMethod(
        MethodSpec.methodBuilder("convertToEntityAttribute")
          .addAnnotation(JavaTypes.Override)
          .addModifiers(PUBLIC)
          .returns(enumClassName)
          .addParameter(ParameterSpec.builder(JavaTypes.String, "dbData", FINAL).build())

          .addCode(
            CodeBlock.builder()
              .beginControlFlow("if (dbData == null)")
              .addStatement("return null")
              .endControlFlow()
              .addStatement("return $T.fromApiValue(dbData)", enumClassName)
              .build()
          )

          .build()
      )
    javaFileUtil.makeFile(className, nameSpaces.jpaConverters, builder)

  }

  def generateEnum(enum: Enum): File = {
    val className = toClassName(enum.name)
    val builder = JavaEnums.standardEnumBuilder(enum, apiDocComments)


    enum.values.foreach(value => {
      val enumValBuilder = TypeSpec.anonymousClassBuilder("$S,$S", value.value.getOrElse(value.name), value.description.getOrElse(""))
      if (value.description.isDefined) enumValBuilder.addJavadoc(value.description.get)
      builder.addEnumConstant(toEnumName(value.name), enumValBuilder.build())
    })
    javaFileUtil.makeFile(className, nameSpaces.model, builder)
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
    javaFileUtil.makeFile(className, nameSpaces.model, builder)
  }

  def generateModel(model: Model, relatedUnions: Seq[Union]): File = {

    logger.info(s"Model ${model.name} attributes ${model.attributes}")

    //Should we add in hibernate
    val useHibernate = Hibernate.fromModel(model).use;

    val snakeCase = SnakeCase.hasAttribute(model)
    logger.info(s"Model ${model.name} snakeCase ${snakeCase}")

    val className = toClassName(nameSpaces.model, model)
    //logger.info(s"Generating Model Class ${className}")


    var baseAnnotations = Seq(
      LombokAnno.Data,
      LombokAnno.Builder,
      LombokAnno.AccessorFluent,
      LombokAnno.EqualsAndHashCode,
      LombokAnno.AllArgsConstructor,
      LombokAnno.NoArgsConstructor,
      LombokAnno.FieldNameConstants,
      JacksonAnno.JsonIncludeNON_EMPTY,
      JacksonAnno.JsonIgnoreProperties_Ignore_unknown
    )

    if (snakeCase) {
      //        val foo = JacksonAnno.JsonNaming(JacksonTypes.SnakeCaseStrategy)
      baseAnnotations = baseAnnotations :+ JacksonAnno.JsonNaming(JacksonTypes.SnakeCaseStrategy)
    }


    val classJavadoc = Seq(apiDocComments, model.description).mkString("\n")


    val classBuilder = TypeSpec.classBuilder(className)
      .addModifiers(PUBLIC)
      .addJavadoc(classJavadoc)
      .addAnnotations(baseAnnotations.asJava)
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
      classBuilder.addAnnotation(PersistenceAnnotations.Entity(springVersion))
      classBuilder.addAnnotation(PersistenceAnnotations.Table(springVersion, model))
    }

    model.fields.foreach(field => {
      val javaDataType = dataTypeFromFieldArraySupport(service, field, nameSpaces.model.nameSpace)
      val jsonFieldName = JsonName.fromField(field)


      val fieldBuilder = FieldSpec.builder(javaDataType, toParamName(field.name, true))
        .addModifiers(PROTECTED)
        .addAnnotation(LombokAnno.Getter)


      if (isModelType(service, field)) {
        fieldBuilder.addAnnotation(ValidationAnnotations.Valid((springVersion)))
      }

      if (JavaPojoUtil.isDateOrTime(field)) {
        fieldBuilder.addAnnotation(JacksonAnno.JsonFormatString)
      }

      if (JavaPojoUtil.isEnumType(service, field.`type`)) {
        if (field.default.isDefined) {
          fieldBuilder.addAnnotation(LombokAnno.BuilderDefault)
          fieldBuilder.initializer("$T.$L", javaDataType, field.default.get)
        }
      }

      if (useHibernate) {
        if (JavaPojoUtil.isEnumType(service, field)) {
          val convertersOpt = Converters.fromField(field)
          if (convertersOpt.isDefined) {
            val converterClassName = AtlasTypes.enumJpaConverterClassName(field.`type`, nameSpaces)
            fieldBuilder.addAnnotation(PersistenceAnnotations.Convert(springVersion, converterClassName))
          } else {
            fieldBuilder.addAnnotation(
              //AnnotationSpec.builder(classOf[Enumerated])
              AnnotationSpec.builder(PersistenceTypes.Enumerated.toClassName(springVersion))
                //                .addMember("value", "$T.STRING", classOf[EnumType])
                .addMember("value", "$T.STRING", PersistenceTypes.EnumType.toClassName(springVersion))
                .build()
            )
          }
        }
      }
      if (field.`type` == "integer" && field.default.isDefined) {
        fieldBuilder.addAnnotation(LombokTypes.BuilderDefault)
          .initializer("$L", field.default.get)
      }

      if (isParameterArray(field.`type`) || isParameterMap(field.`type`)) {
        fieldBuilder.addAnnotation(ValidationAnnotations.Valid(springVersion))
        val singularAttr = Singular.fromField(field)
        if (singularAttr.isDefined) {
          fieldBuilder.addAnnotation(LombokAnno.Singular(singularAttr.get.name))
        } else {
          fieldBuilder.addAnnotation(LombokAnno.Singular)
        }
      }
      if (field.required) {
        fieldBuilder.addAnnotation(ValidationAnnotations.NotNull(springVersion))
      }
      //        if (JavaPojoUtil.isParameterArray(field.`type`)) {
      //          if (field.required) {
      //            fieldBuilder.addAnnotation(JavaxValidationAnnotations.NotEmpty)
      //          }
      //        }

      val sizeAnnotation = JavaPojos.handleSizeAttribute(springVersion, className, field)
      if (sizeAnnotation.isDefined) {
        fieldBuilder.addAnnotation(sizeAnnotation.get)
      }

      if (useHibernate) {
        JavaPojos.handlePersisitanceAnnontations(springVersion, service, className, field).foreach(fieldBuilder.addAnnotation(_))
      }

      if (snakeCase) {
        fieldBuilder.addAnnotation(JacksonAnno.JsonProperty(field.required))
      } else {
        if (jsonFieldName.isDefined) {
          fieldBuilder.addAnnotation(JacksonAnno.JsonProperty(jsonFieldName.get.name, field.required))
        } else {
          fieldBuilder.addAnnotation(JacksonAnno.JsonProperty(field.name, field.required))
        }
      }
      if (field.required) {
        fieldBuilder.addAnnotation(JacksonAnno.JsonIncludeALLWAYS)
      }

      if (
        field.`type`.equals("boolean")
          &&
          field.default.isDefined
      ) {

        val default = field.default.get
        if (default.equals("true") || default.equals("false")) {
          fieldBuilder.addAnnotation(LombokTypes.BuilderDefault)
        }
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
            fieldBuilder.addAnnotation(ValidationAnnotations.Pattern(springVersion, attribute))
          }
          case "email" => {
            fieldBuilder.addAnnotation(ValidationAnnotations.Email(springVersion))
          }
          case _ =>
        }
      })
      classBuilder.addField(fieldBuilder.build)
    }

    )

    if (useHibernate) {
      JPA.addJPAStandardFields(springVersion, model).foreach(classBuilder.addField)
    }
    javaFileUtil.makeFile(className.simpleName(), nameSpaces.model, classBuilder)
  }

}
