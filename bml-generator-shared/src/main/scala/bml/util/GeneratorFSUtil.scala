package bml.util

import com.google.googlejavaformat.java.Formatter
import com.squareup.javapoet.{JavaFile, TypeSpec}
import io.apibuilder.generator.v0.models.File

object GeneratorFSUtil {
  def createDirectoryPath(namespace: String) = namespace.replace('.', '/')

  def makeFile(name: String, path: String, nameSpace: String, builder: TypeSpec.Builder): File = {
    File(s"${name}.java", Some(path), new Formatter().formatSource(JavaFile.builder(nameSpace, builder.build).build.toString))
  }

}
