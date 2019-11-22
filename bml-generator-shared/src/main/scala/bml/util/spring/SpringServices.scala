package bml.util.spring

import bml.util.java.{ClassNames, JavaPojoUtil}
import bml.util.{AnotationUtil, GeneratorFSUtil, NameSpaces}
import com.squareup.javapoet._
import io.apibuilder.generator.v0.models.File
import io.apibuilder.spec.v0.models.{Operation, Parameter, Resource}
import javax.lang.model.element.Modifier.{ABSTRACT, FINAL, PRIVATE, PUBLIC}

object SpringServices {

  def toServiceName(resource: Resource): String = JavaPojoUtil.toClassName(resource.`type`) + "Service"

  def toServiceMockName(resource: Resource): String = toServiceName(resource) + "Mock"

  def toServiceClassName(nameSpaces: NameSpaces, resource: Resource): ClassName = ClassName.get(nameSpaces.service.nameSpace, toServiceName(resource))

  def toServiceMockClassName(nameSpaces: NameSpaces, resource: Resource): ClassName = ClassName.get(nameSpaces.service.nameSpace, toServiceMockName(resource))

  def toOperationName(operation: Operation) = JavaPojoUtil.toMethodName(operation.method.toString.toLowerCase + "_" + operation.path)

  private def modelDataType(nameSpaces: NameSpaces, parameter: Parameter) = JavaPojoUtil.dataTypeFromField(parameter.`type`, nameSpaces.model.nameSpace)

  def generateService(nameSpaces: NameSpaces, resource: Resource): Seq[File] = {
    val serviceName = toServiceClassName(nameSpaces, resource)
    val serviceBuilder = TypeSpec.interfaceBuilder(serviceName).addModifiers(PUBLIC)
    //Generate Service methods from operations and add them to the Service Interface
    resource.operations.flatMap(generateServiceOperation(nameSpaces, resource, _, false))
      .map(_.build())
      .foreach(serviceBuilder.addMethod)
    //Return the generated Service interface
    Seq(GeneratorFSUtil.makeFile(serviceName.simpleName(), nameSpaces.service.path, nameSpaces.service.nameSpace, serviceBuilder))
  }

  def generateServiceMockTests(nameSpaces: NameSpaces, resource: Resource): Seq[File] = {
    val serviceName = toServiceClassName(nameSpaces, resource)
    val implName = toServiceMockClassName(nameSpaces, resource)
    val implBuilder = TypeSpec.classBuilder(implName).addModifiers(PUBLIC)
      .addAnnotation(ClassNames.springBootTest)
      .addField(
        FieldSpec.builder(serviceName, JavaPojoUtil.toParamName(serviceName.simpleName(), true), PRIVATE)
          .addAnnotation(ClassNames.mock)
          .build()
      )
    //.addSuperinterface(serviceName)


    //Generate Service methods from operations and add them to the Service Interface
    //    resource.operations.flatMap(generateServiceMockTestOperation(nameSpaces, resource, _))
    //      .map(_.addAnnotation(AnotationUtil.`override`))
    //      .map(_.build())
    //      .foreach(implBuilder.addMethod)
    //Return the generated Service interface
    Seq(GeneratorFSUtil.makeFile(implName.simpleName(), nameSpaces.service.path, nameSpaces.service.nameSpace, implBuilder))
  }

  private def generateServiceMockTestOperation(nameSpaces: NameSpaces, resource: Resource, operation: Operation): Option[MethodSpec.Builder] = {
    val builder = generateServiceOperation(nameSpaces, resource, operation, true).get
    //builder.addStatement("return null")
    Some(builder)
  }

  private def generateServiceOperation(nameSpaces: NameSpaces, resource: Resource, operation: Operation, isconcrete: Boolean): Option[MethodSpec.Builder] = {
    val methodName = toOperationName(operation)
    val methodSpec = MethodSpec.methodBuilder(methodName)
      .returns(ClassNames.responseEntity)
    if (isconcrete) {
      methodSpec.addModifiers(PUBLIC)
    } else {
      methodSpec.addModifiers(PUBLIC, ABSTRACT)
    }
    //Add Parameters
    operation.parameters.map(operationParamToServiceParam(nameSpaces, _)).foreach(methodSpec.addParameter)
    return Some(methodSpec)
  }

  private def operationParamToServiceParam(nameSpaces: NameSpaces, parameter: Parameter): ParameterSpec = {
    val paramName = JavaPojoUtil.toParamName(parameter.name, true)
    val javaDataType = modelDataType(nameSpaces, parameter)
    val builder = ParameterSpec.builder(javaDataType, paramName, FINAL)
    if (parameter.required || parameter.default.isDefined) builder.addAnnotation(AnotationUtil.notNull)
    builder.build()
  }


}
