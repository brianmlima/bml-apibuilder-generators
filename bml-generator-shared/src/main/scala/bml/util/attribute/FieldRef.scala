package bml.util.attribute

import io.apibuilder.spec.v0.models.Field

class FieldRef(val model: String, val field: String) {

}

object FieldRef {
  val attributeName = "field_ref"
  val modelValueKey = "model"
  val fieldValueKey = "field"

  def fromField(field: Field): Option[FieldRef] = {
    val optional = field.attributes.find(_.name == attributeName)
    if (optional.isEmpty) {
      return None
    }
    val value = optional.get.value
    Some(FieldRef((value \ modelValueKey).as[String], (value \ fieldValueKey).as[String]))
  }

  def apply(model: String, field: String) = new FieldRef(model, field)

}