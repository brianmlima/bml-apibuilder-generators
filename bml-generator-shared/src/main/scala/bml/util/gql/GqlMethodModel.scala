package bml.util.gql

import io.apibuilder.spec.v0.models.{Operation, Parameter, Resource}
import lib.{DatatypeResolver, Text}

/**
 * A class for modeling a {@link Resource} as a Graphql Query or Mutation.
 *
 * @param datatypeResolver a resolver for data types from the api description
 * @param resource         The resource we are modeling methods for
 * @param operation        the specific operation we are modeling
 * @param methodPrefix     a method prefix to allow overriding the default method name convention.
 */
class GqlMethodModel(datatypeResolver: DatatypeResolver, resource: Resource, operation: Operation, methodPrefix: String) {


  def returnDatatype = GraphQLUtil.getGqlSchemaDatatype(datatypeResolver, resource.`type`, true).getOrElse(
    throw new IllegalArgumentException(s"Unable to resolve the resource type ${resource.`type`}. Check to make sure the model is defined")
  )

  def methodName = makeMethodName

  def requiredParams = operation.parameters.filter(_.required).seq

  def optionalParams = operation.parameters.filter(!_.required).seq

  def methodComment = GraphQLUtil.commentFromOpt(operation.description)

  def parametersString = Seq(makeRequiredParams, makeOptionalParams).flatten.mkString(",\n")

  def returnTypeString = returnDatatype.shortName

  override def toString: String = s"\n ${methodComment}${methodName}(\n${parametersString}\n ): ${returnTypeString}"

  private def makeRequiredParams = {
    requiredParams.seq.map(makeParam)
  }

  private def makeOptionalParams = {
    optionalParams.seq.map(makeParam)
  }

  private def makeMethodName(): String = {
    //Beacause there is not overloading in GQL we create the query names based on the input field names
    val requiredStrings = requiredParams.map(_.name).seq.map(GraphQLUtil.toTypeName).mkString("And")
    val optionalStrings = optionalParams.map(_.name).seq.map(GraphQLUtil.toTypeName).mkString("Or")
    s" ${methodPrefix}${returnDatatype.shortName}By${requiredStrings}${if (optionalStrings.nonEmpty) s"Or$optionalStrings" else ""}"
  }

  private def isIdParam(parameter: Parameter): Boolean = {
    "uuid".equals(parameter.`type`)
  }

  private def makeParam(param: Parameter): String = {
    val dataType = GraphQLUtil.getGqlSchemaDatatype(datatypeResolver, param.`type`, param.required).getOrElse(
      throw new IllegalArgumentException(s"Unable to resolve the data type ${param.`type`} parameter name ${param.name} . Check to make sure the model is defined")
    )
    var paramName = Text.initLowerCase(GraphQLUtil.toTypeName(param.name))
    s"  ${GraphQLUtil.commentFromOpt(param.description)}  ${paramName}: ${if (isIdParam(param)) "ID" else dataType.shortName}${if (param.required) "!"}"
  }

}
