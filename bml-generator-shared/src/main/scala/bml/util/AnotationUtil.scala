package bml.util

import bml.util.attribute.FieldRef
import bml.util.java.ClassNames.JavaxTypes.{JavaxPersistanceTypes, JavaxValidationTypes}
import bml.util.java.ClassNames.SpringTypes.{SpringDataTypes, SpringValidationTypes}
import bml.util.java.ClassNames._
import bml.util.java.{ClassNames, JavaPojoUtil, JavaPojos}
import bml.util.jpa.JPA
import io.apibuilder.spec.v0.models.{Attribute, Field, Model, Service}
import lombok.Builder.Default
import lombok._
import lombok.experimental.Accessors

/**
 * An object tree for static common Annotations and Annotation construction methods used in Java code
 * generation.
 */
object AnotationUtil {

  import com.squareup.javapoet._

  /**
   * Jackson Annotations.
   */
  object JacksonAnno {

    val JsonIncludeNON_NULL = AnnotationSpec.builder(JacksonTypes.JsonInclude)
      .addMember("value", "$L", "JsonInclude.Include.NON_NULL")
      .build()
    val JsonIncludeNON_EMPTY = AnnotationSpec.builder(JacksonTypes.JsonInclude)
      .addMember("value", "$L", "JsonInclude.Include.NON_EMPTY")
      .build()

    val JsonIncludeALLWAYS = AnnotationSpec.builder(JacksonTypes.JsonInclude)
      .addMember("value", "$L", "JsonInclude.Include.ALWAYS")
      .build()

    def JsonProperty(name: String, required: Boolean) = AnnotationSpec.builder(ClassNames.jsonProperty)
      .addMember("value", "$S", name)
      .addMember("required", "$L", required.toString)
      .build()

    val JsonIgnoreProperties_Ignore_unknown = AnnotationSpec.builder(JacksonTypes.JsonIgnoreProperties)
      .addMember("ignoreUnknown", CodeBlock.builder().add("true").build).build()

  }

  /**
   * Spring Annotations.
   */
  object SpringAnno {
    val Configuration = AnnotationSpec.builder(ClassNames.SpringTypes.Configuration).build()
    val Autowired = AnnotationSpec.builder(SpringTypes.Autowired).build()

    def GetMappingJson(path: String): AnnotationSpec = {
      AnnotationSpec.builder(SpringTypes.GetMapping)
        .addMember("path", "$S", path)
        .build()
    }

    def PostMappingJson(path: String): AnnotationSpec = {
      AnnotationSpec.builder(SpringTypes.PostMapping)
        .addMember("path", "$S", path)
        .build()
    }

    def PutMappingJson(path: String): AnnotationSpec = {
      AnnotationSpec.builder(SpringTypes.PutMapping)
        .addMember("path", "$S", path)
        .build()
    }

    def DeleteMappingJson(path: String): AnnotationSpec = {
      AnnotationSpec.builder(SpringTypes.DeleteMapping)
        .addMember("path", "$S", path)
        .build()
    }


    /**
     * Spring Test Annotations.
     */
    object SpringTestAnno {
      val runnWithSpringRunner = JunitAnno.ExtendWith(ClassNames.SpringTypes.SpringTestTypes.SpringExtension)
      val SpringBootTest = AnnotationSpec.builder(ClassNames.SpringTypes.SpringTestTypes.SpringBootTest).build()

      def SpringJUnitConfig(className: ClassName) = AnnotationSpec.builder(ClassNames.SpringTypes.SpringTestTypes.SpringJUnitConfig)
        .addMember("value", "$T.class", className).build()

      def Param(name: String) = AnnotationSpec.builder(SpringDataTypes.Param)
        .addMember("value", "$s", name).build()
    }

  }

  /**
   * Spring Data Annotations.
   */
  object SpringDataAnno {

    def Query(query: String) = AnnotationSpec.builder(SpringDataTypes.Query).addMember("value", "$S", query).build()

    val EntityScan = ClassName.bestGuess("org.springframework.boot.autoconfigure.domain.EntityScan")
    val EnableJpaRepositories = ClassName.bestGuess("org.springframework.data.jpa.repository.config.EnableJpaRepositories")

    def EntityScan(packageString: String) = AnnotationSpec.builder(SpringDataTypes.EntityScan).addMember("value", "$S", packageString).build()

  }

  /**
   * Junit Annotations.
   */
  object JunitAnno {
    def ExtendWith(className: ClassName) = AnnotationSpec.builder(ClassNames.JunitTypes.ExtendWith)
      .addMember("value", "$T.class", className).build()

    val Test = AnnotationSpec.builder(ClassNames.JunitTypes.Test).build()

    def DisplayName(displayName: String) = AnnotationSpec.builder(ClassNames.JunitTypes.DisplayName)
      .addMember("value", "$S", displayName)
      .build()
  }

  /**
   * Lombok Annotations.
   */
  object LombokAnno {
    val Slf4j = AnnotationSpec.builder(ClassNames.LombokTypes.Slf4j).build()

    val Data = AnnotationSpec.builder(LombokTypes.Data).build()

    val AllArgsConstructor = AnnotationSpec.builder(LombokTypes.AllArgsConstructor).build()
    val NoArgsConstructor = AnnotationSpec.builder(LombokTypes.NoArgsConstructor).build()
    val FieldNameConstants = AnnotationSpec.builder(LombokTypes.FieldNameConstants).build()
    val Generated = AnnotationSpec.builder(LombokTypes.Generated).build()

    def AccessorFluent = AnnotationSpec
      .builder(classOf[Accessors])
      .addMember("fluent", CodeBlock.builder().add("true").build).build()

    def Getter = AnnotationSpec
      .builder(classOf[Getter])
      .addMember("onMethod", "@__( { @$T } )", LombokTypes.JsonIgnore)
      .build()

    def Getter(className: ClassName): AnnotationSpec = AnnotationSpec
      .builder(classOf[Getter])
      .addMember("onMethod", "@__( { @$T } )", className)
      .build()


    def Builder = AnnotationSpec
      .builder(classOf[Builder])
      .addMember("toBuilder", "$L", "true")
      .build()

    def BuilderDefault = AnnotationSpec
      .builder(classOf[Default])
      .build()

    def EqualsAndHashCode = AnnotationSpec
      .builder(classOf[EqualsAndHashCode])
      .build()

    def Singular = AnnotationSpec.builder(LombokTypes.Singular).build()

  }

  /**
   * Javax Annotations.
   */
  object JavaxAnnotations {

    object JavaxValidationAnnotations {
      def NotNull = AnnotationSpec.builder(JavaxValidationTypes.NotNull).build()

      def NotBlank = AnnotationSpec.builder(JavaxValidationTypes.NotBlank).build()

      def NotEmpty = AnnotationSpec.builder(JavaxValidationTypes.NotEmpty).build()

      val Validated = AnnotationSpec.builder(SpringValidationTypes.Validated).build()

      val Valid = AnnotationSpec.builder(JavaxValidationTypes.Valid).build()

      def Size(min: Int, max: Int): AnnotationSpec = {
        AnnotationSpec.builder(JavaxValidationTypes.Size)
          .addMember("min", "$L", new Integer(min))
          .addMember("max", "$L", new Integer(max))
          .build()
      }

      def Size(min: Option[Long], max: Option[Long]): AnnotationSpec = {
        val spec = AnnotationSpec.builder(JavaxValidationTypes.Size)
        if (min.isDefined) spec.addMember("min", "$L", min.get.toString)
        if (max.isDefined) spec.addMember("max", "$L", max.get.toString)
        spec.build()
      }

      def Size(attribute: Attribute): AnnotationSpec = {
        JavaxValidationAnnotations.Size((attribute.value \ "min").as[Int], (attribute.value \ "max").as[Int])
      }

      def Pattern(regexp: String): AnnotationSpec = {
        AnnotationSpec.builder(JavaxValidationTypes.Pattern)
          .addMember("regexp", "$S", regexp)
          .build()
      }

      def Pattern(attribute: Attribute): AnnotationSpec = {
        Pattern((attribute.value \ "regexp").as[String])
      }
    }

    /**
     * Javax Persistence Annotations.
     */
    object JavaxPersistanceAnnotations {
      def JoinColumn(field: Field) = AnnotationSpec.builder(JavaxPersistanceTypes.JoinColumn)
        .addMember("name", "$S", Text.camelToSnakeCase(field.name) + "_id")
        .build()

      def JoinColumn(service: Service, field: Field) = {
        val isModel = JavaPojoUtil.isModelType(service, field)
        val isList = JavaPojoUtil.isListOfModeslType(service, field)
        val spec = AnnotationSpec.builder(JavaxPersistanceTypes.JoinColumn)
        if (isModel) {
          spec.addMember("name", "$S", Text.camelToSnakeCase(field.name) + "_id")
        }
        if (isList) {
          val fieldRef = FieldRef.fromField(field)
          spec.addMember("name", "$S", Text.camelToSnakeCase(if (fieldRef.isDefined) fieldRef.get.field else ""))
        }
        spec.build()
      }


      val ManyToOne = AnnotationSpec.builder(JavaxPersistanceTypes.ManyToOne).build()

      val OneToMany = AnnotationSpec.builder(JavaxPersistanceTypes.OneToMany).build()

      def Basic(optional: Boolean) = AnnotationSpec.builder(JavaxPersistanceTypes.Basic)
        .addMember("optional", "$L", optional.toString)
        .build()

      val Id = AnnotationSpec.builder(JavaxPersistanceTypes.Id).build()

      def GeneratedValue(strategy: CodeBlock) = AnnotationSpec.builder(JavaxPersistanceTypes.GeneratedValue)
        .addMember("strategy", strategy).build()

      def Column(fieldName: String): AnnotationSpec = {
        AnnotationSpec.builder(JavaxPersistanceTypes.Column)
          .addMember("name", "$S", Text.camelToSnakeCase(fieldName))
          .build()
      }

      def Column(field: Field): AnnotationSpec = {
        val isId = (field.name == "id")
        val isUUID = (field.`type` == "uuid")
        val isString = (field.`type` == "string")

        val spec = AnnotationSpec.builder(JavaxPersistanceTypes.Column)
          .addMember("name", "$S", JPA.toColumnName(field))
          .addMember("nullable", "$L", (!field.required).toString)
          .addMember("unique", "$L", (isId || field.annotations.find(_ == "unique").isDefined).toString)

        if (isString && field.maximum.isDefined) {
          spec.addMember("length", "$L", JavaPojos.toMaxFieldStaticFieldName(field))
        }

        if (isUUID && isId) {
          spec.addMember("updatable", "$L", false.toString)
            .addMember("insertable", "$L", true.toString)
        }
        spec.build()
      }

      def Table(model: Model): AnnotationSpec = {
        AnnotationSpec.builder(JavaxPersistanceTypes.Table).addMember("name", "$S", JPA.toTableName(model)).build()
      }

      val Entity = AnnotationSpec.builder(JavaxPersistanceTypes.Entity).build()
      val Version = AnnotationSpec.builder(JavaxPersistanceTypes.Version).build()

      val TemporalTIMESTAMP = AnnotationSpec.builder(JavaxPersistanceTypes.Temporal).addMember("value", "$T.TIMESTAMP", JavaxPersistanceTypes.TemporalType).build()

      def GeneratedValue(generator: String) = AnnotationSpec.builder(JavaxPersistanceTypes.GeneratedValue)
        .addMember("generator", "$S", generator)
        .build()
    }

  }

  /**
   * Hibernate Annotations.
   */
  object HibernateAnnotations {
    val GeneratedInserted = AnnotationSpec.builder(HibernateTypes.Generated)
      .addMember("value", "$T.INSERT", HibernateTypes.GenerationTime)
      .build()

    def GenericGenerator(name: String, strategy: String) = AnnotationSpec.builder(HibernateTypes.GenericGenerator)
      .addMember("name", "$S", name)
      .addMember("strategy", "$S", strategy)
      .build()
  }

}