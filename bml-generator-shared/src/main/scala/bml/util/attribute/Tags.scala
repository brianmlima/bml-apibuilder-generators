package bml.util.attribute

import io.apibuilder.spec.v0.models.{Attribute, Field}

class Tags(val tags: Seq[String]) {

}

object Tags {

  val attributeName = "tags"
  val tagsValueKey = "tags"

  def fromAttributes(attributes: Seq[Attribute]): Option[Tags] = {
    val optional = attributes.find(_.name == attributeName)
    if (optional.isEmpty) {
      return None
    }
    val value = optional.get.value
    Some(Tags((value \ tagsValueKey).as[Seq[String]]))
  }

  def fromField(field: Field): Option[Tags] = {
    fromAttributes(field.attributes)
  }

  def apply(tags: Seq[String]) = new Tags(tags)
}