package bml.util.persist

import bml.util.gql.GqlAttributeValues
import play.api.libs.json.{JsError, JsObject, JsPath, JsSuccess, Reads}
import play.api.libs.functional.syntax._
import io.apibuilder.spec.v0.models.{Attribute, Field, Model}

case class  PersistModelAttribute
(
  var isId: Boolean = false,
  var generated: Boolean=false,
  var generatedAt: String,
  var generator: String
){
}

object PersistModelAttribute {

  val attributeName = "persistence"

  implicit val persistModelAttributeReads: Reads[PersistModelAttribute] = (
    (JsPath \ "isId").readWithDefault[Boolean](false) and
      (JsPath \ "generated").readWithDefault[Boolean](false) and
      (JsPath \ "generatedAt").readWithDefault[String]("client") and
      (JsPath \ "generator").readWithDefault[String]("unknown")
    ) (PersistModelAttribute.apply _)

  def read(jsObject: Option[JsObject]): Option[PersistModelAttribute] = {
    if (jsObject.isEmpty) return Option.empty
    jsObject.get.validate[PersistModelAttribute] match {
      case s: JsSuccess[PersistModelAttribute] => Some(s.get)
      case e: JsError => Option.empty;
    }
  }

  def read(model: Model): Option[PersistModelAttribute]= {
    val attribute = model.attributes.find(_.name == attributeName)
    if (attribute.isDefined) PersistModelAttribute.read(Some(attribute.get.value)) else None
  }
  def read(field: Field): Option[PersistModelAttribute]= {
    val attribute = field.attributes.find(_.name == attributeName)
    if (attribute.isDefined) PersistModelAttribute.read(Some(attribute.get.value)) else None
  }

}

