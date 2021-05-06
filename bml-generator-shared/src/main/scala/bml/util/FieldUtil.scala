package bml.util

import bml.util.java.JavaPojoUtil
import com.squareup.javapoet.{FieldSpec, TypeName}
import io.apibuilder.spec.v0.models.{Field, Service}
import javax.lang.model.element.Modifier.{FINAL, PRIVATE, STATIC}

import scala.collection.mutable.ListBuffer

case class FieldUtil() {
}

object FieldUtil {

  def javaDoc(field: Field): Option[String] = {
    var comments = new ListBuffer[String]()
    if (field.description.exists(_.trim.nonEmpty)) {
      comments += field.description.get.trim + "\n"
    }
    if (field.example.exists(_.trim.nonEmpty)) {
      comments += s"Example: ${field.example.get.trim}\n"
    }

    if (field.description.exists(_.trim.nonEmpty)) {
      comments += s"@param ${JavaPojoUtil.toFieldName(field)} ${field.description.get.trim}\n"
      comments += s"@return ${field.description.get.trim}\n"
    }


    Option.apply(comments.mkString("\n"))
  }

  def serialVersionUID(service: Service) =
    FieldSpec.builder(
      TypeName.LONG, "serialVersionUID", PRIVATE, STATIC, FINAL)
      .initializer("$LL", service.version.split("\\.")(0))
      .build()

}