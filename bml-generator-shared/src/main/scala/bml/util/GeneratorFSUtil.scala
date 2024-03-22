package bml.util

import bml.util.java.poet.StaticImport
import bml.util.persist.SpringVariableTypes
import bml.util.spring.SpringVersion.SpringVersion
import com.google.googlejavaformat.java.{Formatter, FormatterException, JavaFormatterOptions}
import com.squareup.javapoet.{AnnotationSpec, ClassName, JavaFile, TypeSpec}
import io.apibuilder.generator.v0.models.File
import io.apibuilder.spec.v0.models.Service
import org.slf4j.LoggerFactory
import play.api.libs.json.Json


class GeneratorFSUtil(
                       val springVersion: SpringVersion,
                       val spring5GeneratorName: String,
                       val spring6GeneratorName: String,
                       val service: Service
                     ) {

  val generatedAnnotation = SpringVariableTypes.Generated(
    springVersion,
    springVersion match {
      case bml.util.spring.SpringVersion.SIX => "bml_lombok_spring_6"
      case bml.util.spring.SpringVersion.FIVE => "bml_lombok"
    },
    Json.stringify(
      Json.obj("specVersion" -> service.version)
    )
  )

  def makeFile(className: ClassName, javaNameSpace: JavaNameSpace, builder: TypeSpec.Builder, staticImports: StaticImport*): File = {
    GeneratorFSUtil.makeFile(className.simpleName(), javaNameSpace, builder, Seq(generatedAnnotation), staticImports: _*)
  }

  def makeFile(className: String, javaNameSpace: JavaNameSpace, builder: TypeSpec.Builder, staticImports: StaticImport*): File = {
    GeneratorFSUtil.makeFile(className, javaNameSpace, builder, Seq(generatedAnnotation), staticImports: _*)
  }

}


class GeneratorFormattingException(
                                    val e: FormatterException,
                                    val javaFileBuilder: JavaFile.Builder,
                                    val name: String,
                                    val path: String,
                                    val nameSpace: String
                                  ) extends Exception {
}


/**
 * Handles the Common generation task of creating a class file from a Builder.
 */
object GeneratorFSUtil {

  import collection.JavaConverters._

  def LOG = LoggerFactory.getLogger(classOf[GeneratorFSUtil])

  def createDirectoryPath(namespace: String) = namespace.replace('.', '/')

  def makeFile(className: ClassName, javaNameSpace: JavaNameSpace, builder: TypeSpec.Builder, staticImports: StaticImport*): File = {
    makeFile(className.simpleName(), javaNameSpace.path, javaNameSpace.nameSpace, builder, staticImports: _*)
  }

  def makeFile(className: ClassName, javaNameSpace: JavaNameSpace, builder: TypeSpec.Builder, annotationSpecs: Seq[AnnotationSpec], staticImports: StaticImport*): File = {
    makeFile(className.simpleName(), javaNameSpace.path, javaNameSpace.nameSpace, builder, annotationSpecs, staticImports: _*)
  }

  def makeFile(name: String, javaNameSpace: JavaNameSpace, builder: TypeSpec.Builder, staticImports: StaticImport*): File = {
    makeFile(name, javaNameSpace.path, javaNameSpace.nameSpace, builder, staticImports: _*)
  }

  def makeFile(name: String, javaNameSpace: JavaNameSpace, builder: TypeSpec.Builder, annotationSpecs: Seq[AnnotationSpec], staticImports: StaticImport*): File = {
    makeFile(name, javaNameSpace.path, javaNameSpace.nameSpace, builder, annotationSpecs: Seq[AnnotationSpec], staticImports: _*)
  }


  def makeFile(name: String, path: String, nameSpace: String, builder: TypeSpec.Builder, annotationSpecs: Seq[AnnotationSpec], staticImports: StaticImport*): File = {
    builder.addAnnotations(annotationSpecs.asJava)
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
        throw new GeneratorFormattingException(x, javaFile, name, path, nameSpace)
      }
    }
  }


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
        throw new GeneratorFormattingException(x, javaFile, name, path, nameSpace)
      }
    }
  }

}
