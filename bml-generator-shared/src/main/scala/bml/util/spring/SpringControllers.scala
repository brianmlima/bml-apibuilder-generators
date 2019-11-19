package bml.util.spring

import bml.util.java.JavaPojoUtil
import bml.util.{AnotationUtil, GeneratorFSUtil, NameSpaces}
import com.squareup.javapoet._
import io.apibuilder.generator.v0.models.File
import io.apibuilder.spec.v0.models.Method.Get
import io.apibuilder.spec.v0.models.ParameterLocation._
import io.apibuilder.spec.v0.models.{Operation, Parameter, ParameterLocation, Resource}
import javax.lang.model.element.Modifier.{FINAL, PRIVATE, PUBLIC}
import lib.Text
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.{PathVariable, RequestHeader, RequestParam}

class SpringControllers {

}

object SpringControllers {

  def toControllerName(resource: Resource): String = {
    JavaPojoUtil.toClassName(resource.`type` + "Controller")
  }

  def toControllerParamName(parameter: Parameter): String = {
    JavaPojoUtil.toParamName((parameter.name + "_In"), true)
  }

  def toControllerOperationName(operation: Operation): String = {
    JavaPojoUtil.toMethodName(operation.method.toString.toLowerCase + "_" + operation.path)
  }


  def generateController(nameSpaces: NameSpaces, resource: Resource): Seq[File] = {

    val name = SpringControllers.toControllerName(resource)
    val builder = TypeSpec.classBuilder(name)
      .addAnnotation(classOf[Controller])
      .addAnnotation(AnotationUtil.slf4j)
      .addField(
        FieldSpec.builder(
          ClassName.get(nameSpaces.service.nameSpace, SpringServices.toServiceName(resource)),
          Text.initLowerCase(SpringServices.toServiceName(resource)),
          PRIVATE
        ).addAnnotation(AnotationUtil.autowired)
          .build()
      )
    //Generate Controller methods from operations
    resource.operations.flatMap(SpringControllers.generateControllerOperation(nameSpaces, resource, _)).foreach(builder.addMethod)
    //Return the generated Service interface
    Seq(GeneratorFSUtil.makeFile(name, nameSpaces.controller.path, nameSpaces.controller.nameSpace, builder))
  }


  def generateControllerOperation(nameSpaces: NameSpaces, resource: Resource, operation: Operation): Option[MethodSpec] = {
    val methodName = toControllerOperationName(operation)
    val methodSpec = MethodSpec.methodBuilder(methodName)
      .addModifiers(PUBLIC)
      .returns(classOf[ResponseEntity[Object]])
    if (operation.method.equals(Get)) {
      methodSpec.addAnnotation(AnotationUtil.getMappingJson(operation.path))
    }
    //Add Parameters
    operation.parameters.map(SpringControllers.operationParamToControllerParam(nameSpaces, _)).foreach(methodSpec.addParameter)

    val code = CodeBlock.builder()
    code.add(
      "return $L.$L(\n",
      Text.initLowerCase(SpringServices.toServiceName(resource)),
      methodName
    )
    code.add(operation.parameters.map(toControllerParamName).mkString(",\n"))
    code.add(");")

    methodSpec.addCode(code.build())

    Some(methodSpec.build())
  }

  private def getParamAnnotation(location: ParameterLocation) = {
    location match {
      case Query => Some(classOf[RequestParam])
      case Header => Some(classOf[RequestHeader])
      case Form => Some(classOf[RequestParam])
      case Path => Some(classOf[PathVariable])
    }
  }

  def operationParamToControllerParam(nameSpaces: NameSpaces, parameter: Parameter): ParameterSpec = {
    val paramName = toControllerParamName(parameter)

    val paramInType = JavaPojoUtil.dataTypeFromField(parameter.`type`, nameSpaces.model.nameSpace)

    //val javaDataType = JavaPojoUtil.dataTypeFromField(parameter.`type`, nameSpaces.model.nameSpace)
    val builder = ParameterSpec.builder(paramInType, paramName, FINAL)

    val paramAnnotation = getParamAnnotation(parameter.location)
    if (paramAnnotation.isDefined) {
      builder.addAnnotation(
        AnnotationSpec.builder(paramAnnotation.get)
          .addMember("name", "$S", parameter.name)
          .addMember("required", "$L", parameter.required.toString)
          .build()
      )
    }
    if (parameter.required) {
      if (parameter.`type` == "string") {
        builder.addAnnotation(AnotationUtil.notBlank)
      } else {
        builder.addAnnotation(AnotationUtil.notNull)
      }
    }
    if (parameter.minimum.isDefined || parameter.maximum.isDefined) {
      builder.addAnnotation(AnotationUtil.size(parameter.minimum, parameter.maximum))
    }

    builder.build()
  }

}
