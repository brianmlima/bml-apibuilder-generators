package bml.util.spring

import bml.util.java.ClassNames.{JavaTypes, SpringTypes}
import bml.util.java.{ClassNames, JavaPojoUtil}
import bml.util.spring.SpringVersion.SpringVersion
import bml.util.{GeneratorFSUtil, NameSpaces, ServiceTool}
import com.squareup.javapoet._
import io.apibuilder.generator.v0.models.File
import io.apibuilder.spec.v0.models.{Enum, Service}
import javax.lang.model.element.Modifier


class StringToEnumConverters {

}


object StringToEnumConverters {

  import collection.JavaConverters._

  def toClassName(enum: Enum): String = {
    s"StringTo${JavaPojoUtil.toClassName(enum.name)}Converter"
  }

  def toClassName(nameSpaces: NameSpaces, enum: io.apibuilder.spec.v0.models.Enum): ClassName = {
    return ClassName.get(nameSpaces.converter.nameSpace, toClassName(enum))
  }


  def enumConverters(service: Service, nameSpaces: NameSpaces, enums: Seq[Enum]): Seq[File] = {
    enums.map(enumConverter(service, nameSpaces, _)) ++
      makeConvertersConfigClass(SpringVersion.FIVE, service, nameSpaces, enums)
  }

  def enumConverters(springVersion: SpringVersion, service: Service, nameSpaces: NameSpaces, enums: Seq[Enum]): Seq[File] = {
    enums.map(enumConverter(service, nameSpaces, _)) ++
      makeConvertersConfigClass(springVersion, service, nameSpaces, enums)
  }


  def makeConvertersConfigClass(springVersion: SpringVersion, service: Service, nameSpaces: NameSpaces, enums: Seq[Enum]): Seq[File] = {
    if (enums.isEmpty) {
      return Seq[File]()
    }


    val prefix = springVersion match {
      case bml.util.spring.SpringVersion.SIX => ServiceTool.versionedPrefix(service)
      case bml.util.spring.SpringVersion.FIVE => ""
    }

    val className = ClassName.get(nameSpaces.converter.nameSpace, s"${prefix}ConfigureConverters");
    val builder = TypeSpec.classBuilder(className)
      .addAnnotation(SpringTypes.Configuration)
      .addSuperinterface(ClassNames.webMvcConfigurer)
      .addMethod(
        MethodSpec.methodBuilder("addFormatters")
          .addModifiers(Modifier.PUBLIC)
          .addAnnotation(JavaTypes.Override)
          .addParameter(
            ParameterSpec.builder(SpringTypes.FormatterRegistry, "registry", Modifier.FINAL).build()
          )
          .addCode(
            CodeBlock.join(
              enums.map(toClassName(nameSpaces, _)).map(CodeBlock.of("registry.addConverter(new $T());", _)).asJava,
              " "
            )
          )
          .build()
      )
    Seq(GeneratorFSUtil.makeFile(className.simpleName(), nameSpaces.converter, builder))
  }

  def enumConverter(service: Service, nameSpaces: NameSpaces, enum: Enum): File = {

    val className = toClassName(nameSpaces, enum);


    val enumDataType = JavaPojoUtil.dataTypeFromField(service, `enum`.name, nameSpaces.model)

    val builder = TypeSpec.classBuilder(className)
      .addModifiers(Modifier.PUBLIC)
      .addSuperinterface(ParameterizedTypeName.get(SpringTypes.Converter, JavaTypes.String, enumDataType))
      .addMethod(
        MethodSpec.methodBuilder("convert")
          .addModifiers(Modifier.PUBLIC)
          .addAnnotation(JavaTypes.Override)
          .returns(enumDataType)
          .addParameter(ParameterSpec.builder(JavaTypes.String, "apiValue", Modifier.FINAL).build())
          .addStatement("return $T.fromApiValue($L)", enumDataType, "apiValue")
          .build()
      )
    GeneratorFSUtil.makeFile(className.simpleName(), nameSpaces.converter, builder)
  }


}
