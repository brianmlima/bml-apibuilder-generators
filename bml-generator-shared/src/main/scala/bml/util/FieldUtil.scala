package bml.util

import io.apibuilder.spec.v0.models.Field

import scala.collection.mutable.ListBuffer

case class FieldUtil() {
}

object FieldUtil {

  def javaDoc(field: Field): Option[String] = {
    var comments = new ListBuffer[String]()
    if(field.description.exists(_.trim.nonEmpty)){
      comments += field.description.get.trim+"\n"
    }
    if(field.example.exists(_.trim.nonEmpty)){
      comments += s"Example: ${field.example.get.trim}\n"
    }
    Option.apply(comments.mkString("\n"))
  }
}