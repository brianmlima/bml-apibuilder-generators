package models.generator.java.persistence.sql.generators

import bml.util.java.ClassNames.SpringTypes
import bml.util.java.{ClassNames, JavaPojoUtil}
import com.squareup.javapoet._
import io.apibuilder.spec.v0.models.{Method, Operation, Parameter, Service}
import javax.lang.model.element.Modifier.{FINAL, PRIVATE, PUBLIC}
import lib.Text
import models.generator.java.persistence.sql.{GenUtils, ResourceData}

class ControllerGenerator {

}

object ControllerGenerator extends JavaPojoUtil {

  def generate(service: Service, resourceData: ResourceData): ResourceData = {
    val config = resourceData.config
    resourceData.controllerBuilder = Some(
      TypeSpec.classBuilder(resourceData.controllerClassName).addJavadoc(config.apiDocComments)
        //Is a public class
        .addModifiers(PUBLIC)
        .addAnnotation(ClassNames.slf4j)
        .addAnnotation(SpringTypes.RestController)
    )

    val classBuilder: TypeSpec.Builder = resourceData.controllerBuilder.get

    resourceData.resource.operations
      .foreach(operation => {
        val methodSpec = ControllerGenerator.buildMethod(service, resourceData, operation)
        classBuilder.addMethod(methodSpec)
      })

    //Add api.json documentation
    resourceData.resource.description.map(classBuilder.addJavadoc(_))

    classBuilder.addField(
      FieldSpec.builder(resourceData.serviceClassName, resourceData.serviceFieldName, PRIVATE)
        .addAnnotation(GenUtils.autowired)
        .build()
    )

    resourceData
  }

  def buildMethod(service: Service, resourceData: ResourceData, operation: Operation): MethodSpec = {

    val methodName = controlerMethodName(operation);

    val methodBuilder = MethodSpec.methodBuilder(methodName)
      .addModifiers(PUBLIC)
      .addAnnotation(makeRequestMapping(operation))
      .returns(SpringTypes.ResponseEntity)
    operation.parameters.foreach(
      param => {
        val paramSpec: ParameterSpec = ControllerGenerator.toParameterSpec(service, param, operation, resourceData)
        methodBuilder.addParameter(paramSpec)
      }
    )

    methodBuilder.addCode(CodeBlock.of(
      "$L",
      "return " + resourceData.serviceFieldName + "." + methodName + "(" +
        operation.parameters.map(param => toParamName(param.name, startingWithLowercase = true)).mkString(",")
        + ");"
    ))

    val badRequest = operation.responses.find(_.code == 400);
    if (badRequest.isDefined) {
      methodBuilder
    }

    methodBuilder.build()
  }

  def toParameterSpec(service: Service, param: Parameter, operation: Operation, resourceData: ResourceData): ParameterSpec = {
    val paramName = toParamName(param.name, startingWithLowercase = true)
    val paramType = dataTypeFromField(service, param.`type`, resourceData.config.modelsNameSpace)

    val builder = ParameterSpec.builder(paramType, paramName, FINAL)
    var paramAnnotation: AnnotationSpec.Builder = null;

    if (isPathVariable(param, operation)) {
      paramAnnotation = AnnotationSpec.builder(SpringTypes.PathVariable)
        .addMember("name", "$S", paramName)

    } else {
      paramAnnotation = AnnotationSpec.builder(SpringTypes.RequestParam)
        .addMember("name", "$S", paramName)
    }

    if (param.required) {
      paramAnnotation.addMember("required", "$L", param.required.toString)
      builder.addAnnotation(GenUtils.notNull)
    }
    if (isModelType(resourceData.config.service, `type` = param.`type`)) {
      builder.addAnnotation(GenUtils.valid)
    }

    builder.addAnnotation(paramAnnotation.build())
    builder.build()
  }

  def isPathVariable(parameter: Parameter, operation: Operation): Boolean = {
    operation.path.contains(":" + parameter.name)
  }

  def makeRequestMapping(operation: Operation): AnnotationSpec = {
    val annotationSpec = operation.method match {
      case Method.Get =>
        AnnotationSpec.builder(SpringTypes.GetMapping)
      case Method.Post =>
        AnnotationSpec.builder(SpringTypes.PostMapping)
      case Method.Put =>
        AnnotationSpec.builder(SpringTypes.PutMapping)
      case _ => AnnotationSpec.builder(SpringTypes.RequestMapping)
    }
    annotationSpec
      .addMember("path", "$S", toSpringRequestMappingPath(operation))
      .build()
  }


  def toSpringRequestMappingPath(operation: Operation): String = {
    "/" + operation.path.split("/").filter(!_.isEmpty).map(handlePathElementToSpring).mkString("/")
  }


  def controlerMethodName(operation: Operation): String = {
    operation.method.toString.toLowerCase +
      operation.path.split("/").filter(!_.isEmpty).map(handleMethodNameElement).mkString
  }

  private def handleMethodNameElement(element: String): String = {
    if (element.startsWith(":")) {
      return "By" + Text.initCap(toParamName(element.substring(1), startingWithLowercase = true))
    }
    Text.initCap(element)
  }

  private def handlePathElementToSpring(element: String): String = {
    if (element.startsWith(":")) {
      return "{" + toParamName(element.substring(1), startingWithLowercase = true) + "}"
    }
    element
  }


}