package bml.util.attribute

import io.apibuilder.spec.v0.models.{Field, Model}

class FindBy(val indices: Seq[Seq[String]]) {
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

object FindBy {

  val attributeName = "findby"
  val indicesValueKey = "indices"

  def fromModel(model: Model): Option[Unique] = {
    val optional = model.attributes.find(_.name == attributeName)
    if (optional.isEmpty) {
      return None
    }
    val value = optional.get.value
    Some(
      FindBy((value \ indicesValueKey).as[Seq[Seq[String]]])
    )
  }

  def apply(indices: Seq[Seq[String]]) = new Unique(indices)

}
