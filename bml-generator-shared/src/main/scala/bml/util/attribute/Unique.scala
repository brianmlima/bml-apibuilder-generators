package bml.util.attribute

import akka.http.scaladsl
import akka.http.scaladsl.model
import io.apibuilder.spec.v0.models.{Field, Model}

class Unique(val indices: Seq[Seq[String]]) {
  def indicesToFields(model: Model): Seq[Seq[Field]] = {
    indices.map(
      index =>
        index.flatMap(
          key =>
            model.fields.filter(_.name == key)
        )
    )
  }
}

object Unique {

  val attributeName = "unique"
  val indicesValueKey = "indices"

  def fromModel(model: Model): Option[Unique] = {
    val optional = model.attributes.find(_.name == attributeName)
    if (optional.isEmpty) {
      return None
    }
    val value = optional.get.value
    Some(
      Unique((value \ indicesValueKey).as[Seq[Seq[String]]])
    )
  }

  def apply(indices: Seq[Seq[String]]) = new Unique(indices)

}
