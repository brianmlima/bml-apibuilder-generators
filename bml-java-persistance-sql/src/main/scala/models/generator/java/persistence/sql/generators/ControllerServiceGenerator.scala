package models.generator.java.persistence.sql.generators

import com.squareup.javapoet.{ClassName, MethodSpec, ParameterSpec, TypeSpec}
import io.apibuilder.spec.v0.models.Operation
import javax.lang.model.element.Modifier.{ABSTRACT, PUBLIC}
import models.generator.java.persistence.sql.ResourceData
import models.generator.java.persistence.sql.generators.ControllerGenerator.controlerMethodName

class ControllerServiceGenerator {

}

object ControllerServiceGenerator {


  def generate(resourceData: ResourceData): ResourceData = {
    val config = resourceData.config

    resourceData.serviceBuilder = Some(
      TypeSpec.interfaceBuilder(resourceData.serviceClassName).addJavadoc(config.apiDocComments)
        //Is a public class
        .addModifiers(PUBLIC)
    )

    val classBuilder: TypeSpec.Builder = resourceData.serviceBuilder.get

    resourceData.resource.operations
      .foreach(operation => {
        val methodSpec = ControllerServiceGenerator.buildMethod(resourceData, operation)
        classBuilder.addMethod(methodSpec)
      })

    resourceData
  }


  def buildMethod(resourceData: ResourceData, operation: Operation): MethodSpec = {
    val methodBuilder = MethodSpec.methodBuilder(controlerMethodName(operation))
      .addModifiers(ABSTRACT, PUBLIC)
      .returns(ClassName.get("org.springframework.http", "ResponseEntity"))
    operation.parameters.foreach(
      param => {
        val paramSpec: ParameterSpec = ControllerGenerator.toParameterSpec(param, operation, resourceData)
        methodBuilder.addParameter(paramSpec)
      }
    )
    methodBuilder.build()
  }


}

