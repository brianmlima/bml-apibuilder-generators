package bml.util

import bml.util.java.JavaNamespaceUtil

class JavaNameSpace(parentNameSpace: String, suffix: Option[String]) extends JavaNamespaceUtil {

  var nameSpace = if (suffix.isDefined) parentNameSpace + "." + suffix.get else parentNameSpace
  var path = GeneratorFSUtil.createDirectoryPath(nameSpace)

  def this(parentNameSpace: String, suffix: String) {
    this(parentNameSpace, Option(suffix))
  }

  def this(parentNameSpace: String) {
    this(parentNameSpace, None)
  }

}
