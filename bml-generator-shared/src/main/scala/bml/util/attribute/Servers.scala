package bml.util.attribute

import io.apibuilder.spec.v0.models.{Attribute, Field}

class Servers(val uris: Seq[String]) {

}

object Servers {

  val attributeName = "servers"
  val urisValueKey = "uris"

  def fromAttributes(attributes: Seq[Attribute]): Option[Servers] = {
    val optional = attributes.find(_.name == attributeName)
    if (optional.isEmpty) {
      return None
    }
    val value = optional.get.value
    Some(Servers((value \ urisValueKey).as[Seq[String]]))
  }

  def fromField(field: Field): Option[Servers] = {
    fromAttributes(field.attributes)
  }

  def apply(uris: Seq[String]) = new Servers(uris)
}
