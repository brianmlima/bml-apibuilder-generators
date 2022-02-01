package bml.util.attribute

import io.apibuilder.spec.v0.models.{Field, Resource}

class Version(val drop: Boolean) {

}

object Version {
  val attributeName = "version"
  val dropValueKey = "drop"

  def fromResource(resource: Resource): Option[Version] = {
    val optional = resource.attributes.find(_.name == attributeName)
    if (optional.isEmpty) {
      return None
    }
    val value = optional.get.value
    Some(Version((value \ dropValueKey).as[Boolean]))
  }

  def apply(drop: Boolean) = new Version(drop)

}