package bml.util.java

import bml.util.GeneratorFSUtil.makeFile
import bml.util.NameSpaces
import bml.util.java.ClassNames.{illegalArgumentException, locale, loremIpsum, randomUtils, string, utilityClass}
import com.squareup.javapoet.MethodSpec.methodBuilder
import com.squareup.javapoet.TypeName.{DOUBLE, INT}
import com.squareup.javapoet.{ClassName, CodeBlock, FieldSpec, MethodSpec, ParameterSpec, TypeName, TypeSpec}
import io.apibuilder.generator.v0.models.File
import javax.lang.model.element.Modifier._
import lib.Text
import lib.Text.initCap

import collection.JavaConverters._

/**
 * Builds the LoremTool. A handy tool for generating text with a little probabilistic sugar for nullable testing.
 */
object LoremTooling {

  def loremToolClassName(nameSpaces: NameSpaces) = ClassName.get(nameSpaces.tool.nameSpace, "LoremTool")

  def generateLoremTool(nameSpaces: NameSpaces): File = {
    val className = loremToolClassName(nameSpaces)
    val typeBuilder = TypeSpec.classBuilder(className)
      .addAnnotation(utilityClass)
      .addJavadoc(
        Seq[String](
          "A handy tool for generating text with a little probabilistic sugar for nullable testing.",
          s"Delagates Currently to ${loremIpsum.topLevelClassName().toString}.",
          "<b>Locale Currently not supported</b>, always defaults to Latin but api is present so current usage will not break ",
          "as support for Locales is added."
        ).mkString("\n")
      )
      .addField(
        FieldSpec.builder(loremIpsum, "LOREM", PUBLIC, STATIC, FINAL)
          .initializer(CodeBlock.of("new $T()", loremIpsum))
          .build()
      )
      .addMethod(checkProbParamMethod)
      .addMethod(shouldNullMethod)
      .addMethods(loremMethods.asJava)

    makeFile(className.simpleName(), nameSpaces.tool, typeBuilder)
  }

  private class Param(val spec: ParameterSpec, javadocString: String) {
    val javadoc = s"@param ${spec.name} ${javadocString}"
    val name = spec.name
  }

  private val localeParam = new Param(ParameterSpec.builder(locale, "locale", FINAL).build(), "Currently not supported, always defaults to Latin")
  private val probParam = new Param(ParameterSpec.builder(DOUBLE, "probability", FINAL).build(), "A 0.0 to 100.0 probability the return will be null")
  private val countParam = new Param(ParameterSpec.builder(INT, "count", FINAL).build(), "sets the count of X")
  private val minParam = new Param(ParameterSpec.builder(INT, "min", FINAL).build(), "sets the minimum")
  private val maxParam = new Param(ParameterSpec.builder(INT, "max", FINAL).build(), "sets the maximum")

  private val checkProbParamMethodName = "checkProbParamMethod"
  private val shouldNullMethodName = "shouldNull"

  private class MethodDef(val name: String, val delagate: Boolean, val delagateCount: Boolean, val delagateMinMax: Boolean)

  private def loremMethodDefs = Seq[MethodDef](
    new MethodDef(name = "getCity", delagate = true, delagateCount = false, delagateMinMax = false),
    new MethodDef(name = "getCountry", delagate = true, delagateCount = false, delagateMinMax = false),
    new MethodDef(name = "getEmail", delagate = true, delagateCount = false, delagateMinMax = false),
    new MethodDef(name = "getFirstName", delagate = true, delagateCount = false, delagateMinMax = false),
    new MethodDef(name = "getFirstNameMale", delagate = true, delagateCount = false, delagateMinMax = false),
    new MethodDef(name = "getFirstNameFemale", delagate = true, delagateCount = false, delagateMinMax = false),
    new MethodDef(name = "getLastName", delagate = true, delagateCount = false, delagateMinMax = false),
    new MethodDef(name = "getName", delagate = true, delagateCount = false, delagateMinMax = false),
    new MethodDef(name = "getNameMale", delagate = true, delagateCount = false, delagateMinMax = false),
    new MethodDef(name = "getNameFemale", delagate = true, delagateCount = false, delagateMinMax = false),
    new MethodDef(name = "getTitle", delagate = false, delagateCount = true, delagateMinMax = true),
    new MethodDef(name = "getHtmlParagraphs", delagate = false, delagateCount = false, delagateMinMax = true),
    new MethodDef(name = "getParagraphs", delagate = false, delagateCount = false, delagateMinMax = true),
    new MethodDef(name = "getUrl", delagate = true, delagateCount = false, delagateMinMax = false),
    new MethodDef(name = "getWords", delagate = false, delagateCount = true, delagateMinMax = true),
    new MethodDef(name = "getPhone", delagate = true, delagateCount = false, delagateMinMax = false),
    new MethodDef(name = "getStateAbbr", delagate = true, delagateCount = false, delagateMinMax = false),
    new MethodDef(name = "getStateFull", delagate = true, delagateCount = false, delagateMinMax = false),
    new MethodDef(name = "getZipCode", delagate = true, delagateCount = false, delagateMinMax = false),
  )

  private def loremMethods: Seq[MethodSpec] =
    loremMethodDefs.map(
      (md) => {
        Seq(
          Option.apply(if (md.delagate) delagate(md.name) else null),
          Option.apply(if (md.delagate) delagateProb(md.name) else null),
          Option.apply(if (md.delagateCount) delagateCount(md.name) else null),
          Option.apply(if (md.delagateCount) delagateProbCount(md.name) else null),
          Option.apply(if (md.delagateMinMax) delagateMinMax(md.name) else null),
          Option.apply(if (md.delagateMinMax) delagateProbMinMax(md.name) else null)
        ).filter(_.isDefined).map(_.get)
      }
    ).flatten


  // the generated class is getting too big so stuff some helper methods in there to make it shorter
  private def checkProbParamMethod(): MethodSpec = {
    methodBuilder(checkProbParamMethodName).addModifiers(PRIVATE, STATIC)
      .addJavadoc(
        Seq[String](
          s"Generated Method, throws ${illegalArgumentException}  if ${probParam.name} argument is not between inclusive 0 - 100",
          probParam.javadoc,
          s"@throws ${illegalArgumentException.simpleName()} if the ${probParam.name} argument is not between inclusive 0 - 100"
        ).mkString("\n")
      )
      .addParameter(probParam.spec)
      .addStatement(
        "if($L>100 || $L< 0) throw new $T($T.format(\"probParam must be between 0 and 100 found %s\",$L))",
        probParam.name,
        probParam.name,
        illegalArgumentException,
        string,
        probParam.name
      )
      .build()
  }


  private def shouldNullMethod(): MethodSpec = {
    methodBuilder(shouldNullMethodName).addModifiers(PRIVATE, STATIC)
      .addParameter(probParam.spec)
      .addJavadoc(
        Seq[String](
          s"Generated Method, also checks ${probParam.name} for range violation.",
          probParam.javadoc,
          s"@throws ${illegalArgumentException.simpleName()} if the ${probParam.name} argument is not between inclusive 0 - 100"
        ).mkString("\n")
      )
      .addStatement(
        "if($L>100 || $L< 0) throw new $T(\"probParam must be between 0 and 100 found \" + $L)",
        probParam.name, probParam.name, illegalArgumentException, probParam.name
      )
      .addStatement("return $T.nextDouble(0,100)<=$L", randomUtils, probParam.name)
      .returns(classOf[Boolean])
      .build()
  }


  private def delagate(name: String): MethodSpec = {
    methodBuilder(name).addModifiers(PUBLIC, STATIC)
      .addJavadoc(
        Seq[String](
          s"Generated Method, delagates Currently to ${loremIpsum.topLevelClassName().toString}.${name}().",
          localeParam.javadoc,
        ).mkString("\n")
      )
      .addParameter(localeParam.spec)
      .returns(string)
      .addStatement("return LOREM.$L()", name).build()
  }

  //Builds a delagate method that takes a probability to return null
  private def delagateProb(name: String): MethodSpec = {
    methodBuilder(s"probNull${initCap(name)}").addModifiers(PUBLIC, STATIC)
      .addJavadoc(
        Seq[String](
          s"Generated Method, might want a null, Use the ${probParam.name} parameter to get nulls probabilistically",
          s"Delagates Currently to ${loremIpsum.topLevelClassName().toString}.${name}().",
          localeParam.javadoc, probParam.javadoc,
          s"@throws ${illegalArgumentException.simpleName()} if the ${probParam.name} argument is not between inclusive 0 - 100"
        ).mkString("\n")
      )
      .addParameters(Seq(localeParam, probParam).map(_.spec).asJava)
      .returns(string)
      .addCode(
        CodeBlock.builder()
          .addStatement("if ( $L( $L ) ) return null", shouldNullMethodName, probParam.name)
          .addStatement("return $L($L)", name, localeParam.name)
          .build()
      )
      .build()
  }

  private def delagateProbCount(name: String): MethodSpec = {
    methodBuilder(s"probNull${initCap(name)}").addModifiers(PUBLIC, STATIC)
      .addJavadoc(
        Seq[String](
          s"Generated Method, might want a null, Use the ${probParam.name} parameter to get nulls probabilistically",
          s"Delagates Currently to ${loremIpsum.topLevelClassName().toString}.${name}().",
          localeParam.javadoc, countParam.javadoc, probParam.javadoc,
          s"@throws ${illegalArgumentException.simpleName()} if the ${probParam.name} argument is not between inclusive 0 - 100"
        ).mkString("\n")
      )
      .addParameters(Seq(localeParam, countParam, probParam).map(_.spec).asJava)
      .returns(string)
      .addCode(
        CodeBlock.builder()
          //.addStatement(s"${checkProbParamMethodName}(${probParam.name})")
          .addStatement("if ( $L( $L ) ) return null", shouldNullMethodName, probParam.name)
          .addStatement("return $L($L,$L)", name, localeParam.name, countParam.name)
          .build()
      )
      .build()
  }


  private def delagateCount(name: String): MethodSpec = {
    methodBuilder(name).addModifiers(PUBLIC, STATIC)
      .addJavadoc(
        Seq[String](
          s"Generated Method, delagates Currently to ${loremIpsum.topLevelClassName().toString}.${name}(count).",
          localeParam.javadoc
        ).mkString("\n")
      )
      .addParameter(localeParam.spec)
      .addParameter(INT, "count", FINAL)
      .returns(classOf[String])
      .addStatement("return LOREM.$L($L)", name, countParam.name).build()
  }

  private def delagateMinMax(name: String): MethodSpec = {
    methodBuilder(name).addModifiers(PUBLIC, STATIC)
      .addJavadoc(
        Seq[String](
          s"Generated Method, delagates Currently to ${loremIpsum.topLevelClassName().toString}.${name}(min,max).",
          localeParam.javadoc, minParam.javadoc, maxParam.javadoc
        ).mkString("\n")
      )
      .addParameters(Seq(localeParam, minParam, maxParam).map(_.spec).asJava)
      .returns(string)
      .addStatement("return LOREM.$L($L,$L)", name, minParam.name, maxParam.name).build()
  }

  private def delagateProbMinMax(name: String): MethodSpec = {
    methodBuilder(s"probNull${initCap(name)}").addModifiers(PUBLIC, STATIC)
      .addJavadoc(
        Seq[String](
          s"Generated Method, might want a null, Use the ${probParam.name} parameter to get nulls probabilistically",
          s"Delagates Currently to ${loremIpsum.topLevelClassName().toString}.${name}().",
          localeParam.javadoc, minParam.javadoc, maxParam.javadoc, probParam.javadoc,
          s"@throws ${illegalArgumentException.simpleName()} if the ${probParam.name} argument is not between inclusive 0 - 100"
        ).mkString("\n")
      )
      .addParameters(Seq(localeParam, minParam, maxParam, probParam).map(_.spec).asJava)
      .returns(string)
      .addCode(
        CodeBlock.builder()
          .addStatement("if ( $L( $L ) ) return null", shouldNullMethodName, probParam.name)
          .addStatement("return $L($L,$L,$L)", name, localeParam.name, minParam.name, maxParam.name)
          .build()
      )
      .build()
  }


}
