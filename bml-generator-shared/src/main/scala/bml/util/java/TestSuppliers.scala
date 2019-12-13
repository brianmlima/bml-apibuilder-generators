package bml.util.java

import akka.http.scaladsl.model.headers.CacheDirectives.public
import bml.util.AnotationUtil.fluentAccessor
import bml.util.GeneratorFSUtil.makeFile
import bml.util.{AnotationUtil, NameSpaces, Param}
import bml.util.java.ClassNames.{getter, illegalArgumentException, locale, loremIpsum, math, randomUtils, slf4j, string, stringBuilder, supplier, utilityClass, uuid}
import bml.util.java.JavaPojoTestFixtures.languagesClassName
import com.squareup.javapoet.MethodSpec.methodBuilder
import com.squareup.javapoet.TypeName.{DOUBLE, INT}
import com.squareup.javapoet.{ClassName, CodeBlock, FieldSpec, MethodSpec, ParameterSpec, ParameterizedTypeName, TypeName, TypeSpec, TypeVariableName}
import io.apibuilder.generator.v0.models.File
import javax.lang.model.element.Modifier
import javax.lang.model.element.Modifier._
import lib.Text
import lib.Text.initCap

import collection.JavaConverters._

object TestSuppliers {


  class TestSupplierInfo(val classStringName: String, val methodName: String) {
    def className(nameSpaces: NameSpaces) = ClassName.get(nameSpaces.tool.nameSpace + ".TestSuppliers", classStringName)

  }

  def testSuppliersClassName(nameSpaces: NameSpaces) = ClassName.get(nameSpaces.tool.nameSpace, "TestSuppliers")

  def recallSupplierClassName(nameSpaces: NameSpaces) = ClassName.get(nameSpaces.tool.nameSpace + ".TestSuppliers", "RecallSupplier")

  def booleanSupplierClassName(nameSpaces: NameSpaces) = ClassName.get(nameSpaces.tool.nameSpace + ".TestSuppliers", "BooleanSupplier")

  def localDateSupplierClassName(nameSpaces: NameSpaces) = ClassName.get(nameSpaces.tool.nameSpace + ".TestSuppliers", "LocalDateSupplier")

  def integerSupplierClassName(nameSpaces: NameSpaces) = ClassName.get(nameSpaces.tool.nameSpace + ".TestSuppliers", "IntegerSupplier")

  val uuidSupplierMethodName = "uuidSupplier"

  val wrapRecallMethodName = "wrapRecall"
  val wrapProbNullMethodName = "wrapProbNull"
  val booleanSupplierMethodName = "booleanSupplier"
  val localDateSupplierMethodName = "localDateSupplier"
  val integerSupplierMethodName = "integerSupplier"


  def t = TypeVariableName.get("T")

  private val supplierParam = new Param(ParameterSpec.builder(ParameterizedTypeName.get(supplier, t), "supplier", FINAL).build(), "")
  private val lastValueParam = new Param(ParameterSpec.builder(t, "lastValue").build(), "")

  def testSuppliers(nameSpaces: NameSpaces): File = {
    val className = testSuppliersClassName(nameSpaces)
    val recallSupplierClass = recallSupplierClassName(nameSpaces)
    val typeBuilder = TypeSpec.classBuilder(className).addModifiers(PUBLIC)
      .addAnnotation(slf4j)
      .addMethod(
        MethodSpec.methodBuilder(wrapRecallMethodName).addModifiers(PUBLIC, STATIC)
          .addTypeVariable(t)
          .returns(ParameterizedTypeName.get(ClassName.get("", recallSupplierClass.simpleName()), t))
          .addParameter(supplierParam.parameterSpec)
          .addStatement("return new $L($L)", recallSupplierClass.simpleName(), supplierParam.name)
          .build()
      )
      .addMethod(
        MethodSpec.methodBuilder(wrapProbNullMethodName).addModifiers(PUBLIC, STATIC)
          .addTypeVariable(t)
          .returns(ParameterizedTypeName.get(ClassName.get("", probNullSupplierClassName(nameSpaces).simpleName()), t))
          .addParameters(
            Seq(TestSuppliers.supplierParam, ProbabilityTools.probParam).map(_.parameterSpec).asJava
          )
          .addStatement(
            "return new $L($L,$L)",
            probNullSupplierClassName(nameSpaces).simpleName(),
            TestSuppliers.supplierParam.name,
            ProbabilityTools.probParam.name
          )
          .build()
      )
      .addMethod(
        MethodSpec.methodBuilder(uuidSupplierMethodName).addModifiers(PUBLIC, STATIC)
          .returns(ParameterizedTypeName.get(supplier, uuid))
          .addStatement("return () -> $T.randomUUID()", uuid)
          .build()
      )
      .addMethod(
        MethodSpec.methodBuilder(booleanSupplierMethodName).addModifiers(PUBLIC, STATIC)
          .returns(ParameterizedTypeName.get(supplier, ClassNames.`boolean`))
          .addStatement("return new $T()", ClassName.get("", booleanSupplierClassName(nameSpaces).simpleName()))
          .build()
      )
      .addMethod(
        MethodSpec.methodBuilder(localDateSupplierMethodName).addModifiers(PUBLIC, STATIC)
          .returns(ParameterizedTypeName.get(supplier, ClassNames.localDate))
          .addStatement("return new $T()", ClassName.get("", localDateSupplierClassName(nameSpaces).simpleName()))
          .build()
      )
      .addMethod(
        MethodSpec.methodBuilder(integerSupplierMethodName).addModifiers(PUBLIC, STATIC)
          .returns(ParameterizedTypeName.get(supplier, ClassNames.integer))
          .addStatement("return new $T()", ClassName.get("", integerSupplierClassName(nameSpaces).simpleName()))
          .build()
      )
      .addMethod(generateRangeStringSupplier(nameSpaces))
      .addType(booleanSupplier(nameSpaces))
      .addType(recallSupplier(nameSpaces))
      .addType(probNullSupplier(nameSpaces))
      .addType(localDateSupplier(nameSpaces))
      .addType(integerSupplier(nameSpaces))
    makeFile(className.simpleName(), nameSpaces.tool, typeBuilder)
  }


  private def booleanSupplier(nameSpaces: NameSpaces): TypeSpec = {
    val className = booleanSupplierClassName(nameSpaces)
    TypeSpec.classBuilder(className).addModifiers(PUBLIC, STATIC)
      .addSuperinterface(ParameterizedTypeName.get(supplier, ClassNames.`boolean`))
      .addField(FieldSpec.builder(ClassNames.random, "random").initializer("new $T()", ClassNames.random).build())
      .addMethod(
        MethodSpec.methodBuilder("get").addModifiers(PUBLIC).returns(ClassNames.`boolean`)
          .addCode(
            CodeBlock.builder()
              .addStatement("return random.nextBoolean()")
              .build()
          ).build()
      ).build()
  }

  private def integerSupplier(nameSpaces: NameSpaces): TypeSpec = {
    val className = integerSupplierClassName(nameSpaces)
    TypeSpec.classBuilder(className).addModifiers(PUBLIC, STATIC)
      .addSuperinterface(ParameterizedTypeName.get(supplier, ClassNames.integer))
      .addField(FieldSpec.builder(ClassNames.random, "random").initializer("new $T()", ClassNames.random).build())
      .addMethod(
        MethodSpec.methodBuilder("get").addModifiers(PUBLIC).returns(ClassNames.integer)
          .addCode(
            CodeBlock.builder()
              .addStatement("return random.nextInt()")
              .build()
          ).build()
      ).build()
  }

  private def localDateSupplier(nameSpaces: NameSpaces): TypeSpec = {
    val className = localDateSupplierClassName(nameSpaces)
    TypeSpec.classBuilder(className).addModifiers(PUBLIC, STATIC)
      .addSuperinterface(ParameterizedTypeName.get(supplier, ClassNames.localDate))
      .addField(FieldSpec.builder(ClassNames.random, "random").initializer("new $T()", ClassNames.random).build())
      .addMethod(
        MethodSpec.methodBuilder("get").addModifiers(PUBLIC).returns(ClassNames.localDate)
          .addCode(
            CodeBlock.builder()
              .addStatement("long minDay = $T.of(1970, 1, 1).toEpochDay()", ClassNames.localDate)
              .addStatement("long maxDay = $T.of(2015, 12, 31).toEpochDay()", ClassNames.localDate)
              .addStatement("long randomDay = $T.current().nextLong(minDay, maxDay)", ClassNames.threadLocalRandom)
              .addStatement("return $T.ofEpochDay(randomDay)", ClassNames.localDate)
              .build()
          ).build()
      ).build()
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

  private def probNullSupplierClassName(nameSpaces: NameSpaces) = ClassName.get(nameSpaces.tool.nameSpace, "ProbabilityNullSupplier")

  private def probNullSupplier(nameSpaces: NameSpaces): TypeSpec = {
    val className = probNullSupplierClassName(nameSpaces)
    TypeSpec.classBuilder(className).addModifiers(Modifier.PUBLIC, Modifier.STATIC)
      .addTypeVariable(t)
      .addAnnotation(fluentAccessor)
      .addSuperinterface(ParameterizedTypeName.get(supplier, t))
      .addFields(
        Seq(TestSuppliers.supplierParam, ProbabilityTools.probParam).map(_.fieldSpecFinal).asJava
      )
      .addMethod(
        MethodSpec.constructorBuilder().addModifiers(PUBLIC)
          .addParameters(
            Seq(TestSuppliers.supplierParam, ProbabilityTools.probParam).map(_.parameterSpec).asJava
          )
          .addStatement("this.$L = $L", TestSuppliers.supplierParam.name, TestSuppliers.supplierParam.name)
          .addStatement("this.$L = $L", ProbabilityTools.probParam.name, ProbabilityTools.probParam.name)
          .build()
      )
      .addMethod(
        MethodSpec.methodBuilder("get").addModifiers(PUBLIC).returns(t)
          .addCode(
            CodeBlock.builder()
              .addStatement("if ( $T.$L( $L ) ) return null", ProbabilityTools.probabilityToolClassName(nameSpaces), ProbabilityTools.shouldNullMethodName, ProbabilityTools.probParam.name)
              .addStatement("return $L.get()", TestSuppliers.supplierParam.name)
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
      .addParameter(LoremTooling.localeParam.parameterSpec)
      .addParameter(LoremTooling.minParam.parameterSpec)
      .addParameter(LoremTooling.maxParam.parameterSpec)

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
