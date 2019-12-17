package bml.util.attribute

import io.apibuilder.spec.v0.models.Field
import play.api.libs.json.JsNumber
import play.api.libs.json.{JsObject, JsValue, Json}

class StringValueLength(val minimum: Int, val maximum: Int) {

}

object StringValueLength {

  val attributeName = "string_value_length"
  val minimumValueKey = "minimum"
  val maximumValueKey = "maximum"

  def fromField(field: Field): Option[StringValueLength] = {
    val optional = field.attributes.find(_.name == attributeName)
    if (optional.isEmpty) {
      return None
    }
    val value = optional.get.value
    Some(StringValueLength((value \ minimumValueKey).as[Int], (value \ maximumValueKey).as[Int]))
  }

  def apply(minimum: Int, maximum: Int) = new StringValueLength(minimum, maximum)

}

