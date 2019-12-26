package bml.util.jpa

import bml.util.java.JavaPojoUtil
import io.apibuilder.spec.v0.models.{Field, Model}

object JPA {

  def toTableName(model: Model): String = {
    snakify(JavaPojoUtil.toClassName(model))
  }

  def toColumnName(field: Field): String = {
    snakify(JavaPojoUtil.toFieldName(field))
  }

  private def underscores2camel(name: String) = {
    assert(!(name endsWith "_"), "names ending in _ not supported by this algorithm")
    "[A-Za-z\\d]+_?|_".r.replaceAllIn(name, { x =>
      val x0 = x.group(0)
      if (x0 == "_") x0
      else x0.stripSuffix("_").toLowerCase.capitalize
    })
  }

  private def camel2underscores(x: String) = {
    "_?[A-Z][a-z\\d]+".r.findAllMatchIn(x).map(_.group(0).toLowerCase).mkString("_")
  }

  private def snakify(name: String) = name.replaceAll("([A-Z]+)([A-Z][a-z])", "$1_$2").replaceAll("([a-z\\d])([A-Z])", "$1_$2").toLowerCase

}
