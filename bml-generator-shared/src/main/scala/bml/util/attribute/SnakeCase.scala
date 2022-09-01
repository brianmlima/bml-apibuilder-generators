package bml.util.attribute

import io.apibuilder.spec.v0.models.Model
import play.api.Logger

class SnakeCase() {

}

object SnakeCase {
  val logger: Logger = Logger.apply(this.getClass())

  val attributeName = "snakeCase"


  def hasAttribute(model: Model): Boolean = {
    model.attributes.find(_.name == attributeName).isDefined
  }

  def fromModel(model: Model): Option[SnakeCase] = {

    val optional = model.attributes.find(_.name == attributeName)
    logger.info(s"SnakeCase.isDefined=${optional.isDefined}")
    if (optional.isDefined) {
      Some(SnakeCase())
    }
    None
  }

  def apply() = new SnakeCase()
}


