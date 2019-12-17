package bml.util.spring

import bml.util.java.ClassNames.JavaxTypes.JavaxValidationTypes
import bml.util.java.ClassNames.SpringTypes
import bml.util.java.{ClassNames, JavaPojoUtil}
import bml.util.{AnotationUtil, GeneratorFSUtil, NameSpaces}
import io.apibuilder.generator.v0.models.File
import io.apibuilder.spec.v0.models.Method.Get
import io.apibuilder.spec.v0.models.{Operation, Parameter, ParameterLocation, Resource, Service}
import lib.Text

class SpringControllers {

}

object SpringControllers {

  import com.squareup.javapoet._
  import io.apibuilder.spec.v0.models.ParameterLocation._
  import javax.lang.model.element.Modifier._

  def toControllerName(resource: Resource): String = {
    JavaPojoUtil.toClassName(resource.`type` + "Controller")
  }

  def toControllerParamName(parameter: Parameter): String = {
    JavaPojoUtil.toParamName((parameter.name + "_In"), true)
  }

  def toControllerOperationName(operation: Operation): String = {
    JavaPojoUtil.toMethodName(operation.method.toString.toLowerCase + "_" + operation.path)
  }


  def generateController(service: Service, nameSpaces: NameSpaces, resource: Resource): Seq[File] = {
    val name = SpringControllers.toControllerName(resource)
    val builder = TypeSpec.classBuilder(name)
      .addModifiers(PUBLIC)
      .addAnnotation(SpringTypes.Controller)
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
    resource.operations.flatMap(SpringControllers.generateControllerOperation(service, nameSpaces, resource, _)).foreach(builder.addMethod)
    //Return the generated Service interface
    Seq(GeneratorFSUtil.makeFile(name, nameSpaces.controller.path, nameSpaces.controller.nameSpace, builder))
  }


  def generateControllerOperation(service: Service, nameSpaces: NameSpaces, resource: Resource, operation: Operation): Option[MethodSpec] = {
    val methodName = toControllerOperationName(operation)
    val methodSpec = MethodSpec.methodBuilder(methodName)
      .addModifiers(PUBLIC)
      .returns(SpringTypes.ResponseEntityOfObject)


    val version = nameSpaces.base.nameSpace.split("\\.").last
    val path = operation.path


    if (operation.method.equals(Get)) {
      methodSpec.addAnnotation(AnotationUtil.getMappingJson(s"/$version$path"))
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
      case Query => Some(SpringTypes.RequestParam)
      case Header => Some(SpringTypes.RequestHeader)
      case Form => Some(SpringTypes.RequestParam)
      case Path => Some(SpringTypes.PathVariable)
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
        builder.addAnnotation(JavaxValidationTypes.NotBlank)
      } else {
        builder.addAnnotation(JavaxValidationTypes.NotNull)
      }
    }


    if (parameter.minimum.isDefined || parameter.maximum.isDefined) {
      builder.addAnnotation(AnotationUtil.size(parameter.minimum, parameter.maximum))
    }

    builder.build()
  }

}
