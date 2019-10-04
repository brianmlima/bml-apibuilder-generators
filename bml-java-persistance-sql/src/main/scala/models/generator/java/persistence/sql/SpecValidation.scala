package models.generator.java.persistence.sql

import io.apibuilder.spec.v0.models.{Operation, ResponseCodeInt, Service}
import lombok.extern.slf4j.Slf4j

@Slf4j
object SpecValidation {
  def validate(service: Service, header: Option[String]): Option[Seq[String]] = {
    val errors =
      checkAllPathVarOperationsHave404(service: Service)
    if (errors.isEmpty) {
      None
    } else {
      Some(errors)
    }
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
              }else{
                if(!findResponseCode(operation,404).get.`type`.equals("unit")){
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
