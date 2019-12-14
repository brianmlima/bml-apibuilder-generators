package bml.util.java

import akka.http.scaladsl.model.headers.CacheDirectives.public
import bml.util.AnotationUtil.fluentAccessor
import bml.util.GeneratorFSUtil.makeFile
import bml.util.{AnotationUtil, NameSpaces, Param}
import bml.util.java.ClassNames.{getter, illegalArgumentException, locale, loremIpsum, math, randomUtils, slf4j, string, stringBuilder, supplier, utilityClass, uuid}
import bml.util.java.JavaPojoTestFixtures.languagesClassName
import bml.util.java.TestSuppliers.{TestSupplierInfo, `boolean`, integerSupplier}
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


  class TestSupplierInfo(val simpleName: String, val suppliedType: ClassName, methodNameOption: Option[String] = None) {
    def className(nameSpaces: NameSpaces) = ClassName.get(nameSpaces.tool.nameSpace + ".TestSuppliers", simpleName)

    val methodName = if (methodNameOption.isDefined) methodNameOption.get else Text.initLowerCase(simpleName)

    val simpleClassName = ClassName.get("", simpleName)

    val supplierParameterizedType = ClassNames.supplier(suppliedType)

    val emptyGetDefaultSupplierMethod = MethodSpec.methodBuilder(methodName).addModifiers(PUBLIC, STATIC)
      .returns(supplierParameterizedType)
      .addStatement("return new $T()", simpleClassName)
      .build()

  }

  def className(nameSpaces: NameSpaces) = ClassName.get(nameSpaces.tool.nameSpace, "TestSuppliers")

  private val recallSupplier = new TestSupplierInfo("RecallSupplier", ClassName.get("", "T"))
  private val probNull = new TestSupplierInfo("ProbabilityNullSupplier", ClassName.get("", "T"))
  private val `boolean` = new TestSupplierInfo("BooleanSupplier", ClassNames.`boolean`)
  private val localDate = new TestSupplierInfo("LocalDateSupplier", ClassNames.localDate)
  private val integer = new TestSupplierInfo("IntegerSupplier", ClassNames.integer)
  private val uuid = new TestSupplierInfo("UUIDSupplier", ClassNames.uuid, Some("UUIDSupplier"))


  //####################################################################################################################
  // BEGIN java class method names for use in java classes that need to refrence them ##################################
  object methods {
    val uuidSupplier = uuid.methodName
    val wrapProbNull = "wrapProbNull"
    val wrapRecall = recallSupplier.methodName
    val booleanSupplier = `boolean`.methodName
    val localDateSupplier = localDate.methodName
    val stringRangeSupplier = "stringRangeSupplier"
    val integerSupplier = integer.methodName
  }

  // END java class method names for use in java classes that need to refrence them ####################################
  //####################################################################################################################

  def t = TypeVariableName.get("T")

  private val supplierParam = new Param(ParameterSpec.builder(ParameterizedTypeName.get(supplier, t), "supplier", FINAL).build(), "")
  private val lastValueParam = new Param(ParameterSpec.builder(t, "lastValue").build(), "")

  private val randomField = FieldSpec.builder(ClassNames.random, "random").initializer("new $T()", ClassNames.random).build()


  def testSuppliers(nameSpaces: NameSpaces): File = {
    //##################################################################################################################
    // BEGIN Methods that expose suppliers #############################################################################
    val wrapRecallMethod = MethodSpec.methodBuilder(methods.wrapRecall).addModifiers(PUBLIC, STATIC)
      .addTypeVariable(t)
      .returns(recallSupplier.supplierParameterizedType)
      .addParameter(supplierParam.parameterSpec)
      .addStatement("return new $L($L)", recallSupplier.simpleClassName, supplierParam.name)
      .build()
    val wrapProbNullMethod = MethodSpec.methodBuilder(methods.wrapProbNull).addModifiers(PUBLIC, STATIC)
      .addTypeVariable(t)
      .returns(ParameterizedTypeName.get(ClassName.get("", probNull.className(nameSpaces).simpleName()), t))
      .addParameters(Seq(TestSuppliers.supplierParam, ProbabilityTools.probParam).map(_.parameterSpec).asJava)
      .addStatement(
        "return new $L($L,$L)",
        probNull.className(nameSpaces).simpleName(),
        TestSuppliers.supplierParam.name,
        ProbabilityTools.probParam.name
      )
      .build()
    //    val uuidSupplierMethod = MethodSpec.methodBuilder(uuid.methodName).addModifiers(PUBLIC, STATIC)
    //      .returns(uuid.supplierParameterizedType)
    //      .addStatement("return new $T()", uuid.simpleClassName)
    //      .build()
    //    val booleanSupplierMethod = MethodSpec.methodBuilder(`boolean`.methodName).addModifiers(PUBLIC, STATIC)
    //      .returns(ParameterizedTypeName.get(supplier, `boolean`.suppliedType))
    //      .addStatement("return new $T()", `boolean`.simpleClassName)
    //      .build()
    //    val localDateSupplierMethod = MethodSpec.methodBuilder(localDate.methodName).addModifiers(PUBLIC, STATIC)
    //      .returns(ParameterizedTypeName.get(supplier, localDate.suppliedType))
    //      .addStatement("return new $T()", localDate.simpleClassName)
    //      .build()

    val stdGetMethods = Seq(uuid, `boolean`, localDate, integer).map(
      testSupplierInfo =>
        MethodSpec.methodBuilder(testSupplierInfo.methodName).addModifiers(PUBLIC, STATIC)
          .returns(testSupplierInfo.supplierParameterizedType)
          .addStatement("return new $T()", testSupplierInfo.simpleClassName)
          .build()

    )

    // END Methods that expose suppliers ###############################################################################
    //##################################################################################################################


    val theClassName = className(nameSpaces)

    val typeBuilder = TypeSpec.classBuilder(theClassName).addModifiers(PUBLIC)
      .addAnnotation(slf4j)
      .addMethods(
        (Seq(
          wrapRecallMethod,
          wrapProbNullMethod,
          generateRangeStringSupplier(nameSpaces)
        ) ++ stdGetMethods).asJava
      )
      .addTypes(
        Seq(
          booleanSupplier(nameSpaces),
          recallSupplier(nameSpaces),
          probNullSupplier(nameSpaces),
          localDateSupplier(nameSpaces),
          integerSupplier(nameSpaces),
          uuidSupplier(nameSpaces)
        ).asJava
      )
    makeFile(theClassName.simpleName(), nameSpaces.tool, typeBuilder)
  }


  private def booleanSupplier(nameSpaces: NameSpaces): TypeSpec = {
    TypeSpec.classBuilder(`boolean`.className(nameSpaces)).addModifiers(PUBLIC, STATIC)
      .addSuperinterface(`boolean`.supplierParameterizedType)
      .addField(randomField)
      .addMethod(
        MethodSpec.methodBuilder("get").addModifiers(PUBLIC).returns(`boolean`.suppliedType)
          .addCode(
            CodeBlock.builder()
              .addStatement("return random.nextBoolean()")
              .build()
          ).build()
      ).build()
  }

  private def uuidSupplier(nameSpaces: NameSpaces): TypeSpec = {
    TypeSpec.classBuilder(uuid.className(nameSpaces)).addModifiers(PUBLIC, STATIC)
      .addSuperinterface(uuid.supplierParameterizedType)
      .addMethod(
        MethodSpec.methodBuilder("get").addModifiers(PUBLIC).returns(uuid.suppliedType)
          .addStatement("return $T.randomUUID()", uuid.suppliedType)
          .build()
      ).build()
  }


  private def integerSupplier(nameSpaces: NameSpaces): TypeSpec = {
    TypeSpec.classBuilder(integer.className(nameSpaces)).addModifiers(PUBLIC, STATIC)
      .addSuperinterface(integer.supplierParameterizedType)
      .addField(randomField)
      .addMethod(
        MethodSpec.methodBuilder("get").addModifiers(PUBLIC).returns(integer.suppliedType)
          .addCode(
            CodeBlock.builder()
              .addStatement("return random.nextInt()")
              .build()
          ).build()
      ).build()
  }

  private def localDateSupplier(nameSpaces: NameSpaces): TypeSpec =
    TypeSpec.classBuilder(localDate.className(nameSpaces)).addModifiers(PUBLIC, STATIC)
      .addSuperinterface(localDate.supplierParameterizedType)
      .addField(randomField)
      .addMethod(
        MethodSpec.methodBuilder("get").addModifiers(PUBLIC).returns(localDate.suppliedType)
          .addCode(
            CodeBlock.builder()
              .addStatement("long minDay = $T.of(1970, 1, 1).toEpochDay()", localDate.suppliedType)
              .addStatement("long maxDay = $T.of(2015, 12, 31).toEpochDay()", localDate.suppliedType)
              .addStatement("long randomDay = $T.current().nextLong(minDay, maxDay)", ClassNames.threadLocalRandom)
              .addStatement("return $T.ofEpochDay(randomDay)", localDate.suppliedType)
              .build()
          ).build()
      ).build()


  private def recallSupplier(nameSpaces: NameSpaces): TypeSpec = {
    TypeSpec.classBuilder(recallSupplier.className(nameSpaces)).addModifiers(PUBLIC, STATIC)
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


  private def probNullSupplier(nameSpaces: NameSpaces): TypeSpec = {
    TypeSpec.classBuilder(probNull.className(nameSpaces)).addModifiers(Modifier.PUBLIC, Modifier.STATIC)
      .addTypeVariable(t)
      .addAnnotation(fluentAccessor)
      .addSuperinterface(probNull.supplierParameterizedType)
      .addFields(Seq(supplierParam, ProbabilityTools.probParam).map(_.fieldSpecFinal).asJava)
      .addMethod(
        MethodSpec.constructorBuilder().addModifiers(PUBLIC)
          .addParameters(Seq(supplierParam, ProbabilityTools.probParam).map(_.parameterSpec).asJava)
          .addStatement("this.$L = $L", supplierParam.name, supplierParam.name)
          .addStatement("this.$L = $L", ProbabilityTools.probParam.name, ProbabilityTools.probParam.name)
          .build()
      )
      .addMethod(
        MethodSpec.methodBuilder("get").addModifiers(PUBLIC).returns(probNull.suppliedType)
          .addCode(
            CodeBlock.builder()
              .addStatement("if ( $T.$L( $L ) ) return null", ProbabilityTools.probabilityToolClassName(nameSpaces), ProbabilityTools.shouldNullMethodName, ProbabilityTools.probParam.name)
              .addStatement("$T result = $L.get()", t, supplierParam.name)
              .addStatement("log.trace(\"Returning {} class instance={}\", result.getClass().getSimpleName(),result)")
              .addStatement("return result")
              .build()
          ).build()
      ).build()
  }


  def generateRangeStringSupplier(nameSpaces: NameSpaces): MethodSpec = {
    val min = "min"
    val max = "max"

    MethodSpec.methodBuilder(methods.stringRangeSupplier)
      .addModifiers(PUBLIC, STATIC)
      .returns(supplier(string))
      .addParameters(Seq(
        LoremTooling.localeParam,
        LoremTooling.minParam,
        LoremTooling.maxParam
      ).map(_.parameterSpec).asJava)
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
          .add(
            CodeBlock
              .builder()
              .beginControlFlow("for ($T word : words)", string)
              .add(
                CodeBlock.builder()
                  .beginControlFlow("if (buff.length() <= randStringLength)")
                  .addStatement("buff.append(word)")
                  .endControlFlow()
                  .add("else {break;}")
                  .build()
              )
              .endControlFlow()
              .build()
          )
          .addStatement("$T returnValue = (buff.length() > randStringLength) ? buff.toString().substring(0, randStringLength).trim() : buff.toString()", string)
          .addStatement("log.trace(\"Returning Lorem String length={} text=\\\"{}\\\"\", returnValue.length(),returnValue)")
          .addStatement("return returnValue")
          .addStatement("}")
          .build()
      ).build()
  }
}
