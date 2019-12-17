package bml.util.java

import bml.util.{NameSpaces, Param}
import bml.util.java.ClassNames.{JavaTypes, illegalArgumentException, locale, loremIpsum, utilityClass}
import bml.util.java.ProbabilityTools.probParam
import com.squareup.javapoet.TypeName.{DOUBLE, INT}
import com.squareup.javapoet.{ClassName, CodeBlock, FieldSpec, MethodSpec, ParameterSpec, TypeSpec}
import io.apibuilder.generator.v0.models.File


/**
 * Builds the LoremTool. A handy tool for generating text with a little probabilistic sugar for nullable testing.
 */
object LoremTooling {

  import bml.util.GeneratorFSUtil.makeFile
  import com.squareup.javapoet.MethodSpec.methodBuilder
  import javax.lang.model.element.Modifier._
  import lib.Text.initCap
  //import JavaTypes._
  import collection.JavaConverters._


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
      //.addMethod(ProbabilityTools.checkProbParamMethod)
      //.addMethod(ProbabilityTools.shouldNullMethod)
      .addMethods(loremMethods(nameSpaces).asJava)

    makeFile(className.simpleName(), nameSpaces.tool, typeBuilder)
  }

  //  public class Param(val spec: ParameterSpec, javadocString: String) {
  //    val javadoc = s"@param ${spec.name} ${javadocString}"
  //    val name = spec.name
  //  }

  val localeParam = new Param(ParameterSpec.builder(locale, "locale", FINAL).build(), "Currently not supported, always defaults to Latin")
  //val probParam = new Param(ParameterSpec.builder(DOUBLE, "probability", FINAL).build(), "A 0.0 to 100.0 probability the return will be null")
  val countParam = new Param(ParameterSpec.builder(INT, "count", FINAL).build(), "sets the count of X")
  val minParam = new Param(ParameterSpec.builder(INT, "min", FINAL).build(), "sets the minimum")
  val maxParam = new Param(ParameterSpec.builder(INT, "max", FINAL).build(), "sets the maximum")

  //  private val checkProbParamMethodName = "checkProbParamMethod"
  //  private val shouldNullMethodName = "shouldNull"

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

  private def loremMethods(nameSpaces: NameSpaces): Seq[MethodSpec] =
    loremMethodDefs.map(
      (md) => {
        Seq(
          Option.apply(if (md.delagate) delagate(md.name) else null),
          Option.apply(if (md.delagate) delagateProb(nameSpaces, md.name) else null),
          Option.apply(if (md.delagateCount) delagateCount(md.name) else null),
          Option.apply(if (md.delagateCount) delagateProbCount(nameSpaces, md.name) else null),
          Option.apply(if (md.delagateMinMax) delagateMinMax(md.name) else null),
          Option.apply(if (md.delagateMinMax) delagateProbMinMax(nameSpaces, md.name) else null)
        ).filter(_.isDefined).map(_.get)
      }
    ).flatten

  private def delagate(name: String): MethodSpec = {
    methodBuilder(name).addModifiers(PUBLIC, STATIC)
      .addJavadoc(
        Seq[String](
          s"Generated Method, delagates Currently to ${loremIpsum.topLevelClassName().toString}.${name}().",
          localeParam.javadoc,
        ).mkString("\n")
      )
      .addParameter(localeParam.parameterSpec)
      .returns(JavaTypes.String)
      .addStatement("return LOREM.$L()", name).build()
  }

  //Builds a delagate method that takes a probability to return null
  private def delagateProb(nameSpaces: NameSpaces, name: String): MethodSpec = {
    methodBuilder(s"probNull${initCap(name)}").addModifiers(PUBLIC, STATIC)
      .addJavadoc(
        Seq[String](
          s"Generated Method, might want a null, Use the ${probParam.name} parameter to get nulls probabilistically",
          s"Delagates Currently to ${loremIpsum.topLevelClassName().toString}.${name}().",
          localeParam.javadoc, probParam.javadoc,
          s"@throws ${illegalArgumentException.simpleName()} if the ${probParam.name} argument is not between inclusive 0 - 100"
        ).mkString("\n")
      )
      .addParameters(Seq(localeParam, probParam).map(_.parameterSpec).asJava)
      .returns(JavaTypes.String)
      .addCode(
        CodeBlock.builder()
          .addStatement("if ( $T.$L( $L ) ) return null", ProbabilityTools.probabilityToolClassName(nameSpaces), ProbabilityTools.shouldNullMethodName, probParam.name)
          .addStatement("return $L($L)", name, localeParam.name)
          .build()
      )
      .build()
  }

  private def delagateProbCount(nameSpaces: NameSpaces, name: String): MethodSpec = {
    methodBuilder(s"probNull${initCap(name)}").addModifiers(PUBLIC, STATIC)
      .addJavadoc(
        Seq[String](
          s"Generated Method, might want a null, Use the ${probParam.name} parameter to get nulls probabilistically",
          s"Delagates Currently to ${loremIpsum.topLevelClassName().toString}.${name}().",
          localeParam.javadoc, countParam.javadoc, probParam.javadoc,
          s"@throws ${illegalArgumentException.simpleName()} if the ${probParam.name} argument is not between inclusive 0 - 100"
        ).mkString("\n")
      )
      .addParameters(Seq(localeParam, countParam, probParam).map(_.parameterSpec).asJava)
      .returns(JavaTypes.String)
      .addCode(
        CodeBlock.builder()
          //.addStatement(s"${checkProbParamMethodName}(${probParam.name})")
          .addStatement("if ( $T.$L( $L ) ) return null", ProbabilityTools.probabilityToolClassName(nameSpaces), ProbabilityTools.shouldNullMethodName, probParam.name)
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
      .addParameter(localeParam.parameterSpec)
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
      .addParameters(Seq(localeParam, minParam, maxParam).map(_.parameterSpec).asJava)
      .returns(JavaTypes.String)
      .addStatement("return LOREM.$L($L,$L)", name, minParam.name, maxParam.name).build()
  }

  private def delagateProbMinMax(nameSpaces: NameSpaces, name: String): MethodSpec = {
    methodBuilder(s"probNull${initCap(name)}").addModifiers(PUBLIC, STATIC)
      .addJavadoc(
        Seq[String](
          s"Generated Method, might want a null, Use the ${probParam.name} parameter to get nulls probabilistically",
          s"Delagates Currently to ${loremIpsum.topLevelClassName().toString}.${name}().",
          localeParam.javadoc, minParam.javadoc, maxParam.javadoc, probParam.javadoc,
          s"@throws ${illegalArgumentException.simpleName()} if the ${probParam.name} argument is not between inclusive 0 - 100"
        ).mkString("\n")
      )
      .addParameters(Seq(localeParam, minParam, maxParam, probParam).map(_.parameterSpec).asJava)
      .returns(JavaTypes.String)
      .addCode(
        CodeBlock.builder()
          .addStatement("if ( $T.$L( $L ) ) return null", ProbabilityTools.probabilityToolClassName(nameSpaces), ProbabilityTools.shouldNullMethodName, probParam.name)
          .addStatement("return $L($L,$L,$L)", name, localeParam.name, minParam.name, maxParam.name)
          .build()
      )
      .build()
  }


}
