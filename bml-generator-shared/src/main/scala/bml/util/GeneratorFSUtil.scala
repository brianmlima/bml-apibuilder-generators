package bml.util

import bml.util.java.poet.StaticImport
import com.google.googlejavaformat.java.{Formatter, JavaFormatterOptions}
import com.squareup.javapoet.{ClassName, JavaFile, TypeSpec}
import io.apibuilder.generator.v0.models.File
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._

class GeneratorFSUtil {

}

object GeneratorFSUtil {
  def LOG = LoggerFactory.getLogger(classOf[GeneratorFSUtil])

  def createDirectoryPath(namespace: String) = namespace.replace('.', '/')

  def makeFile(name: String, javaNameSpace: JavaNameSpace, builder: TypeSpec.Builder, staticImports: StaticImport*): File = {
    makeFile(name, javaNameSpace.path, javaNameSpace.nameSpace, builder, staticImports: _*)
  }

  //####################################################################################################################
  // We have a StaticImport class for people who need that to make a file because ... readability is a thing.
  //  class StaticImport(val className: ClassName, val names: String*) {}
  //
  //  object StaticImport {
  //    def apply(className: ClassName, names: String*): StaticImport = new StaticImport(className, names: _*)
  //  }

  //####################################################################################################################


  def makeFile(name: String, path: String, nameSpace: String, builder: TypeSpec.Builder, staticImports: StaticImport*): File = {
    val javaFile = JavaFile.builder(nameSpace, builder.build)
    staticImports.foreach(
      staticImport =>
        javaFile.addStaticImport(staticImport.className, staticImport.names: _*)
    )
    try {
      val options = JavaFormatterOptions.builder().style(JavaFormatterOptions.Style.GOOGLE).build()
      File(s"${name}.java", Some(path), new Formatter(options).formatSource(javaFile.build.toString))
      //      File(s"${name}.java", Some(path), javaFile.build.toString)
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
