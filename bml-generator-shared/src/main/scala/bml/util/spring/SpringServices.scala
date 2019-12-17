package bml.util.spring

import bml.util.AnotationUtil.JavaxAnnotations.JavaxValidationAnnotations
import bml.util.java.ClassNames._
import bml.util.java.ClassNames.java.`override`
import bml.util.java.{ClassNames, JavaPojoUtil}
import bml.util.{AnotationUtil, GeneratorFSUtil, NameSpaces}
import com.squareup.javapoet._
import io.apibuilder.generator.v0.models.File
import io.apibuilder.spec.v0.models.{Operation, Parameter, Resource, Service}

object SpringServices {

  import javax.lang.model.element.Modifier._

  def toServiceName(resource: Resource): String = JavaPojoUtil.toClassName(resource.`type`) + "Service"

  def toServiceMockName(resource: Resource): String = toServiceName(resource) + "Mock"

  def toServiceClassName(nameSpaces: NameSpaces, resource: Resource): ClassName = ClassName.get(nameSpaces.service.nameSpace, toServiceName(resource))

  def toServiceMockClassName(nameSpaces: NameSpaces, resource: Resource): ClassName = ClassName.get(nameSpaces.service.nameSpace, toServiceMockName(resource))

  def toOperationName(operation: Operation) = JavaPojoUtil.toMethodName(operation.method.toString.toLowerCase + "_" + operation.path)

  private def modelDataType(nameSpaces: NameSpaces, parameter: Parameter) = JavaPojoUtil.dataTypeFromField(parameter.`type`, nameSpaces.model.nameSpace)


  def generateBaseConfiguration(nameSpaces: NameSpaces, service: Service): Seq[File] = {
    val serviceName = JavaPojoUtil.toClassName(service.name + "-base-confiuration")

    val mediaTypeYaml = "MEDIA_TYPE_YAML"
    val mediaTypeYml = "MEDIA_TYPE_YML"

    val serviceBuilder = TypeSpec.classBuilder(serviceName)
      .addModifiers(PUBLIC)
      .addSuperinterface(webMvcConfigurer)
      .addAnnotation(configuration)
      .addAnnotation(enableWebMvc)
      .addField(FieldSpec.builder(mediaType, mediaTypeYaml, PUBLIC, STATIC, FINAL).initializer(" MediaType.valueOf(\"text/yaml\")").build())
      .addField(FieldSpec.builder(mediaType, mediaTypeYml, PUBLIC, STATIC, FINAL).initializer(" MediaType.valueOf(\"text/yml\")").build())
      .addMethod(
        MethodSpec.methodBuilder("jsonObjectMapper").addModifiers(PUBLIC).returns(objectMapper)
          .addAnnotation(primary)
          .addAnnotation(AnnotationSpec.builder(bean).addMember("name", "$S", "jsonObjectMapper").build())
          .addStatement(CodeBlock.of("return new $T()", objectMapper))
          .build()
      )
      .addMethod(
        MethodSpec.methodBuilder("yamlObjectMapper").addModifiers(PUBLIC).returns(objectMapper)
          .addAnnotation(AnnotationSpec.builder(bean).addMember("name", "$S", "yamlObjectMapper").build())
          .addStatement(CodeBlock.of("return new $T(new $T())", objectMapper, yAMLFactory))
          .build()
      ).addMethod(
      MethodSpec.methodBuilder("configureContentNegotiation")
        .addModifiers(PUBLIC)
        .addAnnotation(`override`)
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
        .addAnnotation(`override`)
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
            .addStatement("super(new $T(new $T()))", objectMapper, yAMLFactory)
            .addStatement("this.setSupportedMediaTypes($T.of(MEDIA_TYPE_YML, MEDIA_TYPE_YAML))", immutableList)
            .build()
        ).build()
    )
    //
    //    @Component
    //    public static class YamlMessageConverter extends MappingJackson2HttpMessageConverter {
    //      public YamlMessageConverter() {
    //        //can use overloaded constructor to set supported MediaType
    //        super(new ObjectMapper(new YAMLFactory()));
    //        this.setSupportedMediaTypes(ImmutableList.of(MEDIA_TYPE_YML, MEDIA_TYPE_YAML));
    //      }
    //    }


    //Return the generated Service interface
    Seq(GeneratorFSUtil.makeFile(serviceName, nameSpaces.config, serviceBuilder))
  }


  def generateService(nameSpaces: NameSpaces, resource: Resource): Seq[File] = {
    val serviceName = toServiceClassName(nameSpaces, resource)
    val serviceBuilder = TypeSpec.interfaceBuilder(serviceName).addModifiers(PUBLIC)
      .addJavadoc(resource.description.getOrElse(""))
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
    Seq(GeneratorFSUtil.makeFile(implName.simpleName(), nameSpaces.service, implBuilder))
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
    methodSpec.addJavadoc(operation.description.getOrElse(""))
      .addJavadoc(operation.parameters.map(serviceParamJavadoc(nameSpaces, _)).mkString("\n"))
      .addJavadoc(s"@return ${ClassNames.responseEntity.simpleName()}")
    //Add Parameters
    operation.parameters.map(operationParamToServiceParam(nameSpaces, _)).foreach(methodSpec.addParameter)
    return Some(methodSpec)
  }

  private def serviceParamJavadoc(nameSpaces: NameSpaces, parameter: Parameter): String = {
    val paramName = JavaPojoUtil.toParamName(parameter.name, true)
    val javaDataType = modelDataType(nameSpaces, parameter)
    s"@param $paramName ${javaDataType.toString} ${parameter.description.getOrElse("")}".trim
  }

  private def operationParamToServiceParam(nameSpaces: NameSpaces, parameter: Parameter): ParameterSpec = {
    val paramName = JavaPojoUtil.toParamName(parameter.name, true)
    val javaDataType = modelDataType(nameSpaces, parameter)
    val builder = ParameterSpec.builder(javaDataType, paramName)
    if (parameter.required || parameter.default.isDefined) builder.addAnnotation(JavaxValidationAnnotations.NotNull)
    builder.build()
  }


}
