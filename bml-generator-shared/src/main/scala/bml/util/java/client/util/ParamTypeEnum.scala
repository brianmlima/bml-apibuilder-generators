package bml.util.java.client.util

import com.squareup.javapoet.{ClassName, CodeBlock, TypeSpec}
import javax.lang.model.element.Modifier.{PROTECTED, STATIC}

class ParamTypeEnum(val containerClass: ClassName) {

  val typeName = ClassName.get(containerClass.canonicalName(), "UriParamType")

  val header = "HEADER"
  val path = "PATH"
  val query = "QUERY"

  def makeType(): TypeSpec = {
    TypeSpec
      .enumBuilder(typeName)
      .addJavadoc("A helper class for building uri's in a way that makes code generation cleaner.")
      .addModifiers(PROTECTED, STATIC)
      .addEnumConstant("HEADER")
      .addEnumConstant("PATH")
      .addEnumConstant("QUERY")
      .build()
  }

}
