package models.generator.graphQlSchema

import bml.util.gql.{GqlAttributeValues, GqlMethodModel, GraphQLUtil}
import io.apibuilder.generator.v0.models.{File, InvocationForm}
import io.apibuilder.spec.v0.models.{Attribute, Enum, EnumValue, Model, Operation, Resource, Service, Union}
import lib.generator.{CodeGenerator, GeneratorUtil}
import org.slf4j.LoggerFactory
import play.api.libs.json.Reads._
import play.api.libs.json._ // Combinator syntax


object GraphQLSchema extends CodeGenerator {

  import scala.language.implicitConversions

  var log = LoggerFactory.getLogger("GraphQLSchema")

  override def invoke(form: InvocationForm): Either[Seq[String], Seq[File]] = invoke(form, addHeader = true)

  def invoke(form: InvocationForm, addHeader: Boolean = false): Either[Seq[String], Seq[File]] = Right(generateCode(form, addHeader))

  private def generateCode(form: InvocationForm, addHeader: Boolean = true): Seq[File] = {
    val header =
      if (addHeader) Some(new GraphQlSchemaComments(form.service.version, form.userAgent).forClassFile)
      else None
    new GqlSchemaGenerator(form.service, header).generateSchema()
  }

  class GqlSchemaGenerator(service: Service, header: Option[String]) {
    //Resolves data types for built in types and models
    private val datatypeResolver = GeneratorUtil.datatypeResolver(service)

    // Used for generating the pojos to couple wit the GQL schema
    private val safeNamespace = service.namespace.split("\\.").map {
      GraphQLUtil.checkForReservedWord
    }.mkString(".")

    // creates the apropriate dir for generated files
    def createDirectoryPath(namespace: String) = namespace.replace('.', '/')

    //Package declaration for any generated classes
    private val modelsPackageDeclaration = s"package $safeNamespace.gql;"

    //The directory models will be placed in. For simplicity all generated models go in one place
    private val modelsDirectoryPath = createDirectoryPath(s"$safeNamespace.gql")


    //Generates the GQL schema.
    def generateSchema() = {

      val generatedEnums = service.enums.map(generateEnum)

      val generatedUnionTypes = service.unions.map(generateUnionType)

      val generatedModels = service.models.filter(includeInSchema).map(
        model => {
          generateModel(
            model,
            service.unions.filter(_.types.exists(_.`type` == model.name)))
        }
      )

      val generatedQueries = service.resources.flatMap(generateQueries)

      val generatedMutations = service.resources.flatMap(generateMutations)

      //Clean this mess up!
      val source = List(

        Seq[Option[String]](Some("#Graphql has no formal scalar type for long and int is only 32-bits\nscalar long\n")),
        generatedEnums,
        generatedUnionTypes,
        generatedModels,
        Seq[Option[String]](Some("# The top level Query type.\ntype Query {")),
        Seq[Option[String]](Some(generatedQueries.filter(_.nonEmpty).map(_.get).mkString("\n"))),
        Seq[Option[String]](Some("\n}\n")),
        Seq[Option[String]](Some("# The top level Query type.\ntype Mutation {")),
        Seq[Option[String]](Some(generatedMutations.filter(_.nonEmpty).map(_.get).mkString("\n"))),
        Seq[Option[String]](Some("\n}\n")),

        Seq[Option[String]](Some("# The top level Schema type.\nschema {\n  query: Query\n  mutation: Mutation\n}"))
      ).flatten.flatten.mkString("\n") + "\n"
      Seq(File("schema.graphqls", Some(modelsDirectoryPath), source))
    }

    //Generate the enumeration types.
    def generateEnum(enum: Enum): Option[String] = {
      def generateEnumValue(enumValue: EnumValue): String = {
        commentFromOpt(enumValue.description) +
          GraphQLUtil.textToEnumValue(enumValue.name)
      }

      val enumName = GraphQLUtil.toTypeName(enum.name)

      val enumDeclaration = {
        import lib.Text._
        commentFromOpt(enum.description) +
          s"enum $enumName {\n" +
          enum.values.map {
            generateEnumValue
          }.mkString(",\n").indent(4) + "\n" +
          "}\n"
      }
      return new Some(enumDeclaration)
    }

    def generateQueries(resource: Resource): Seq[Option[GqlMethodModel]] = {
      resource.operations.seq.filter(isOperationQuery).map(new GqlMethodModel(datatypeResolver, resource, _, "find")).map(Some(_))
    }

    def generateMutations(resource: Resource): Seq[Option[GqlMethodModel]] = {
      resource.operations.seq.filter(isOperationMutation).map(op => new GqlMethodModel(datatypeResolver, resource, op, getMutationMethodPrefix(op).getOrElse("mutate"))).map(new Some(_))
    }

    def generateUnionType(union: Union): Option[String] = {
      return Option.empty;
    }

    def generateUndefinedUnionType(union: Union): Option[String] = {
      return Option.empty;
    }

    def generateModel(model: Model, relatedUnions: Seq[Union]): Option[SchemaTypeInfo] = {
      new Some(new SchemaTypeInfo(datatypeResolver, model, relatedUnions))
    }

  }

  def valueToModel(jsObject: JsObject): Option[GqlModelAttributeValue] = Option(jsObject.asInstanceOf[GqlModelAttributeValue])

  def gqlAttributes(attributes: Seq[Attribute]): Option[GqlAttributeValues] = {
    GraphQLUtil.getGqlAttributeValues(attributes)
  }


  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // I hate this code but until I find a decent play JSON tutorial and can convert this to a pojo without getting Nill
  // problems this is what it is
  def getMutationMethodPrefix(operation: Operation): Option[String] = {
    val attribute = operation.attributes.find(_.name == "gql")
    if (attribute.isEmpty) return Option.empty;
    (attribute.get.value \ "methodPrefix").validate[String] match {
      case s: JsSuccess[String] => Some(s.get)
      case e: JsError => Option.empty;
    }
  }

  def isOperationMutation(model: Operation): Boolean = {
    val attribute = model.attributes.find(_.name == "gql")
    if (attribute.isEmpty) return false;
    (attribute.get.value \ "isMutation").validate[Boolean] match {
      case s: JsSuccess[Boolean] => s.get
      case e: JsError => false
    }
  }

  def isOperationQuery(model: Operation): Boolean = {
    val attribute = model.attributes.find(_.name == "gql")
    if (attribute.isEmpty) return false;
    (attribute.get.value \ "isQuery").validate[Boolean] match {
      case s: JsSuccess[Boolean] => s.get
      case e: JsError => false
    }
  }

  def includeInSchema(model: Model): Boolean = {
    val attribute = model.attributes.find(_.name == "gql")
    if (attribute.isEmpty) return true;
    (attribute.get.value \ "include").validate[Boolean] match {
      case s: JsSuccess[Boolean] => s.get
      case e: JsError => true
    }
  }

  def gqlAttribute(attributes: Seq[Attribute]): Option[Attribute] = attributes.find(_.name == "gql")

  def getBoolean(attribute: Option[Attribute], key: String, defaultValue: Boolean): Boolean = {
    if (attribute.nonEmpty) return defaultValue
    (attribute.get.value \ key).validate[Boolean] match {
      case s: JsSuccess[Boolean] => s.get
      case e: JsError => defaultValue
    }
  }

  def getString(attribute: Option[Attribute], key: String, defaultValue: String): String = {
    if (attribute.nonEmpty) return defaultValue
    (attribute.get.value \ key).validate[String] match {
      case s: JsSuccess[String] => s.get
      case e: JsError => defaultValue
    }
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


  private def
  commentFromOpt(opt: Option[String]) = {
    opt.fold("") { s => GraphQLUtil.textToComment(s) + "\n" }
  }


}
