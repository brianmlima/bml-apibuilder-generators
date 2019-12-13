package bml.util.java

import bml.util.GeneratorFSUtil.makeFile
import bml.util.{NameSpaces, Param}
import bml.util.java.ClassNames
import com.squareup.javapoet.{ClassName, MethodSpec, ParameterSpec, TypeSpec}
import com.squareup.javapoet.TypeName.DOUBLE
import javax.lang.model.element.Modifier.FINAL
import com.squareup.javapoet.MethodSpec.methodBuilder
import io.apibuilder.generator.v0.models.File
import javax.lang.model.element.Modifier

object ProbabilityTools {

  val checkProbParamMethodName = "checkProbParamMethod"
  val shouldNullMethodName = "shouldNull"
  val probParam = new Param(ParameterSpec.builder(DOUBLE, "probability", FINAL).build(), "A 0.0 to 100.0 probability the return will be null")

  def probabilityToolClassName(nameSpaces: NameSpaces) = ClassName.get(nameSpaces.tool.nameSpace, "ProbabilityTool")

  def probabilityTool(nameSpaces: NameSpaces): File = {
    val className = probabilityToolClassName(nameSpaces)
    val typeBuilder = TypeSpec.classBuilder(className).addModifiers(Modifier.PUBLIC)
      .addAnnotation(ClassNames.utilityClass)
      .addMethod(shouldNullMethod())
      .addMethod(checkProbParamMethod())
    makeFile(className.simpleName(), nameSpaces.tool, typeBuilder)
  }

  // the generated class is getting too big so stuff some helper methods in there to make it shorter
  def checkProbParamMethod(): MethodSpec = {
    methodBuilder(checkProbParamMethodName).addModifiers(Modifier.PUBLIC, Modifier.STATIC)
      .addJavadoc(
        Seq[String](
          s"Generated Method, throws ${ClassNames.illegalArgumentException}  if ${probParam.name} argument is not between inclusive 0 - 100",
          probParam.javadoc,
          s"@throws ${ClassNames.illegalArgumentException.simpleName()} if the ${probParam.name} argument is not between inclusive 0 - 100"
        ).mkString("\n")
      )
      .addParameter(probParam.parameterSpec)
      .addStatement(
        "if($L>100 || $L< 0) throw new $T($T.format(\"probParam must be between 0 and 100 found %s\",$L))",
        probParam.name,
        probParam.name,
        ClassNames.illegalArgumentException,
        ClassNames.string,
        probParam.name
      )
      .build()
  }


  def shouldNullMethod(): MethodSpec = {
    methodBuilder(shouldNullMethodName).addModifiers(Modifier.PUBLIC, Modifier.STATIC)
      .addParameter(probParam.parameterSpec)
      .addJavadoc(
        Seq[String](
          s"Generated Method, also checks ${probParam.name} for range violation.",
          probParam.javadoc,
          s"@throws ${ClassNames.illegalArgumentException.simpleName()} if the ${probParam.name} argument is not between inclusive 0 - 100"
        ).mkString("\n")
      )
      .addStatement(
        "if($L>100 || $L< 0) throw new $T(\"probParam must be between 0 and 100 found \" + $L)",
        probParam.name, probParam.name, ClassNames.illegalArgumentException, probParam.name
      )
      .addStatement("return $T.nextDouble(0,100)<=$L", ClassNames.randomUtils, probParam.name)
      .returns(classOf[Boolean])
      .build()
  }


}
