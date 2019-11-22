package controllers

import io.apibuilder.generator.v0.models.Generator
import io.apibuilder.generator.v0.models.json._
import lib.generator.{CodeGenTarget, CodeGenerator}
import play.api.libs.json._
import play.api.mvc._

class Generators extends InjectedController {
  def get(
           key: Option[String] = None,
           limit: Integer = 100,
           offset: Integer = 0
         ) = Action {
    val generators = Generators.targets.
      filter(t => t.codeGenerator.isDefined && t.status != lib.generator.Status.Proposal).
      filter(t => key.isEmpty || key == Some(t.metaData.key)).
      map(t => t.metaData)

    Ok(Json.toJson(generators.drop(offset).take(limit)))
  }

  def getByKey(key: String) = Action {
    Generators.findGenerator(key) match {
      case Some((target, _)) => Ok(Json.toJson(target.metaData))
      case _ => NotFound
    }
  }
}

object Generators {
  def findGenerator(key: String): Option[(CodeGenTarget, CodeGenerator)] = for {
    target <- targets.find(_.metaData.key == key)
    codeGenerator <- target.codeGenerator
  } yield (target -> codeGenerator)

  val targets = Seq(
    CodeGenTarget(
      metaData = Generator(
        key = "bml_lombok",
        name = "BML Lombok Models",
        description = Some("Generate Java models from the API description."),
        language = Some("Java")
      ),
      status = lib.generator.Status.InDevelopment,
      codeGenerator = Some(models.generator.lombok.LombokPojoClasses)
    ),
    CodeGenTarget(
      metaData = Generator(
        key = "ebsco_spring_service",
        name = "EBSCO Spring Service",
        description = Some("Generate EBSCO specific Spring Service from the API description."),
        language = Some("Java")
      ),
      status = lib.generator.Status.InDevelopment,
      codeGenerator = Some(models.generator.ebscoservice.EbscoServices)
    ),

    CodeGenTarget(
      metaData = Generator(
        key = "ebsco_spring_service_testing",
        name = "EBSCO Spring Service Testing",
        description = Some("Generate Tests and Test infrastructure for EBSCO specific Spring Service from the API description."),
        language = Some("Java")
      ),
      status = lib.generator.Status.InDevelopment,

      codeGenerator = Some(models.generator.ebscoservicetesting.EbscoServicesTesting)
    ),

    //    CodeGenTarget(
    //      metaData = Generator(
    //        key = "java_persistance_sql",
    //        name = "SQL based Persistance for pojos generated by the bml_lombok plugin",
    //        description = Some("Generate Java models from the API description."),
    //        language = Some("Java")
    //      ),
    //      status = lib.generator.Status.InDevelopment,
    //      codeGenerator = Some(models.generator.java.persistence.sql.JavaPersistanceSqlClasses)
    //    ),

    CodeGenTarget(
      metaData = Generator(
        key = "graphql_schema_generator",
        name = "GraphQL Schema Generator",
        description = Some("Generate a Graphql Schema for use with Graphql-Java."),
        language = Some("GraphQL")
      ),
      status = lib.generator.Status.InDevelopment,
      codeGenerator = Some(models.generator.graphQlSchema.GraphQLSchema)
    )
  ).sortBy(_.metaData.key)
}
