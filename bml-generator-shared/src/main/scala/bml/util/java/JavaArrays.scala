package bml.util.java

import com.squareup.javapoet.{ClassName, ParameterizedTypeName}

object JavaArrays {


  private def listClassName = ClassName.bestGuess("java.util.List")

  private def stringClassName = ClassName.get(classOf[String])

  def stringArray() = arrayOf(stringClassName)

  def arrayOf(value: ClassName): ParameterizedTypeName = {
    ParameterizedTypeName.get(listClassName, value)
  }

  def arrayOf(clazz: Class[_]): ParameterizedTypeName = {
    arrayOf(ClassName.get(clazz))
  }

}
