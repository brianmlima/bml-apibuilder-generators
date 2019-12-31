package bml.util.spring

import bml.util.java.ClassNames.JavaxTypes.JavaxValidationTypes
import bml.util.java.ClassNames.{JavaTypes, SpringTypes}
import bml.util.java.{ClassNames, JavaPojoUtil}
import bml.util.{AnotationUtil, GeneratorFSUtil, NameSpaces}
import io.apibuilder.generator.v0.models.File
import io.apibuilder.spec.v0.models.Method.{Get, Post}
import io.apibuilder.spec.v0.models._
import javax.lang.model.element.Modifier
import lib.Text


class SpringControllers {

}

object SpringControllers {

  import com.squareup.javapoet._
  import io.apibuilder.spec.v0.models.ParameterLocation._
  import javax.lang.model.element.Modifier._
  import collection.JavaConverters._

  def toControllerName(resource: Resource): String = {
    JavaPojoUtil.toClassName(resource.`type` + "Controller")
  }

  def toControllerParamName(parameter: Parameter): String = {
    JavaPojoUtil.toParamName((parameter.name + "_In"), true)
  }

  def toControllerOperationName(operation: Operation): String = {
    JavaPojoUtil.toMethodName(operation.method.toString.toLowerCase + "_" + operation.path)
  }

  def controllerOperationNameFields(service: Service, resource: Resource): Seq[FieldSpec] = {
    Seq[FieldSpec](

      FieldSpec.builder(JavaTypes.String, "API_VERSION", PUBLIC, STATIC)
        .initializer("$S",
          String.format("v%s", service.version.split("\\.")(0))
        )
        .build(),

      FieldSpec.builder(JavaTypes.String, "RESOURCE_PATH", PUBLIC, STATIC)
        .initializer("$S", s"/v${service.version.split("\\.")(0)}${resource.path.get}")
        .build(),
    )
  }

  def generateController(service: Service, nameSpaces: NameSpaces, resource: Resource): Seq[File] = {
    val name = SpringControllers.toControllerName(resource)
    val builder = TypeSpec.classBuilder(name)
      .addModifiers(PUBLIC)
      .addAnnotation(SpringTypes.Controller)
      .addAnnotation(ClassNames.slf4j)
      .addFields(controllerOperationNameFields(service, resource).asJava)
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


    def toSpringPath(path: String): String = {
      path.split("/").map(
        element =>
          if (element.startsWith(":")) {
            element.replace(":", "{") + "}"
          } else {
            element
          }
      ).mkString("/")
    }


    operation.method match {


      case Get =>
        operation.parameters.map(SpringControllers.operationParamToControllerParam(nameSpaces, _)).foreach(methodSpec.addParameter)

        methodSpec
          .addAnnotation(AnotationUtil.getMappingJson(toSpringPath(s"/$version$path")))
          .addCode(
            CodeBlock.builder()
              .add("return $L.$L(\n", Text.initLowerCase(SpringServices.toServiceName(resource)), methodName)
              .add(operation.parameters.map(toControllerParamName).mkString(",\n"))
              .add(");")
              .build()
          )
      case Post =>
        methodSpec.addAnnotation(AnotationUtil.postMappingJson(toSpringPath(s"/$version$path")))
        if (operation.body.isDefined) {
          val body = operation.body.get
          val bodyClassName = JavaPojoUtil.toClassName(nameSpaces.model, body.`type`)
          methodSpec
            .addParameter(
              ParameterSpec.builder(bodyClassName, JavaPojoUtil.toFieldName(bodyClassName.simpleName()), Modifier.FINAL)
                .addAnnotation(
                  AnnotationSpec.builder(SpringTypes.RequestBody).build()
                ).build()
            )
            .addCode(
              CodeBlock.builder()
                .add("return $L.$L(\n", Text.initLowerCase(SpringServices.toServiceName(resource)), methodName)
                .add(JavaPojoUtil.toFieldName(bodyClassName.simpleName()))
                .add(");")
                .build()
            )
        }
    }

    //Add Parameters


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
