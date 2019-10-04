package bml.util.gql

import io.apibuilder.spec.v0.models.{Attribute, Model}
import lib.{DatatypeResolver, Text}
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json.{JsObject, JsPath, Reads, _} // Combinator syntax

object GraphQLUtil {
  private val ReservedWords = Set(
    "schema", "type", "query", "mutation", "Int", "Float", "String", "Boolean", "ID", "scalar", "enum")

  def checkForReservedWord(word: String): String =
    if (ReservedWords.contains(word)) word + "_"
    else word

  def textToComment(text: String): String = textToComment(Seq(text))

  def textToComment(text: Seq[String]): String = {
    "# " + text.mkString("\n # ")
  }

  def toTypeName(modelName: String) = {
    Text.safeName(Text.splitIntoWords(modelName).map {
      checkForUpperCase(_).capitalize
    }.mkString)
  }

  def textToEnumValue(modelName: String) = {
    Text.safeName(Text.splitIntoWords(modelName).map {
      checkForUpperCase(_).toUpperCase
    }.mkString("_"))
  }

  def checkForUpperCase(word: String): String =
    if (word == word.toUpperCase) word.toLowerCase
    else word


  def commentFromOpt(opt: Option[String]) = {
    opt.fold("") { s => GraphQLUtil.textToComment(s) + "\n" }
  }

  def resolveGqlschemaType(model: Model): String = {
    if(getGqlAttributeValues(model.attributes).map(_.isInputType).getOrElse(false))
      "input"
    else
      "type"
  }

  def getGqlSchemaDatatype(datatypeResolver: DatatypeResolver, typeName: String, required: Boolean) = Option[GqlSchemaDatatype] {
    val apiDatatype = datatypeResolver.parse(typeName, true).getOrElse {
      sys.error(s"Unable to parse datatype ${typeName}")
    }
    GqlSchemaDatatype(apiDatatype)
  }

  implicit val gqlAttributeValuesReads: Reads[GqlAttributeValues] = (
    (JsPath \ "include").readWithDefault[Boolean](true) and
      (JsPath \ "methodPrefix").readWithDefault[String]("find") and
      (JsPath \ "isQuery").readWithDefault[Boolean](false) and
      (JsPath \ "isMutation").readWithDefault[Boolean](false) and
      (JsPath \ "isInputType").readWithDefault[Boolean](false)
    ) (GqlAttributeValues.apply _)

  def getGqlAttributeValues(jsObject: Option[JsObject]): Option[GqlAttributeValues] = {
    if (jsObject.isEmpty) return Option.empty
    jsObject.get.validate[GqlAttributeValues] match {
      case s: JsSuccess[GqlAttributeValues] => Some(s.get)
      case e: JsError => Option.empty;
    }
  }

  def getGqlAttributeValues(attributes: Seq[Attribute]): Option[GqlAttributeValues] = {
    if(attributes.isEmpty) Option.empty else
    getGqlAttributeValues(attributes.find(_.name == "gql").map(_.value): Option[JsObject])
  }


}
