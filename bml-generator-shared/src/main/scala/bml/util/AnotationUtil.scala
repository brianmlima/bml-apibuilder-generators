package bml.util

import bml.util.java.ClassNames
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.squareup.javapoet.{AnnotationSpec, ClassName, CodeBlock, TypeSpec}
import io.apibuilder.spec.v0.models.Attribute
import javax.persistence.Table
import lombok.experimental.Accessors
import lombok.extern.slf4j.Slf4j
import lombok.{AllArgsConstructor, Builder, EqualsAndHashCode, NoArgsConstructor, Singular}
import org.springframework.validation.annotation.Validated

object AnotationUtil {


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


  def fluentAccessor = AnnotationSpec
    .builder(classOf[Accessors])
    .addMember("fluent", CodeBlock.builder().add("true").build).build()

  def notNull = ClassNames.notNull

  def notBlank = ClassNames.notBlank

  def `override` = ClassNames.java.`override`

  def autowired = ClassNames.autowired

  def springBootTest = ClassNames.springBootTest

  def jsonProperty(name: String, required: Boolean) = AnnotationSpec.builder(ClassNames.jsonProperty)
    .addMember("value", "$S", name)
    .addMember("required", "$L", required.toString)
    .build()

  def getMappingJson(path: String): AnnotationSpec = {
    AnnotationSpec.builder(ClassNames.getMapping)
      .addMember("path", "$S", path)
      .addMember("produces", "$T.$L", ClassNames.mediaType, "APPLICATION_JSON_VALUE")
      .build()
  }

  def size(min: Int, max: Int): AnnotationSpec = {
    AnnotationSpec.builder(ClassNames.size)
      .addMember("min", "$L", new Integer(min))
      .addMember("max", "$L", new Integer(max))
      .build()
  }

  def size(min: Option[Long], max: Option[Long]): AnnotationSpec = {
    val spec = AnnotationSpec.builder(ClassNames.size)
    if (min.isDefined) spec.addMember("min", "$L", min.get.toString)
    if (max.isDefined) spec.addMember("max", "$L", max.get.toString)
    spec.build()
  }

  def size(attribute: Attribute): AnnotationSpec = {
    size((attribute.value \ "min").as[Int], (attribute.value \ "max").as[Int])
  }

  def pattern(regexp: String): AnnotationSpec = {
    AnnotationSpec.builder(ClassNames.pattern)
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

  def validated(): AnnotationSpec = AnnotationSpec.builder(classOf[Validated]).build()

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