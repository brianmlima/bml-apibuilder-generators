package bml.util.attribute

import io.apibuilder.spec.v0.models.Field

class Singular(val singular: String) {

}

object Singular {

  val attributeName = "singular"
  val singularValueKey = "name"

  def fromField(field: Field): Option[JsonName] = {
    val optional = field.attributes.find(_.name == attributeName)
    if (optional.isEmpty) {
      return None;
    }
    val value = optional.get.value
    val singular = (value \ singularValueKey);

    if (singular.isDefined) {
      Some(new JsonName((value \ singularValueKey).as[String]))
    } else {
      None
    }
  }

  def apply(singular: String) = new Singular(singular)

}


