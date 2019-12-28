package bml.util.jpa

import bml.util.AnotationUtil.JavaxAnnotations.JavaxPersistanceAnnotations
import bml.util.java.ClassNames.{HibernateTypes, JavaTypes}
import bml.util.java.{ClassNames, JavaPojoUtil}
import bml.util.{AnotationUtil, JavaNameSpace, Text}
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

  def toColumnName(field: Field): String = {
    Text.camelToSnakeCase(JavaPojoUtil.toFieldName(field))
  }


  //  def snake(string: String) = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, string)

  //
  //  private def underscores2camel(name: String) = {
  //    assert(!(name endsWith "_"), "names ending in _ not supported by this algorithm")
  //    "[A-Za-z\\d]+_?|_".r.replaceAllIn(name, { x =>
  //      val x0 = x.group(0)
  //      if (x0 == "_") x0
  //      else x0.stripSuffix("_").toLowerCase.capitalize
  //    })
  //  }
  //
  //  private def camel2underscores(x: String) = {
  //    "_?[A-Z][a-z\\d]+".r.findAllMatchIn(x).map(_.group(0).toLowerCase).mkString("_")
  //  }
  //
  //  private def snakify(name: String) = name.replaceAll("([A-Z]+)([A-Z][a-z])", "$1_$2").replaceAll("([a-z\\d])([A-Z])", "$1_$2").toLowerCase
  //

  def addJPAStandardFields(model: Model): Seq[FieldSpec] = {
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
        .addAnnotation(AnotationUtil.jsonProperty(versionFieldName, false))
        .addAnnotation(JavaxPersistanceAnnotations.Version)
        .addAnnotation(JavaxPersistanceAnnotations.Column(versionFieldName))
        .build(),

      FieldSpec.builder(ClassNames.JavaTypes.LocalDateTime, createdAtFieldName, Modifier.PROTECTED)
        .addJavadoc(
          Seq[String](
            "The moment this resources was created."
          ).mkString("\n")
        )
        .addAnnotation(HibernateTypes.CreationTimestamp)
        .addAnnotation(AnotationUtil.jsonProperty(createdAtFieldName, false))
        //.addAnnotation(JavaxPersistanceAnnotations.TemporalTIMESTAMP)
        .addAnnotation(JavaxPersistanceAnnotations.Column(createdAtFieldName))
        .build(),

      FieldSpec.builder(ClassNames.JavaTypes.LocalDateTime, updatedAtFieldName, Modifier.PROTECTED)
        .addJavadoc(
          Seq[String](
            "The moment this resources was last updated."
          ).mkString("\n")
        )
        .addAnnotation(HibernateTypes.UpdateTimestamp)
        .addAnnotation(AnotationUtil.jsonProperty(updatedAtFieldName, false))
        //.addAnnotation(JavaxPersistanceAnnotations.TemporalTIMESTAMP)
        .addAnnotation(JavaxPersistanceAnnotations.Column(updatedAtFieldName))
        .build(),

    )


  }

}
