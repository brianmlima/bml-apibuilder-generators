package bml.util

import com.google.googlejavaformat.java.Formatter
import com.squareup.javapoet.{JavaFile, TypeSpec}
import io.apibuilder.generator.v0.models.File
import org.slf4j.LoggerFactory

class GeneratorFSUtil {

}

object GeneratorFSUtil {
  def LOG = LoggerFactory.getLogger(classOf[GeneratorFSUtil])

  def createDirectoryPath(namespace: String) = namespace.replace('.', '/')

  def makeFile(name: String, javaNameSpace: JavaNameSpace, builder: TypeSpec.Builder): File = {
    makeFile(name, javaNameSpace.path, javaNameSpace.nameSpace, builder)
  }

  def makeFile(name: String, path: String, nameSpace: String, builder: TypeSpec.Builder): File = {
    val javaFile = JavaFile.builder(nameSpace, builder.build)
    try {
      File(s"${name}.java", Some(path), new Formatter().formatSource(javaFile.build.toString))
    } catch {
      case x: com.google.googlejavaformat.java.FormatterException => {
        LOG.error(javaFile.build().toString)
        LOG.error(x.diagnostics().toString)
        LOG.error(x.getLocalizedMessage)
        throw x
      }
    }
  }

}
