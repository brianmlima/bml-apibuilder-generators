package bml.util

import akka.http.scaladsl.model.headers.LinkParams.`type`
import bml.util.java.{JavaNamespaceUtil, JavaPojoUtil}
import com.squareup.javapoet.ClassName
import io.apibuilder.spec.v0.models.{Field, Service}

class NameSpaces(nameSpaceString: String) extends JavaNamespaceUtil {

  val nameSpace = makeNameSpace(nameSpaceString)
  val base = new JavaNameSpace(nameSpace)
  val application = new JavaNameSpace(nameSpace, NameSpaceSuffixes.application)
  val model = new JavaNameSpace(nameSpace, NameSpaceSuffixes.model)
  val modelFactory = new JavaNameSpace(model.nameSpace, NameSpaceSuffixes.modelFactory)
  val service = new JavaNameSpace(nameSpace, NameSpaceSuffixes.service)
  val controller = new JavaNameSpace(nameSpace, NameSpaceSuffixes.controller)
  val config = new JavaNameSpace(nameSpace, NameSpaceSuffixes.config)
  val tool = new JavaNameSpace(nameSpace, NameSpaceSuffixes.tool)
  val jpa = new JavaNameSpace(nameSpace, NameSpaceSuffixes.jpa)
  val jpaConverters = new JavaNameSpace(nameSpace, NameSpaceSuffixes.jpaConverters)
  val converter = new JavaNameSpace(nameSpace, NameSpaceSuffixes.converter)
  val client = new JavaNameSpace(nameSpace, NameSpaceSuffixes.client)

  def this(apiBuilderService: Service) {
    this(apiBuilderService.namespace);
  }
}

object NameSpaces {
  def fromEnum(`type`: String): Option[NameSpaces] = {
    if (JavaPojoUtil.isModelNameWithPackage(`type`)) {
      val safeName = `type`.split('.').dropRight(2).mkString(".");
      println(s"Name=${`type`} SafeName=${safeName}")
      Some(new NameSpaces(safeName))
    } else {
      None
    }
  }
}

object NameSpaceSuffixes {
  val application = "application"
  val model = "models"
  val modelFactory = "factory"
  val service = "service"
  val controller = "controller"
  val config = "config"
  val tool = "tool"
  val jpa = "jpa"
  val jpaConverters = s"${model}.jpa.converters"
  val converter = "converter"
  val client = "client"
}





