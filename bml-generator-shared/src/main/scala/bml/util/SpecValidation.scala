package bml.util

import bml.util.attribute.{FieldRef, Hibernate}
import bml.util.java.JavaPojoUtil
import io.apibuilder.spec.v0.models.{Method, Operation, ResponseCodeInt, Service}
import lombok.extern.slf4j.Slf4j
import org.slf4j.LoggerFactory
import play.api.libs.json.JsNumber

@Slf4j
object SpecValidation {
  val log = LoggerFactory.getLogger(this.getClass)

  def validate(service: Service, header: Option[String]): Option[Seq[String]] = {
    val errors =
      Seq(
//                checkAllPathVarOperationsHave404(service),
//                checkAllFieldsWithMinRequirementHaveAMax(service),
//                checkAllModelsHaveADescription(service),
//                checkAllStringFieldsHaveMinMax(service),
//                checkAllModelsDontHaveUnderscores(service),
//                checkAllModelFieldsDontHaveUnderscores(service),
//                checkAllEnumsDontHaveUnderscores(service),
//                checkAllStringArraysHaveAStringValueLengthAttribute(service),
//                checkAllArraysHaveMaximum(service),
        checkAllHibernateModelsHaveFieldRefWhereNecessary(service),
        checkAllPostOperationsHaveBody(service)
      ).flatten
    if (errors.isEmpty) {
      None
    } else {
      Some(errors)
    }
  }


  def checkAllHibernateModelsHaveFieldRefWhereNecessary(service: Service): Seq[String] = {
    var out: Seq[String] = Seq()

    service.models.filter(Hibernate.fromModel(_).use)
      .foreach(
        model =>
          model.fields.foreach(
            field =>
              if (JavaPojoUtil.isListOfModeslType(service, field)) {
                if (FieldRef.fromField(field).isEmpty) {
                  val msg = s"ERROR: A model that has the hibernate attribute has a field that is and array of models and does not have a field_ref attribute defined. This is used to derrive foreign keys and is required. Service='${service.name}' Model '${model.name}' Field='${field.name}'"
                  log.error(msg)
                  out = out ++ Seq(msg)
                }
              }
          )
      )
    out
  }


  def checkAllArraysHaveMaximum(service: Service): Seq[String] = {
    var out: Seq[String] = Seq()
    service.models.foreach(
      model =>
        model.fields.foreach(
          field => {
            if (JavaPojoUtil.isParameterArray(field)) {
              if (field.maximum.isEmpty) {
                val msg = s"ERROR: All Fields of array type must have an maximum set for array sizing constraints. This eliminates recursion problems in self referential models. Organization='${service.organization.key}' Service='${service.name}' Model '${model.name}' Field='${field.name}'"
                log.error(msg)
                out = out ++ Seq(msg)
              }
            }
          }
        )
    )
    out
  }


  def checkAllStringArraysHaveAStringValueLengthAttribute(service: Service): Seq[String] = {
    val value = "string_value_length"
    var out: Seq[String] = Seq()
    service.models.foreach(
      model =>
        model.fields.filter(JavaPojoUtil.isParameterArray)
          .filter(_.`type` == "[string]").foreach(
          field => {
            val option = field.attributes.find(_.name == value)

            if (option.isEmpty) {
              val foundAttributes = field.attributes.map(_.name).mkString(", ")
              out = out ++ Seq(s"ERROR: All Fields with a type of [string] must have an ${value} attribute. Service='${service.name}' Model '${model.name}' Field='${field.name}' Found Attributes = ${foundAttributes}")
            } else {
              val attribute = option.get
              val minimumValue = attribute.value.value.get("minimum")
              val maximumValue = attribute.value.value.get("maximum")
              if (minimumValue.isEmpty) {
                out = out ++ Seq(s"ERROR: All ${value} attributes must have a minimum field defined in their value object. Service='${service.name}' Model '${model.name}' Field='${field.name}' Attribute='${value}'")
              } else {
                if (!minimumValue.get.isInstanceOf[JsNumber]) {
                  out = out ++ Seq(s"ERROR: All ${value} attributes must have a minimum field defined as an Integer in their value object. Service='${service.name}' Model '${model.name}' Field='${field.name}' Attribute='${value}' minimum='${minimumValue.get.toString()}'")
                }
              }
              if (maximumValue.isEmpty) {
                out = out ++ Seq(s"ERROR: All ${value} attributes must have a maximum field defined in their value object. Service='${service.name}' Model '${model.name}' Field='${field.name}' Attribute='${value}'")
              } else {
                if (!maximumValue.get.isInstanceOf[JsNumber]) {
                  out = out ++ Seq(s"ERROR: All ${value} attributes must have a maximum field defined as an Integer in their value object. Service='${service.name}' Model '${model.name}' Field='${field.name}' Attribute='${value}' minimum='${maximumValue.get.toString()}'")
                }
              }
            }
          }
        )
    )
    out
  }


  def checkAllModelsHaveADescription(service: Service): Seq[String] = {
    var out: Seq[String] = Seq()
    service.models.foreach(
      model =>
        if (model.description.isEmpty) {
          out = out ++ Seq(s"ERROR: All Models must have a description. Service='${service.name}' Model '${model.name}'")
        }
    )
    out
  }

  def checkAllModelsDontHaveUnderscores(service: Service): Seq[String] = {
    var out: Seq[String] = Seq()
    service.models.foreach(
      model => {
        val name = model.name.replaceFirst("_form$", "")
        if (name.contains("_") || name.contains("-")) {
          out = out ++ Seq(s"ERROR: All Model names should be camel case and must not have underscores or hyphens except the suffix _form. Service='${service.name}'=Model='${model.name}'")
        }
      }
    )
    out
  }

  def checkAllEnumsDontHaveUnderscores(service: Service): Seq[String] = {
    var out: Seq[String] = Seq()
    service.enums.foreach(
      `enum` =>
        if (`enum`.name.contains("_") || `enum`.name.contains("-")) {
          out = out ++ Seq(s"ERROR: All Enum names should be camel case and must not have underscores or hyphens. Service='${service.name}'=Model='${`enum`.name}'")
        }
    )
    out
  }


  def checkAllModelFieldsDontHaveUnderscores(service: Service): Seq[String] = {
    var out: Seq[String] = Seq()
    service.models.foreach(
      model =>
        model.fields.foreach(
          field =>
            if (field.name.contains("_") || field.name.contains("-")) {
              out = out ++ Seq(s"ERROR: All Model Field names should be camel case and must not have underscores or hyphens. Service='${service.name}' Model='${model.name}' Field='${field.name}'")
            }
        )
    )
    out
  }


  def checkAllStringFieldsHaveMinMax(service: Service): Seq[String] = {
    var out: Seq[String] = Seq()
    service.models.foreach(
      model =>
        model.fields.foreach(
          field => {
            if (field.`type` == "string") {
              if (field.minimum.isEmpty) {
                out = out ++ Seq(s"ERROR: All Model Fields must have a 'minimum' field defined. Service='${service.name}' Model='${model.name}' Field='${field.name}'")
              }
              if (field.maximum.isEmpty) {
                out = out ++ Seq(s"ERROR: All Model Fields must have a 'maximum' field defined. Service='${service.name}' Model='${model.name}' Field='${field.name}'")
              }
            }
          }
        )
    )
    out
  }


  def checkAllFieldsWithMinRequirementHaveAMax(service: Service): Seq[String] = {
    var out: Seq[String] = Seq()
    service.models.foreach(
      model =>
        model.fields.foreach(
          field =>
            if (field.minimum.isDefined && field.maximum.isEmpty) {
              out = out ++ Seq(s"ERROR: Model Fields that have a minimum set must have a maximum. Model '${model.name}' field '${field.name}'")
            }
        )
    )
    out
  }


  def checkAllPathVarOperationsHave404(service: Service): Seq[String] = {
    var out: Seq[String] = Seq()
    service.resources.foreach(
      resource =>
        resource.operations.foreach(
          operation =>
            if (hasPathParam(operation)) {
              if (!has404(operation)) {
                out = out ++ Seq(s"ERROR: Resource Operations that contain path parameters must have 404 response defined. Resource '${resource.`type`}' Operation '${operation.method} ${operation.path}'")
              } else {
                if (!findResponseCode(operation, 404).get.`type`.equals("unit")) {
                  out = out ++ Seq(s"ERROR: Resource Operations that contain path parameters must have 404 response defined with a unit type. Resource '${resource.`type`}' Operation '${operation.method} ${operation.path}' exepced type unit ")
                }
              }
            }
        )
    )
    out
  }

  def checkAllPostOperationsHaveBody(service: Service): Seq[String] = {
    var out: Seq[String] = Seq()
    service.resources.foreach(
      resource =>
        resource.operations.foreach(
          operation =>
            if (operation.method == Method.Post) {
              if (operation.body.isEmpty) {
                out = out ++ Seq(s"ERROR: Post Resource Operations have to have a body defined. Resource '${resource.`type`}' Operation '${operation.method} ${operation.path}'")
              }
            }
        )
    )
    out
  }


  def has404AsUnit(operation: Operation) = {

  }


  def has404(operation: Operation) = {
    hasResponseCode(operation, 404)
  }

  def hasResponseCode(operation: Operation, code: Int) = {
    operation.responses.filter(
      response => response.code.equals(ResponseCodeInt.apply(code))
    ).nonEmpty
  }

  def findResponseCode(operation: Operation, code: Int) = {
    operation.responses.find(
      response => response.code.equals(ResponseCodeInt.apply(code))
    )
  }


  def hasPathParam(operation: Operation) = {
    operation.path.contains(":")
  }


}
