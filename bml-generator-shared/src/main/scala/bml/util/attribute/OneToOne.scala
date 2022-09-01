package bml.util.attribute


import bml.util.attribute
import bml.util.java.ClassNames.JavaxTypes.JavaxPersistanceTypes
import com.squareup.javapoet.AnnotationSpec
import io.apibuilder.spec.v0.models.Field
import javax.persistence.{CascadeType, OneToOne}


class OneToOne(val mappedBy: String, cascadeType: Option[CascadeType]) {

  def toAnnotation(): AnnotationSpec = {

    val builder = AnnotationSpec.builder(JavaxPersistanceTypes.OneToOne)
      .addMember("mappedBy", "%S", this.mappedBy)

    if (cascadeType.isDefined) {
      builder.addMember("cascade", "$T.$L", JavaxPersistanceTypes.CascadeType, cascadeType.get.name()
      )
    }

    builder.build()
  }

}

object OneToOne {

  val cascades = CascadeType.values().zipWithIndex.map { case (v, i) => (v.name().toLowerCase, v) }.toMap


  val attributeName = "one_to_many"
  val mapedByValueKey = "mapped_by"
  val cascadeTypeValueKey = "cascade_type"

  def fromField(field: Field): Option[OneToOne] = {
    val optional = field.attributes.find(_.name == attributeName)
    if (optional.isEmpty) {
      return None
    }
    val value = optional.get.value
    val mappedBy = (value \ mapedByValueKey).as[String]
    val cascadeKey = (value \ cascadeTypeValueKey).as[String]
    val cascadeType = cascades.get(cascadeKey)
    if (cascadeType.isEmpty) {
      Some(OneToOne(mappedBy, Option.empty))
    } else {
      Some(OneToOne(mappedBy, cascadeType))
    }
  }

  def apply(mappedBy: String, cascadeType: Option[CascadeType]) = new OneToOne(mappedBy, cascadeType)

}
