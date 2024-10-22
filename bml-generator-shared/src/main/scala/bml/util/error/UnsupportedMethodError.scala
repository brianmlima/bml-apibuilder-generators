package bml.util.error

import bml.util.NameSpaces
import io.apibuilder.spec.v0.models.{Operation, Resource, Service}
import play.api.libs.json._

/**
 * Represents an error that occurs when a method is not supported in a specific generator.
 *
 * @param message       The error message describing the unsupported method error.
 * @param generatorName The name of the generator where the method is not supported.
 * @param service       The service associated with the unsupported method.
 * @param nameSpaces    The namespaces related to the unsupported method.
 * @param resource      The resource on which the operation was attempted.
 * @param operation     The specific operation that is not supported.
 */
class UnsupportedMethodError(
                              message: String,
                              service: Service,
                              val nameSpaces: NameSpaces,
                              val resource: Resource,
                              val operation: Operation
                            ) extends GeneratorError(message,service) {

  override def toString: String = {
    s"UnsupportedMethodError(message = $message, service = ${service.name}," +
      s" nameSpaces = ${nameSpaces.service.nameSpace}, resource = ${resource.path}, operation.path = ${operation.path}," +
      s" operation.path = ${operation.method.toString})"
  }

  // method for converting to json
  def toJson: JsValue = {
    Json.obj(
      "message" -> message,
      "service" -> serviceToJson(),
      "nameSpaces" -> nameSpaces.service.nameSpace,
      "resource" -> resource.path,
      "operation" -> Json.obj(
        "method" -> operation.method.toString,
        "path" -> operation.path
      )
    )
  }
}

object UnsupportedMethodError {
  def apply(
             message: String,
             service: Service,
             nameSpaces: NameSpaces,
             resource: Resource,
             operation: Operation
           ): UnsupportedMethodError = {
    new UnsupportedMethodError(message, service, nameSpaces, resource, operation)
  }
}