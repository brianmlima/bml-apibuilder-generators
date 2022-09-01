package bml.util.spring

import java.net.URISyntaxException
import java.util

import bml.util.java.ClassNames.CommonsLangTypes.StringUtils
import bml.util.java.ClassNames.SpringTypes.{Bean, Configuration, Primary}
import bml.util.{GeneratorFSUtil, NameSpaces}
import bml.util.java.ClassNames.{CommonsLangTypes, JavaTypes, SpringTypes}
import bml.util.java.{ClassNames, JavaPojoUtil}
import bml.util.spring.SwaggerUiConfig.SpringFoxTypes.{Docket, DocumentationType, EnableSwagger2, RequestHandlerSelectors, SwaggerResource}
import com.squareup.javapoet.{ClassName, CodeBlock, FieldSpec, MethodSpec, ParameterSpec, TypeSpec}
import io.apibuilder.generator.v0.models.File
import io.apibuilder.spec.v0.models.Service
import javax.lang.model.element.Modifier._

import collection.JavaConverters._

object SwaggerUiConfig {


  object SpringFoxTypes {
    val RequestHandlerSelectors = ClassName.bestGuess("springfox.documentation.builders.RequestHandlerSelectors")
    val DocumentationType = ClassName.bestGuess("springfox.documentation.spi.DocumentationType")
    val Docket = ClassName.bestGuess("springfox.documentation.spring.web.plugins.Docket")
    val SwaggerResource = ClassName.bestGuess("springfox.documentation.swagger.web.SwaggerResource")
    val SwaggerResourcesProvider = ClassName.bestGuess("springfox.documentation.swagger.web.SwaggerResourcesProvider")
    val EnableSwagger2 = ClassName.bestGuess("springfox.documentation.swagger2.annotations.EnableSwagger2")
  }


  def generate(nameSpaces: NameSpaces, service: Service): Seq[File] = {
    val className = ClassName.get(nameSpaces.config.nameSpace, "SwaggerUiConfig")

    val hostFieldName = "host"
    val defaultHost = "http://localhost:8080"
    val hostJavadoc = s"In order to locate the openapi files swagger-ui needs to know the host. Sorry I did not build it. Defaults to ${defaultHost}"

    val builder = TypeSpec.classBuilder(className).addModifiers(PUBLIC)
      .addAnnotation(Configuration)
      .addAnnotation(EnableSwagger2)
      .addMethod(
        MethodSpec.constructorBuilder().addModifiers(PUBLIC)
          .addParameter(
            ParameterSpec.builder(JavaTypes.String, hostFieldName, FINAL)
              .addAnnotation(SpringTypes.Value(s"$${${service.name}.${hostFieldName}:${defaultHost}"))
              .addJavadoc(hostJavadoc)
              .build()
          )
          .addCode("this.$L=$L;", hostFieldName, hostFieldName)
          .build()
      )
      .addField(
        FieldSpec.builder(JavaTypes.String, hostFieldName, PRIVATE).addJavadoc(hostJavadoc).build()
      )
      .addMethod(
        MethodSpec.methodBuilder("api").addModifiers(PUBLIC).returns(Docket)
          .addAnnotation(Bean)
          .addJavadoc("Sets up the base Doclet for SpringFox Swagger-ui.\n @return The SpringFox Docket.")
          .addCode(
            CodeBlock.builder()
              .addStatement(
                "return new Docket($L.SWAGGER_2)" +
                  ".host($L)" +
                  ".forCodeGeneration(true)" +
                  ".select()" +
                  ".apis($L.any())" +
                  ".build()",
                DocumentationType,
                hostFieldName,
                RequestHandlerSelectors)
              .build()
          )
          .build()
      )
      .addMethod(
        MethodSpec.methodBuilder("swaggerResourcesProvider")
          .addJavadoc("Provides a bean for directing SpringFox to use generated openapi files as opposed to trying to use reflection to guess the api.\n @return A SwaggerResourcesProvider object that references generated openapi files. ")
          .addModifiers(PUBLIC)
          .addAnnotation(Bean)
          .addAnnotation(Primary)
          .returns(SpringFoxTypes.SwaggerResourcesProvider)
          .addCode(
            "return () -> {" +
              " $T resources = new $L<>();" +
              "  try {" +
              "    resources.add(getSwaggerResource(\"/$L/$L.yml\"));" +
              "  } catch ($T e) {" +
              "    e.printStackTrace();" +
              "  }" +
              "  return resources;" +
              "};",
            JavaTypes.List(SwaggerResource),
            JavaTypes.ArrayList,
            service.name,
            JavaPojoUtil.toClassName(service.name),
            JavaTypes.URISyntaxException
          )
          .build()
      )
      .addMethod(
        MethodSpec.methodBuilder("getSwaggerResource")
          .addModifiers(PRIVATE)
          .returns(SwaggerResource)
          .addException(JavaTypes.URISyntaxException)
          .addParameter(ParameterSpec.builder(JavaTypes.String, "file", FINAL).build())
          .addCode(
            "$T r = new $T();" +
              "r.setName(fileToResourceName(file));" +
              "r.setSwaggerVersion(\"3.0.2\");" +
              "r.setLocation(file);" +
              "return r;",
            SwaggerResource,
            SwaggerResource
          )
          .build()
      )
      .addMethod(
        MethodSpec.methodBuilder("fileToResourceName")
          .addModifiers(PRIVATE)
          .returns(JavaTypes.String)
          .addParameter(ParameterSpec.builder(JavaTypes.String, "file", FINAL).build())
          .addCode(
            "return $T.join(" +
              " file" +
              "   .substring(file.lastIndexOf('/') + 1, file.lastIndexOf('.'))" +
              "   .split(\"(?=\\\\p{Upper})\")" +
              ", \" \");",
            StringUtils
          )
          .build()


      )

    //    private String fileToResourceName(final String file) {
    //      return StringUtils.join(
    //        file
    //          .substring(file.lastIndexOf('/') + 1, file.lastIndexOf('.'))
    //          .split("(?=\\p{Upper})")
    //        , " ");
    //    }


    Seq(GeneratorFSUtil.makeFile(className.simpleName(), nameSpaces.config, builder))
  }


}
