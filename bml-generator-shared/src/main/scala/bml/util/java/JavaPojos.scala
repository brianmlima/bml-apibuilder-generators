package bml.util.java


import bml.util.AnotationUtil.HibernateAnnotations
import bml.util.AnotationUtil.JavaxAnnotations.JavaxPersistanceAnnotations
import bml.util.attribute.FieldRef
import bml.util.{NameSpaces, attribute}
import bml.util.java.ClassNames.JavaTypes
import bml.util.java.ClassNames.JavaxTypes.JavaxValidationTypes
import bml.util.java.JavaPojoUtil.toStaticFieldName
import bml.util.persist.UUIDIfNullGenerator
import com.squareup.javapoet.TypeName.BOOLEAN
import com.squareup.javapoet._
import io.apibuilder.spec.v0.models.{Field, Model, Service}
import javax.lang.model.element.Modifier._
import play.api.Logger

object JavaPojos {
  private val LOG: Logger = Logger.apply(this.getClass())

  def toMinFieldStaticFieldName(field: Field, overrideType: Option[String] = None): String = {
    toStaticFieldName(field.name) + "_MIN" + (if (overrideType.getOrElse(field.`type`) == "string") "_LENGTH" else "_SIZE")
  }

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
    val fieldSpec = FieldSpec.builder(JavaTypes.List(JavaTypes.String), requiredFieldsFieldName, PUBLIC, STATIC, FINAL)
      .addJavadoc("$L is a generated field of fields that should never be null. This is handy for use in reflection based error logging and validation.", requiredFieldsFieldName)
    if (fieldNames.isEmpty) {
      fieldSpec.initializer("$T.emptyList()", JavaTypes.Collections)
    } else {
      fieldSpec.initializer(
        "$T.unmodifiableList(new $T($T.asList($L)))",
        JavaTypes.Collections,
        JavaTypes.LinkedList(JavaTypes.String),
        JavaTypes.Arrays,
        fieldNames.map(name => s"Fields.${JavaPojoUtil.toFieldName(name)} ").mkString(",")
      )
    }
    fieldSpec.build()
  }

  def toMinStringValueLengthStaticFieldName(field: Field): String = {
    toMinStringValueLengthStaticFieldName(field.name)
  }

  def toMinStringValueLengthStaticFieldName(fieldName: String): String = {
    toStaticFieldName(fieldName) + "_MIN" + "_ENTRY_LENGTH"
  }

  def toMaxStringValueLengthStaticFieldName(field: Field): String = {
    toMaxStringValueLengthStaticFieldName(field.name)
  }

  def toMaxStringValueLengthStaticFieldName(fieldName: String): String = {
    toStaticFieldName(fieldName) + "_MAX" + "_ENTRY_LENGTH"
  }


  def getStringValueLengthStaticFields(model: Model): Seq[FieldSpec] = {
    model.fields.filter(JavaPojoUtil.isParameterArray).filter(_.`type` == "[string]").flatMap(
      field => {
        var optional = attribute.StringValueLength.fromField(field)
        if (optional.isEmpty) {
          optional = Some(attribute.StringValueLength(0, 1024))
          //          throw new IllegalArgumentException(s"String array fields must have a ${StringValueLength.attributeName} attribute. Model=${model.name} Field=${field.name}")
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
            .initializer("$L", field.maximum.getOrElse(1024).toString)
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
    model.fields.filter(JavaPojoUtil.isParameterArray)
      //      .filter(field => field.maximum.isDefined || field.minimum.isDefined)
      .map(
        field => {
          Seq(
            FieldSpec.builder(TypeName.INT, toMinListSizeStaticFieldName(field), PUBLIC, STATIC, FINAL)
              .initializer("$L",
                field.minimum.getOrElse("0").toString
              )
              .addJavadoc("Generated, provides a static reference to the minimum size of the list field $L.", JavaPojoUtil.toFieldName(field.name))
              .build(),
            FieldSpec.builder(TypeName.INT, toMaxListSizeStaticFieldName(field), PUBLIC, STATIC, FINAL)
              .initializer("$L", field.maximum.getOrElse(Integer.MAX_VALUE).toString)
              .addJavadoc("Generated, provides a static reference to the maximum size of the list field $L.", JavaPojoUtil.toFieldName(field.name))
              .build()
          )
        }
      ).flatten
  }


  //private

  def getApiPathElement(service: Service, model: Model): FieldSpec = {
    FieldSpec.builder(JavaTypes.String, "API_PATH_ELEMENT", PUBLIC, STATIC, FINAL)
      .initializer("$S", s"/v${service.version.split("\\.")(0)}/${model.plural}")
      .build()
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
              .addJavadoc("Generated, provides a static reference to the minimum size of the field $L.", JavaPojoUtil.toFieldName(field.name))
              .build(),
            FieldSpec.builder(TypeName.INT, toMaxFieldStaticFieldName(field), PUBLIC, STATIC, FINAL)
              .initializer("$L", field.maximum.getOrElse(2048).toString)
              .addJavadoc("Generated, provides a static reference to the maximum size of the field $L.", JavaPojoUtil.toFieldName(field.name))
              .build()
          )
        }
      ).flatten
  }

  def handleSizeAttribute(className: ClassName, field: Field): Option[AnnotationSpec] = {
    val isString = (field.`type` == "string")
    val isList = JavaPojoUtil.isParameterArray(field)

    if (!isString && !isList) {
      return None
    }

    val minStaticParamName = toMinFieldStaticFieldName(field)
    val maxStaticParamName = toMaxFieldStaticFieldName(field)
    val spec = AnnotationSpec.builder(JavaxValidationTypes.Size)


    if (isList) {
      return Some(spec.addMember("min", "$T.$L", className, minStaticParamName)
        .addMember("max", "$T.$L", className, maxStaticParamName)
        .build())
    }

    val hasMin = field.minimum.isDefined
    val hasMax = field.maximum.isDefined

    if (hasMin || hasMax) {
      spec.addMember("min", "$T.$L", className, minStaticParamName)
    }

    //    if (hasMax) {
    //LOG.trace("{} field.maximum.isDefined=true",field.name)
    spec.addMember("max", "$T.$L", className, maxStaticParamName)
    //    }
    //    else {
    //      throw new IllegalArgumentException(s"The field ${field.name} has a minimum defined but no maximum, spec validation should have caught this")
    //    }
    Some(spec.build())
  }

  def handlePersisitanceAnnontations(service: Service, className: ClassName, field: Field): Seq[AnnotationSpec] = {
    val isId = (field.name == "id")
    val isUUID = (field.`type` == "uuid")
    val isString = (field.`type` == "string")

    val isModel = JavaPojoUtil.isModelType(service, field)
    val isList = JavaPojoUtil.isParameterArray(field)

    var out: Seq[AnnotationSpec] = Seq()
    out ++ Seq(JavaxPersistanceAnnotations.Id)

    out = out ++ Seq(JavaxPersistanceAnnotations.Basic(field.required))

    if (isId) {
      LOG.info(s"Found id field in class=${className.toString}")
      out = out ++ Seq(JavaxPersistanceAnnotations.Id)
    }
    if (isModel) {
      out = out ++ Seq(JavaxPersistanceAnnotations.ManyToOne, JavaxPersistanceAnnotations.JoinColumn(service, field))
    } else if (JavaPojoUtil.isListOfModeslType(service, field)) {


      if(FieldRef.fromField(field).isDefined){
        out = out ++ Seq(JavaxPersistanceAnnotations.OneToMany, JavaxPersistanceAnnotations.JoinColumn(service, field))
      }else{
        out = out ++ Seq(JavaxPersistanceAnnotations.OneToMany, JavaxPersistanceAnnotations.JoinTable(service, className, field))
      }




    } else {
      out = out ++ Seq(JavaxPersistanceAnnotations.Column(field))
    }
    if (isId && isUUID) {
      val nameSpaces = new NameSpaces(service)
      out = out ++ Seq(
        JavaxPersistanceAnnotations.GeneratedValue("UUID"),
        HibernateAnnotations.GenericGenerator("UUID", UUIDIfNullGenerator.className(nameSpaces).canonicalName())
      )
    }
    out
  }

}
