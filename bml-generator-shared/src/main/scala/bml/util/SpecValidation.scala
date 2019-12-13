package bml.util

import akka.http.scaladsl
import akka.http.scaladsl.model
import io.apibuilder.spec.v0.models.{Operation, ResponseCodeInt, Service}
import lombok.extern.slf4j.Slf4j

@Slf4j
object SpecValidation {
  def validate(service: Service, header: Option[String]): Option[Seq[String]] = {
    val errors =
      checkAllPathVarOperationsHave404(service) ++
        checkAllFieldsWithMinRequirementHaveAMax(service) ++
        checkAllModelsHaveADescription(service) ++
        checkAllStringFieldsHaveMinMax(service) ++
        checkAllModelsDontHaveUnderscores(service) ++
        checkAllModelFieldsDontHaveUnderscores(service) ++
        checkAllEnumsDontHaveUnderscores(service)
    if (errors.isEmpty) {
      None
    } else {
      Some(errors)
    }
  }

  def checkAllModelsHaveADescription(service: Service): Seq[String] = {
    var out: Seq[String] = Seq()
    service.models.foreach(
      model =>
        if (model.description.isEmpty) {
          out = out ++ Seq(s"ERROR: All Models must have a description. Service='${service.name}' Model '${model.name}'")
        }
    )
    out
  }

  def checkAllModelsDontHaveUnderscores(service: Service): Seq[String] = {
    var out: Seq[String] = Seq()
    service.models.foreach(
      model =>
        if (model.name.contains("_") || model.name.contains("-")) {
          out = out ++ Seq(s"ERROR: All Model names should be camel case and must not have underscores or hyphens. Service='${service.name}'=Model='${model.name}'")
        }
    )
    out
  }

  def checkAllEnumsDontHaveUnderscores(service: Service): Seq[String] = {
    var out: Seq[String] = Seq()
    service.enums.foreach(
      `enum` =>
        if (`enum`.name.contains("_") || `enum`.name.contains("-")) {
          out = out ++ Seq(s"ERROR: All Enum names should be camel case and must not have underscores or hyphens. Service='${service.name}'=Model='${`enum`.name}'")
        }
    )
    out
  }


  def checkAllModelFieldsDontHaveUnderscores(service: Service): Seq[String] = {
    var out: Seq[String] = Seq()
    service.models.foreach(
      model =>
        model.fields.foreach(
          field =>
            if (field.name.contains("_") || field.name.contains("-")) {
              out = out ++ Seq(s"ERROR: All Model Field names should be camel case and must not have underscores or hyphens. Service='${service.name}' Model='${model.name}' Field='${field.name}'")
            }
        )
    )
    out
  }


  def checkAllStringFieldsHaveMinMax(service: Service): Seq[String] = {
    var out: Seq[String] = Seq()
    service.models.foreach(
      model =>
        model.fields.foreach(
          field => {
            if (field.`type` == "string") {
              if (field.minimum.isEmpty) {
                out = out ++ Seq(s"ERROR: All Model Fields must have a 'minimum' field defined. Service='${service.name}' Model='${model.name}' Field='${field.name}'")
              }
              if (field.maximum.isEmpty) {
                out = out ++ Seq(s"ERROR: All Model Fields must have a 'maximum' field defined. Service='${service.name}' Model='${model.name}' Field='${field.name}'")
              }
            }
          }
        )
    )
    out
  }


  def checkAllFieldsWithMinRequirementHaveAMax(service: Service): Seq[String] = {
    var out: Seq[String] = Seq()
    service.models.foreach(
      model =>
        model.fields.foreach(
          field =>
            if (field.minimum.isDefined && field.maximum.isEmpty) {
              out = out ++ Seq(s"ERROR: Model Fields that have a minimum set must have a maximum. Model '${model.name}' field '${field.name}'")
            }
        )
    )
    out
  }


  def checkAllPathVarOperationsHave404(service: Service): Seq[String] = {
    var out: Seq[String] = Seq()
    service.resources.foreach(
      resource =>
        resource.operations.foreach(
          operation =>
            if (hasPathParam(operation)) {
              if (!has404(operation)) {
                out = out ++ Seq(s"ERROR: Resource Operations that contain path parameters must have 404 response defined. Resource '${resource.`type`}' Operation '${operation.method} ${operation.path}'")
              } else {
                if (!findResponseCode(operation, 404).get.`type`.equals("unit")) {
                  out = out ++ Seq(s"ERROR: Resource Operations that contain path parameters must have 404 response defined with a unit type. Resource '${resource.`type`}' Operation '${operation.method} ${operation.path}' exepced type unit ")
                }
              }
            }
        )
    )
    out
  }

  def has404AsUnit(operation: Operation) = {

  }


  def has404(operation: Operation) = {
    hasResponseCode(operation, 404)
  }

  def hasResponseCode(operation: Operation, code: Int) = {
    operation.responses.filter(
      response => response.code.equals(ResponseCodeInt.apply(code))
    ).nonEmpty
  }

  def findResponseCode(operation: Operation, code: Int) = {
    operation.responses.find(
      response => response.code.equals(ResponseCodeInt.apply(code))
    )
  }


  def hasPathParam(operation: Operation) = {
    operation.path.contains(":")
  }


}
