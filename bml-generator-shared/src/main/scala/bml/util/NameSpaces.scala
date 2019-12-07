package bml.util

import bml.util.java.JavaNamespaceUtil
import io.apibuilder.spec.v0.models.Service

class NameSpaces(apiBuilderService: Service) extends JavaNamespaceUtil {

  val nameSpace = makeNameSpace(apiBuilderService.namespace)
  val base = new JavaNameSpace(nameSpace)
  val application = new JavaNameSpace(nameSpace, "application")
  val model = new JavaNameSpace(nameSpace, "models")
  val service = new JavaNameSpace(nameSpace, "service")
  val controller = new JavaNameSpace(nameSpace, "controller")
  val config = new JavaNameSpace(nameSpace, "config")

}
