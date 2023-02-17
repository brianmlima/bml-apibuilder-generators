package bml.util.java

trait JavaNamespaceUtil {
  def makeNameSpace(namespace: String): String = {
    namespace.split("\\.").map {
      JavaReservedWordUtil.checkForReservedWord
    }.mkString(".")
  }

}
