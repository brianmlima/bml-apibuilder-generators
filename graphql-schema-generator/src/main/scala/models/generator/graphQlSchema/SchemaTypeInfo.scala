package models.generator.graphQlSchema

import io.apibuilder.spec.v0.models.{Model, Union}
import lib.DatatypeResolver
import lib.Text._
import bml.util.gql.{GqlFieldInfo, GraphQLUtil}

class SchemaTypeInfo(datatypeResolver: DatatypeResolver,model: Model, relatedUnions: Seq[Union]) {

  def fieldInfo = model.fields.map(new GqlFieldInfo(datatypeResolver, _))


  def gqlTypeName = GraphQLUtil.toTypeName(model.name)

  def unionClassNames = relatedUnions.map ( u => GraphQLUtil.toTypeName(u.name) )

  def implementsClause = if (unionClassNames.isEmpty) "" else unionClassNames.mkString(" implements ", ", ", "")

  def gqlSchemaType = GraphQLUtil.resolveGqlschemaType(model)

  override def toString = {
    GraphQLUtil.commentFromOpt(model.description) +
      s"$gqlSchemaType $gqlTypeName$implementsClause {\n" +
      (
        fieldInfo
          .mkString(",\n") + "\n\n"
        ).indent(4) + "\n" +
      "}\n"
  }

}
