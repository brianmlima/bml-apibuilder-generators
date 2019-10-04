package bml.util.gql

import play.api.libs.json.Reads._
import play.api.libs.json.{JsObject, JsPath, Reads, _} // Combinator syntax


case class  GqlAttributeValues
(
  var include: Boolean = true,
  var methodPrefix: String,
  var isQuery: Boolean = true,
  var isMutation: Boolean = false,
  var isInputType: Boolean = false,
){


}


















