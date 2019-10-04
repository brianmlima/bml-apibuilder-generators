package models.generator.java.persistence.sql

import bml.util.java.JavaPojoUtil
import com.squareup.javapoet.{ClassName, TypeSpec}
import io.apibuilder.generator.v0.models.File
import io.apibuilder.spec.v0.models.Resource
import lib.Text

case class ResourceData(resource: Resource, config: GenConfig) extends JavaPojoUtil {

  var controllerBuilder: Option[TypeSpec.Builder] = None
  var serviceBuilder: Option[TypeSpec.Builder] = None

  val simpleName = toClassName(resource.`type`)

  def controllerClassName = ClassName.get(config.resourceNameSpace, simpleName + "Controller")

  def serviceFieldName = Text.initLowerCase(serviceClassName.simpleName())

  def makeControllerFile(): Option[File] = {
    if (controllerBuilder.isDefined) {
      Some(makeFile(controllerClassName.simpleName(), config.resourceDirectoryPath, config.resourceNameSpace, controllerBuilder.get))
    } else {
      None
    }
  }

  def serviceClassName = ClassName.get(config.resourceNameSpace, simpleName + "ControllerService")

  def makeServiceFile(): Option[File] = {
    if (serviceBuilder.isDefined) {
      Some(makeFile(serviceClassName.simpleName(), config.resourceDirectoryPath, config.resourceNameSpace, serviceBuilder.get))
    } else {
      None
    }
  }


}