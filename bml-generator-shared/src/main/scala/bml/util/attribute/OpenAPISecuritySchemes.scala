package bml.util.attribute

import bml.util.openapi.model.{AuthorizationCode, SecuritySchema}
import io.apibuilder.spec.v0.models.Attribute
import play.api.libs.json.{JsObject, JsValue}

class OpenAPISecuritySchemes(
                              val oauth2: JsValue
                              //                              val name: String,
                              //                              val `type`: String
                            ) {

}

object OpenAPISecuritySchemes {

  val attributeName = "OpenAPISecuritySchemes"
  val securitySchemesName = "securitySchemes"
  val securitySchemesNameName = "name"
  val securitySchemesTypeName = "type"
  val securitySchemesflowsName = "flows"


  //val nameName = "name"

  def isThisAttribute(attribute: Attribute): Boolean = {
    attribute.name.equals(attributeName)
  }

  def fromAttributes(attributes: Seq[Attribute]): Option[OpenAPISecuritySchemes] = {
    val optional = attributes.find(_.name == attributeName)
    if (optional.isEmpty) {
      return None
    }
    asThisAttribute(optional.get)
  }


  def asThisAttribute(attributeIn: Attribute): Option[OpenAPISecuritySchemes] = {
    if (!attributeIn.name.equals(attributeName)) None

    val valueIn = attributeIn.value.value;
    val securitySchemesIn = valueIn.get("securitySchemes")
    //    if (securitySchemesIn.isDefined) {
    //      val securitySchemesInJsValue = securitySchemesIn.get
    //      val oauth2In = securitySchemesInJsValue \ "OAuth2"
    //      if (oauth2In.isDefined) {
    //        val securityScheme = SecuritySchema.builder()
    //        val oauth2Jsvalue = oauth2In.get
    //        val typeIn = oauth2Jsvalue \ "type"
    //        if (typeIn.isDefined) {
    //          securityScheme.`type`(typeIn.get.toString())
    //        }
    //        val flowsIn = oauth2Jsvalue \ "flows"
    //        if (flowsIn.isDefined) {
    //          val flowsJsValue = flowsIn.get
    //          //
    //          val authorizationCodeIn = flowsJsValue \ "authorizationCode"
    //          if (authorizationCodeIn.isDefined) {
    //            val authorizationCodeJsValue = authorizationCodeIn.get
    //            //            val authorizationCode = AuthorizationCode.builder()
    //            //            val authorizationUrlIn = (authorizationCodeJsValue \ "authorizationUrl")
    //            //            val authorizationCodeIn = (authorizationCodeJsValue \ "tokenUrl")
    //          }
    //        }
    //        //
    //        //
    //      }
    //      securitySchemesIn.get.result.
    //      val oauth2 = (securitySchemesIn.get \ "Oauth2")
    //
    //      return Option.apply(new OpenAPISecuritySchemes(oauth2))
    //}

    return None

    //    val nameOut = securitySchemaIn.get(securitySchemesNameName).toString
    //    val typeOut = securitySchemaIn.get(securitySchemesTypeName).toString
    //
    //
    //    SecuritySchema.builder()
    //      .name(nameOut)
    //      .`type`(typeOut)


  }


}
