package bml.util

import bml.util.java.ClassNames.getter
import com.squareup.javapoet.{FieldSpec, ParameterSpec}
import javax.lang.model.element.Modifier.{FINAL, PRIVATE}

class Param(val parameterSpec: ParameterSpec, javadocString: String) {
  val javadoc = s"@param ${parameterSpec.name} ${javadocString}"
  val name = parameterSpec.name
  val fieldSpec = FieldSpec.builder(parameterSpec.`type`, name, PRIVATE)
    .addJavadoc(javadocString)
    .addAnnotation(getter)
    .build()

  def fieldSpecFinal = fieldSpec.toBuilder.addModifiers(FINAL).build()
}
