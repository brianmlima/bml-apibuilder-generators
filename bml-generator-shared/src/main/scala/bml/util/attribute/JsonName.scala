package bml.util.attribute

import java.util.Optional

import akka.http.scaladsl
import akka.http.scaladsl.model
import io.apibuilder.spec.v0.models.{Field, Model}
import play.api.libs.json.{JsError, JsResult}
import play.api.libs.json.JsResult.Exception

class JsonName(val name: String) {

}

object JsonName {

  val attributeName = "altName"
  val nameValueKey = "name"
  val singularValueKey = "singular"

  def fromField(field: Field): Option[JsonName] = {
    val optional = field.attributes.find(_.name == attributeName)
    if (optional.isEmpty) {
      return None;
    }
    val value = optional.get.value
    val name = (value \ nameValueKey);
    val singular = (value \ singularValueKey);

    if (name.isDefined) {
      Some(new JsonName((value \ nameValueKey).as[String]))
    } else {
      None
    }
  }

  def apply(name: String) = new JsonName(name)

}


