package bml.util.error

import io.apibuilder.spec.v0.models.Service
import play.api.libs.json.{JsValue, Json}

/**
 * Custom Exception class for errors encountered during code generation.
 * Inherits from the standard Exception class.
 *
 * @param message       A String representing the error message associated with the exception.
 * @param generatorName A String representing the name of the generator that encountered the error.
 */
class GeneratorError(val message: String,
                     val service: Service
                    ) extends Exception(message) {


  protected def serviceToJson(): JsValue = {
    Json.obj(
      "name" -> service.name,
      "version" -> service.version,
      "description" -> service.description,
      "organization" -> service.organization.key,
      "base_url" -> service.baseUrl
    )
  }
}

/**
 * Represents an error that occurs in the Generator class.
 *
 * @param message A String containing the error message.
 */
object GeneratorError {
  def apply(message: String,
            service: Service
           ): GeneratorError = new GeneratorError(message, service);
}
