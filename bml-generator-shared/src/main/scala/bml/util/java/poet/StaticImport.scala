package bml.util.java.poet

import com.squareup.javapoet.ClassName

class StaticImport(val className: ClassName, val names: String*) {}

object StaticImport {
  def apply(className: ClassName, names: String*): StaticImport = new StaticImport(className, names: _*)
}