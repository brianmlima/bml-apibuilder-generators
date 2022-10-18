package bml.util.java.client

import java.net.URI
import java.nio.charset.StandardCharsets

import akka.http.scaladsl.model.headers.CacheDirectives.public
import bml.util.AnotationUtil.LombokAnno
import bml.util.attribute.Version
import bml.util.java.ClassNames.SpringTypes.SpringValidationTypes
import bml.util.java.ClassNames.{JacksonTypes, JavaTypes, LombokTypes, SpringTypes}
import bml.util.java.client.util.{ParamTypeEnum, Params, UriParam}
import bml.util.java.{ClassNames, JavaDataTypes, JavaPojoUtil}
import bml.util.{GeneratorFSUtil, NameSpaces}
import bml.util.spring.SpringControllers
import bml.util.spring.SpringServices.{responseCodeToString, toResponseSubTypeCLassName}
import com.squareup.javapoet.{ClassName, CodeBlock, FieldSpec, MethodSpec, ParameterSpec, ParameterizedTypeName, TypeName, TypeSpec, TypeVariableName}
import io.apibuilder.generator.v0.models.File
import io.apibuilder.spec.v0.models.Method.{Get, Post, Put}
import io.apibuilder.spec.v0.models.{Operation, ParameterLocation, Resource, Response, Service}
import javax.lang.model.element.Modifier
import javax.validation.constraints.NotNull
import lombok.Getter
import org.slf4j.LoggerFactory

object JavaClients {

  import javax.lang.model.element.Modifier._
  import collection.JavaConverters._
  import bml.util.GeneratorFSUtil.makeFile


  val log = LoggerFactory.getLogger(this.getClass)


  def toClientClassName(service: Service, nameSpaces: NameSpaces): ClassName = {
    ClassName.get(nameSpaces.client.nameSpace, JavaPojoUtil.toClassName(service.name) + "Client")
  }

  val webClientFieldName = "webClient"
  val baseUriFieldName = "baseUri"
  val objectMapperFieldName = "objectMapper"

  //  val hostFieldName = "host"
  //  val portFieldName = "port"

  val configFieldName = "config"


  def generateClient(service: Service, nameSpaces: NameSpaces): Seq[File] = {
    val clientClassName = toClientClassName(service, nameSpaces);
    val configClassName = ClassName.get(clientClassName.packageName() + "." + clientClassName.simpleName(), "Config")

    service.baseUrl


    val baseUriField = FieldSpec.builder(JavaTypes.URI, baseUriFieldName, PRIVATE, FINAL)
      .addAnnotation(LombokTypes.Getter)
      .addAnnotation(classOf[NotNull])

    if (service.baseUrl.isDefined) {
      baseUriField.addAnnotation(LombokAnno.BuilderDefault)
        .initializer("$S", service.baseUrl.get)
    }

    val configType = TypeSpec.classBuilder(configClassName)
      .addModifiers(PUBLIC, STATIC)
      .addAnnotation(LombokAnno.Generated)
      .addAnnotation(LombokTypes.Builder)
      .addAnnotation(LombokAnno.AccessorFluent)
      .addFields(
        Seq(
          FieldSpec.builder(SpringTypes.WebClient, webClientFieldName, PRIVATE, FINAL)
            .addAnnotation(LombokTypes.Getter)
            .addAnnotation(classOf[NotNull])
            .build(),
          baseUriField.build(),
          FieldSpec.builder(JacksonTypes.ObjectMapper, objectMapperFieldName, PRIVATE, FINAL)
            .addAnnotation(LombokTypes.Getter)
            .addAnnotation(classOf[NotNull])
            .build()
        ).asJava
      ).build()


    val paramTypeEnum = new ParamTypeEnum(clientClassName);
    val uriParamClass = new UriParam(clientClassName, paramTypeEnum);


    val responseModelType = TypeSpec.classBuilder("ResponseModel").addTypeVariable(TypeVariableName.get("T"))
      .addModifiers(PUBLIC, STATIC)
      .addAnnotation(LombokAnno.Generated)
      .addAnnotation(LombokTypes.Builder)
      .addAnnotation(LombokAnno.AccessorFluent)
      .addField(
        FieldSpec.builder(SpringTypes.HttpStatus, "httpStatus", PRIVATE, FINAL)
          .addAnnotation(classOf[Getter])
          .addAnnotation(classOf[NotNull])
          .build()
      )
      .addField(
        FieldSpec.builder(SpringTypes.Headers, "headers", PRIVATE, FINAL)
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
          LombokAnno.Generated,
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
                  val className = ClassName.get("", JavaPojoUtil.toClassName(resource.`type`) + "ResourceClient")
                  val fieldName = JavaPojoUtil.toFieldName(resource.`type`)
                  CodeBlock.of("this.$L= new $L.$T($L);", fieldName, clientClassName.canonicalName(), className, configFieldName)
                }
              ).asJava, ""
            )
          )
          .build()
      )


      .addFields(
        service.resources.map(
          resource => {
            val className = ClassName.get("", JavaPojoUtil.toClassName(resource.`type`) + "ResourceClient")
            FieldSpec.builder(className, JavaPojoUtil.toFieldName(resource.`type`), PRIVATE)
              .addAnnotation(LombokTypes.Getter)
              .addJavadoc(resource.description.getOrElse(""))
              .build()
          }
        ).asJava
      )
      .addType(configType)
      .addType(uriParamClass.uriParamType)
      .addType(paramTypeEnum.makeType())
      .addType(responseModelType)
      .addTypes(
        service.resources.map(
          resource =>
            TypeSpec.classBuilder(JavaPojoUtil.toClassName(resource.`type`) + "ResourceClient")
              .addAnnotations(
                Seq(
                  LombokAnno.Generated,
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


                    val containerClassName = ClassName.bestGuess(JavaPojoUtil.toClassName(toOperationClientClassName(operation)))

                    val paramsUtils = new Params(
                      service,
                      nameSpaces,
                      containerClassName,
                      operation.parameters.filter(p => (p.location == ParameterLocation.Path || p.location == ParameterLocation.Query)),
                      uriParamClass
                    )
                    val spec = TypeSpec.classBuilder(containerClassName)
                      .addModifiers(PUBLIC, STATIC)
                      .addAnnotation(LombokAnno.Generated)
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
                      .addMethod(generateStaticAccessAsyncParamHandler(service, resource, operation, nameSpaces))
                      .addMethod(generateStaticAccessAsync(service, resource, operation, nameSpaces))


                    if (paramsUtils.uriParams.nonEmpty) {
                      spec.addType(paramsUtils.makeType())
                    }


                    spec.build()
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
        ""
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
      SpringTypes.Mono,
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

  def generateStaticAccessAsyncParamHandler(service: Service, resource: Resource, operation: Operation, nameSpaces: NameSpaces): MethodSpec = {

    return MethodSpec.methodBuilder("buildUri").returns(Void.TYPE).build()

  }


  def generateStaticAccessAsync(service: Service, resource: Resource, operation: Operation, nameSpaces: NameSpaces): MethodSpec = {


    val version = Version.fromResource(resource)

    val configClass = ClassName.get(
      nameSpaces.client.nameSpace + "." + toClientClassName(service, nameSpaces).simpleName(),
      "Config"
    )


    val returnType = ParameterizedTypeName.get(
      SpringTypes.Mono,
      ParameterizedTypeName.get(
        ClassName.get("", "ResponseModel"),
        ClassName.get("", "BodyModel")
      )
    )


    val defaultVersionPath = "/v" + service.version.split("\\.").head

    val versionPath = if (version.isEmpty) defaultVersionPath else if (version.get.drop) "" else defaultVersionPath


    val uriComponentsPath = versionPath + operation.path.split("/").map(
      part => if (part.startsWith(":")) {
        s"{${part.replaceFirst(":", "")}}"
      } else {
        part
      }
    ).mkString("/")



    val uriBlock =
      if (operation.path.contains(":")) {
//        val path = versionPath + operation.path.split("/").map(
//          part => if (part.startsWith(":")) {
//            "%s"
//          } else {
//            part
//          }
//        ).mkString("/")
//        val params = operation.path.split("/").filter(_.startsWith(":"))
//          .map(
//            param =>
//              JavaPojoUtil.toParamName(param.drop(1) + "Encoded", true)
//          ).mkString(",")

//        val encoded = operation.path.split("/").seq
//          .filter(_.startsWith(":"))
//          .map(
//            param =>
//              CodeBlock.builder()
//                .addStatement("final $T $L = $T.encode($L.toString(),$S)",
//                  JavaTypes.String,
//                  JavaPojoUtil.toParamName(param.drop(1) + "Encoded", true),
//                  JavaTypes.URLEncoder,
//                  JavaPojoUtil.toParamName(param.drop(1), true),
//                  "UTF-8"
//                ).build()
//          ).asJava


        val uriComponentsPath = versionPath + operation.path.split("/").map(
          part => if (part.startsWith(":")) {
            s"{${part.replaceFirst(":", "")}}"
          } else {
            part
          }
        ).mkString("/")


        val code = CodeBlock.builder()
          .addStatement(
            CodeBlock.builder()
              .add("val uriBuilder = $T.fromUri(config.baseUri())", SpringTypes.UriComponentsBuilder)
              .add(".pathSegment($S)", uriComponentsPath)
              .build()
          )
        code
          .add(
            "val uri  = uriBuilder"
          )


        operation.parameters
          .filter(_.location == ParameterLocation.Query)
          .foreach(
            param => {
              val staticFieldName = JavaPojoUtil.toStaticFieldName(param.name)
              code.add(
                ".queryParamIfPresent(Params.$L.name, Optional.ofNullable($L).or(() -> Params.$L.defaultValue()))",
                staticFieldName,
                param.name,
                staticFieldName
              )
            }
          )


        code.addStatement(".buildAndExpand($L).toUri()",
          operation.path.split("/").seq
            .filter(_.startsWith(":"))
            .map(
              _.drop(1)
            ).mkString(", ")
        )
        code.addStatement("log.info(\"Calling uri={}\",uri)")
          //.addStatement("final $T uri = new $T($L.baseUri().toString() + path)", classOf[URI], classOf[URI], configFieldName)
          .build()
      }

      else {

        val code = CodeBlock.builder()
          //          .add(JavaPojoUtil.textToComment("Encode url path paramters"))
          //          .add(CodeBlock.join(encoded, ""))
          //          .addStatement("final $T path = $T.format($S,$L)", JavaTypes.String, JavaTypes.String, path, params)
          .addStatement(
            CodeBlock.builder()
              .add("val uriBuilder = $T.fromUri(config.baseUri())", SpringTypes.UriComponentsBuilder)
              .add(".pathSegment($S)", uriComponentsPath)
              .build()
          )
        code
          .add(
            "val uri  = uriBuilder"
          )
        code.addStatement(".buildAndExpand($L).toUri()",
          operation.path.split("/").seq
            .filter(_.startsWith(":"))
            .map(
              _.drop(1)
            ).mkString(", ")
        )
        code.addStatement("log.info(\"Calling uri={}\",uri)")
          //.addStatement("final $T uri = new $T($L.baseUri().toString() + path)", classOf[URI], classOf[URI], configFieldName)
          .build()
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

    val webClientBlock = CodeBlock.builder()
      .add("final $T exchange =  $L.$L().method($T.$L)",
        ParameterizedTypeName.get(SpringTypes.Mono, SpringTypes.ClientResponse),
        configFieldName,
        webClientFieldName,
        ClassNames.HttpMethod,
        operation.method.toString().toUpperCase
      )
      .add(".uri(uri)")
      .add(".acceptCharset($T.UTF_8)", classOf[StandardCharsets])

    if (operation.body.isDefined) {
      webClientBlock.add(".contentType($T.APPLICATION_JSON)", SpringTypes.MediaType)
        .add(".body($T.fromValue($L))", SpringTypes.BodyInserters, JavaPojoUtil.toParamName(operation.body.get.`type`, true))
    }


    operation.parameters.foreach(
      p =>
        log.info("Service {} Operation {} Parameter {} Location {}", service.name, operation.path, p.name, p.location)

    )

    val headers = operation.parameters.filter(p => p.location.equals(ParameterLocation.Header))

    headers.foreach(
      p => {

        webClientBlock.add(".header($S,$L)", p.name, JavaPojoUtil.toParamName(p.name, true))
      }
    )


    webClientBlock
      .addStatement(".exchange()")
      .add("return exchange.flatMap(")
      .beginControlFlow("($T responseIn) ->", SpringTypes.ClientResponse)
      .addStatement("$T httpStatusIn = responseIn.statusCode()", LombokTypes.`val`)
      .addStatement("$T responseOut = ResponseModel.<BodyModel>builder().httpStatus(httpStatusIn).headers(responseIn.headers())", LombokTypes.`val`)
      .addStatement("val bodyOut = BodyModel.builder()")

      .beginControlFlow("switch (httpStatusIn.value())")

    operation.responses.foreach(
      response => {
        val code = responseCodeToString(response.code)
        val containerAccessorName = responseToContainerName(response)
        val responseType = JavaPojoUtil.dataTypeFromField(service, response.`type`, nameSpaces.model)
        webClientBlock.add("case $L :", code)
        if (response.`type` == "unit") {
          webClientBlock.add("return $T.create((sink) -> sink.success(responseOut.body(bodyOut.$L($T.of(true)).build()).build()));",
            SpringTypes.Mono,
            containerAccessorName,
            JavaTypes.Optional
          )
        } else if (response.`type` == "string") {
          webClientBlock.add("return responseIn.bodyToMono($T.class).map(", responseType)
          webClientBlock.add(" (content) -> responseOut.body(")
          webClientBlock.add("  bodyOut.$L(", containerAccessorName)
          webClientBlock.add("$T.of(content)", JavaTypes.Optional)
          webClientBlock.add(").build()")
          webClientBlock.add(").build()")
          webClientBlock.add(");")
        }
        else if (JavaPojoUtil.isModelType(service, response.`type`) || JavaPojoUtil.isModelNameWithPackage(response.`type`)) {
          webClientBlock.add("return responseIn.bodyToMono($T.class).map(", JavaTypes.String)
          webClientBlock.add(" (content) -> responseOut.body(")
          webClientBlock.add("  bodyOut.$L(", containerAccessorName)
          webClientBlock.add("read(config.objectMapper(), content, $T.class)", responseType)
          webClientBlock.add(").build()")
          webClientBlock.add(").build()")
          webClientBlock.add(");")
        } else if (
          JavaPojoUtil.isListOfModeslType(service, response.`type`) ||
            JavaPojoUtil.isListOfEnumlType(service, response.`type`) ||
            JavaPojoUtil.islistOfModelNameWithPackage(response.`type`)
        ) {
          webClientBlock.add("return responseIn.bodyToMono($T.class).map(", JavaTypes.String)
          webClientBlock.add(" (content) -> responseOut.body(")
          webClientBlock.add("  bodyOut.$L(", containerAccessorName)
          webClientBlock.add("read(config.objectMapper(), content, new $T<$T>(){})", JacksonTypes.TypeReference, responseType)
          webClientBlock.add(").build()")
          webClientBlock.add(").build()")
          webClientBlock.add(");")
        }
      }
    )

    webClientBlock.addStatement(
      "default: return responseIn.bodyToMono($T.class).map((content) -> responseOut.body(bodyOut.unexpected($T.of(content)).build()).build())",
      JavaTypes.String,
      JavaTypes.Optional
    )


    webClientBlock
      .endControlFlow()
      .endControlFlow()
      .addStatement(")")

    val method = MethodSpec.methodBuilder(operation.method.toString.toLowerCase + "Async")
      .addModifiers(PUBLIC, STATIC)
      .addException(JavaTypes.URISyntaxException)
      .returns(returnType)
      .addParameter(configClass, "config", FINAL)
      .addParameters(params.asJava)
      .addCode(uriBlock)
      .addCode(webClientBlock.build())

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
        .addAnnotation(LombokAnno.BuilderDefault)
        .initializer("$T.empty()", JavaTypes.Optional)
        .build()
    }

    val spec = TypeSpec.classBuilder(className)
      .addModifiers(PUBLIC, STATIC)
      .addAnnotations(Seq(LombokAnno.Builder, LombokAnno.AccessorFluent, LombokAnno.Generated).asJava)
    spec.addFields(
      operation.responses.map(doResponse(_)).asJava
    )
      .addField(
        FieldSpec.builder(
          ParameterizedTypeName.get(JavaTypes.Optional, JavaTypes.String), "unexpected")
          .addAnnotation(LombokAnno.Getter)
          .addAnnotation(LombokAnno.BuilderDefault)
          .initializer("$T.empty()", JavaTypes.Optional)
          .build()
      )
    spec.build()
  }

  def responseToContainerName(response: Response): String = {
    JavaPojoUtil.toParamName(response.`type`, true) + responseCodeToString(response.code)
  }


}
