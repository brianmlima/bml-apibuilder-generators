package bml.util.java

import bml.util.java.ClassNames.JavaTypes
import com.squareup.javapoet.{ClassName, ParameterizedTypeName}

class JavaMaps {


}

object JavaMaps {

  def linkedHashMapClassName = ClassName.bestGuess("java.util.LinkedHashMap")


  def mapClassName = ClassName.bestGuess("java.util.Map")

  def stringToStringMap = mapStringTo(JavaTypes.String)

  def mapOf(key: ClassName, value: ClassName): ParameterizedTypeName = {
    ParameterizedTypeName.get(mapClassName, key, value)
  }

  def mapStringTo(value: ClassName): ParameterizedTypeName = {
    mapOf(JavaTypes.String, value)
  }

  def mapStringTo(className: String): ParameterizedTypeName = {
    val value = ClassName.bestGuess(className)
    mapStringTo(value)
  }

}
