package bml.util.spring

import bml.util.java.JavaArrays
import bml.util.{GeneratorFSUtil, NameSpaces}
import com.squareup.javapoet._
import io.apibuilder.generator.v0.models.File
import javax.lang.model.element.Modifier.{FINAL, PRIVATE, PUBLIC}
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.validation.{BindingResult, FieldError}
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.{ControllerAdvice, ExceptionHandler, ResponseBody, ResponseStatus}

object MethodArgumentNotValidExceptionHandler {


  private def fieldValidationResponse = ClassName.get("org.bml.ebsco.validation.v0.models", "FieldValidationResponse")

  private def fieldValidationError = ClassName.get("org.bml.ebsco.validation.v0.models", "FieldValidationError")


  private def fieldValidationResponseBuilder = ClassName.get("org.bml.ebsco.validation.v0.models.FieldValidationResponse", "FieldValidationResponseBuilder")


  def get(nameSpaces: NameSpaces): Seq[File] = {

    val name = "MethodArgumentNotValidExceptionHandler"
    val builder = TypeSpec.classBuilder(name)
      .addAnnotation(
        AnnotationSpec.builder(classOf[Order])
          .addMember("value", "$T.HIGHEST_PRECEDENCE", classOf[Ordered])
          .build()
      )
      .addAnnotation(
        AnnotationSpec.builder(classOf[ControllerAdvice]).build()
      )
      .addMethod(
        MethodSpec.methodBuilder("methodArgumentNotValidException")
          .returns(fieldValidationResponse)
          .addParameter(classOf[MethodArgumentNotValidException], "ex")
          .addModifiers(PUBLIC)
          .addAnnotation(AnnotationSpec.builder(classOf[ResponseStatus]).addMember("value", "$T.BAD_REQUEST", classOf[HttpStatus]).build())
          .addAnnotation(AnnotationSpec.builder(classOf[ResponseBody]).build())
          .addAnnotation(AnnotationSpec.builder(classOf[ExceptionHandler]).addMember("value", "$T.class", classOf[MethodArgumentNotValidException]).build())
          .addStatement(CodeBlock.of("final $T result = ex.getBindingResult()", classOf[BindingResult]))
          .addStatement(CodeBlock.of("final $T fieldErrors = result.getFieldErrors()", JavaArrays.arrayOf(classOf[FieldError])))
          .addStatement(CodeBlock.of("return processFieldErrors(fieldErrors)"))
          .build()
      )
      .addMethod(
        MethodSpec.methodBuilder("processFieldErrors")
          .returns(fieldValidationResponse)
          .addParameter(JavaArrays.arrayOf(classOf[FieldError]), "fieldErrors", FINAL)
          .addModifiers(PRIVATE)
          .addCode("final $T responseBuilder = $T.builder()", fieldValidationResponseBuilder, fieldValidationResponse)
          .addCode(".status($T.BAD_REQUEST.value())", classOf[HttpStatus])
          .addCode(".message($S);", "validation error")
          .addCode(
            CodeBlock.builder()
              .beginControlFlow("for($T fieldError: fieldErrors)", classOf[FieldError])
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


    Seq(GeneratorFSUtil.makeFile(name, nameSpaces.controller.path, nameSpaces.controller.nameSpace, builder))
  }

}
