package bml.util.java.client

import bml.util.AnotationUtil.LombokAnno
import bml.util.java.ClassNames.{JavaTypes, LombokTypes, SpringTypes}
import bml.util.java.JavaPojoUtil
import bml.util.NameSpaces
import com.squareup.javapoet.{ClassName, CodeBlock, FieldSpec, MethodSpec, ParameterSpec, TypeName, TypeSpec}
import io.apibuilder.generator.v0.models.File
import io.apibuilder.spec.v0.models.Service

object JavaClients {

  import javax.lang.model.element.Modifier._
  import collection.JavaConverters._
  import bml.util.GeneratorFSUtil.makeFile


  def toClientClassName(service: Service, nameSpaces: NameSpaces): ClassName = {
    ClassName.get(nameSpaces.client.nameSpace, JavaPojoUtil.toClassName(service.name) + "Client")
  }

  val restTemplateFieldName = "restTemplate"
  val hostFieldName = "host"
  val portFieldName = "port"

  val configFieldName = "config"


  def generateClient(service: Service, nameSpaces: NameSpaces): Seq[File] = {
    val clientClassName = toClientClassName(service, nameSpaces);
    val configClassName = ClassName.get("", clientClassName.simpleName() + "Config")

    //    val restTemplateConstructor = MethodSpec.constructorBuilder()
    //      .addModifiers(PUBLIC)
    //      .addParameters(
    //        Seq(
    //          ParameterSpec.builder(configClassName, hostFieldName, FINAL).build(),
    //          ParameterSpec.builder(TypeName.INT, portFieldName, FINAL).build(),
    //          ParameterSpec.builder(SpringTypes.RestTemplate, restTemplateFieldName, FINAL).build()
    //        ).asJava
    //      )
    //      .addCode(
    //        CodeBlock.builder()
    //          .addStatement("this.$L=$L", hostFieldName, hostFieldName)
    //          .addStatement("this.$L=$L", portFieldName, portFieldName)
    //          .addStatement("this.$L=$L", restTemplateFieldName, restTemplateFieldName)
    //          .build()
    //      )
    //      .build()


    //        val minimalConstructor = MethodSpec.constructorBuilder()
    //          .addModifiers(PUBLIC)
    //          .addParameters(
    //            Seq(
    //              ParameterSpec.builder(JavaTypes.String, hostFieldName, FINAL).build(),
    //              ParameterSpec.builder(TypeName.INT, portFieldName, FINAL).build()
    //            ).asJava
    //          )
    //          .addStatement("this($L,$L,new $T())", hostFieldName, portFieldName, SpringTypes.RestTemplate)
    //          .build()


    val configType = TypeSpec.classBuilder(configClassName)
      .addAnnotation(LombokTypes.Builder)
      .addAnnotation(LombokAnno.fluentAccessor)
      .addFields(
        Seq(
          FieldSpec.builder(SpringTypes.RestTemplate, restTemplateFieldName, PRIVATE)
            .addAnnotation(LombokTypes.Getter)
            .addAnnotation(LombokTypes.BuilderDefault)
            .initializer("new $T()", SpringTypes.RestTemplate)
            .build(),
          FieldSpec.builder(JavaTypes.String, hostFieldName, PRIVATE, FINAL)
            .addAnnotation(LombokTypes.Getter)
            .build(),
          FieldSpec.builder(TypeName.INT, portFieldName, PRIVATE, FINAL)
            .addAnnotation(LombokTypes.Getter)
            .build()
        )
          .asJava
      ).build()

    val stdClientFields = Seq(
      FieldSpec.builder(configClassName, configFieldName, PRIVATE, FINAL)
        .addAnnotation(LombokTypes.Getter)
        .build()
    )

    val clientSpec = TypeSpec.classBuilder(clientClassName)
      .addAnnotations(
        Seq(
          LombokAnno.fluentAccessor,
          LombokAnno.Slf4j
        ).asJava
      )
      .addFields(stdClientFields.asJava)
      //      .addMethod(
      //        restTemplateConstructor.toBuilder()
      //          .addStatement(
      //            CodeBlock.join(
      //              service.resources.map(
      //                resource => {
      //                  CodeBlock.of(
      //                    "this.$L = new $T($L,$L,$L)",
      //                    JavaPojoUtil.toFieldName(resource.`type`),
      //                    ClassName.get("", JavaPojoUtil.toClassName(resource.`type`) + "Client"),
      //                    hostFieldName, portFieldName, restTemplateFieldName)
      //                }
      //              ).asJava, ";"
      //            )
      //          ).build()
      //      )
      //      .addMethod(minimalConstructor)

      .addFields(
        service.resources.map(
          resource => {
            val className = ClassName.get("", JavaPojoUtil.toClassName(resource.`type`) + "Client")
            FieldSpec.builder(className, JavaPojoUtil.toFieldName(resource.`type`), PRIVATE, FINAL)
              .build()
          }
        ).asJava

      )
      .addType(configType)

      .addTypes(
        service.resources.map(
          resource =>
            TypeSpec.classBuilder(JavaPojoUtil.toClassName(resource.`type`) + "Client")
              .addAnnotations(
                Seq(
                  LombokAnno.fluentAccessor,
                  LombokAnno.Slf4j
                ).asJava
              )
              //.addFields(stdClientFields.asJava)
              //.addMethod(restTemplateConstructor)
              .build()
        ).asJava
      )


    Seq(makeFile(clientClassName.simpleName(), nameSpaces.client, clientSpec))
  }


}
