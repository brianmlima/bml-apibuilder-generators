package bml.util

import bml.util.java.JavaNamespaceUtil
import io.apibuilder.spec.v0.models.Service

import lib.Text._

class NameSpaces(apiBuilderService: Service) extends JavaNamespaceUtil {

  val nameSpace = makeNameSpace(apiBuilderService.namespace)
  val base = new JavaNameSpace(nameSpace)
  val application = new JavaNameSpace(nameSpace, "application")
  val model = new JavaNameSpace(nameSpace, "models")
  val modelFactory = new JavaNameSpace(model.nameSpace, "factory")
  val service = new JavaNameSpace(nameSpace, "service")
  val controller = new JavaNameSpace(nameSpace, "controller")
  val config = new JavaNameSpace(nameSpace, "config")
  val tool = new JavaNameSpace(nameSpace, "tool")

}
