package bml.util.attribute

import io.apibuilder.spec.v0.models.Model

class Hibernate(val use: Boolean) {

}

object Hibernate {

  val attributeName = "hibernate"
  val useValueKey = "use"
  val maximumValueKey = "maximum"

  def fromModel(field: Model): Option[Hibernate] = {
    val optional = field.attributes.find(_.name == attributeName)
    if (optional.isEmpty) {
      return None
    }
    val value = optional.get.value
    Some(Hibernate((value \ useValueKey).as[Boolean]))
  }

  def apply(use: Boolean) = new Hibernate(use)

}


