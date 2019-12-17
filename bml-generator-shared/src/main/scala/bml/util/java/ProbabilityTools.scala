package bml.util.java

import bml.util.{NameSpaces, Param}
import bml.util.java.ClassNames.JavaTypes
import com.squareup.javapoet.{ClassName, MethodSpec, ParameterSpec, TypeSpec}
import io.apibuilder.generator.v0.models.File

object ProbabilityTools {

  import javax.lang.model.element.Modifier._
  import bml.util.GeneratorFSUtil.makeFile
  import com.squareup.javapoet.TypeName.DOUBLE
  import com.squareup.javapoet.MethodSpec.methodBuilder

  val checkProbParamMethodName = "checkProbParamMethod"
  val shouldNullMethodName = "shouldNull"
  val probParam = new Param(ParameterSpec.builder(DOUBLE, "probability", FINAL).build(), "A 0.0 to 100.0 probability the return will be null")

  def probabilityToolClassName(nameSpaces: NameSpaces) = ClassName.get(nameSpaces.tool.nameSpace, "ProbabilityTool")

  def probabilityTool(nameSpaces: NameSpaces): File = {
    val className = probabilityToolClassName(nameSpaces)
    val typeBuilder = TypeSpec.classBuilder(className).addModifiers(PUBLIC)
      .addAnnotation(ClassNames.utilityClass)
      .addMethod(shouldNullMethod())
      .addMethod(checkProbParamMethod())
    makeFile(className.simpleName(), nameSpaces.tool, typeBuilder)
  }

  // the generated class is getting too big so stuff some helper methods in there to make it shorter
  def checkProbParamMethod(): MethodSpec = {
    methodBuilder(checkProbParamMethodName).addModifiers(PUBLIC, STATIC)
      .addJavadoc(
        Seq[String](
          s"Generated Method, throws ${JavaTypes.IllegalArgumentException}  if ${probParam.name} argument is not between inclusive 0 - 100",
          probParam.javadoc,
          s"@throws ${JavaTypes.IllegalArgumentException.simpleName()} if the ${probParam.name} argument is not between inclusive 0 - 100"
        ).mkString("\n")
      )
      .addParameter(probParam.parameterSpec)
      .addStatement(
        "if($L>100 || $L< 0) throw new $T($T.format(\"probParam must be between 0 and 100 found %s\",$L))",
        probParam.name,
        probParam.name,
        JavaTypes.IllegalArgumentException,
        JavaTypes.String,
        probParam.name
      )
      .build()
  }


  def shouldNullMethod(): MethodSpec = {
    methodBuilder(shouldNullMethodName).addModifiers(PUBLIC, STATIC)
      .addParameter(probParam.parameterSpec)
      .addJavadoc(
        Seq[String](
          s"Generated Method, also checks ${probParam.name} for range violation.",
          probParam.javadoc,
          s"@throws ${JavaTypes.IllegalArgumentException.simpleName()} if the ${probParam.name} argument is not between inclusive 0 - 100"
        ).mkString("\n")
      )
      .addStatement(
        "if($L>100 || $L< 0) throw new $T(\"probParam must be between 0 and 100 found \" + $L)",
        probParam.name, probParam.name, JavaTypes.IllegalArgumentException, probParam.name
      )
      .addStatement("return $T.nextDouble(0,100)<=$L", ClassNames.randomUtils, probParam.name)
      .returns(classOf[Boolean])
      .build()
  }


}
