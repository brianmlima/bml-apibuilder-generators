package bml.util

import bml.util.java.JavaNamespaceUtil

class JavaNameSpace(parentNameSpace: String, suffix: String) extends JavaNamespaceUtil {
  var nameSpace = parentNameSpace + "." + suffix
  var path = GeneratorFSUtil.createDirectoryPath(nameSpace)
}
