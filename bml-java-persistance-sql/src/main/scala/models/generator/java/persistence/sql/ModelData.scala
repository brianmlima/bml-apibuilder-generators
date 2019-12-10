package models.generator.java.persistence.sql

import bml.util.GeneratorFSUtil.makeFile
import bml.util.java._
import bml.util.persist.PersistModelAttribute
import com.squareup.javapoet.{ClassName, TypeName, TypeSpec}
import io.apibuilder.generator.v0.models.File
import io.apibuilder.spec.v0.models.{Field, Model, Union}

case class ModelData(model: Model, relatedUnions: Seq[Union], config: GenConfig) extends JavaPojoUtil {

  val persistModelAttribute = PersistModelAttribute.read(model)

  val fields = model.fields;

  val fieldToPersistModelAttrMap = fields.map(f => f -> PersistModelAttribute.read(f))

  val simpleName = toClassName(model.name)

  var classBuilder: Option[TypeSpec.Builder] = None

  var repoBuilder: Option[TypeSpec.Builder] = None

  def isPersistedModel(): Boolean = persistModelAttribute.isDefined

  def className = ClassName.get(config.modelsNameSpace, simpleName)

  def repoClassName = ClassName.get(config.jpaNameSpace, simpleName + "Repository")

  def getIdField(): Option[Field] = {
    val idEntry = fieldToPersistModelAttrMap.filter(_._2.isDefined).find(_._2.get.isId)
    Option.apply(idEntry.get._1)
  }

  def getIdFieldClassName(): Option[TypeName] = {
    val idField = getIdField()
    if (idField.isDefined) {
      Some(dataTypeFromField(idField.get.`type`, config.modelsNameSpace))
    } else {
      None
    }
  }


  def makeClassFile(): Option[File] = {
    if (classBuilder.isDefined) {
      Some(makeFile(simpleName, config.modelsDirectoryPath, config.modelsNameSpace, classBuilder.get))
    } else {
      None
    }
  }

  def makeRepoFile(): Option[File] = {
    if (repoBuilder.isDefined) {
      Some(makeFile(repoClassName.simpleName(), config.jpaDirectoryPath, config.jpaNameSpace, repoBuilder.get))
    } else {
      None
    }
  }

}
