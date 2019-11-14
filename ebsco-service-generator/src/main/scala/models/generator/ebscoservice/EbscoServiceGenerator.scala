package models.generator.ebscoservice

import bml.util.GeneratorFSUtil
import bml.util.java.JavaPojoUtil
import com.squareup.javapoet.{MethodSpec, ParameterSpec, TypeSpec}
import io.apibuilder.generator.v0.models.{File, InvocationForm}
import io.apibuilder.spec.v0.models.{Operation, Parameter, Resource, Service}
import javax.lang.model.element.Modifier
import lib.generator.{CodeGenerator, GeneratorUtil}
import org.springframework.http.ResponseEntity


class EbscoServiceGenerator extends CodeGenerator with JavaPojoUtil {

  def getJavaDocFileHeader() = "WARNING: not all features (notably unions) and data types work with the java generator yet. \nplease contact brianmlima@gmail.com"

  override def invoke(form: InvocationForm): Either[Seq[String], Seq[File]] = invoke(form, addHeader = true)

  def invoke(form: InvocationForm, addHeader: Boolean = false): Either[Seq[String], Seq[File]] = Right(generateCode(form, addHeader))

  private def generateCode(form: InvocationForm, addHeader: Boolean = true): Seq[File] = {
    val header =
      if (addHeader) Some(new ApidocComments(form.service.version, form.userAgent).forClassFile)
      else None
    new Generator(form.service, header).generateSourceFiles()
  }

  class Generator(service: Service, header: Option[String]) {
    private val nameSpaces = new NameSpaces(service)
    //Resolves data types for built in types and models
    private val datatypeResolver = GeneratorUtil.datatypeResolver(service)

    //Run Generation
    def generateSourceFiles(): Seq[File] = {
      val generatedServices = generateServices(service.resources)
      generatedServices
    }

    //Generates Services from Resources
    def generateServices(resources: Seq[Resource]): Seq[File] = {
      resources.flatMap(generateService)
    }

    def generateService(resource: Resource): Seq[File] = {

      val serviceName = toClassName(resource.`type`)
      val serviceBuilder = TypeSpec.interfaceBuilder(serviceName)
      //Generate Service methods from operations and add them to the Service Interface
      resource.operations.flatMap(generateServiceOperation(resource, _)).foreach(serviceBuilder.addMethod)
      //Return the generated Service interface
      Seq(GeneratorFSUtil.makeFile(serviceName, nameSpaces.service.path, nameSpaces.service.nameSpace, serviceBuilder))
    }

    def generateServiceOperation(resource: Resource, operation: Operation): Option[MethodSpec] = {

      val methodName = toMethodName(operation.method.toString.toLowerCase + "_" + operation.path)

      val methodSpec = MethodSpec.methodBuilder(methodName)
        .addModifiers(Modifier.ABSTRACT, Modifier.PUBLIC)
        .returns(classOf[ResponseEntity[Object]])

      //Add Parameters
      operation.parameters.map(operationParamToServiceParam).foreach(methodSpec.addParameter)

      return Some(methodSpec.build())

    }

    def operationParamToServiceParam(parameter: Parameter): ParameterSpec = {

      val paramName = toParamName(parameter.name, true)
      val javaDataType = dataTypeFromField(parameter.`type`, nameSpaces.model.nameSpace)

      val builder = ParameterSpec.builder(javaDataType, paramName, Modifier.FINAL)

      builder.build()
    }


  }

}