package bml.util.java

import com.squareup.javapoet.{AnnotationSpec, FieldSpec, TypeName, TypeSpec}
import io.apibuilder.spec.v0.models.Field
import javax.lang.model.element.Modifier._
import JavaPojoUtil.toStaticFieldName
import bml.util.java.ClassNames.size
import com.squareup.javapoet.TypeName.INT
import net.bytebuddy.implementation.bytecode.Throw
import play.api.Logger
import play.api.libs.json.JsResult.Exception

import scala.collection.JavaConverters._

object JavaPojos {
  val LOG: Logger = Logger.apply(this.getClass())

  def handleSizeAttribute(classSpec: TypeSpec.Builder, field: Field) = {
    val isString = (field.`type` == "string")
    val minStaticParamName = toStaticFieldName(field.name) + "_MIN" + (if (isString) "_LENGTH" else "_SIZE")
    val maxStaticParamName = toStaticFieldName(field.name) + "_MAX" + (if (isString) "_LENGTH" else "_SIZE")
    val spec = AnnotationSpec.builder(size)

    val hasMin = field.minimum.isDefined
    val hasMax = field.maximum.isDefined

    if (hasMin || hasMax) {
      LOG.info("field.minimum.isDefined")
      spec.addMember("min", "$L", minStaticParamName)
      classSpec.addField(
        FieldSpec.builder(INT, minStaticParamName, PUBLIC, STATIC, FINAL).initializer("$L", field.minimum.getOrElse(1).toString).build()
      )
    }

    if (hasMax) {
      LOG.info("{} field.maximum.isDefined=true")
      spec.addMember("max", "$L", maxStaticParamName)
      classSpec.addField(
        FieldSpec.builder(INT, maxStaticParamName, PUBLIC, STATIC, FINAL).initializer("$L", field.maximum.get.toString).build()
      )
    } else {
      throw new IllegalArgumentException(s"The field ${field.name} has a minimum defined but no maximum, spec validation should have caught this")
    }
    spec.build()
  }

}
