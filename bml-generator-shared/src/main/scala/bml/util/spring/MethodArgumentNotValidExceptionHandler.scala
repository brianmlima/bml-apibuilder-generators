package bml.util.spring

import bml.util.NameSpaces
import bml.util.java.ClassNames.{JavaTypes, SpringTypes}
import io.apibuilder.generator.v0.models.File

object MethodArgumentNotValidExceptionHandler {

  import com.squareup.javapoet._
  import javax.lang.model.element.Modifier._
  import com.squareup.javapoet.CodeBlock.of
  import bml.util.GeneratorFSUtil.makeFile

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

    val staticImports = Seq(JavaTypes.toList.staticImport)

    val builder = TypeSpec.classBuilder(name)
      .addAnnotation(AnnotationSpec.builder(SpringTypes.Order).addMember("value", "$T.HIGHEST_PRECEDENCE", SpringTypes.Ordered).build())
      .addAnnotation(SpringTypes.ControllerAdvice)
      .addMethod(
        MethodSpec.methodBuilder("methodArgumentNotValidException")
          .returns(fieldValidationResponse)
          .addParameter(SpringTypes.MethodArgumentNotValidException, "ex")
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
            "(fieldError)->$T.builder().path(fieldError.getField()).message(fieldError.getDefaultMessage()).build()" +
            ").collect($T.toList()))",
            fieldValidationError,
            JavaTypes.Collectors
          )
          .addStatement("return responseBuilder.build()")
          .build()
      )

    Seq(makeFile(name, nameSpaces.controller, builder, staticImports: _*))
  }


  private def fieldValidationResponse = ClassName.get("org.bml.ebsco.validation.v0.models", "FieldValidationResponse")

  private def fieldValidationError = ClassName.get("org.bml.ebsco.validation.v0.models", "FieldValidationError")

  private def fieldValidationResponseBuilder = ClassName.get("org.bml.ebsco.validation.v0.models.FieldValidationResponse", "FieldValidationResponseBuilder")

  private def responseStatusBadRequest = AnnotationSpec.builder(SpringTypes.ResponseStatus).addMember("value", "$T.BAD_REQUEST", SpringTypes.HttpStatus).build()

  private def badArgExceptionHandler = AnnotationSpec.builder(SpringTypes.ExceptionHandler).addMember("value", "$T.class", SpringTypes.MethodArgumentNotValidException).build()


}
