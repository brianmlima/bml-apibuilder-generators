package bml.util.java

import akka.http.scaladsl.model.headers.CacheDirectives.public
import bml.util.AnotationUtil.fluentAccessor
import bml.util.GeneratorFSUtil.makeFile
import bml.util.{AnotationUtil, NameSpaces}
import bml.util.java.ClassNames.{getter, illegalArgumentException, locale, loremIpsum, math, randomUtils, slf4j, string, stringBuilder, supplier, utilityClass, uuid}
import bml.util.java.JavaPojoTestFixtures.languagesClassName
import com.squareup.javapoet.MethodSpec.methodBuilder
import com.squareup.javapoet.TypeName.{DOUBLE, INT}
import com.squareup.javapoet.{ClassName, CodeBlock, FieldSpec, MethodSpec, ParameterSpec, ParameterizedTypeName, TypeName, TypeSpec, TypeVariableName}
import io.apibuilder.generator.v0.models.File
import javax.lang.model.element.Modifier._
import lib.Text
import lib.Text.initCap

import collection.JavaConverters._

object TestSuppliers {

  def testSuppliersClassName(nameSpaces: NameSpaces) = ClassName.get(nameSpaces.tool.nameSpace, "TestSuppliers")

  def recallSupplierClassName(nameSpaces: NameSpaces) = ClassName.get(nameSpaces.tool.nameSpace, "RecallSupplier")


  def t = TypeVariableName.get("T")


  private class Param(val parameterSpec: ParameterSpec, javadocString: String) {
    val javadoc = s"@param ${parameterSpec.name} ${javadocString}"
    val name = parameterSpec.name
    val fieldSpec = FieldSpec.builder(parameterSpec.`type`, name, PRIVATE)
      .addJavadoc(javadocString)
      .addAnnotation(getter)
      .build()

    def fieldSpecFinal = fieldSpec.toBuilder.addModifiers(FINAL).build()
  }

  private val supplierParam = new Param(ParameterSpec.builder(ParameterizedTypeName.get(supplier, t), "supplier", FINAL).build(), "")
  private val lastValueParam = new Param(ParameterSpec.builder(t, "lastValue").build(), "")

  def testSuppliers(nameSpaces: NameSpaces): File = {
    val className = testSuppliersClassName(nameSpaces)
    val recallSupplierClass = recallSupplierClassName(nameSpaces)
    val typeBuilder = TypeSpec.classBuilder(className)
      .addAnnotation(slf4j)
      .addMethod(
        MethodSpec.methodBuilder("wrapRecall").addModifiers(PUBLIC, STATIC)
          .addTypeVariable(t)
          .returns(ParameterizedTypeName.get(ClassName.get("", recallSupplierClass.simpleName()), t))
          .addParameter(supplierParam.parameterSpec)
          .addStatement("return new $L($L)", recallSupplierClass.simpleName(), supplierParam.name)
          .build()
      )
      .addMethod(
        MethodSpec.methodBuilder("uuidSupplier").addModifiers(PUBLIC, STATIC)
          .returns(ParameterizedTypeName.get(supplier, uuid))
          .addStatement("return () -> $T.randomUUID()", uuid)
          .build()
      )
      .addMethod(generateRangeStringSupplier(nameSpaces))
      .addType(recallSupplier(nameSpaces))
    makeFile(className.simpleName(), nameSpaces.tool, typeBuilder)
  }


  private def recallSupplier(nameSpaces: NameSpaces): TypeSpec = {
    val className = recallSupplierClassName(nameSpaces)
    TypeSpec.classBuilder(className).addModifiers(PUBLIC, STATIC)
      .addTypeVariable(t)
      .addAnnotation(fluentAccessor)
      .addSuperinterface(ParameterizedTypeName.get(supplier, t))
      .addFields(Seq(supplierParam.fieldSpecFinal, lastValueParam.fieldSpec).asJava)
      .addMethod(
        MethodSpec.constructorBuilder().addModifiers(PUBLIC)
          .addParameter(supplierParam.parameterSpec)
          .addStatement("this.$L = $L", supplierParam.name, supplierParam.name)
          .build()
      )
      .addMethod(
        MethodSpec.methodBuilder("get").addModifiers(PUBLIC).returns(t)
          .addCode(
            CodeBlock.builder()
              .addStatement("$L = $L.get()", lastValueParam.name, supplierParam.name)
              .addStatement("return $L", lastValueParam.name)
              .build()
          ).build()
      ).build()
  }


  val stringRangeSupplierMethodName = "stringRangeSupplier"

  def generateRangeStringSupplier(nameSpaces: NameSpaces): MethodSpec = {
    val min = "min"
    val max = "max"

    MethodSpec.methodBuilder(stringRangeSupplierMethodName)
      .addModifiers(PUBLIC, STATIC)
      .returns(supplier(string))
      .addParameter(LoremTooling.localeParam.spec)
      .addParameter(LoremTooling.minParam.spec)
      .addParameter(LoremTooling.maxParam.spec)

      .addCode(
        CodeBlock.builder()
          .beginControlFlow("if ($L < $L)", max, min)
          .addStatement(
            "throw new $T($T.format(\"$L param can not be less than min param. $L={} $L={}\", $L, $L))",
            illegalArgumentException, string, max, min, max, min, max
          ).endControlFlow().build()
      )
      .addCode(
        CodeBlock.builder()
          .beginControlFlow("if ($L == 0 && $L == 0)", min, max)
          .addStatement(
            "throw new $T(\"$L param and $L param can not both be 0\")",
            illegalArgumentException, max, min
          ).endControlFlow().build()
      )
      .addCode(
        CodeBlock.builder()
          .beginControlFlow("if ($L < 0)", min)
          .addStatement(
            "throw new $T($T.format(\"$L aram can not be less than 0. $L={} $L={}\", $L, $L))",
            illegalArgumentException, string, min, min, max, min, max
          ).endControlFlow().build()
      )
      .addCode(
        CodeBlock.builder()
          .beginControlFlow("if ($L < 0)", max)
          .addStatement(
            "throw new $T($T.format(\"$L aram can not be less than 0. $L={} $L={}\", $L, $L))",
            illegalArgumentException, string, max, min, max, min, max
          ).endControlFlow().build()
      )
      .addCode(
        CodeBlock.builder()
          .add("return () -> {\n")
          .addStatement("int requestWordCount = (int) $T.ceil(max / $T.ENGLISH_AVG_WORD_LENGTH)", math, languagesClassName(nameSpaces))
          .addStatement("final $T[] words = $T.getWords(locale, requestWordCount).split(\"[ ]\")", string, LoremTooling.loremToolClassName(nameSpaces))
          .addStatement("final int randStringLength = $T.nextInt(min, max)", randomUtils)
          .addStatement("final $T buff = new $T()", stringBuilder, stringBuilder)
          .beginControlFlow("for ($T word : words)", string)
          .beginControlFlow("if (buff.length() <= randStringLength)")
          .addStatement("buff.append(word)")
          .endControlFlow()
          .add("else {break;}")
          .endControlFlow()
          .addStatement("$T returnValue = (buff.length() > randStringLength) ? buff.toString().substring(0, randStringLength).trim() : buff.toString()", string)
          .addStatement("log.debug(\"Returning Lorem String length={}\", returnValue.length())")
          .addStatement("return returnValue")
          .addStatement("}")
          .build()
      ).build()
  }
}
