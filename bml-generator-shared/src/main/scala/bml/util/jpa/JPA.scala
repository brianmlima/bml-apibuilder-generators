package bml.util.jpa

import bml.util.AnotationUtil.JacksonAnno
import bml.util.java.ClassNames.{HibernateTypes, JavaTypes}
import bml.util.java.{ClassNames, JavaPojoUtil}
import bml.util.persist.SpringVariableTypes.PersistenceAnnotations
import bml.util.spring.SpringVersion.SpringVersion
import bml.util.{JavaNameSpace, Text}
import com.squareup.javapoet.{ClassName, FieldSpec}
import io.apibuilder.spec.v0.models.{Field, Model}
import javax.lang.model.element.Modifier

object JPA {

  def toRepositoryName(model: Model): String = {
    JavaPojoUtil.toClassName(model) + "Repository"
  }

  def toRepositoryClassName(nameSpace: JavaNameSpace, model: Model): ClassName = {
    ClassName.get(nameSpace.nameSpace, toRepositoryName(model))
  }

  def toTableName(model: Model): String = {
    Text.camelToSnakeCase(JavaPojoUtil.toClassName(model))
  }

  def toTableName(className: ClassName): String = {
    Text.camelToSnakeCase(className.simpleName())
  }

  def toTableName(model: String): String = {
    Text.camelToSnakeCase(JavaPojoUtil.toClassName(model))
  }

  def toColumnName(field: Field): String = {
    Text.camelToSnakeCase(JavaPojoUtil.toFieldName(field))
  }

  def addJPAStandardFields(springVersion: SpringVersion, model: Model): Seq[FieldSpec] = {
    val versionFieldName = "version"
    val createdAtFieldName = "createdAt"
    val updatedAtFieldName = "updatedAt"

    Seq[FieldSpec](
      FieldSpec.builder(JavaTypes.Long, versionFieldName, Modifier.PROTECTED)
        .addJavadoc(
          Seq[String](
            "This resources version.",
            "Helps with update ordering and transactions."
          ).mkString("\n")
        )
        .addAnnotation(JacksonAnno.JsonProperty(versionFieldName, false))
        .addAnnotation(PersistenceAnnotations.Version(springVersion))
        .addAnnotation(PersistenceAnnotations.Column(springVersion, versionFieldName))
        .build(),

      FieldSpec.builder(ClassNames.JavaTypes.LocalDateTime, createdAtFieldName, Modifier.PROTECTED)
        .addJavadoc(
          Seq[String](
            "The moment this resources was created."
          ).mkString("\n")
        )
        .addAnnotation(HibernateTypes.CreationTimestamp)
        .addAnnotation(JacksonAnno.JsonProperty(createdAtFieldName, false))
        .addAnnotation(JacksonAnno.JsonFormatString)
        .addAnnotation(PersistenceAnnotations.Column(springVersion, createdAtFieldName))
        .build(),

      FieldSpec.builder(ClassNames.JavaTypes.LocalDateTime, updatedAtFieldName, Modifier.PROTECTED)
        .addJavadoc(
          Seq[String](
            "The moment this resources was last updated."
          ).mkString("\n")
        )
        .addAnnotation(HibernateTypes.UpdateTimestamp)
        .addAnnotation(JacksonAnno.JsonFormatString)
        .addAnnotation(JacksonAnno.JsonProperty(updatedAtFieldName, false))
        .addAnnotation(PersistenceAnnotations.Column(springVersion, updatedAtFieldName))
        .build()
    )
  }
}
