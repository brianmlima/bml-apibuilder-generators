package bml.util.java


import io.apibuilder.spec.v0.models.{Enum, Field, Model}
import javax.lang.model.element.Modifier._
import JavaPojoUtil.toStaticFieldName
import bml.util.GeneratorFSUtil.makeFile
import bml.util.java.ClassNames.{arrays, collections, linkedList, size, string}
import com.squareup.javapoet.{AnnotationSpec, FieldSpec, TypeSpec}
import com.squareup.javapoet.TypeName.{BOOLEAN, INT}
import io.apibuilder.generator.v0.models.File
import javax.print.DocFlavor.STRING
import net.bytebuddy.implementation.bytecode.Throw
import play.api.Logger
import play.api.libs.json.JsResult.Exception

import scala.collection.JavaConverters._

object JavaPojos {
  private val LOG: Logger = Logger.apply(this.getClass())

  def toMinFieldStaticFieldName(field: Field): String = {
    toStaticFieldName(field.name) + "_MIN" + (if (field.`type` == "string") "_LENGTH" else "_SIZE")
  }

  def toMaxFieldStaticFieldName(field: Field): String = {
    toStaticFieldName(field.name) + "_MAX" + (if (field.`type` == "string") "_LENGTH" else "_SIZE")
  }

  def requiredFieldStaticFieldName(field: Field): String = {
    toStaticFieldName(field.name) + "_REQUIRED"
  }

  def handleRequiredFieldAddition(classSpec: TypeSpec.Builder, field: Field): Unit = {
    val staticParamName = requiredFieldStaticFieldName(field)
    classSpec.addField(
      FieldSpec.builder(BOOLEAN, staticParamName, PUBLIC, STATIC, FINAL).initializer("$L", field.required.toString)
        .addJavadoc(s" Is the field ${JavaPojoUtil.toParamName(field.name, true)} is a required. Useful for reflection test rigging.")
        .build()
    )
  }

  def makeRequiredFieldsField(model: Model): FieldSpec = {
    val fieldNames: Seq[String] = model.fields.filter(_.required).map(_.name).seq
    val fieldSpec = FieldSpec.builder(ClassNames.list(string), "REQUIRED_FIELDS", PUBLIC, STATIC, FINAL)
    if (fieldNames.isEmpty) {
      fieldSpec.initializer("$T.emptyList", collections)
    } else {
      fieldSpec.initializer(
        "$T.unmodifiableList(new $T($T.asList($L)))",
        collections,
        linkedList(string),
        arrays,
        fieldNames.map(name => "\"" + name + "\"").mkString(",")
      )
    }
    fieldSpec.build()
  }


  def handleSizeAttribute(classSpec: TypeSpec.Builder, field: Field) = {
    val isString = (field.`type` == "string")

    val minStaticParamName = toMinFieldStaticFieldName(field)
    val maxStaticParamName = toMaxFieldStaticFieldName(field)
    val spec = AnnotationSpec.builder(size)

    val hasMin = field.minimum.isDefined
    val hasMax = field.maximum.isDefined

    if (hasMin || hasMax) {
      LOG.info("field.minimum.isDefined")
      spec.addMember("min", "$L", minStaticParamName)
      classSpec.addField(
        FieldSpec.builder(INT, minStaticParamName, PUBLIC, STATIC, FINAL).initializer("$L", field.minimum.getOrElse(1).toString)
          .addJavadoc(s"The minimum ${if (isString) "length" else "size"} of the field ${JavaPojoUtil.toParamName(field.name, true)}. Useful for reflection test rigging.")
          .build()
      )
    }

    if (hasMax) {
      LOG.info("{} field.maximum.isDefined=true")
      spec.addMember("max", "$L", maxStaticParamName)
      classSpec.addField(
        FieldSpec.builder(INT, maxStaticParamName, PUBLIC, STATIC, FINAL).initializer("$L", field.maximum.get.toString)
          .addJavadoc(s"The maximum ${if (isString) "length" else "size"} of the field ${JavaPojoUtil.toParamName(field.name, true)}. Useful for reflection test rigging.")
          .build()
      )
    } else {
      throw new IllegalArgumentException(s"The field ${field.name} has a minimum defined but no maximum, spec validation should have caught this")
    }
    spec.build()
  }

}
