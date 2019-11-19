package bml.util

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.squareup.javapoet.{AnnotationSpec, CodeBlock, TypeSpec}
import io.apibuilder.spec.v0.models.Attribute
import javax.persistence.Table
import javax.validation.constraints.{Pattern, Size}
import lombok.experimental.Accessors
import lombok.extern.slf4j.Slf4j
import lombok.{EqualsAndHashCode, Singular}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping

object AnotationUtil {

  def fluentAccessor = AnnotationSpec
    .builder(classOf[Accessors])
    .addMember("fluent", CodeBlock.builder().add("true").build).build()

  def notNull = classOf[javax.validation.constraints.NotNull]

  def notBlank = classOf[javax.validation.constraints.NotBlank]

  def `override` = classOf[Override]

  def slf4j = classOf[Slf4j]

  def autowired = classOf[Autowired]

  def getMappingJson(path: String): AnnotationSpec = {
    AnnotationSpec.builder(classOf[GetMapping])
      .addMember("path", "$S", path)
      .addMember("produces", "$T.$L", classOf[MediaType], "APPLICATION_JSON_VALUE")
      .build()
  }

  def size(min: Int, max: Int): AnnotationSpec = {
    AnnotationSpec.builder(classOf[Size])
      .addMember("min", "$L", new Integer(min))
      .addMember("max", "$L", new Integer(max))
      .build()
  }

  def size(min: Option[Long], max: Option[Long]): AnnotationSpec = {
    val spec = AnnotationSpec.builder(classOf[Size])
    if (min.isDefined) spec.addMember("min", "$L", min.get.toString)
    if (max.isDefined) spec.addMember("max", "$L", max.get.toString)
    spec.build()
  }

  def size(attribute: Attribute): AnnotationSpec = {
    size((attribute.value \ "min").as[Int], (attribute.value \ "max").as[Int])
  }

  def pattern(regexp: String): AnnotationSpec = {
    AnnotationSpec.builder(classOf[Pattern])
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
      .addAnnotation(classOf[lombok.Builder])
      .addAnnotation(classOf[lombok.AllArgsConstructor])
      .addAnnotation(classOf[lombok.NoArgsConstructor])
      .addAnnotation(
        AnnotationSpec.builder(classOf[JsonIgnoreProperties])
          .addMember("ignoreUnknown", CodeBlock.builder().add("true").build).build()
      )
  }

  def singular = AnnotationSpec.builder(classOf[Singular]).build()


}