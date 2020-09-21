package bml.util.spring

import akka.http.scaladsl.settings.PoolImplementation.New
import bml.util.java.ClassNames.JavaxTypes.JavaxValidationTypes
import bml.util.java.ClassNames.{JavaTypes, SpringTypes}
import bml.util.java.{ClassNames, JavaDataType, JavaDataTypes, JavaPojoUtil}
import bml.util.{AnotationUtil, GeneratorFSUtil, NameSpaces}
import io.apibuilder.generator.v0.models.File
import io.apibuilder.spec.v0.models.Method.{Get, Post, Put}
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

  def toControllerParamName(name: String): String = {
    JavaPojoUtil.toParamName((name + "_In"), true)
  }

  def toControllerOperationName(operation: Operation): String = {
    //    JavaPojoUtil.toMethodName(operation.method.toString.toLowerCase + "_" + operation.path)
    JavaPojoUtil.toMethodName(toOperationClientClassName(operation))
  }

  def toOperationClientClassName(operation: Operation): String = {
    val method = operation.method.toString.toLowerCase()
    val paramterAnds = (
      operation.path.split("/").filter(_.contains(":"))
        .map(v => JavaPojoUtil.toClassName(v.drop(1))) ++
        operation.parameters.filter(_.location != ParameterLocation.Path)
          .map(v => JavaPojoUtil.toClassName(v.name))
      ).mkString("And")
    var name =
      if (operation.method == Get) {
        val ok = operation.responses.find(_.code.productElement(0) == 200)
        if (ok.isDefined) {
          JavaPojoUtil.toClassName(ok.get.`type`).capitalize +
            (
              if (JavaPojoUtil.isParameterArray(ok.get.`type`)) {
                "s"
              } else {
                ""
              }
              )
        } else {
          "OkNotDefined"
        }
      } else if (operation.method == Post || operation.method == Put) {
        if (operation.body.isDefined) {
          JavaPojoUtil.toClassName(operation.body.get.`type`).capitalize
        } else {
          "BodyNotDefined"
        }
      } else {
        "NotHandled"
      }

    JavaPojoUtil.toMethodName(method + name +
      (
        if (paramterAnds.isEmpty()) {
          ""
        } else {
          "By"
        }
        ) + paramterAnds
    )
  }


  def undefinedResponseModelExcpetionClassName(nameSpaces: NameSpaces) =
    ClassName.get(nameSpaces.controller.nameSpace, JavaPojoUtil.toClassName("UndefinedResponseModelExcpetion"))

  private def undefinedResponseModelExcpetion(nameSpaces: NameSpaces): TypeSpec = {
    TypeSpec.classBuilder(undefinedResponseModelExcpetionClassName(nameSpaces))
      .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
      .addSuperinterface(classOf[Exception])
      .build()
  }


  def controllerOperationNameFields(service: Service, resource: Resource): Seq[FieldSpec] = {
    Seq[FieldSpec](

      FieldSpec.builder(JavaTypes.String, "API_VERSION", PUBLIC, STATIC, FINAL)
        .initializer("$S",
          String.format("v%s", service.version.split("\\.")(0))
        )
        .build(),

      FieldSpec.builder(JavaTypes.String, "RESOURCE_PATH", PUBLIC, STATIC, FINAL)
        .initializer("$S", s"/v${service.version.split("\\.")(0)}${resource.path.get}")
        .build(),
    )
  }

  def generateController(service: Service, nameSpaces: NameSpaces, resource: Resource): Seq[File] = {
    val name = SpringControllers.toControllerName(resource)

    val serviceClassName = ClassName.get(nameSpaces.service.nameSpace, SpringServices.toServiceName(resource))
    val serviceFieldName = Text.initLowerCase(SpringServices.toServiceName(resource))

    val builder = TypeSpec.classBuilder(name)
      .addModifiers(PUBLIC)
      .addAnnotation(SpringTypes.Controller)
      .addAnnotation(ClassNames.slf4j)
      .addFields(controllerOperationNameFields(service, resource).asJava)
      .addField(
        FieldSpec.builder(serviceClassName, serviceFieldName, PRIVATE)
          //.addAnnotation(AnotationUtil.autowired)
          .build()
      )
      .addMethod(
        MethodSpec.constructorBuilder()
          .addModifiers(PUBLIC)
          .addParameter(
            ParameterSpec.builder(serviceClassName, serviceFieldName, FINAL)
              .addAnnotation(AnotationUtil.autowired)
              .build()
          )
          .addStatement("this.$L = $L", serviceFieldName, serviceFieldName)
          .build()
      )


    //Generate Controller methods from operations
    resource.operations.flatMap(SpringControllers.generateControllerOperation(service, nameSpaces, resource, _)).foreach(builder.addMethod)






    //Return the generated Service interface
    Seq(GeneratorFSUtil.makeFile(name, nameSpaces.controller.path, nameSpaces.controller.nameSpace, builder))

  }


  /**
   *
   * @param service    the root service we are working on.
   * @param nameSpaces the current namespace object we are working with.
   * @param resource   the current resource we are working on.
   * @param operation  the Operation we are generating.
   * @return
   */
  def generateControllerOperation(service: Service, nameSpaces: NameSpaces, resource: Resource, operation: Operation): Option[MethodSpec] = {
    val methodName = toControllerOperationName(operation)
    val methodSpec = MethodSpec.methodBuilder(methodName)
      .addModifiers(PUBLIC)
      .returns(SpringTypes.ResponseEntity)

    val version = nameSpaces.base.nameSpace.split("\\.").last
    val path = operation.path


    def operationDescription(): String = {
      var out = Seq[String]()
      if (operation.description.isDefined) {
        out = out ++ Seq(operation.description.get)
      }
      out.mkString("  *", "\n  *", "")
    }

    methodSpec.addComment(operationDescription())


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


    //    def buildController

    val bodyTypes = operation.body.map(_.`type`)

    operation.parameters
      .filter(
        param =>
          !bodyTypes.contains(param.`type`)
      )
      .map(SpringControllers.operationParamToControllerParam(service, nameSpaces, _)).foreach(methodSpec.addParameter)

    operation.method match {
      case Get =>
        methodSpec
          .addAnnotation(AnotationUtil.getMappingJson(toSpringPath(s"/$version$path")))
      case Post =>
        methodSpec.addAnnotation(AnotationUtil.postMappingJson(toSpringPath(s"/$version$path")))
        // add the response body to the controller method params
        if (operation.body.isDefined) {
          val body = operation.body.get
          val bodyDataType = JavaPojoUtil.dataTypeFromField(service,body.`type`,nameSpaces.model)
          val bodyClassName = JavaPojoUtil.toClassName(nameSpaces.model, body.`type`)
          val paramName = toControllerParamName(bodyClassName.simpleName())
          methodSpec
            .addParameter(
              ParameterSpec.builder(bodyDataType, paramName, Modifier.FINAL)
                .addAnnotation(
                  AnnotationSpec.builder(SpringTypes.RequestBody).build()
                ).build()
            )
        }
      case Put =>
        methodSpec.addAnnotation(AnotationUtil.putMappingJson(toSpringPath(s"/$version$path")))
        // add the response body to the controller method params
        if (operation.body.isDefined) {
          val body = operation.body.get
          val bodyClassName = JavaPojoUtil.toClassName(nameSpaces.model, body.`type`)
          val paramName = toControllerParamName(bodyClassName.simpleName())
          methodSpec
            .addParameter(
              ParameterSpec.builder(bodyClassName, paramName, Modifier.FINAL)
                .addAnnotation(
                  AnnotationSpec.builder(SpringTypes.RequestBody).build()
                ).build()
            )
        }
    }

    val exceptionClassName = ApiImplementationException.getClassName(nameSpaces);


    def buildControllerRespondCodeBlock(response: Response, nameSpaces: NameSpaces): CodeBlock = {
      val paramName = JavaPojoUtil.toParamName(response.`type`, true) + SpringServices.responseCodeToString(response.code)
      val responseClassName = JavaPojoUtil.toClassName(response.`type`)

      val responseCode: ResponseCode = response.code


      //      val exceptionClassName = ApiImplementationException.getClassName(nameSpaces);

      val codeBlock = CodeBlock.builder()

        .beginControlFlow("if(responseModel.$L() !=null)", paramName)
        .addStatement("final int code = responseModel.$L().getStatusCode().value()", paramName)
        .beginControlFlow("if(code!=$L)", responseCode.productElement(0).toString)
        .add("throw new $T( String.format(\"$L ResponseEntity<$L> code must be $L found %s\",code));", exceptionClassName, paramName, responseClassName, responseCode.productElement(0).toString)

        .endControlFlow()
        //.addStatement("if(responseModel.$L().getStatusCode().value()!=$L){/** Throw runtime exception*/}", paramName, responseCode.productElement(0).toString)
        .addStatement("return responseModel.$L()", paramName)
      codeBlock
        .endControlFlow()
        .build()

    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Call the service and assign the return value to a variable.
    var codeBlock = CodeBlock.builder()
      .add(
        "final $T responseModel = $L.$L(\n",
        SpringServices.toResponseSubTypeCLassName(nameSpaces, operation),
        Text.initLowerCase(SpringServices.toServiceName(resource)),
        methodName
      )

    operation.method match {
      case Get =>
        codeBlock.add(operation.parameters.map(toControllerParamName).mkString(",\n"))
          .add(");").build()
      case Post =>
        val body = operation.body
        var params = operation.parameters.map(toControllerParamName)
        if (body.isDefined) {
          params = params ++ Seq(toControllerParamName(body.get.`type`))
        }
        codeBlock.add(params.mkString(","))
        codeBlock.add(");")
      case Put =>
        val body = operation.body
        var params = operation.parameters.map(toControllerParamName)
        if (body.isDefined) {
          params = params ++ Seq(toControllerParamName(body.get.`type`))
        }
        codeBlock.add(params.mkString(","))
        codeBlock.add(");")

    }
    methodSpec.addCode(codeBlock.build())
    methodSpec.addCode(
      CodeBlock.join(operation.responses.map(buildControllerRespondCodeBlock(_, nameSpaces)).asJava, "\n")
    )
    methodSpec.addCode(
      CodeBlock.of("throw new $T();", exceptionClassName)
    )
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


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

  def operationParamToControllerParam(service: Service, nameSpaces: NameSpaces, parameter: Parameter): ParameterSpec = {
    val paramName = toControllerParamName(parameter)

    val paramInType = JavaPojoUtil.dataTypeFromField(service, parameter.`type`, nameSpaces.model.nameSpace)

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
