package bml.util.attribute

import akka.http.scaladsl.model.headers.LinkParams.`type`
import akka.stream.TLSClientAuth.None
import bml.util.attribute.ExampleAttribute.{attributeName, stringValueKey}
import io.apibuilder.spec.v0.models.Attribute

class OpenAPISecuritySchemes(
                              val name: String,
                              val `type`: String
                            ) {

}

object OpenAPISecuritySchemes {

  val attributeName = "OpenAPISecuritySchemes"
  val securitySchemesName = "securitySchemes"
  val securitySchemesNameName = "name"
  val securitySchemesTypeName = "type"
  val nameName = "name"

  def isThisAttribute(attribute: Attribute): Boolean = {
    attribute.name.equals(attributeName)
  }

  def asThisAttribute(attributeIn: Attribute): Option[OpenAPISecuritySchemes] = {
    if (!attributeIn.name.equals(attributeName)) None

    val valueIn = attributeIn.value.value;
    val securitySchemesIn = valueIn.get(securitySchemesName)

    val nameOut = valueIn.get(securitySchemesNameName).toString
    val typeOut = valueIn.get(securitySchemesTypeName).toString

    return Option.apply(new OpenAPISecuritySchemes(nameOut, typeOut))
  }


}
