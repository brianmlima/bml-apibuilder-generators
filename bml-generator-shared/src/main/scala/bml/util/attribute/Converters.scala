package bml.util.attribute

import io.apibuilder.spec.v0.models.{Field, Enum}

class Converters(val useJpaConverter: Boolean) {

}


object Converters {

  val attributeName = "converters"
  val useJpaConverterKey = "jpa"

  def fromEnum(enum: Enum): Option[Converters] = {
    val optional = enum.attributes.find(_.name == attributeName)
    if (optional.isEmpty) {
      return None;
    }
    val value = optional.get.value
    val useJpaConverter = (value \ useJpaConverterKey);

    if (useJpaConverter.isDefined) {
      Some(new Converters((value \ useJpaConverterKey).as[Boolean]))
    } else {
      None
    }

  }

  def fromField(field: Field): Option[Converters] = {
    val optional = field.attributes.find(_.name == attributeName)
    if (optional.isEmpty) {
      return None;
    }
    val value = optional.get.value
    val useJpaConverter = (value \ useJpaConverterKey);

    if (useJpaConverter.isDefined) {
      Some(new Converters((value \ useJpaConverterKey).as[Boolean]))
    } else {
      None
    }
  }

  def apply(useJpaConverter: Boolean) = new Converters(useJpaConverter)

  def docString() = "The Converters annotation is used to control how generators treat Value conversion."


}
