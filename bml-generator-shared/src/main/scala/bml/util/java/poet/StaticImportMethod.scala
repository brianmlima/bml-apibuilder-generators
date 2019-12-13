package bml.util.java.poet

import com.squareup.javapoet.ClassName

class StaticImportMethod(val className: ClassName, val methodName: String) {
  val method = ClassName.get(className.toString, methodName)
  val staticImport = StaticImport(className, methodName)
}

object StaticImportMethod {
  def apply(className: ClassName, name: String) = new StaticImportMethod(className, name)
}
