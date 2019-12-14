package bml.util.spring

import bml.util.GeneratorFSUtil.makeFile
import bml.util.NameSpaces
import bml.util.java.ClassNames._
import bml.util.java.JavaArrays.arrayOf
import com.squareup.javapoet.CodeBlock.of
import com.squareup.javapoet._
import io.apibuilder.generator.v0.models.File
import javax.lang.model.element.Modifier._


object MethodArgumentNotValidExceptionHandler {

  /**
   * The generated class simple name
   */
  val name = "MethodArgumentNotValidExceptionHandler"

  /**
   * Generats a spring @ControllerAdvice that handles validation errors and responses for all endpoints.
   * This was much easier than writing a handler for each @Controller
   *
   * @param nameSpaces the NameSpaces object we are generating in
   * @return a @ControllerAdvice impl class
   */
  def get(nameSpaces: NameSpaces): Seq[File] = {

    val builder = TypeSpec.classBuilder(name)
      .addAnnotation(AnnotationSpec.builder(order).addMember("value", "$T.HIGHEST_PRECEDENCE", ordered).build())
      .addAnnotation(controllerAdvice)
      .addMethod(
        MethodSpec.methodBuilder("methodArgumentNotValidException")
          .returns(fieldValidationResponse)
          .addParameter(methodArgumentNotValidException, "ex")
          .addModifiers(PUBLIC)
          .addAnnotation(responseStatusBadRequest)
          .addAnnotation(responseBody)
          .addAnnotation(badArgExceptionHandler)
          .addStatement(of("$T result = ex.getBindingResult()", bindingResult))
          .addStatement(of("$T fieldErrors = result.getFieldErrors()", arrayOf(fieldError)))
          .addStatement(of("return processFieldErrors(fieldErrors)"))
          .build()
      )
      .addMethod(
        MethodSpec.methodBuilder("processFieldErrors")
          .returns(fieldValidationResponse)
          .addParameter(arrayOf(fieldError), "fieldErrors", FINAL)
          .addModifiers(PRIVATE)
          .addCode("final $T responseBuilder = $T.builder()", fieldValidationResponseBuilder, fieldValidationResponse)
          .addCode(".status($T.BAD_REQUEST.value())", httpStatus)
          .addCode(".message($S);", "validation error")
          .addCode(
            CodeBlock.builder()
              .beginControlFlow("for($T fieldError: fieldErrors)", fieldError)
              .add(
                CodeBlock.builder()
                  .add("responseBuilder.error(")
                  .add("$T.builder().path(fieldError.getField()).message(fieldError.getDefaultMessage()).build()", fieldValidationError)
                  .add(");")
                  .build()
              )
              .endControlFlow()
              .build()
          )
          .addStatement("return responseBuilder.build()")
          .build()
      )


    Seq(makeFile(name, nameSpaces.controller, builder))
  }


  private def fieldValidationResponse = ClassName.get("org.bml.ebsco.validation.v0.models", "FieldValidationResponse")

  private def fieldValidationError = ClassName.get("org.bml.ebsco.validation.v0.models", "FieldValidationError")

  private def fieldValidationResponseBuilder = ClassName.get("org.bml.ebsco.validation.v0.models.FieldValidationResponse", "FieldValidationResponseBuilder")

  private def responseStatusBadRequest = AnnotationSpec.builder(responseStatus).addMember("value", "$T.BAD_REQUEST", httpStatus).build()

  private def badArgExceptionHandler = AnnotationSpec.builder(exceptionHandler).addMember("value", "$T.class", methodArgumentNotValidException).build()


}
