package bml.util.java

import java.util.Locale
import java.util.regex.Pattern

import akka.http.scaladsl.model.headers.CacheDirectives.public
import bml.util.AnotationUtil.LombokAnno.AccessorFluent
import bml.util.java.ClassNames.{CommonsLangTypes, FakerTypes, JavaTypes, LombokTypes}
import bml.util.{NameSpaces, Param}
import com.squareup.javapoet._
import io.apibuilder.generator.v0.models.File


/**
 * Builds the TestSuppliers java class.
 */
object TestSuppliers {

  //import bml.util.java.ClassNames.{supplier}

  import bml.util.GeneratorFSUtil.makeFile
  import bml.util.java.ProbabilityTools.{probParam, probabilityToolClassName, shouldNullMethodName}
  import com.squareup.javapoet.MethodSpec.methodBuilder
  import com.squareup.javapoet.TypeSpec.classBuilder
  import javax.lang.model.element.Modifier._
  import lib.Text.initLowerCase

  import collection.JavaConverters._

  //####################################################################################################################
  // BEGIN PUBLIC ##################################


  /**
   * Provides a namespaced ClassName for TestSuppliers.
   *
   * @param nameSpaces NameSpaces for the service
   * @return a namespaced ClassName for TestSuppliers
   */
  def className(nameSpaces: NameSpaces) = ClassName.get(nameSpaces.tool.nameSpace, "TestSuppliers")

  //####################################################################################################################
  // BEGIN java class method names for use in java classes that need to refrence them ##################################
  /**
   * Encapsulates method names for generated classes.
   * Generated Java class method names for use in java classes that need to refrence them
   */
  object methods {
    val uuidSupplier = templates.uuid.methodName
    val wrapProbNull = "wrapProbNull"
    val wrapRecall = templates.recallSupplier.methodName
    val booleanSupplier = templates.`boolean`.methodName
    val localDateSupplier = templates.localDate.methodName
    val localDateTimeSupplier = templates.localDateTime.methodName
    val stringRangeSupplier = "stringRangeSupplier"
    val integerSupplier = templates.integer.methodName
    val longSupplier = templates.long.methodName
    val listSupplier = templates.list.methodName
    val patternedStringSupplier = patternedStringSupplierMethodName

  }

  // END java class method names for use in java classes that need to refrence them ####################################
  //####################################################################################################################

  def testSuppliers(nameSpaces: NameSpaces): File = {
    //##################################################################################################################
    // BEGIN Methods that expose suppliers #############################################################################
    val wrapRecallMethod = methodBuilder(methods.wrapRecall).addModifiers(PUBLIC, STATIC)
      .addTypeVariable(t)
      .returns(templates.recallSupplier.supplierParameterizedType)
      .addParameter(supplierParam.parameterSpec)
      .addStatement("return new $L($L)", templates.recallSupplier.simpleClassName, supplierParam.name)
      .build()
    val wrapProbNullMethod = methodBuilder(methods.wrapProbNull).addModifiers(PUBLIC, STATIC)
      .addTypeVariable(t)
      .returns(ParameterizedTypeName.get(ClassName.get("", templates.probNull.className(nameSpaces).simpleName()), t))
      .addParameters(Seq(supplierParam, probParam).map(_.parameterSpec).asJava)
      .addStatement("return new $L($L,$L)", templates.probNull.className(nameSpaces).simpleName(), supplierParam.name, probParam.name)
      .build()

    val listSupplierlMethod = methodBuilder(templates.list.methodName).addModifiers(PUBLIC, STATIC)
      .addTypeVariable(t)
      .returns(templates.list.supplierParameterizedType)
      .addParameters(Seq(supplierParam.parameterSpec, maxSizeParameter).asJava)
      .addStatement("return new $L($L,$L)", templates.list.className(nameSpaces).simpleName(), supplierParam.name, maxSizeParameter.name)
      .build()


    //generate the common ones with a template
    val stdGetMethods = Seq(templates.uuid, templates.`boolean`, templates.localDate, templates.localDateTime, templates.integer, templates.long).map(
      testSupplierInfo =>
        methodBuilder(testSupplierInfo.methodName).addModifiers(PUBLIC, STATIC)
          .returns(testSupplierInfo.supplierParameterizedType)
          .addStatement("return new $T()", testSupplierInfo.simpleClassName)
          .build()

    )

    // END Methods that expose suppliers ###############################################################################
    //##################################################################################################################


    //##################################################################################################################
    // BEGIN Make the class ############################################################################################
    val theClassName = className(nameSpaces)

    val typeBuilder = classBuilder(theClassName).addModifiers(PUBLIC)
      .addAnnotation(LombokTypes.Slf4j)
      .addField(fakerServiceStaticField())
      .addMethod(patternedStringSupplierMethod)
      .addMethods(
        (Seq(
          wrapRecallMethod,
          wrapProbNullMethod,
          listSupplierlMethod,

          generateRangeStringSupplier(nameSpaces)
        ) ++ stdGetMethods).asJava
      )
      .addTypes(
        Seq(
          booleanSupplier(nameSpaces),
          recallSupplier(nameSpaces),
          probNullSupplier(nameSpaces),
          localDateSupplier(nameSpaces),
          localDateTimeSupplier(nameSpaces),
          integerSupplier(nameSpaces),
          longSupplier(nameSpaces),
          uuidSupplier(nameSpaces),
          listSupplier(nameSpaces)
        ).asJava
      )
    // END Make the class ##############################################################################################
    //##################################################################################################################
    // Send back the File container
    makeFile(theClassName.simpleName(), nameSpaces.tool, typeBuilder)
  }


  val fakerServiceFieldName = "FAKER_SERVICE"

  def fakerServiceStaticField(): FieldSpec = {
    FieldSpec.builder(
      FakerTypes.FakeValuesService,
      fakerServiceFieldName,
      PUBLIC, STATIC, FINAL
    )
      .initializer(
        CodeBlock.of("new $T(new $T(\"en-US\"), new $T())", FakerTypes.FakeValuesService, classOf[Locale], FakerTypes.RandomService)
      ).build()
  }

  val patternedStringSupplierMethodName = "patternedStringSupplier"

  def patternedStringSupplierMethod(): MethodSpec = {
    MethodSpec.methodBuilder(patternedStringSupplierMethodName)
      .addModifiers(PUBLIC, STATIC, FINAL)
      .addParameter(ParameterSpec.builder(classOf[Pattern], "pattern", FINAL).build())
      .addCode(
        CodeBlock.of("return () -> $L.regexify(pattern.pattern());", fakerServiceFieldName)
      )
      .returns(JavaTypes.Supplier(ClassName.get(classOf[java.lang.String])))
      .build()
  }

  /**
   * Just a container util for keeping track of methods,classes,paths n such.
   *
   * @param simpleName       The simple name of the supplier implementation class
   * @param suppliedType     the type that is supplied
   * @param methodNameOption pass this to override the default which makes the method name using the class name
   */
  private class TestSupplierInfo(val simpleName: String, val suppliedType: TypeName, methodNameOption: Option[String] = None) {
    def className(nameSpaces: NameSpaces) = ClassName.get(nameSpaces.tool.nameSpace + ".TestSuppliers", simpleName)

    val methodName = if (methodNameOption.isDefined) methodNameOption.get else initLowerCase(simpleName)

    val simpleClassName = ClassName.get("", simpleName)

    val supplierParameterizedType = JavaTypes.supplier(suppliedType)

    val emptyGetDefaultSupplierMethod = methodBuilder(methodName).addModifiers(PUBLIC, STATIC)
      .returns(supplierParameterizedType)
      .addStatement("return new $T()", simpleClassName)
      .build()

  }

  private object templates {
    // These are used to template common methods
    val recallSupplier = new TestSupplierInfo("RecallSupplier", ClassName.get("", "T"))
    val probNull = new TestSupplierInfo("ProbabilityNullSupplier", ClassName.get("", "T"))

    val `boolean` = new TestSupplierInfo("BooleanSupplier", JavaTypes.`Boolean`)
    val localDate = new TestSupplierInfo("LocalDateSupplier", JavaTypes.LocalDate)
    val localDateTime = new TestSupplierInfo("LocalDateTimeSupplier", JavaTypes.LocalDateTime)
    val integer = new TestSupplierInfo("IntegerSupplier", JavaTypes.Integer)
    val long = new TestSupplierInfo("LongSupplier", JavaTypes.Long)
    val uuid = new TestSupplierInfo("UUIDSupplier", JavaTypes.UUID, Some("UUIDSupplier"))
    val list = new TestSupplierInfo("ListSupplier", JavaTypes.List(t))

  }

  // gotta have a T to do generic methods.
  private val t = TypeVariableName.get("T")


  private val supplierParam = new Param(ParameterSpec.builder(ParameterizedTypeName.get(JavaTypes.Supplier, t), "supplier", FINAL).build(), "")
  private val lastValueParam = new Param(ParameterSpec.builder(t, "lastValue").build(), "")

  private val randomField = FieldSpec.builder(JavaTypes.Random, "random").initializer("new $T()", JavaTypes.Random).build()

  private def booleanSupplier(nameSpaces: NameSpaces): TypeSpec = {
    classBuilder(templates.`boolean`.className(nameSpaces)).addModifiers(PUBLIC, STATIC)
      .addSuperinterface(templates.`boolean`.supplierParameterizedType)
      .addField(randomField)
      .addMethod(
        methodBuilder("get").addModifiers(PUBLIC).returns(templates.`boolean`.suppliedType)
          .addStatement("return random.nextBoolean()")
          .build()
      ).build()
  }

  private def uuidSupplier(nameSpaces: NameSpaces): TypeSpec = {
    classBuilder(templates.uuid.className(nameSpaces)).addModifiers(PUBLIC, STATIC)
      .addSuperinterface(templates.uuid.supplierParameterizedType)
      .addMethod(
        methodBuilder("get").addModifiers(PUBLIC).returns(templates.uuid.suppliedType)
          .addStatement("return $T.randomUUID()", templates.uuid.suppliedType)
          .build()
      ).build()
  }


  private def integerSupplier(nameSpaces: NameSpaces): TypeSpec = {
    classBuilder(templates.integer.className(nameSpaces)).addModifiers(PUBLIC, STATIC)
      .addSuperinterface(templates.integer.supplierParameterizedType)
      .addField(randomField)
      .addMethod(
        methodBuilder("get").addModifiers(PUBLIC).returns(templates.integer.suppliedType)
          .addStatement("return random.nextInt()")
          .build()
      ).build()
  }

  private def longSupplier(nameSpaces: NameSpaces): TypeSpec = {
    classBuilder(templates.long.className(nameSpaces)).addModifiers(PUBLIC, STATIC)
      .addSuperinterface(templates.long.supplierParameterizedType)
      .addField(randomField)
      .addMethod(
        methodBuilder("get").addModifiers(PUBLIC).returns(templates.long.suppliedType)
          .addStatement("return random.nextLong()")
          .build()
      ).build()
  }

  private def localDateSupplier(nameSpaces: NameSpaces): TypeSpec =
    classBuilder(templates.localDate.className(nameSpaces)).addModifiers(PUBLIC, STATIC)
      .addSuperinterface(templates.localDate.supplierParameterizedType)
      .addMethod(
        methodBuilder("get").addModifiers(PUBLIC).returns(templates.localDate.suppliedType)
          .addStatement("long minDay = $T.of(1970, 1, 1).toEpochDay()", templates.localDate.suppliedType)
          .addStatement("long maxDay = $T.of(2015, 12, 31).toEpochDay()", templates.localDate.suppliedType)
          .addStatement("long randomDay = $T.current().nextLong(minDay, maxDay)", JavaTypes.ThreadLocalRandom)
          .addStatement("return $T.ofEpochDay(randomDay)", templates.localDate.suppliedType)
          .build()
      ).build()

  private def localDateTimeSupplier(nameSpaces: NameSpaces): TypeSpec =
    classBuilder(templates.localDateTime.className(nameSpaces)).addModifiers(PUBLIC, STATIC)
      .addSuperinterface(templates.localDateTime.supplierParameterizedType)
      .addMethod(
        methodBuilder("get").addModifiers(PUBLIC).returns(templates.localDateTime.suppliedType)
          .addStatement("$T zone = $T.UTC", JavaTypes.ZoneOffset, JavaTypes.ZoneOffset)
          .addStatement("long minTime = 0")
          .addStatement("long maxTime = $T.now().toEpochSecond(zone)", templates.localDateTime.suppliedType)
          .addStatement("long epochSecond = $T.nextLong(minTime, maxTime)", CommonsLangTypes.RandomUtils)
          .addStatement("return $T.ofEpochSecond(epochSecond, 0, zone)", templates.localDateTime.suppliedType)
          .build()
      ).build()

  //  public static class LocalDateTimeSupplier implements Supplier<LocalDateTime> {
  //    public LocalDateTime get() {
  //      var zone = ZoneOffset.UTC;
  //      long minTime = LocalDateTime.MIN.toEpochSecond(zone);
  //      long maxTime = LocalDateTime.now().toEpochSecond(zone);
  //      var epochSecond = RandomUtils.nextLong(minTime, maxTime);
  //      return LocalDateTime.ofEpochSecond(epochSecond, 0, zone);
  //    }
  //  }


  private def recallSupplier(nameSpaces: NameSpaces): TypeSpec = {
    classBuilder(templates.recallSupplier.className(nameSpaces)).addModifiers(PUBLIC, STATIC)
      .addTypeVariable(t)
      .addAnnotation(AccessorFluent)
      .addSuperinterface(ParameterizedTypeName.get(JavaTypes.Supplier, t))
      .addFields(Seq(supplierParam.fieldSpecFinal, lastValueParam.fieldSpec).asJava)
      .addMethod(
        MethodSpec.constructorBuilder().addModifiers(PUBLIC)
          .addParameter(supplierParam.parameterSpec)
          .addStatement("this.$L = $L", supplierParam.name, supplierParam.name)
          .build()
      )
      .addMethod(
        methodBuilder("get").addModifiers(PUBLIC).returns(t)
          .addStatement("$L = $L.get()", lastValueParam.name, supplierParam.name)
          .addStatement("return $L", lastValueParam.name)
          .build()
      ).build()
  }


  private def probNullSupplier(nameSpaces: NameSpaces): TypeSpec = {
    classBuilder(templates.probNull.className(nameSpaces)).addModifiers(PUBLIC, STATIC)
      .addTypeVariable(t)
      .addAnnotation(AccessorFluent)
      .addSuperinterface(templates.probNull.supplierParameterizedType)
      .addFields(Seq(supplierParam, probParam).map(_.fieldSpecFinal).asJava)
      .addMethod(
        MethodSpec
          .constructorBuilder().addModifiers(PUBLIC)
          .addParameters(Seq(supplierParam, probParam).map(_.parameterSpec).asJava)
          .addStatement("this.$L = $L", supplierParam.name, supplierParam.name)
          .addStatement("this.$L = $L", probParam.name, probParam.name)
          .build()
      )
      .addMethod(
        methodBuilder("get").addModifiers(PUBLIC).returns(templates.probNull.suppliedType)
          .addStatement("if ( $T.$L( $L ) ) return null", probabilityToolClassName(nameSpaces), shouldNullMethodName, probParam.name)
          .addStatement("$T result = $L.get()", t, supplierParam.name)
          .addStatement("log.trace(\"Returning {} class instance={}\", result.getClass().getSimpleName(),result)")
          .addStatement("return result")
          .build()
      ).build()
  }


  def generateRangeStringSupplier(nameSpaces: NameSpaces): MethodSpec = {
    val min = "min"
    val max = "max"

    methodBuilder(methods.stringRangeSupplier)
      .addModifiers(PUBLIC, STATIC)
      .returns(JavaTypes.supplier(JavaTypes.String))
      .addParameters(Seq(
        LoremTooling.localeParam,
        LoremTooling.minParam,
        LoremTooling.maxParam
      ).map(_.parameterSpec).asJava)
      .addStatement("if ($L < $L) throw new $T($T.format(\"$L param can not be less than min param. $L={} $L={}\", $L, $L))", max, min, JavaTypes.IllegalArgumentException, JavaTypes.String, max, min, max, min, max)
      .addStatement("if ($L == 0 && $L == 0) throw new $T(\"$L param and $L param can not both be 0\")", min, max, JavaTypes.IllegalArgumentException, max, min)
      .addStatement("if ($L < 0) throw new $T($T.format(\"$L aram can not be less than 0. $L={} $L={}\", $L, $L))", min, JavaTypes.IllegalArgumentException, JavaTypes.String, min, min, max, min, max)
      .addStatement("if ($L < 0) throw new $T($T.format(\"$L aram can not be less than 0. $L={} $L={}\", $L, $L))", max, JavaTypes.IllegalArgumentException, JavaTypes.String, max, min, max, min, max)
      .addCode(
        CodeBlock.builder()
          .add("return () -> {\n")
          .addStatement("int requestWordCount = (int) $T.ceil(max / $T.ENGLISH_AVG_WORD_LENGTH)", JavaTypes.Math, JavaPojoTestFixtures.languagesClassName(nameSpaces))
          .addStatement("final $T[] words = $T.getWords(locale, requestWordCount).split(\"[ ]\")", JavaTypes.String, LoremTooling.loremToolClassName(nameSpaces))
          .addStatement("final int randStringLength = $T.nextInt(min, max)", ClassNames.randomUtils)
          .addStatement("final $T buff = new $T()", JavaTypes.StringBuilder, JavaTypes.StringBuilder)
          .beginControlFlow("for ($T word : words)", JavaTypes.String)
          .add("if (buff.length() <= randStringLength) { if(buff.length()!=0){buff.append(' ');}buff.append(word); } else { break; }")
          .endControlFlow()
          .addStatement("$T returnValue = (buff.length() > randStringLength) ? buff.toString().substring(0, randStringLength).trim() : buff.toString().trim()", JavaTypes.String)
          .addStatement("log.trace(\"Returning Lorem String length={} text=\\\"{}\\\"\", returnValue.length(),returnValue)")
          .addStatement("return returnValue")
          .addStatement("}")
          .build()
      ).build()
  }

  //val sourceParameter = ParameterSpec.builder(JavaTypes.supplier(t), "source", FINAL).build()
  val maxSizeParameter = ParameterSpec.builder(TypeName.INT, "maxSize", FINAL).build()


  def listSupplier(nameSpaces: NameSpaces): TypeSpec = {

    val sourceField = FieldSpec.builder(JavaTypes.supplier(t), "supplier", PRIVATE, FINAL).build()
    val maxSizeField = FieldSpec.builder(TypeName.INT, "maxSize", PRIVATE, FINAL).build()


    val fields = Seq(sourceField, maxSizeField, randomField).asJava


    val constructor = MethodSpec.constructorBuilder().addModifiers(PUBLIC)
      .addParameter(supplierParam.parameterSpec)
      .addParameter(maxSizeParameter)
      .addStatement("this.supplier = supplier")
      .addStatement("this.maxSize = maxSize")
      .build()

    val getMethod = MethodSpec.methodBuilder("get").addModifiers(PUBLIC).returns(JavaTypes.List(t))
      .addJavadoc("WARNING: we are catching an exception to fix the recursion bug in models that refrence themselves.")
      .addStatement("int size = random.nextInt(maxSize)")
      .addStatement("$T list = new $T(maxSize)", JavaTypes.List(t), JavaTypes.ArrayList(t))
      .addCode("try{")
      .addCode("for (int c = 1; c < size; c++) {list.add(supplier.get());}")
      .addComment("WARNING: we are catching an exception to fix the recursion bug in models that refrence themselves.")
      .addCode("}catch(Exception e){return list;}")
      .addStatement("return list")
      .build()

    TypeSpec.classBuilder(templates.list.className(nameSpaces))
      .addModifiers(PUBLIC, STATIC)
      .addTypeVariable(t)
      .addSuperinterface(JavaTypes.supplier(JavaTypes.List(t)))
      .addFields(fields)
      .addMethod(constructor)
      .addMethod(getMethod)
      .build()
  }


}
