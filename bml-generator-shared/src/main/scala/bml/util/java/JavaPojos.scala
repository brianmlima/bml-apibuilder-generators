package bml.util.java


import io.apibuilder.spec.v0.models.{Field, Model}
import javax.lang.model.element.Modifier._
import JavaPojoUtil.toStaticFieldName
import akka.http.scaladsl.model.headers.LinkParams.`type`
import bml.util.attribute
import bml.util.attribute.StringValueLength
import bml.util.java.ClassNames.{JavaTypes, linkedList, size}
import com.squareup.javapoet.{AnnotationSpec, FieldSpec, TypeName, TypeSpec}
import com.squareup.javapoet.TypeName.{BOOLEAN, INT}
import play.api.Logger

import scala.collection.JavaConverters._

object JavaPojos {
  private val LOG: Logger = Logger.apply(this.getClass())

  def toMinFieldStaticFieldName(field: Field, overrideType: Option[String] = None): String = {
    toStaticFieldName(field.name) + "_MIN" + (if (overrideType.getOrElse(field.`type`) == "string") "_LENGTH" else "_SIZE")
  }

  //  def toMinFieldStaticFieldName(field: Field): String = {
  //    toStaticFieldName(field.name) + "_MIN" + (if (field.`type` == "string") "_LENGTH" else "_SIZE")
  //  }


  //  def toMinFieldStaticFieldName(name: String, `type`: String): String = {
  //    toStaticFieldName(name) + "_MIN" + (if (`type` == "string") "_LENGTH" else "_SIZE")
  //  }
  //
  //  def toMaxFieldStaticFieldName(name: String, `type`: String): String = {
  //    toStaticFieldName(name) + "_MAX" + (if (`type` == "string") "_LENGTH" else "_SIZE")
  //  }

  def toMaxFieldStaticFieldName(field: Field, overrideType: Option[String] = None): String = {
    toStaticFieldName(field.name) + "_MAX" + (if (overrideType.getOrElse(field.`type`) == "string") "_LENGTH" else "_SIZE")
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

  val requiredFieldsFieldName = "REQUIRED_FIELDS"

  def makeRequiredFieldsField(model: Model): FieldSpec = {
    val fieldNames: Seq[String] = model.fields.filter(_.required).map(_.name)
    val fieldSpec = FieldSpec.builder(ClassNames.list(JavaTypes.String), requiredFieldsFieldName, PUBLIC, STATIC, FINAL)
    if (fieldNames.isEmpty) {
      fieldSpec.initializer("$T.emptyList", JavaTypes.Collections)
    } else {
      fieldSpec.initializer(
        "$T.unmodifiableList(new $T($T.asList($L)))",
        JavaTypes.Collections,
        linkedList(JavaTypes.String),
        JavaTypes.Arrays,
        fieldNames.map(name => s"Fields.${JavaPojoUtil.toFieldName(name)} ").mkString(",")
      )
    }
    fieldSpec.build()
  }

  def toMinStringValueLengthStaticFieldName(field: Field): String = {
    toStaticFieldName(field.name) + "_MIN" + "_ENTRY_LENGTH"
  }

  def toMaxStringValueLengthStaticFieldName(field: Field): String = {
    toStaticFieldName(field.name) + "_MAX" + "_ENTRY_LENGTH"
  }


  def getStringValueLengthStaticFields(model: Model): Seq[FieldSpec] = {
    model.fields.filter(JavaPojoUtil.isParameterArray).filter(_.`type` == "[string]").flatMap(
      field => {
        val optional = attribute.StringValueLength.fromField(field)
        if (optional.isEmpty) {
          throw new IllegalArgumentException(s"String array fields must have a ${StringValueLength.attributeName} attribute. Model=${model.name} Field=${field.name}")
        }
        val stringValueLength = optional.get
        val out = Seq[FieldSpec](
          FieldSpec.builder(TypeName.INT, toMinStringValueLengthStaticFieldName(field), PUBLIC, STATIC, FINAL)
            .initializer("$L", stringValueLength.minimum.toString)
            .build(),
          FieldSpec.builder(TypeName.INT, toMaxStringValueLengthStaticFieldName(field), PUBLIC, STATIC, FINAL)
            .initializer("$L", stringValueLength.maximum.toString)
            .build(),

          FieldSpec.builder(TypeName.INT, toMinFieldStaticFieldName(field, Some("string")), PUBLIC, STATIC, FINAL)
            .initializer("$L", if (field.required) "1" else field.minimum.getOrElse(0).toString)
            .build(),
          FieldSpec.builder(TypeName.INT, toMaxFieldStaticFieldName(field, Some("string")), PUBLIC, STATIC, FINAL)
            .initializer("$L", field.maximum.get.toString)
            .build()
        )
        out
      }
    )
  }


  def toMinListSizeStaticFieldName(field: Field): String = {
    toStaticFieldName(field.name) + "_MIN" + "_SIZE"
  }

  def toMaxListSizeStaticFieldName(field: Field): String = {
    toStaticFieldName(field.name) + "_MAX" + "_SIZE"
  }

  def getListSizeStaticFields(model: Model): Seq[FieldSpec] = {
    model.fields.filter(JavaPojoUtil.isParameterArray).filter(
      field => field.maximum.isDefined || field.minimum.isDefined)
      .map(
        field => {
          Seq(
            FieldSpec.builder(TypeName.INT, toMinListSizeStaticFieldName(field), PUBLIC, STATIC, FINAL)
              .initializer("$L",
                field.minimum.getOrElse(if (field.required) "1" else "0").toString
              )
              .addJavadoc("Added By getListSizeStaticFields")
              .build(),
            FieldSpec.builder(TypeName.INT, toMaxListSizeStaticFieldName(field), PUBLIC, STATIC, FINAL)
              .initializer("$L", field.maximum.getOrElse(Integer.MAX_VALUE).toString)
              .addJavadoc("Added By getListSizeStaticFields")
              .build()
          )
        }
      ).flatten
  }

  def getSizeStaticFields(model: Model): Seq[FieldSpec] = {
    model.fields.filter(_.`type` == "string")
      .map(
        field => {
          Seq(
            FieldSpec.builder(TypeName.INT, toMinFieldStaticFieldName(field), PUBLIC, STATIC, FINAL)
              .initializer("$L",
                field.minimum.getOrElse(if (field.required) "1" else "0").toString
              )
              .addJavadoc("Added By getSizeStaticFields")
              .build(),
            FieldSpec.builder(TypeName.INT, toMaxFieldStaticFieldName(field), PUBLIC, STATIC, FINAL)
              .initializer("$L", field.maximum.getOrElse(Integer.MAX_VALUE).toString)
              .addJavadoc("Added By getSizeStaticFields")
              .build()
          )
        }
      ).flatten
  }

  //  def getSizeAttributesForStringList(model: Model): Seq[FieldSpec] = {
  //    val out = Seq[FieldSpec]()
  //    model.fields.filter(JavaPojoUtil.isParameterArray)
  //      .filter(_.`type` == "[string]")
  //      .map(
  //        field => {
  //
  //
  //          Seq(
  //            FieldSpec.builder(
  //              TypeName.INT,
  //              toMinFieldStaticFieldName(field, Some("string")),
  //              PUBLIC, STATIC, FINAL
  //            ).build(),
  //            FieldSpec.builder(
  //              TypeName.INT,
  //              toMaxFieldStaticFieldName(field, Some("string")),
  //              PUBLIC, STATIC, FINAL
  //            ).build()
  //          )
  //        }
  //      ).flatten
  //  }


  def handleSizeAttribute(classSpec: TypeSpec.Builder, field: Field): AnnotationSpec = {
    val isString = (field.`type` == "string")

    val isList = JavaPojoUtil.isParameterArray(field)


    val minStaticParamName = toMinFieldStaticFieldName(field)
    val maxStaticParamName = toMaxFieldStaticFieldName(field)
    val spec = AnnotationSpec.builder(size)

    if (isList) {
      return spec.addMember("min", "$L", minStaticParamName)
        .addMember("max", "$L", minStaticParamName)
        .build()
    }

    val hasMin = field.minimum.isDefined
    val hasMax = field.maximum.isDefined

    if (hasMin || hasMax) {
      //sLOG.trace("field.minimum.isDefined")
      spec.addMember("min", "$L", minStaticParamName)
      //      classSpec.addField(
      //        FieldSpec.builder(INT, minStaticParamName, PUBLIC, STATIC, FINAL).initializer("$L", field.minimum.getOrElse(1).toString)
      //          .addJavadoc(s"The minimum ${if (isString) "length" else "size"} of the field ${JavaPojoUtil.toParamName(field.name, true)}. Useful for reflection test rigging.")
      //          .build()
      //      )
    }

    if (hasMax) {
      //LOG.trace("{} field.maximum.isDefined=true",field.name)
      spec.addMember("max", "$L", maxStaticParamName)
      //      classSpec.addField(
      //        FieldSpec.builder(INT, maxStaticParamName, PUBLIC, STATIC, FINAL).initializer("$L", field.maximum.get.toString)
      //          .addJavadoc(s"The maximum ${if (isString) "length" else "size"} of the field ${JavaPojoUtil.toParamName(field.name, true)}. Useful for reflection test rigging.")
      //          .build()
      //      )
    } else {
      throw new IllegalArgumentException(s"The field ${field.name} has a minimum defined but no maximum, spec validation should have caught this")
    }
    spec.build()
  }

}
