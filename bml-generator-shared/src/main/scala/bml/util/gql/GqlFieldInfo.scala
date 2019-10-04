package bml.util.gql

import io.apibuilder.spec.v0.models.Field
import lib.{DatatypeResolver, Text}

class GqlFieldInfo(datatypeResolver: DatatypeResolver, field: Field) {

  def apiDatatype = datatypeResolver.parse(field.`type`, field.required).getOrElse {
    sys.error(s"Unable to parse datatype ${field.`type`}")
  }

  def gqlDatatype = GqlSchemaDatatype(apiDatatype)

  def defaultValue = field.default.fold("") {
    " = " + gqlDatatype.valueFromString(_)
  }

  def gqlFieldName = GraphQLUtil.checkForReservedWord(Text.snakeToCamelCase(field.name))

  def required = if (field.required) "!" else ""

  override def toString = {
    GraphQLUtil.commentFromOpt(field.description) + s" ${gqlFieldName}: ${gqlDatatype.name}${if (field.required) "!" else ""}"
  }

}
