package bml.util

import bml.util.java.{ClassNames, JavaPojoUtil}
import bml.util.java.ClassNames.{HibernateTypes, JavaTypes, SpringTypes}
import bml.util.java.ClassNames.JavaxTypes.{JavaxPersistanceTypes, JavaxValidationTypes}
import bml.util.java.ClassNames.SpringTypes.SpringValidationTypes
import bml.util.jpa.JPA
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import io.apibuilder.spec.v0.models.{Attribute, Field, Model}
import javax.persistence.Table
import lombok.experimental.Accessors
import lombok.{AllArgsConstructor, Builder, EqualsAndHashCode, NoArgsConstructor, Singular}
//import org.springframework.validation.annotation.Validated
import scala.collection.JavaConverters._

object AnotationUtil {

  import com.squareup.javapoet._

  object SpringAnno {
    val Configuration = AnnotationSpec.builder(ClassNames.SpringTypes.Configuration).build()

    object SpringTestAnno {
      val runnWithSpringRunner = JunitAnno.ExtendWith(ClassNames.SpringTypes.SpringTestTypes.SpringExtension)
      val SpringBootTest = AnnotationSpec.builder(ClassNames.SpringTypes.SpringTestTypes.SpringBootTest).build()

      def SpringJUnitConfig(className: ClassName) = AnnotationSpec.builder(ClassNames.SpringTypes.SpringTestTypes.SpringJUnitConfig)
        .addMember("value", "$T.class", className).build()
    }

  }

  object JunitAnno {
    def ExtendWith(className: ClassName) = AnnotationSpec.builder(ClassNames.JunitTypes.ExtendWith)
      .addMember("value", "$T.class", className).build()

    val Test = AnnotationSpec.builder(ClassNames.JunitTypes.Test).build()

    def DisplayName(displayName: String) = AnnotationSpec.builder(ClassNames.JunitTypes.DisplayName)
      .addMember("value", "$S", displayName)
      .build()
  }

  object LombokAnno {
    val Slf4j = AnnotationSpec.builder(ClassNames.LombokTypes.Slf4j).build()
  }

  object JavaxAnnotations {

    object JavaxValidationAnnotations {
      def NotNull = AnnotationSpec.builder(JavaxValidationTypes.NotNull).build()

      def NotBlank = AnnotationSpec.builder(JavaxValidationTypes.NotBlank).build()

      def NotEmpty = AnnotationSpec.builder(JavaxValidationTypes.NotEmpty).build()
    }

    object JavaxPersistanceAnnotations {
      def Basic(optional: Boolean) = AnnotationSpec.builder(JavaxPersistanceTypes.Basic)
        .addMember("optional", "$L", optional.toString)
        .build()

      val Id = AnnotationSpec.builder(JavaxPersistanceTypes.Id).build()

      //@GeneratedValue(strategy = GenerationType.IDENTITY)

      def GeneratedValue(strategy: CodeBlock) = AnnotationSpec.builder(JavaxPersistanceTypes.GeneratedValue)
        .addMember("strategy", strategy).build()

      def Column(field: Field) = {
        val isId = (field.name == "id")
        val isUUID = (field.`type` == "uuid")

        AnnotationSpec.builder(JavaxPersistanceTypes.Column)
          .addMember("name", "$S", JPA.toColumnName(field))
          .addMember("nullable", "$L", (!field.required).toString)
          .addMember("unique", "$L", (isId || field.annotations.find(_ == "unique").isDefined).toString)
          .addMember("insertable", "$L", (!(isId && isUUID)).toString)
          .build()
      }

      def Table(model: Model): AnnotationSpec = {
        AnnotationSpec.builder(JavaxPersistanceTypes.Table).addMember("name", "$S", JPA.toTableName(model)).build()
      }

    }

  }

  object HibernateAnnotations {
    val GeneratedInserted = AnnotationSpec.builder(HibernateTypes.Generated)
      .addMember("value", "$T.INSERT", HibernateTypes.GenerationTime)
      .build()
  }


  def fluentAccessor = AnnotationSpec
    .builder(classOf[Accessors])
    .addMember("fluent", CodeBlock.builder().add("true").build).build()

  def `override` = JavaTypes.`Override`

  def autowired = SpringTypes.Autowired

  def jsonProperty(name: String, required: Boolean) = AnnotationSpec.builder(ClassNames.jsonProperty)
    .addMember("value", "$S", name)
    .addMember("required", "$L", required.toString)
    .build()

  def getMappingJson(path: String): AnnotationSpec = {
    AnnotationSpec.builder(SpringTypes.GetMapping)
      .addMember("path", "$S", path)
      .addMember("produces", "$T.$L", ClassNames.mediaType, "APPLICATION_JSON_VALUE")
      .build()
  }

  def size(min: Int, max: Int): AnnotationSpec = {
    AnnotationSpec.builder(JavaxValidationTypes.Size)
      .addMember("min", "$L", new Integer(min))
      .addMember("max", "$L", new Integer(max))
      .build()
  }

  def size(min: Option[Long], max: Option[Long]): AnnotationSpec = {
    val spec = AnnotationSpec.builder(JavaxValidationTypes.Size)
    if (min.isDefined) spec.addMember("min", "$L", min.get.toString)
    if (max.isDefined) spec.addMember("max", "$L", max.get.toString)
    spec.build()
  }

  def size(attribute: Attribute): AnnotationSpec = {
    size((attribute.value \ "min").as[Int], (attribute.value \ "max").as[Int])
  }

  def pattern(regexp: String): AnnotationSpec = {
    AnnotationSpec.builder(JavaxValidationTypes.Pattern)
      .addMember("regexp", "$S", regexp)
      .build()
  }

  def pattern(attribute: Attribute): AnnotationSpec = {
    pattern((attribute.value \ "regexp").as[String])
  }

  def table(tableName: String): AnnotationSpec = {
    AnnotationSpec.builder(classOf[Table])
      .addMember("name", "$S", tableName)
      .build()
  }

  def validated(): AnnotationSpec = AnnotationSpec.builder(SpringValidationTypes.Validated).build()

  def equalsAndHashCode(onlyExplicitlyIncluded: Boolean): AnnotationSpec = {
    AnnotationSpec.builder(classOf[EqualsAndHashCode]).addMember("onlyExplicitlyIncluded", "$L", onlyExplicitlyIncluded.toString).build()
  }


  def addDataClassAnnotations(builder: TypeSpec.Builder) {
    builder.addAnnotation(AnnotationSpec.builder(classOf[Accessors])
      .addMember("fluent", CodeBlock.builder().add("true").build).build())
      .addAnnotation(classOf[Builder])
      .addAnnotation(classOf[AllArgsConstructor])
      .addAnnotation(classOf[NoArgsConstructor])
      .addAnnotation(
        AnnotationSpec.builder(classOf[JsonIgnoreProperties])
          .addMember("ignoreUnknown", CodeBlock.builder().add("true").build).build()
      )
  }

  def singular = AnnotationSpec.builder(classOf[Singular]).build()


}