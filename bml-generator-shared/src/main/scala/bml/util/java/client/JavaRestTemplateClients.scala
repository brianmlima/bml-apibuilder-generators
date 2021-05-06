package bml.util.java.client

import java.net.URI
import java.nio.charset.StandardCharsets

import bml.util.AnotationUtil.LombokAnno
import bml.util.NameSpaces
import bml.util.java.ClassNames.{JacksonTypes, JavaTypes, LombokTypes, SpringTypes}
import bml.util.java.{ClassNames, JavaPojoUtil}
import bml.util.spring.SpringServices
import bml.util.spring.SpringServices.responseCodeToString
import com.squareup.javapoet._
import io.apibuilder.generator.v0.models.File
import io.apibuilder.spec.v0.models.Method.{Get, Post, Put}
import io.apibuilder.spec.v0.models._
import javax.validation.constraints.NotNull
import lombok.Getter

object JavaRestTemplateClients {

  import bml.util.GeneratorFSUtil.makeFile
  import javax.lang.model.element.Modifier._

  import collection.JavaConverters._


  def toClientClassName(service: Service, nameSpaces: NameSpaces): ClassName = {
    ClassName.get(nameSpaces.client.nameSpace, JavaPojoUtil.toClassName(service.name) + "Client")
  }

  val restTemplateFieldName = "restTemplate"
  val baseUriFieldName = "baseUri"
  val objectMapperFieldName = "objectMapper"

  //  val hostFieldName = "host"
  //  val portFieldName = "port"

  val configFieldName = "config"


  def generateClient(service: Service, nameSpaces: NameSpaces): Seq[File] = {
    val clientClassName = toClientClassName(service, nameSpaces);
    val configClassName = ClassName.get(clientClassName.packageName() + "." + clientClassName.simpleName(), "Config")

    val configType = TypeSpec.classBuilder(configClassName)
      .addModifiers(PUBLIC, STATIC)
      .addAnnotation(LombokTypes.Builder)
      .addAnnotation(LombokAnno.AccessorFluent)
      .addFields(
        Seq(
          FieldSpec.builder(SpringTypes.RestTemplate, restTemplateFieldName, PRIVATE, FINAL)
            .addAnnotation(LombokTypes.Getter)
            .addAnnotation(classOf[NotNull])
            .build(),
          FieldSpec.builder(JavaTypes.URI, baseUriFieldName, PRIVATE, FINAL)
            .addAnnotation(LombokTypes.Getter)
            .addAnnotation(classOf[NotNull])
            .build(),
          FieldSpec.builder(JacksonTypes.ObjectMapper, objectMapperFieldName, PRIVATE, FINAL)
            .addAnnotation(LombokTypes.Getter)
            .addAnnotation(classOf[NotNull])
            .build()
        ).asJava
      ).build()


    val responseModelType = TypeSpec.classBuilder("ResponseModel").addTypeVariable(TypeVariableName.get("T"))
      .addModifiers(PUBLIC, STATIC)
      .addAnnotation(LombokTypes.Builder)
      .addAnnotation(LombokAnno.AccessorFluent)
      .addField(
        FieldSpec.builder(SpringTypes.HttpStatus, "httpStatus", PRIVATE, FINAL)
          .addAnnotation(classOf[Getter])
          .addAnnotation(classOf[NotNull])
          .build()
      )
      .addField(
        FieldSpec.builder(SpringTypes.HttpHeaders, "headers", PRIVATE, FINAL)
          .addAnnotation(classOf[Getter])
          .addAnnotation(classOf[NotNull])
          .build()
      )
      .addField(
        FieldSpec.builder(TypeVariableName.get("T"), "body", PRIVATE, FINAL)
          .addAnnotation(classOf[Getter])
          .addAnnotation(classOf[NotNull])
          .build()
      )
      .build()


    val stdClientFields = Seq(
      FieldSpec.builder(configClassName, configFieldName, PRIVATE)
        .addAnnotation(LombokTypes.Getter)
        .build()
    )

    val clientSpec = TypeSpec.classBuilder(clientClassName)
      .addModifiers(PUBLIC)
      .addAnnotations(
        Seq(
          LombokAnno.AccessorFluent,
          LombokAnno.Slf4j
        ).asJava
      )
      .addFields(stdClientFields.asJava)
      .addMethod(
        MethodSpec.constructorBuilder()
          .addModifiers(PUBLIC)
          .addParameter(
            ParameterSpec.builder(configClassName, configFieldName, FINAL).build()
          ).addCode("this.$L=$L;", configFieldName, configFieldName)
          .addCode(
            CodeBlock.join(
              service.resources.map(
                resource => {
                  val className = ClassName.get("", JavaPojoUtil.toClassName(resource.`type`) + "Client")
                  val fieldName = JavaPojoUtil.toFieldName(resource.`type`)
                  CodeBlock.of("this.$L= new $T($L);", fieldName, className, configFieldName)
                }
              ).asJava, ""
            )
          )
          .build()
      )


      .addFields(
        service.resources.map(
          resource => {
            val className = ClassName.get("", JavaPojoUtil.toClassName(resource.`type`) + "Client")
            FieldSpec.builder(className, JavaPojoUtil.toFieldName(resource.`type`), PRIVATE)
              .addAnnotation(LombokTypes.Getter)
              .addJavadoc(resource.description.getOrElse(""))
              .build()
          }
        ).asJava

      )
      .addType(configType)
      .addType(responseModelType)

      .addTypes(
        service.resources.map(
          resource =>
            TypeSpec.classBuilder(JavaPojoUtil.toClassName(resource.`type`) + "Client")
              .addAnnotations(
                Seq(
                  LombokAnno.AccessorFluent,
                  LombokAnno.Slf4j
                ).asJava
              )
              .addModifiers(PUBLIC, STATIC)
              .addFields(stdClientFields.asJava)
              .addFields(
                resource.operations.map(
                  operation => {
                    val className = ClassName.bestGuess(JavaPojoUtil.toClassName(toOperationClientClassName(operation)))
                    val fieldName = JavaPojoUtil.toParamName(toOperationClientClassName(operation), true)
                    FieldSpec.builder(className, fieldName, PRIVATE, FINAL)
                      .addAnnotation(LombokTypes.Getter)

                      .build()
                  }
                ).asJava
              )
              .addMethod(
                MethodSpec.constructorBuilder()
                  .addModifiers(PUBLIC)
                  .addParameter(
                    ParameterSpec.builder(configClassName, configFieldName, FINAL).build()
                  ).addCode("this.$L=$L;", configFieldName, configFieldName)
                  .addCode(
                    CodeBlock.join(
                      resource.operations.map(
                        operation => {
                          val className = ClassName.bestGuess(JavaPojoUtil.toClassName(toOperationClientClassName(operation)))
                          val fieldName = JavaPojoUtil.toParamName(toOperationClientClassName(operation), true)
                          CodeBlock.of("this.$L= new $T($L);", fieldName, className, configFieldName)
                        }
                      ).asJava,
                      ""
                    )
                  )
                  .build()
              )

              //.addMethod(restTemplateConstructor)
              .addTypes(
                resource.operations.map(
                  operation => {
                    //                    TypeSpec.classBuilder(JavaPojoUtil.toClassName(SpringControllers.toControllerOperationName(operation) + "Client"))
                    TypeSpec.classBuilder(JavaPojoUtil.toClassName(toOperationClientClassName(operation)))


                      .addModifiers(PUBLIC, STATIC)
                      .addFields(stdClientFields.asJava)
                      .addMethod(
                        MethodSpec.constructorBuilder()
                          .addModifiers(PUBLIC)
                          .addParameter(
                            ParameterSpec.builder(configClassName, configFieldName, FINAL).build()
                          ).addCode("this.$L=$L;", configFieldName, configFieldName)
                          .build()
                      )
                      .addType(generateServiceOperationResponseContainer(service, resource, operation, nameSpaces))
                      .addMethod(generateAccessAsync(service, resource, operation, nameSpaces))
                      .addMethod(generateStaticAccessAsync(service, resource, operation, nameSpaces))
                      .build()
                  }
                ).asJava


              )
              .build()
        ).asJava
      )
      .addMethods(readMethods.asJava)

    Seq(makeFile(clientClassName.simpleName(), nameSpaces.client, clientSpec))
  }


  def readMethods(): Seq[MethodSpec] = {

    val t = TypeVariableName.get("T")

    val contentName = "content"
    val className = "clazz"
    Seq(
      MethodSpec.methodBuilder("read").addModifiers(PRIVATE, STATIC, FINAL)
        .addTypeVariable(t)
        .addParameter(ParameterSpec.builder(JacksonTypes.ObjectMapper, objectMapperFieldName, FINAL).build())
        .addParameter(ParameterSpec.builder(JavaTypes.String, contentName, FINAL).build())
        .addParameter(
          ParameterSpec.builder(
            ParameterizedTypeName.get(JavaTypes.`Class`, t),
            className,
            FINAL
          ).build()
        )
        .returns(ParameterizedTypeName.get(JavaTypes.Optional, t))
        .addCode(
          CodeBlock.builder()
            .beginControlFlow("try")
            .addStatement("return $T.of($L.readValue($L, $L))", JavaTypes.Optional, objectMapperFieldName, contentName, className)

            .nextControlFlow("catch ($T e)", JavaTypes.IOException)
            .addStatement("e.printStackTrace()")
            .addStatement("return $T.empty()", JavaTypes.Optional)
            .endControlFlow()
            .build()
        )
        .build()

      ,
      MethodSpec.methodBuilder("read").addModifiers(PRIVATE, STATIC, FINAL)
        .addTypeVariable(t)
        .addParameter(ParameterSpec.builder(JacksonTypes.ObjectMapper, objectMapperFieldName, FINAL).build())
        .addParameter(ParameterSpec.builder(JavaTypes.String, contentName, FINAL).build())
        .addParameter(
          ParameterSpec.builder(
            ParameterizedTypeName.get(JacksonTypes.TypeReference, t),
            className,
            FINAL
          ).build()
        )
        .returns(ParameterizedTypeName.get(JavaTypes.Optional, t))
        .addCode(
          CodeBlock.builder()
            .beginControlFlow("try")
            .addStatement("return $T.of($L.readValue($L, $L))", JavaTypes.Optional, objectMapperFieldName, contentName, className)

            .nextControlFlow("catch ($T e)", JavaTypes.IOException)
            .addStatement("e.printStackTrace()")
            .addStatement("return $T.empty()", JavaTypes.Optional)
            .endControlFlow()
            .build()
        )
        .build()
    )
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

  def generateAccessAsync(service: Service, resource: Resource, operation: Operation, nameSpaces: NameSpaces): MethodSpec = {

    val methodName = operation.method.toString.toLowerCase + "Async";

    val returnType = ParameterizedTypeName.get(
      JavaTypes.Optional,
      ParameterizedTypeName.get(
        ClassName.get("", "ResponseModel"),
        ClassName.get("", "BodyModel")
      )
    )

    val paramsMap = operation.parameters.map(
      parameter => {
        val paramName = JavaPojoUtil.toParamName(parameter, true)
        val paramType = JavaPojoUtil.dataTypeFromField(service, parameter.`type`, nameSpaces.model)
        (paramName, paramType)
      }
    ) ++ operation.body.map(
      body => {
        val paramName = JavaPojoUtil.toParamName(body.`type`, true)
        val paramType = JavaPojoUtil.dataTypeFromField(service, body.`type`, nameSpaces.model)
        (paramName, paramType)
      }
    )


    val methodParams = paramsMap.map((k) => ParameterSpec.builder(k._2, k._1, FINAL).build())

    val callParams = (Seq(configFieldName) ++ paramsMap.map(_._1)).mkString(",")

    val call = CodeBlock.builder()
      .addStatement("return $L($L)", methodName, callParams)
      .build()

    val methodSpec = MethodSpec.methodBuilder(methodName)
      .addModifiers(PUBLIC)
      .returns(returnType)
      .addException(JavaTypes.URISyntaxException)
      .addParameters(methodParams.asJava)
      .addCode(call)


    if (!methodParams.isEmpty) {
      methodSpec.addException(JavaTypes.UnsupportedEncodingException)
    }


    methodSpec.build()

  }

  def generateStaticAccessAsync(service: Service, resource: Resource, operation: Operation, nameSpaces: NameSpaces): MethodSpec = {


    val configClass = ClassName.get(
      nameSpaces.client.nameSpace + "." + toClientClassName(service, nameSpaces).simpleName(),
      "Config"
    )


    val returnType = ParameterizedTypeName.get(
      JavaTypes.Optional,
      ParameterizedTypeName.get(
        ClassName.get("", "ResponseModel"),
        ClassName.get("", "BodyModel")
      )
    )

    val uriBlock =
      if (operation.path.contains(":")) {
        val path = "/v" + service.version.split("\\.").head + operation.path.split("/").map(
          part => if (part.startsWith(":")) {
            "%s"
          } else {
            part
          }
        ).mkString("/")
        val params = operation.path.split("/").filter(_.startsWith(":"))
          .map(
            param =>
              JavaPojoUtil.toParamName(param.drop(1) + "Encoded", true)
          ).mkString(",")

        val encoded = operation.path.split("/").seq
          .filter(_.startsWith(":"))
          .map(
            param =>
              CodeBlock.builder()
                .addStatement("final $T $L = $T.encode($L.toString(),$S)",
                  JavaTypes.String,
                  JavaPojoUtil.toParamName(param.drop(1) + "Encoded", true),
                  JavaTypes.URLEncoder,
                  JavaPojoUtil.toParamName(param.drop(1), true),
                  "UTF-8"
                ).build()
          ).asJava


        CodeBlock.builder()
          .add(JavaPojoUtil.textToComment("Encode url path paramters"))
          .add(CodeBlock.join(encoded, ""))
          .addStatement("$T path = $T.format($S,$L)", LombokTypes.`val`, JavaTypes.String, path, params)
          .addStatement("$T uri = new $T($L.baseUri().toString() + path)", LombokTypes.`val`, classOf[URI], configFieldName)
          .build()
      }

      else {
        CodeBlock.builder().addStatement("$T path = $S", LombokTypes.`val`, "/v" + service.version.split("\\.").head + operation.path)
          .addStatement("$T uri = new $T($L.baseUri().toString() + path)", LombokTypes.`val`, classOf[URI], configFieldName)
          .build();
      }

    val params = operation.parameters.map(
      parameter => {
        val paramName = JavaPojoUtil.toParamName(parameter, true)
        val paramType = JavaPojoUtil.dataTypeFromField(service, parameter.`type`, nameSpaces.model)
        ParameterSpec.builder(paramType, paramName, FINAL).build()
      }
    ) ++ operation.body.map(
      body => {
        val paramName = JavaPojoUtil.toParamName(body.`type`, true)
        val paramType = JavaPojoUtil.dataTypeFromField(service, body.`type`, nameSpaces.model)
        ParameterSpec.builder(paramType, paramName, FINAL).build()
      }
    )

    val responseModelBuilderName = "responseModelBuilder";
    val bodyBuilderName = "bodyBuilder";
    val exchangeName = "exchange"
    val restTemplateName = "config.restTemplate"


    val restTemplateBlock = CodeBlock.builder()
      .add(
        "$T requestEntity = $T.$L(uri).accept($T.APPLICATION_JSON)",
        LombokTypes.`val`,
        SpringTypes.RequestEntity,
        operation.method.toString.toLowerCase,
        SpringTypes.MediaType
      )
      .add(".acceptCharset($T.UTF_8)", JavaTypes.StandardCharsets)

    if (operation.method == Post) {
      restTemplateBlock.addStatement(".contentType($T.APPLICATION_JSON).body($L)", SpringTypes.MediaType, JavaPojoUtil.toParamName(operation.body.get.`type`, true))

    } else {
      restTemplateBlock.addStatement(".build()")
    }


    restTemplateBlock
      .addStatement("$T $L = ResponseModel.<BodyModel>builder()", LombokTypes.`val`, responseModelBuilderName)
      .addStatement("$T $L  = BodyModel.builder()", LombokTypes.`val`, bodyBuilderName)

    restTemplateBlock.beginControlFlow("try")
      .addStatement("val exchange = config.restTemplate.exchange(uri, $T.$L, requestEntity, String.class)", SpringTypes.HttpMethod, operation.method)
      .addStatement("responseModelBuilder.headers(exchange.getHeaders()).httpStatus(exchange.getStatusCode())")
      .beginControlFlow("switch (exchange.getStatusCode().value())")
    operation.responses.filter(v => !SpringServices.is4XX(v.code)).foreach(
      response => {
        val code = responseCodeToString(response.code)
        val containerAccessorName = responseToContainerName(response)
        val responseType = JavaPojoUtil.dataTypeFromField(service, response.`type`, nameSpaces.model)
        restTemplateBlock.add("case $L :", code)
        if (response.`type` == "unit") {
          restTemplateBlock.addStatement("$L.$L($T.of(true))", bodyBuilderName, containerAccessorName, JavaTypes.Optional)
        } else if (JavaPojoUtil.isModelType(service, response.`type`) || JavaPojoUtil.isModelNameWithPackage(response.`type`)) {

          if (
            JavaPojoUtil.islistOfModelNameWithPackage(response.`type`)
          ) {
            restTemplateBlock
              .add("$L.$L(", bodyBuilderName, containerAccessorName)
              .add("read(config.objectMapper(),$L.getBody(), new $T<$T>(){})", exchangeName, JacksonTypes.TypeReference, responseType)
              .addStatement(")")
          } else {

            restTemplateBlock
              .add("$L.$L(", bodyBuilderName, containerAccessorName)
              .add("read(config.objectMapper(), $L.getBody(), $T.class)", exchangeName, responseType)
              .addStatement(")")
              .addStatement("break")
          }
        } else if (
          JavaPojoUtil.isListOfModeslType(service, response.`type`) ||
            JavaPojoUtil.isListOfEnumlType(service, response.`type`) ||
            JavaPojoUtil.islistOfModelNameWithPackage(response.`type`)
        ) {
          restTemplateBlock
            .add("$L.$L(", bodyBuilderName, containerAccessorName)
            .add("read(config.objectMapper(),$L.getBody(), new $T<$T>(){})", exchangeName, JacksonTypes.TypeReference, responseType)
            .addStatement(")")
        }
      }
    )

    //        case 200:
    //        bodyBuilder.record200(read(config.objectMapper(), exchange.getBody(), Record.class));
    //        break;

    restTemplateBlock
      .add("default:")
      .addStatement("String body = exchange.getBody()")
      .beginControlFlow("if (body != null)")
      .addStatement("bodyBuilder.unexpected(Optional.of(exchange.getBody()))")
      .endControlFlow()
      .beginControlFlow("else")
      .addStatement("bodyBuilder.unexpected(Optional.of(\"\"))")
      .endControlFlow()
      .addStatement("break")
      .endControlFlow()
      .endControlFlow()
      .beginControlFlow("catch ($T e)", SpringTypes.HttpClientErrorException)
      .addStatement("responseModelBuilder.headers(e.getResponseHeaders()).httpStatus(e.getStatusCode())")
      .beginControlFlow("switch (e.getRawStatusCode())")

    operation.responses.filter(v => SpringServices.is4XX(v.code)).foreach(
      response => {
        val code = responseCodeToString(response.code)
        val containerAccessorName = responseToContainerName(response)
        val responseType = JavaPojoUtil.dataTypeFromField(service, response.`type`, nameSpaces.model)
        restTemplateBlock.add("case $L :", code)
        if (response.`type` == "unit") {
          restTemplateBlock.addStatement("$L.$L($T.of(true))", bodyBuilderName, containerAccessorName, JavaTypes.Optional)
        } else if (JavaPojoUtil.isModelType(service, response.`type`) || JavaPojoUtil.isModelNameWithPackage(response.`type`)) {


          if (
            JavaPojoUtil.islistOfModelNameWithPackage(response.`type`)
          ) {
            restTemplateBlock
              .add("$L.$L(", bodyBuilderName, containerAccessorName)
              .add("read(config.objectMapper(),$L.getResponseBodyAsString(), new $T<$T>(){})", "e", JacksonTypes.TypeReference, responseType)
              .addStatement(")")
          } else {
            restTemplateBlock
              .add("$L.$L(", bodyBuilderName, containerAccessorName)
              .add("read(config.objectMapper(), $L.getResponseBodyAsString(), $T.class)", "e", responseType)
              .addStatement(")")
              .addStatement("break")
          }
        } else if (
          JavaPojoUtil.isListOfModeslType(service, response.`type`) ||
            JavaPojoUtil.isListOfEnumlType(service, response.`type`) ||
            JavaPojoUtil.islistOfModelNameWithPackage(response.`type`)
        ) {
          restTemplateBlock
            .add("$L.$L(", bodyBuilderName, containerAccessorName)
            .add("read(config.objectMapper(),$L.getResponseBodyAsString(), new $T<$T>(){})", "e", JacksonTypes.TypeReference, responseType)
            .addStatement(")")
        }
      }
    )


    //        case 400:
    //        bodyBuilder.fieldValidationResponse400(
    //          read(config.objectMapper(), e.getResponseBodyAsString(), FieldValidationResponse.class));
    //        break;
    //        case 401:
    //          bodyBuilder.unit401(Optional.of(true));
    //        case 404:
    //          bodyBuilder.unit404(Optional.of(true));
    restTemplateBlock.add("default:")
      .addStatement("String body = e.getResponseBodyAsString()")
      .beginControlFlow("if (body != null)")
      .addStatement("bodyBuilder.unexpected(Optional.of(body))")
      .endControlFlow()
      .beginControlFlow("else")
      .addStatement("bodyBuilder.unexpected(Optional.of(\"\"))")
      .endControlFlow()
      .addStatement("break")
      .endControlFlow()
      .endControlFlow()

    restTemplateBlock.beginControlFlow("catch ($T e)", SpringTypes.HttpServerErrorException)
      .addStatement("responseModelBuilder.headers(e.getResponseHeaders()).httpStatus(e.getStatusCode())")
      .addStatement("String body = e.getResponseBodyAsString()")
      .beginControlFlow("if (body != null)")
      .addStatement("bodyBuilder.unexpected(Optional.of(body))")
      .endControlFlow()
      .beginControlFlow("else")
      .addStatement("bodyBuilder.unexpected(Optional.of(\"\"))")

      .endControlFlow()
      .endControlFlow()
    //      .addStatement("return Optional.of(responseModelBuilder.body(bodyBuilder.build()).build())")

    restTemplateBlock.addStatement("return $T.of($L.body($L.build()).build())", JavaTypes.Optional, responseModelBuilderName, bodyBuilderName)











    //    restTemplateBlock.add("config.restTemplate.setErrorHandler(")
    //      .beginControlFlow("new $T() ", SpringTypes.DefaultResponseErrorHandler)
    //      .add("@$T", JavaTypes.Override)
    //      .beginControlFlow(" protected boolean hasError(final $T statusCode)", SpringTypes.HttpStatus)
    //      .addStatement("int code = statusCode.value()")
    //      ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //      // Fix me I need to be dynamic
    //      .addStatement("return (code != 200 && code != 400 && code != 401 && code != 404)")
    //      .endControlFlow()
    //      .endControlFlow()
    //      .addStatement(")")
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    //    restTemplateBlock
    //      .addStatement(
    //        "$T $L = $L.exchange(uri, $T.GET, requestEntity, $T.class)",
    //        LombokTypes.`val`,
    //        exchangeName,
    //        restTemplateName,
    //        SpringTypes.HttpMethod,
    //        JavaTypes.String
    //      )
    //      .add("$T $L = ResponseModel.<BodyModel>builder()", LombokTypes.`val`, responseModelBuilderName)
    //      .add(".headers($L.getHeaders())", exchangeName)
    //      .addStatement(".httpStatus($L.getStatusCode())", exchangeName)
    //      .addStatement("$T $L  = BodyModel.builder()", LombokTypes.`val`, bodyBuilderName)
    //      .beginControlFlow("switch ($L.getStatusCode().value())", exchangeName)
    //    operation.responses.foreach(
    //      response => {
    //        val code = responseCodeToString(response.code)
    //        val containerAccessorName = responseToContainerName(response)
    //        val responseType = JavaPojoUtil.dataTypeFromField(service, response.`type`, nameSpaces.model)
    //        restTemplateBlock.add("case $L :", code)
    //        if (response.`type` == "unit") {
    //          restTemplateBlock.addStatement("$L.$L($T.of(true))", bodyBuilderName, containerAccessorName, JavaTypes.Optional)
    //        } else if (JavaPojoUtil.isModelType(service, response.`type`) || JavaPojoUtil.isModelNameWithPackage(response.`type`)) {
    //
    //          restTemplateBlock
    //            .add("$L.$L(", bodyBuilderName, containerAccessorName)
    //            .add("read(config.objectMapper(), $L.getBody(), $T.class)", exchangeName, responseType)
    //            .addStatement(")")
    //            .addStatement("break")
    //        } else if (
    //          JavaPojoUtil.isListOfModeslType(service, response.`type`) ||
    //            JavaPojoUtil.isListOfEnumlType(service, response.`type`) ||
    //            JavaPojoUtil.islistOfModelNameWithPackage(response.`type`)
    //        ) {
    //          restTemplateBlock
    //            .add("$L.$L(", bodyBuilderName, containerAccessorName)
    //            .add("read(config.objectMapper(),$L.getBody(), new $T<$T>(){})", exchangeName, JacksonTypes.TypeReference, responseType)
    //            .addStatement(")")
    //        }
    //      }
    //    )
    //
    //
    //    restTemplateBlock.add("default:")
    //      .addStatement("String body = exchange.getBody()")
    //      .beginControlFlow("if (body != null) ")
    //      .addStatement("$L.unexpected($T.of($L.getBody()))", bodyBuilderName, JavaTypes.Optional, exchangeName)
    //      .endControlFlow()
    //      .beginControlFlow("else")
    //      .addStatement("$L.unexpected(Optional.of(\"\"))", bodyBuilderName)
    //      .endControlFlow()
    //      .addStatement("break")
    //      .endControlFlow()


    //    restTemplateBlock.addStatement("return $T.of($L.body($L.build()).build())", JavaTypes.Optional, responseModelBuilderName, bodyBuilderName)


    //    restTemplateBlock
    //      .endControlFlow()

    val method = MethodSpec.methodBuilder(operation.method.toString.toLowerCase + "Async")
      .addModifiers(PUBLIC, STATIC)
      .addException(JavaTypes.URISyntaxException)
      .returns(returnType)
      .addParameter(configClass, "config", FINAL)
      .addParameters(params.asJava)
      .addCode(uriBlock)
      .addCode(restTemplateBlock.build())

    if (!params.isEmpty) {
      method.addException(JavaTypes.UnsupportedEncodingException)
    }


    method.build()
  }

  def generateServiceOperationResponseContainer(service: Service, resource: Resource, operation: Operation, nameSpaces: NameSpaces): TypeSpec = {
    val className = JavaPojoUtil.toClassName("BodyModel")

    def doResponse(response: Response): FieldSpec = {
      val paramClassName =
        if (response.`type` != "unit")
          ParameterizedTypeName.get(JavaTypes.Optional, JavaPojoUtil.dataTypeFromField(service, response.`type`, nameSpaces.model))
        else
          ParameterizedTypeName.get(JavaTypes.Optional, JavaTypes.`Boolean`)
      FieldSpec.builder(paramClassName, responseToContainerName(response))
        .addAnnotation(LombokAnno.Getter)
        .addAnnotation(LombokTypes.BuilderDefault)
        .initializer("$T.empty()", JavaTypes.Optional)
        .build()
    }

    val spec = TypeSpec.classBuilder(className)
      .addModifiers(PUBLIC, STATIC)
      .addAnnotations(Seq(LombokAnno.Builder, LombokAnno.AccessorFluent).asJava)
    spec.addFields(
      operation.responses.map(doResponse(_)).asJava
    )
      .addField(
        FieldSpec.builder(
          ParameterizedTypeName.get(JavaTypes.Optional, JavaTypes.String), "unexpected")
          .addAnnotation(LombokAnno.Getter)
          .addAnnotation(LombokTypes.BuilderDefault)
          .initializer("$T.empty()", JavaTypes.Optional)
          .build()
      )
    spec.build()
  }

  def responseToContainerName(response: Response): String = {
    JavaPojoUtil.toParamName(response.`type`, true) + responseCodeToString(response.code)
  }


}
