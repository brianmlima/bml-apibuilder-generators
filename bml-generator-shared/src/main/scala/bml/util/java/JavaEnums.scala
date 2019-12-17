package bml.util.java

import bml.util.AnotationUtil
import bml.util.java.ClassNames.JavaTypes
import com.squareup.javapoet._
import io.apibuilder.spec.v0.models.Enum
import javax.lang.model.element.Modifier.{FINAL, PRIVATE, PUBLIC, STATIC}
import lib.Text
import lombok.Getter

object JavaEnums {

  def stringValueParam = "apiValue"

  def stringMapFieldName = toEnumName(stringValueParam + "_Map")

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
    Text.safeName(input.replaceAll("\\.", "_").replaceAll("-", "_")).toUpperCase
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
      .addAnnotation(AnotationUtil.fluentAccessor)
      //Add the api value field
      .addField(
        FieldSpec.builder(
          JavaMaps.mapStringTo(className), stringMapFieldName, PRIVATE, STATIC, FINAL)
          .addJavadoc("Maps the values used to generate this enum to specific enum instances")
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
        FieldSpec.builder(classOf[String], stringValueParam, PRIVATE, FINAL)
          .addAnnotation(classOf[Getter])
          .addJavadoc("Holder for defined value for lookup and toString support\n")
          .build()
      )
      .addMethod(
        MethodSpec.constructorBuilder()
          .addParameter(classOf[String], stringValueParam, FINAL)
          .addStatement(CodeBlock.of("this.$L=$L", stringValueParam, stringValueParam))
          .build()
      )
      .addMethod(
        MethodSpec.methodBuilder("from" + stringValueParam.capitalize)
          .addModifiers(PUBLIC, FINAL)
          .addParameter(classOf[String], stringValueParam, FINAL)
          .addStatement(CodeBlock.of("return $L.get($L)", stringMapFieldName, stringValueParam))
          .returns(className)
          .build()
      ).addMethod(
      MethodSpec
        .methodBuilder("toString")
        .addModifiers(PUBLIC, FINAL)
        .addAnnotation(classOf[Override])
        .returns(classOf[String])
        .addStatement(CodeBlock.of("return this.$L", stringValueParam))
        .build()
    ).addJavadoc("\n")
      .addJavadoc(enum.description.mkString("\n"))
      .addJavadoc("\n")
      .addJavadoc("\n")
      .addJavadoc(genericDoc.mkString("\n"))
  }


}
