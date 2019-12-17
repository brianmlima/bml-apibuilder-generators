package models.generator.java.persistence.sql

import bml.util.java.ClassNames
import bml.util.java.ClassNames.SpringTypes
import com.fasterxml.jackson.annotation.{JsonIgnoreProperties, JsonProperty}
import com.squareup.javapoet._
import io.apibuilder.spec.v0.models.{Attribute, Field, Model}
import javax.lang.model.element.Modifier.{FINAL, PUBLIC, STATIC}
import javax.persistence._
import lombok.experimental.Accessors
import lombok.{AllArgsConstructor, Builder, Data, EqualsAndHashCode}
import org.springframework.beans.factory.annotation.Autowired

import scala.collection.mutable.ListBuffer

/**
 * Utilities for generating java persistance code.
 */
class GenUtils {

}

object GenUtils {

  //@Accessors(fluent=true)
  def accessorFluent = AnnotationSpec
    .builder(classOf[Accessors])
    .addMember("fluent", CodeBlock.builder().add("true").build).build()

  //@NotNull
  def notNull = AnnotationSpec.builder(classOf[NotNull]).build()

  def jsonProperty = AnnotationSpec.builder(classOf[JsonProperty]).build()

  //@Basic(optional = false)
  def basicOptional(optional: Boolean) = AnnotationSpec.builder(classOf[Basic])
    .addMember("optional", optional.toString)
    .build()

  //@GeneratedValue(strategy = GenerationType.AUTO)
  def generatedValueAuto = {
    AnnotationSpec.builder(classOf[GeneratedValue])
      .addMember("strategy", "$T.$L", classOf[GenerationType], GenerationType.AUTO)
      .build()
  }

  def id = AnnotationSpec.builder(classOf[Id]).build()

  def entity = AnnotationSpec.builder(classOf[Entity]).build()

  def builder = AnnotationSpec.builder(classOf[Builder]).build()

  def data = AnnotationSpec.builder(classOf[Data]).build()

  def allArgsConstructor = AnnotationSpec.builder(classOf[AllArgsConstructor]).build()

  def notBlank = AnnotationSpec.builder(ClassNames.notBlank).build()

  def jsonIgnoreProperties = AnnotationSpec.builder(classOf[JsonIgnoreProperties]).addMember("ignoreUnknown", CodeBlock.builder().add("true").build).build()

  def table(model: Model) = AnnotationSpec.builder(classOf[Table]).addMember("name", "$S", model.plural).build()

  //@Size(min=${min},max=${max})
  def size(min: Int, max: Int): AnnotationSpec = AnnotationSpec.builder(ClassNames.size).addMember("min", "$L", new Integer(min)).addMember("max", "$L", new Integer(max)).build()

  def size(attribute: Attribute): AnnotationSpec = size((attribute.value \ "min").as[Int], (attribute.value \ "max").as[Int])

  def size(
            classBuilder: TypeSpec.Builder,
            fieldBuilder: FieldSpec.Builder,
            fieldName: String, min: Int, max: Int): Unit = {
    var maxFieldName = s"${fieldName.toUpperCase()}_MAX_LEN"
    var minFieldName = s"${fieldName.toUpperCase()}_MIN_LEN"
    var staticFields = Iterable(
    )


    classBuilder.addField(FieldSpec.builder(TypeName.INT, minFieldName, PUBLIC, STATIC, FINAL).initializer(s"${min}").build())
    classBuilder.addField(FieldSpec.builder(TypeName.INT, maxFieldName, PUBLIC, STATIC, FINAL).initializer(s"${max}").build())
    fieldBuilder.addAnnotation(
      AnnotationSpec.builder(ClassNames.size)
        .addMember("min", "$L", minFieldName)
        .addMember("max", "$L", maxFieldName).build()
    )
  }


  def pattern(regexp: String): AnnotationSpec = AnnotationSpec.builder(ClassNames.pattern).addMember("regexp", "$S", regexp).build()

  def pattern(attribute: Attribute): AnnotationSpec = pattern((attribute.value \ "regexp").as[String])

  def table(tableName: String): AnnotationSpec = AnnotationSpec.builder(classOf[Table]).addMember("name", "$S", tableName).build()

  def validated: AnnotationSpec = AnnotationSpec.builder(SpringTypes.Validated).build()

  def valid: AnnotationSpec = AnnotationSpec.builder(ClassNames.valid).build()

  def autowired: AnnotationSpec = AnnotationSpec.builder(classOf[Autowired]).build()

  def equalsAndHashCode(onlyExplicitlyIncluded: Boolean): AnnotationSpec =
    AnnotationSpec.builder(classOf[EqualsAndHashCode])
      .addMember("onlyExplicitlyIncluded", "$L", onlyExplicitlyIncluded.toString)
      .build()

  //@Enumerated(EnumType.STRING)
  def enumerated(): AnnotationSpec = {
    AnnotationSpec.builder(classOf[Enumerated])
      .addMember("value", "$T.$L", classOf[EnumType], EnumType.STRING.name())
      .build()
  }

  def column(field: Field): AnnotationSpec = {
    val builder = AnnotationSpec.builder(classOf[Column])
      .addMember("name", "$S", field.name)
    if (field.required) {
      builder.addMember("nullable", "false")
    }

    builder.build()
  }


  def javaDoc(fieldBuilder: FieldSpec.Builder, field: Field): Unit = {
    val docString = javaDoc(field)
    if (docString.isDefined) {
      fieldBuilder.addJavadoc(docString.get)
    }
  }

  def javaDoc(field: Field): Option[String] = {
    var comments = new ListBuffer[String]()
    if (field.description.exists(_.trim.nonEmpty)) {
      comments += field.description.get.trim + "\n"
    }
    if (field.example.exists(_.trim.nonEmpty)) {
      comments += s"Example: ${field.example.get.trim}\n"
    }
    Option.apply(comments.mkString("\n"))
  }


}
