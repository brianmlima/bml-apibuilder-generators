package bml.util.spring

import bml.util.AnotationUtil.LombokAnno
import bml.util.java.ClassNames._
import bml.util.java.JavaPojoUtil
import bml.util.persist.SpringVariableTypes.ValidationAnnotations
import bml.util.spring.SpringVersion.SpringVersion
import bml.util.{GeneratorFSUtil, NameSpaces, ServiceTool}
import io.apibuilder.generator.v0.models.File
import io.apibuilder.spec.v0.models.Method.{Delete, Post, Put}
import io.apibuilder.spec.v0.models._

import javax.lang.model.element.Modifier
import org.slf4j.LoggerFactory


class SpringServices {

}

object SpringServices {

  val LOG = LoggerFactory.getLogger(classOf[SpringServices])

  import com.squareup.javapoet._
  import javax.lang.model.element.Modifier._

  import collection.JavaConverters._

  def toServiceName(resource: Resource): String = JavaPojoUtil.toClassName(resource.`type`) + "Service"

  def toServiceName(springVersion: SpringVersion, service: Service, resource: Resource): String = {
    springVersion match {
      case bml.util.spring.SpringVersion.SIX => JavaPojoUtil.toClassName(s"${ServiceTool.prefix(springVersion, service)}-${JavaPojoUtil.toClassName(resource.`type`)}Service")
      case bml.util.spring.SpringVersion.FIVE => JavaPojoUtil.toClassName(s"${JavaPojoUtil.toClassName(resource.`type`)}Service")
    }
  }

  def toServiceMockName(resource: Resource): String = toServiceName(resource) + "Mock"

  def toServiceClassName(nameSpaces: NameSpaces, resource: Resource): ClassName = {
    ClassName.get(nameSpaces.service.nameSpace, toServiceName(resource))
  }

  def toServiceClassName(springVersion: SpringVersion, service: Service, nameSpaces: NameSpaces, resource: Resource): ClassName = {
    ClassName.get(nameSpaces.service.nameSpace, toServiceName(springVersion, service, resource))
  }

  def toResponseSubTypeCLassName(nameSpaces: NameSpaces, operation: Operation): ClassName = {
    JavaPojoUtil.toClassName(nameSpaces.service, "ResponseModel" + toOperationName(operation).capitalize)
  }

  def toServiceMockClassName(nameSpaces: NameSpaces, resource: Resource): ClassName = ClassName.get(nameSpaces.service.nameSpace, toServiceMockName(resource))

  def toOperationName(operation: Operation) = {
    JavaPojoUtil.toMethodName(SpringControllers.toControllerOperationName(operation))
    //JavaPojoUtil.toMethodName(operation.method.toString.toLowerCase + "_" + operation.path)
  }

  private def modelDataType(service: Service, nameSpaces: NameSpaces, parameter: Parameter) = JavaPojoUtil.dataTypeFromField(service, parameter.`type`, nameSpaces.model.nameSpace)


  def generateServiceOperationResponseContainer(service: Service, nameSpaces: NameSpaces): Seq[File] = {
    service.resources.flatMap(
      resource =>
        resource.operations.flatMap(
          operation =>
            operation.responses.flatMap(
              response =>
                SpringServices.generateServiceOperationResponseContainer(service, resource, operation, response, nameSpaces)
            )
        )
    )
  }


  def responseCodeToString(responseCode: ResponseCode): String = {
    responseCode.toString.replace("ResponseCodeInt(", "").replace(")", "")
  }

  def is4XX(responseCode: ResponseCode): Boolean = {
    val code = responseCodeToString(responseCode).toInt
    (code >= 400 && code <= 499)
  }


  def responseToContainerMemberName(response: Response): String = {
    JavaPojoUtil.toParamName(response.`type`, true) + responseCodeToString(response.code)
  }

  def generateServiceOperationResponseContainer(service: Service, resource: Resource, operation: Operation, response: Response, nameSpaces: NameSpaces): Seq[File] = {
    val className = toResponseSubTypeCLassName(nameSpaces, operation)

    def doResponse(response: Response): FieldSpec = {
      val paramClassName = ParameterizedTypeName.get(SpringTypes.ResponseEntity, JavaPojoUtil.dataTypeFromField(service, response.`type`, nameSpaces.model))

      //      val code = response.code.toString.replace("ResponseCodeInt(", "").replace(")", "")
      //      FieldSpec.builder(paramClassName, JavaPojoUtil.toParamName(response.`type`, true) + responseCodeToString(response.code))
      FieldSpec.builder(paramClassName, responseToContainerMemberName(response))
        .addModifiers(PRIVATE)
        .addAnnotation(LombokAnno.Getter)
        .addJavadoc(
          "Field for Response Code $S, description $S .",
          responseCodeToString(response.code),
          response.description.get
        )
        .build()

    }

    def doVoidResponseBuilderHelper(responses: Seq[Response]): TypeSpec = {

      val builderClass = ClassName.bestGuess(s"${className.simpleName()}Builder")

      val spec = TypeSpec.classBuilder(builderClass).addModifiers(PUBLIC, STATIC)
      spec.addJavadoc("Added so we can write shortcut methods so you dont have to create a Response entity if you dont want to.");

      //
      responses.foreach(
        response => {
          val responseType = JavaPojoUtil.dataTypeFromField(service, response.`type`, nameSpaces.model);
          val paramClassName = ParameterizedTypeName.get(SpringTypes.ResponseEntity, responseType)
          val fieldName = responseToContainerMemberName(response);

          if (response.`type` == "unit" || response.`type` == "string") {
            spec.addField(
              FieldSpec.builder(paramClassName, fieldName)
                .addModifiers(PRIVATE)
                .addJavadoc(
                  "Field for Response Code $S, description $S .",
                  responseCodeToString(response.code),
                  response.description.get
                )
                .build()
            )
          }
          if (!JavaPojoUtil.isParameterArray(response.`type`) && response.`type` != "unit") {
            spec.addMethod(
              MethodSpec.methodBuilder(fieldName)
                .addModifiers(PUBLIC)
                .returns(builderClass)
                .addParameter(ParameterSpec.builder(responseType, "responseBody", FINAL).build())
                .addCode(
                  CodeBlock.builder()
                    .addStatement("this.$L=$T.status($L).body($L)", fieldName, SpringTypes.ResponseEntity, responseCodeToString(response.code), "responseBody")
                    .addStatement("return this")
                    .build()
                )
                .build()
            )
            //            spec.addMethod(
            //              MethodSpec.methodBuilder(fieldName)
            //                .addModifiers(PUBLIC)
            //                .returns(builderClass)
            //                .addParameter(ParameterSpec.builder(paramClassName, "responseEntity", FINAL).build())
            //                .addCode(
            //                  CodeBlock.builder()
            //                    .addStatement("this.$L=$L", fieldName, "responseEntity")
            //                    .addStatement("return this")
            //                    .build()
            //                )
            //                .build()
            //            )
            spec.addMethod(
              MethodSpec.methodBuilder(fieldName)
                .addModifiers(PUBLIC)
                .returns(builderClass)
                .addParameter(ParameterSpec.builder(paramClassName, "responseEntity", FINAL).build())
                .addCode(
                  CodeBlock.builder()
                    .addStatement("this.$L=$L", fieldName, "responseEntity")
                    .addStatement("return this")
                    .build()
                )
                .build()
            )
          }


          if (response.`type` == "unit") {
            spec.addMethod(
              MethodSpec.methodBuilder(fieldName)
                .addModifiers(PUBLIC)
                .returns(builderClass)
                .addCode(
                  CodeBlock.builder()
                    .addStatement("this.$L=$T.status($L).build()", fieldName, SpringTypes.ResponseEntity, responseCodeToString(response.code))
                    .addStatement("return this")
                    .build()
                )
                .build()
            )
            spec.addMethod(
              MethodSpec.methodBuilder(fieldName)
                .addModifiers(PUBLIC)
                .returns(builderClass)
                .addParameter(ParameterSpec.builder(paramClassName, "responseEntity", FINAL).build())
                .addCode(
                  CodeBlock.builder()
                    .addStatement("this.$L=$L", fieldName, "responseEntity")
                    .addStatement("return this")
                    .build()
                )
                .build()
            )
          }
        }
      )


      return spec.build()
    }


    val spec = TypeSpec.classBuilder(className)
      .addType(doVoidResponseBuilderHelper(operation.responses))
      .addModifiers(Modifier.PUBLIC)
      .addAnnotations(Seq(LombokAnno.Builder, LombokAnno.AccessorFluent).asJava)
      .addJavadoc(operation.description.getOrElse(""))
    spec.addFields(
      operation.responses.map(doResponse(_)).asJava
    )


    Seq(GeneratorFSUtil.makeFile(className.simpleName(), nameSpaces.service, spec))
  }


  def generateBaseConfiguration(nameSpaces: NameSpaces, service: Service): Seq[File] = {
    val configName = JavaPojoUtil.toClassName(service.name + "-base-confiuration")

    val mediaTypeYaml = "MEDIA_TYPE_YAML"
    val mediaTypeYml = "MEDIA_TYPE_YML"

    val objectMapperBeanNameJson = "jsonObjectMapper"
    val objectMapperBeanNameYml = "yamlObjectMapper"

    val objectMapperBeanNameFieldJson = "JSON_OBJECT_MAPPER_BEAN_NAME"
    val objectMapperBeanNameFieldYml = "YAML_OBJECT_MAPPER_BEAN_NAME"

    val configBuilder = TypeSpec.classBuilder(configName)
      .addJavadoc(
        "Add json or yaml content negotiation for all endpoints.\n" +
          ""
      )
      .addJavadoc("\n")
      .addJavadoc("Provides two {@link ObjectMapper}s {@link #jsonObjectMapper} and {@link #yamlObjectMapper} \n")

      .addModifiers(PUBLIC)
      .addSuperinterface(webMvcConfigurer)
      .addAnnotation(configuration)
      //      .addAnnotation(enableWebMvc)
      .addField(
        FieldSpec.builder(mediaType, mediaTypeYaml, PUBLIC, STATIC, FINAL)
          .addJavadoc("Media type for text/yaml.")
          .initializer(" MediaType.valueOf(\"text/yaml\")")
          .build()
      )
      .addField(
        FieldSpec.builder(mediaType, mediaTypeYml, PUBLIC, STATIC, FINAL)
          .addJavadoc("Media type for text/yam.")
          .initializer(" MediaType.valueOf(\"text/yml\")")
          .build()
      )
      .addField(
        FieldSpec.builder(classOf[String], objectMapperBeanNameFieldJson, PUBLIC, STATIC, FINAL)
          .addJavadoc("Bean name for Json ObjectMapper.")
          .initializer("$S", objectMapperBeanNameJson)
          .build()
      )
      .addField(
        FieldSpec.builder(classOf[String], objectMapperBeanNameFieldYml, PUBLIC, STATIC, FINAL)
          .addJavadoc("Bean name for Json ObjectMapper.")
          .initializer("$S", objectMapperBeanNameYml)
          .build()
      )
      .addMethod(
        MethodSpec.methodBuilder("jsonObjectMapper").addModifiers(PUBLIC).returns(objectMapper)
          .addJavadoc("Provides an ObjectMapper for JSON.\n @return An ObjectMapper that uses the JSON format for serde.")
          .addAnnotation(primary)
          .addAnnotation(AnnotationSpec.builder(bean).addMember("name", "$L", objectMapperBeanNameFieldJson).build())
          .addStatement("return new $T().registerModule(new $T())", objectMapper, JacksonTypes.JavaTimeModule)
          .build()
      )
      .addMethod(
        MethodSpec.methodBuilder("yamlObjectMapper").addModifiers(PUBLIC).returns(objectMapper)
          .addJavadoc("Provides an ObjectMapper for YAML.\n @return An ObjectMapper that uses the YAML format for serde.")
          .addAnnotation(AnnotationSpec.builder(bean).addMember("name", "$L", objectMapperBeanNameFieldYml).build())
          .addStatement(CodeBlock.of("return new $T(new $T()).registerModule(new $T())", objectMapper, yAMLFactory, JacksonTypes.JavaTimeModule))
          .build()
      ).addMethod(
      MethodSpec.methodBuilder("configureContentNegotiation")
        .addModifiers(PUBLIC)
        .addAnnotation(JavaTypes.Override)
        .addParameter(ParameterSpec.builder(contentNegotiationConfigurer, "configurer", FINAL).build())
        .addCode(
          CodeBlock.builder()
            .add("configurer")
            .add(".favorPathExtension($L)", true.toString)
            .add(".favorParameter($L)", false.toString)
            .add(".ignoreAcceptHeader($L)", false.toString)
            .add(".defaultContentType($T.APPLICATION_JSON)", mediaType)
            .add(".mediaType($T.APPLICATION_JSON.getSubtype(),", mediaType)
            .add("$T.APPLICATION_JSON)", mediaType)
            .add(".mediaType($L.getSubtype(), $L)", mediaTypeYaml, mediaTypeYaml)
            .addStatement(".mediaType($L.getSubtype(), $L)", mediaTypeYml, mediaTypeYml)
            .build()
        ).build()
    ).addMethod(

      MethodSpec.methodBuilder("extendMessageConverters")
        .addModifiers(PUBLIC)
        .addAnnotation(JavaTypes.Override)
        .addParameter(
          ParameterSpec.builder(
            ParameterizedTypeName.get(JavaTypes.List, ParameterizedTypeName.get(httpMessageConverter, ClassName.get("", "?"))),
            "converters",
            FINAL
          ).build()
        ).addStatement("converters.add(new $T())", ClassName.get("", "YamlMessageConverter"))
        .build()
    ).addType(
      TypeSpec.classBuilder("YamlMessageConverter").addModifiers(PUBLIC, STATIC)
        .superclass(mappingJackson2HttpMessageConverter)
        .addMethod(
          MethodSpec.constructorBuilder()
            .addComment("can use overloaded constructor to set supported MediaType")
            //.addStatement("super(new $T(new $T()))", objectMapper, yAMLFactory)
            .addCode(
              CodeBlock.builder()
                .add("")
                .add("super(")
                .add("new $T(", objectMapper)
                .add("new $T()", yAMLFactory)
                .add(".disable($T.Feature.WRITE_DOC_START_MARKER)", JacksonTypes.YAMLGenerator)
                .add(").registerModule(new $T())", JacksonTypes.JavaTimeModule)
                .add(".configure($T.WRITE_DATES_AS_TIMESTAMPS, false)", JacksonTypes.SerializationFeature)
                .addStatement(")")
                .build()
            )
            .addStatement("this.setSupportedMediaTypes($T.of(MEDIA_TYPE_YML, MEDIA_TYPE_YAML))", immutableList)
            .build()
        ).build()
    )

    //Return the generated Service interface
    Seq(GeneratorFSUtil.makeFile(configName, nameSpaces.config, configBuilder))
  }


  def generateService(springVersion: SpringVersion, service: Service, nameSpaces: NameSpaces, resource: Resource): Seq[File] = {

    val serviceName = toServiceClassName(springVersion, service, nameSpaces, resource)
    val serviceBuilder = TypeSpec.interfaceBuilder(serviceName).addModifiers(PUBLIC)
      .addJavadoc(resource.description.getOrElse(""))
    //Generate Service methods from operations and add them to the Service Interface
    resource.operations.flatMap(generateServiceOperation(springVersion, service, nameSpaces, resource, _, false))
      .map(_.build())
      .foreach(serviceBuilder.addMethod)
    //Return the generated Service interface
    Seq(GeneratorFSUtil.makeFile(serviceName.simpleName(), nameSpaces.service, serviceBuilder))
  }

  def generateServiceOperation(springVersion: SpringVersion, service: Service, nameSpaces: NameSpaces, resource: Resource, operation: Operation, isconcrete: Boolean): Option[MethodSpec.Builder] = {
    LOG.info("service={} resource={} operationPath={} operationMethod={} isconcrete={}", service.name, resource.`type`, operation.path, operation.method.toString, isconcrete.toString)

    val methodName = toOperationName(operation)
    val methodSpec = MethodSpec.methodBuilder(methodName)
      //      .returns(SpringTypes.ResponseEntity(toResponseSubTypeCLassName(nameSpaces, operation)))
      .returns(toResponseSubTypeCLassName(nameSpaces, operation))

    if (isconcrete) {
      methodSpec.addModifiers(PUBLIC)
    } else {
      methodSpec.addModifiers(PUBLIC, ABSTRACT)
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    var javadocs = Seq[String](operation.description.getOrElse("")).filter(_ != "")

    javadocs = javadocs ++ operation.parameters.map(serviceParamJavadoc(service, nameSpaces, _))



    operation.method match {
      case Method.Post =>
        if (operation.body.isDefined) {
          var body = operation.body.get
          javadocs = javadocs ++ Seq[String](serviceBodyJavadoc(nameSpaces, body))
        }
      case _ =>
    }
    javadocs = javadocs ++ Seq[String](s"@return ${SpringTypes.ResponseEntity.simpleName()}")
    methodSpec.addJavadoc(javadocs.mkString("\n"))
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    val bodyTypes = operation.body.map(_.`type`)

    operation.parameters
      .filter(
        param =>
          !bodyTypes.contains(param.`type`)
      )
      .map(
        operationParamToServiceParam(springVersion, service, operation, nameSpaces, _))
      .foreach(methodSpec.addParameter)

    if (operation.body.isDefined) {
      val body = operation.body.get
      val bodyClassName = JavaPojoUtil.toClassName(nameSpaces.model, body.`type`)
      val bodyDataType = JavaPojoUtil.dataTypeFromField(service, body.`type`, nameSpaces.model)

      operation.method match {
        case Post|Put|Delete =>
          methodSpec.addParameter(
            ParameterSpec.builder(bodyDataType, JavaPojoUtil.toFieldName(bodyClassName.simpleName()))
              .addAnnotation(ValidationAnnotations.NotNull(springVersion))
              .addAnnotation(ValidationAnnotations.Valid(springVersion))
              .build()
          )
        case _ =>
      }
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    //Add Parameters
    return Some(methodSpec)
  }

  private def serviceParamJavadoc(service: Service, nameSpaces: NameSpaces, parameter: Parameter): String = {
    val paramName = JavaPojoUtil.toParamName(parameter.name, true)
    val javaDataType = modelDataType(service, nameSpaces, parameter)
    s"@param $paramName ${javaDataType.toString} ${parameter.description.getOrElse("")}".trim
  }

  private def serviceBodyJavadoc(nameSpaces: NameSpaces, body: Body): String = {
    val bodyClassName = JavaPojoUtil.toClassName(nameSpaces.model, body.`type`)
    val paramName = JavaPojoUtil.toParamName(bodyClassName.simpleName(), true)
    //val javaDataType = modelDataType(nameSpaces, parameter)
    s"@param $paramName ${bodyClassName.simpleName()} ${body.description.getOrElse("")}".trim
  }

  private def operationParamToServiceParam(springVersion: SpringVersion, service: Service, operation: Operation, nameSpaces: NameSpaces, parameter: Parameter): ParameterSpec = {
    val paramName = JavaPojoUtil.toParamName(parameter.name, true)
    val javaDataType = modelDataType(service, nameSpaces, parameter)
    val builder = ParameterSpec.builder(javaDataType, paramName)
    if (parameter.required || parameter.default.isDefined) builder.addAnnotation(ValidationAnnotations.NotNull(springVersion))

    if (parameter.required || parameter.default.isDefined && JavaPojoUtil.isModelNameWithPackage(parameter.`type`))
      builder.addAnnotation(ValidationAnnotations.Valid(springVersion))


    //if(parameter.name == operation.body.map())


    builder.build()
  }


}
