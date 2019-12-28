package bml.util.attribute

import io.apibuilder.spec.v0.models.Model

class Hibernate(val use: Boolean) {

}

object Hibernate {

  val attributeName = "hibernate"
  val useValueKey = "use"
  val maximumValueKey = "maximum"

  def fromModel(model: Model): Hibernate = {
    val optional = model.attributes.find(_.name == attributeName)
    if (optional.isEmpty) {
      return Hibernate(false)
    }
    val value = optional.get.value
    Hibernate((value \ useValueKey).as[Boolean])
  }

  def apply(use: Boolean) = new Hibernate(use)

}


