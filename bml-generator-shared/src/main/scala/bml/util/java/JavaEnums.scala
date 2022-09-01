package bml.util.java

import bml.util.AnotationUtil.LombokAnno
import bml.util.java.ClassNames.{JacksonTypes, JavaTypes, LombokTypes}
import com.squareup.javapoet.{AnnotationSpec, _}
import io.apibuilder.spec.v0.models.Enum
import javax.lang.model.element.Modifier.{FINAL, PRIVATE, PUBLIC, STATIC}
import lib.Text

object JavaEnums {

  def stringValueParam = "apiValue"

  def descriptionParam = "description"

  def stringMapFieldName = toEnumName(stringValueParam + "Map")

  def genericDoc = List(
    "This enum has the ability to map string values to enum instances.",
    "This is required because this enum was generated from a definition ",
    "that has enumeration values that do not necessarily conform to java enum naming conventions",
    "",
    ""
  ).mkString("\n")

  /**
   * Translates an input value into an valid java enum name.
   *
   * @param input a value to translate into an enum name
   * @return input value translated in to a valid java enum name
   */
  def toEnumName(input: String): String = {
    var safe = Text.safeName(input.replaceAll("\\.", "_")
      .replaceAll("-", " ")
    )

    if (safe.equals(safe.toUpperCase)) {
      return safe
    }

    safe = "[A-Z\\d]".r.replaceAllIn(safe, m => "_" + m.group(0).toLowerCase())
    safe.toUpperCase().stripPrefix("_").replaceAll("__", "_")
  }

  def toEnumName(enum: Enum): String = {
    toEnumName(enum.name)
  }

  def standardEnumBuilder(enum: Enum, apiDocComments: String): TypeSpec.Builder = {
    val className = ClassName.bestGuess(JavaPojoUtil.toClassName(enum.name))
    val genericDoc = List(
      "This enum has the ability to map string values to enum instances.",
      "This is required because this enum was generated from a definition ",
      "that has enumeration values that do not necessarily conform to java enum naming conventions",
      ""
    )

    //enum.attributes.filter(ExampleAttribute.isThisAttribute).map(ExampleAttribute.asThisAttribute)


    TypeSpec.enumBuilder(className)
      .addModifiers(PUBLIC)
      .addJavadoc(apiDocComments)
      .addAnnotation(LombokAnno.AccessorFluent)
      //Add the api value field
      .addField(
        FieldSpec.builder(
          JavaMaps.mapStringTo(className), stringMapFieldName, PRIVATE, STATIC, FINAL)
          .addJavadoc("Maps the values used to generate this enum to specific enum instances.")
          .build()
      )
      //Add in the lookup map ofr api values
      .addStaticBlock(
        CodeBlock
          .builder()
          .add(CodeBlock.of("//Initialize $L to support lookup based on original enum value\n", stringMapFieldName))
          .addStatement("$T tmp = new $T<>()", JavaMaps.mapStringTo(className), JavaMaps.linkedHashMapClassName)
          .add(
            CodeBlock
              .builder()
              .add(CodeBlock.of("//Add all values and original strings to tmp\n", stringMapFieldName))
              .beginControlFlow("for ($T e : values())", className)
              .addStatement("tmp.put(e.$L,e)", stringValueParam)
              .endControlFlow()
              .build()
          ).addStatement("$L = $T.unmodifiableMap(tmp)", stringMapFieldName, JavaTypes.Collections)
          .build()
      )
      .addField(
        FieldSpec.builder(JavaTypes.String, stringValueParam, PRIVATE, FINAL)
          .addAnnotation(LombokAnno.Getter(LombokTypes.JsonValue)
          )
          .addJavadoc("Holder for defined value for lookup and toString support.\n")
          .build()
      )
      .addField(
        FieldSpec.builder(JavaTypes.String, descriptionParam, PRIVATE, FINAL)
          .addAnnotation(LombokTypes.Getter)
          .addJavadoc("Holder for defined enum field description.\n")
          .build()
      )
      .addMethod(
        MethodSpec.constructorBuilder()
          .addParameter(JavaTypes.String, stringValueParam, FINAL)
          .addParameter(JavaTypes.String, descriptionParam, FINAL)
          .addStatement(CodeBlock.of("this.$L=$L", stringValueParam, stringValueParam))
          .addStatement(CodeBlock.of("this.$L=$L", descriptionParam, descriptionParam))
          .build()
      )
      .addMethod(
        MethodSpec.methodBuilder("from" + stringValueParam.capitalize)
          .addModifiers(PUBLIC, STATIC)
          .addParameter(JavaTypes.String, stringValueParam, FINAL)
          .addStatement(CodeBlock.of("return $L.get($L)", stringMapFieldName, stringValueParam))
          .returns(className)
          .build()
      ).addMethod(
      MethodSpec
        .methodBuilder("toString")
        .addModifiers(PUBLIC, FINAL)
        .addAnnotation(JavaTypes.Override)
        .returns(JavaTypes.String)
        .addStatement(CodeBlock.of("return this.$L", stringValueParam))
        .build()
    ).addJavadoc("\n")
      .addJavadoc(enum.description.mkString("\n"))
      .addJavadoc("\n")
      .addJavadoc("\n")
      .addJavadoc(genericDoc.mkString("\n"))

      .addAnnotation(
        AnnotationSpec.builder(JacksonTypes.JsonDeserialize)
          .addMember("converter", "$T.Converters.ToEnum.class", className)
          .build()
      ).addAnnotation(
      AnnotationSpec.builder(JacksonTypes.JsonSerialize)
        .addMember("converter", "$T.Converters.ToString.class", className)
        .build()
    )
      .addType(makeConverters(className))

  }


  def makeConverters(enumClassName: ClassName): TypeSpec = {
    val convertersClassName = ClassName.get("", "Converters")

    TypeSpec.classBuilder(convertersClassName).addModifiers(PUBLIC, STATIC)
      .addAnnotation(LombokTypes.UtilityClass)
      .addAnnotation(LombokTypes.Generated)
      .addType(
        TypeSpec.classBuilder("ToEnum").addModifiers(PUBLIC, STATIC)
          .addAnnotation(LombokTypes.Generated)
          .addSuperinterface(ParameterizedTypeName.get(JacksonTypes.Converter, JavaTypes.String, enumClassName))
          .addMethod(
            MethodSpec.methodBuilder("convert")
              .addModifiers(PUBLIC)
              .addAnnotation(JavaTypes.Override)
              .returns(enumClassName)
              .addParameter(ParameterSpec.builder(JavaTypes.String, "apiValue", FINAL).build())
              .addStatement("return $T.fromApiValue(apiValue)", enumClassName)
              .build()
          ).addMethod(
          MethodSpec.methodBuilder("getInputType")
            .addModifiers(PUBLIC)
            .addAnnotation(JavaTypes.Override)
            .returns(JacksonTypes.JavaType)
            .addParameter(ParameterSpec.builder(JacksonTypes.TypeFactory, Text.initLowerCase(JacksonTypes.TypeFactory.simpleName()), FINAL).build())
            .addStatement("return $L.constructType($T.class)", Text.initLowerCase(JacksonTypes.TypeFactory.simpleName()), JavaTypes.String)
            .build()
        ).addMethod(
          MethodSpec.methodBuilder("getOutputType")
            .addModifiers(PUBLIC)
            .addAnnotation(JavaTypes.Override)
            .returns(JacksonTypes.JavaType)
            .addParameter(ParameterSpec.builder(JacksonTypes.TypeFactory, Text.initLowerCase(JacksonTypes.TypeFactory.simpleName()), FINAL).build())
            .addStatement("return $L.constructType($T.class)", Text.initLowerCase(JacksonTypes.TypeFactory.simpleName()), enumClassName)
            .build()
        ).build()
      )
      .addType(
        TypeSpec.classBuilder("ToString").addModifiers(PUBLIC, STATIC)
          .addAnnotation(LombokTypes.Generated)
          .addSuperinterface(ParameterizedTypeName.get(JacksonTypes.Converter, enumClassName, JavaTypes.String))
          .addMethod(
            MethodSpec.methodBuilder("convert")
              .addModifiers(PUBLIC)
              .addAnnotation(JavaTypes.Override)
              .returns(JavaTypes.String)
              .addParameter(ParameterSpec.builder(enumClassName, "enumValue", FINAL).build())
              .addStatement("return $L.apiValue()", "enumValue")
              .build()
          ).addMethod(
          MethodSpec.methodBuilder("getInputType")
            .addModifiers(PUBLIC)
            .addAnnotation(JavaTypes.Override)
            .returns(JacksonTypes.JavaType)
            .addParameter(ParameterSpec.builder(JacksonTypes.TypeFactory, Text.initLowerCase(JacksonTypes.TypeFactory.simpleName()), FINAL).build())
            .addStatement("return $L.constructType($T.class)", Text.initLowerCase(JacksonTypes.TypeFactory.simpleName()), enumClassName)
            .build()
        ).addMethod(
          MethodSpec.methodBuilder("getOutputType")
            .addModifiers(PUBLIC)
            .addAnnotation(JavaTypes.Override)
            .returns(JacksonTypes.JavaType)
            .addParameter(ParameterSpec.builder(JacksonTypes.TypeFactory, Text.initLowerCase(JacksonTypes.TypeFactory.simpleName()), FINAL).build())
            .addStatement("return $L.constructType($T.class)", Text.initLowerCase(JacksonTypes.TypeFactory.simpleName()), JavaTypes.String)
            .build()
        ).build()
      ).build()


  }


}
