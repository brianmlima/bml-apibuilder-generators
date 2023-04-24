package bml.util.spring

import bml.util.{NameSpaces, ServiceTool, VersionTool}
import bml.util.java.ClassNames.{JavaTypes, LombokTypes, SpringTypes}
import bml.util.java.JavaPojoUtil
import bml.util.spring.MethodArgumentNotValidExceptionHandler.className
import bml.util.spring.SpringVersion.SpringVersion
import io.apibuilder.generator.v0.models.File
import io.apibuilder.spec.v0.models.Service

object MethodArgumentNotValidExceptionHandler {

  import com.squareup.javapoet._
  import javax.lang.model.element.Modifier._
  import com.squareup.javapoet.CodeBlock.of
  import bml.util.GeneratorFSUtil.makeFile

  def className(springVersion: SpringVersion, service: Service): String = {

    springVersion match {
      case bml.util.spring.SpringVersion.SIX => JavaPojoUtil.toClassName(s"${ServiceTool.prefix(springVersion, service)}-method-argument-not-valid-exception-handler")
      case bml.util.spring.SpringVersion.FIVE => JavaPojoUtil.toClassName(s"${service.name}-method-argument-not-valid-exception-handler")
    }
  }

  /**
   * Generats a spring @ControllerAdvice that handles validation errors and responses for all endpoints.
   * This was much easier than writing a handler for each @Controller
   *
   * @param nameSpaces the NameSpaces object we are generating in
   * @return a @ControllerAdvice impl class
   */
  def get(service: Service, nameSpaces: NameSpaces): Seq[File] = {
    get(SpringVersion.FIVE, service, nameSpaces)
  }

  def get(springVersion: SpringVersion, service: Service, nameSpaces: NameSpaces): Seq[File] = {

    val staticImports = Seq(JavaTypes.toList.staticImport)
    val name = className(springVersion, service)

    val builder = TypeSpec.classBuilder(name)
      .addAnnotation(LombokTypes.Generated)
      .addJavadoc("An Exception handler used by controllers to marshal $T into $T objects. This ensures that all API's have a consistent response for validation errors.", SpringTypes.MethodArgumentNotValidException, fieldValidationResponse)
      .addAnnotation(AnnotationSpec.builder(SpringTypes.Order).addMember("value", "$T.HIGHEST_PRECEDENCE", SpringTypes.Ordered).build())
      .addAnnotation(SpringTypes.ControllerAdvice)
      .addMethod(
        MethodSpec.methodBuilder("methodArgumentNotValidException")
          .returns(fieldValidationResponse)
          .addParameter(SpringTypes.MethodArgumentNotValidException, "ex", FINAL)
          .addModifiers(PUBLIC)
          .addAnnotation(responseStatusBadRequest)
          .addAnnotation(SpringTypes.ResponseBody)
          .addAnnotation(badArgExceptionHandler)
          .addStatement(of("$T result = ex.getBindingResult()", SpringTypes.BindingResult))
          .addStatement(of("$T fieldErrors = result.getFieldErrors()", JavaTypes.List(SpringTypes.FieldError)))
          .addStatement(of("return processFieldErrors(fieldErrors)"))
          .build()
      )
      .addMethod(
        MethodSpec.methodBuilder("processFieldErrors")
          .returns(fieldValidationResponse)
          .addParameter(JavaTypes.List(SpringTypes.FieldError), "fieldErrors", FINAL)
          .addModifiers(PRIVATE)
          .addCode("final $T responseBuilder = $T.builder()", fieldValidationResponseBuilder, fieldValidationResponse)
          .addCode(".status($T.BAD_REQUEST.value())", SpringTypes.HttpStatus)
          .addCode(".message($S);", "validation error")
          .addStatement("responseBuilder.errors(" +
            "fieldErrors.stream().map(" +
            "fieldError->$T.builder().path(fieldError.getField()).message(fieldError.getDefaultMessage()).build()" +
            ").collect($T.toList()))",
            fieldValidationError,
            JavaTypes.Collectors
          )
          .addStatement("return responseBuilder.build()")
          .build()
      )

    Seq(makeFile(name, nameSpaces.controller, builder, staticImports: _*))
  }

  private def fieldValidationResponse

  = ClassName.get("org.bml.validation.v0.models", "FieldValidationResponse")

  private def fieldValidationError

  = ClassName.get("org.bml.validation.v0.models", "FieldValidationError")

  private def fieldValidationResponseBuilder

  = ClassName.get("org.bml.validation.v0.models.FieldValidationResponse", "FieldValidationResponseBuilder")

  private def responseStatusBadRequest

  = AnnotationSpec.builder(SpringTypes.ResponseStatus).addMember("value", "$T.BAD_REQUEST", SpringTypes.HttpStatus).build()

  private def badArgExceptionHandler

  = AnnotationSpec.builder(SpringTypes.ExceptionHandler).addMember("value", "$T.class", SpringTypes.MethodArgumentNotValidException).build()


}
