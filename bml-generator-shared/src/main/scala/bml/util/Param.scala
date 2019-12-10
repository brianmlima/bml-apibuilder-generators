package bml.util

import com.squareup.javapoet.ParameterSpec

class Param(val spec: ParameterSpec, javadocString: String) {
  val javadoc = s"@param ${spec.name} ${javadocString}"
  val name = spec.name
}
