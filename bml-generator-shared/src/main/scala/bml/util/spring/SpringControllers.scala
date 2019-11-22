package bml.util.spring

import bml.util.java.{ClassNames, JavaPojoUtil}
import bml.util.{AnotationUtil, GeneratorFSUtil, NameSpaces}
import com.squareup.javapoet.{AnnotationSpec, _}
import io.apibuilder.generator.v0.models.File
import io.apibuilder.spec.v0.models.Method.Get
import io.apibuilder.spec.v0.models.ParameterLocation.{UNDEFINED, _}
import io.apibuilder.spec.v0.models.{Operation, Parameter, ParameterLocation, Resource}
import javax.lang.model.element.Modifier.{FINAL, PRIVATE, PUBLIC}
import lib.Text

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
      .addAnnotation(ClassNames.controller)
      .addAnnotation(ClassNames.slf4j)
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
      .returns(ClassNames.responseEntityOfObject)
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
      case Query => Some(ClassNames.requestParam)
      case Header => Some(ClassNames.requestHeader)
      case Form => Some(ClassNames.requestParam)
      case Path => Some(ClassNames.pathVariable)
      case UNDEFINED(_) => {
        None
      }
    }
  }

  def operationParamToControllerParam(nameSpaces: NameSpaces, parameter: Parameter): ParameterSpec = {
    val paramName = toControllerParamName(parameter)

    val paramInType = JavaPojoUtil.dataTypeFromField(parameter.`type`, nameSpaces.model.nameSpace)

    //val javaDataType = JavaPojoUtil.dataTypeFromField(parameter.`type`, nameSpaces.model.nameSpace)
    val builder = ParameterSpec.builder(paramInType, paramName, FINAL)

    val paramAnnotation = getParamAnnotation(parameter.location)
    if (paramAnnotation.isDefined) {
      //Build anno
      val paramAnnotationBuilder = AnnotationSpec.builder(paramAnnotation.get)
        .addMember("name", "$S", parameter.name)
        .addMember("required", "$L", parameter.required.toString)
      //Add default
      if (parameter.default.isDefined) {
        paramAnnotationBuilder.addMember("defaultValue", "$S", parameter.default.get.toString)
      }
      //set anno
      builder.addAnnotation(paramAnnotationBuilder.build())
    }
    if (parameter.required) {
      if (parameter.`type` == "string") {
        builder.addAnnotation(ClassNames.notBlank)
      } else {
        builder.addAnnotation(ClassNames.notNull)
      }
    }


    if (parameter.minimum.isDefined || parameter.maximum.isDefined) {
      builder.addAnnotation(AnotationUtil.size(parameter.minimum, parameter.maximum))
    }

    builder.build()
  }

}
