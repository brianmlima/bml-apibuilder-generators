package bml.util.persist

import bml.util.Text
import bml.util.attribute.FieldRef
import bml.util.java.ClassNames.JavaxTypes.{JavaxPersistanceTypes, JavaxValidationTypes}
import bml.util.java.ClassNames.SpringTypes.SpringValidationTypes
import bml.util.java.{JavaPojoUtil, JavaPojos}
import bml.util.jpa.JPA
import bml.util.spring.SpringVersion.SpringVersion
import com.squareup.javapoet.{AnnotationSpec, ClassName, CodeBlock, ParameterizedTypeName, TypeName}
import io.apibuilder.spec.v0.models.{Attribute, Field, Model, Service}

object SpringVariableTypes {

  class AType(val className: String, val subPackage: String) {

    private val javax = "javax"
    private val jakarta = "jakarta"

    private def getTopLevelPackage(springVersion: SpringVersion): String = {
      springVersion match {
        case bml.util.spring.SpringVersion.SIX => jakarta
        case bml.util.spring.SpringVersion.FIVE => javax
      }
    }

    def toClassName(springVersion: SpringVersion): ClassName = {
      ClassName.get(s"${getTopLevelPackage(springVersion)}.${subPackage}", className)
    }


  }

  object AType {
    def apply(className: String, subPackage: String) = new AType(className, subPackage)
  }


  object ValidationTypes {
    val NotNull = AType("NotNull", "validation.constraints")
    val NotBlank = AType("NotBlank", "validation.constraints")
    val NotEmpty = AType("NotEmpty", "validation.constraints")
    val Pattern = AType("Pattern", "validation.constraints")
    val Size = AType("Size", "validation.constraints")
    val Email = AType("Email", "validation.constraints")
    val Valid = AType("Valid", "validation")
    val Validation = AType("Validation", "validation")
    val Validator = AType("Validator", "validation")
    val ConstraintViolation = AType("ConstraintViolation", "validation")
  }

  object ValidationAnnotations {

    import com.squareup.javapoet._


    def Email(springVersion: SpringVersion) = AnnotationSpec.builder(ValidationTypes.Email.toClassName(springVersion)).build()

    def NotNull(springVersion: SpringVersion) = AnnotationSpec.builder(ValidationTypes.NotNull.toClassName(springVersion)).build()

    def NotBlank(springVersion: SpringVersion) = AnnotationSpec.builder(ValidationTypes.NotBlank.toClassName(springVersion)).build()

    def NotEmpty(springVersion: SpringVersion) = AnnotationSpec.builder(ValidationTypes.NotEmpty.toClassName(springVersion)).build()

    def Validated(springVersion: SpringVersion) = AnnotationSpec.builder(SpringValidationTypes.Validated).build()

    def Valid(springVersion: SpringVersion) = AnnotationSpec.builder(ValidationTypes.Valid.toClassName(springVersion)).build()

    def Size(springVersion: SpringVersion, min: Int, max: Int): AnnotationSpec = {
      AnnotationSpec.builder(ValidationTypes.Size.toClassName(springVersion))
        .addMember("min", "$L", new Integer(min))
        .addMember("max", "$L", new Integer(max))
        .build()
    }

    def Size(springVersion: SpringVersion, min: Option[Long], max: Option[Long]): AnnotationSpec = {
      val spec = AnnotationSpec.builder(ValidationTypes.Size.toClassName(springVersion))
      if (min.isDefined) spec.addMember("min", "$L", min.get.toString)
      if (max.isDefined) spec.addMember("max", "$L", max.get.toString)
      spec.build()
    }

    def Size(springVersion: SpringVersion, attribute: Attribute): AnnotationSpec = {
      Size(springVersion, (attribute.value \ "min").as[Int], (attribute.value \ "max").as[Int])
    }

    def Pattern(springVersion: SpringVersion, regexp: String): AnnotationSpec = {
      AnnotationSpec.builder(ValidationTypes.Pattern.toClassName(springVersion))
        .addMember("regexp", "$S", regexp)
        .build()
    }

    def Pattern(springVersion: SpringVersion, attribute: Attribute): AnnotationSpec = {
      Pattern(
        springVersion, regexp = (attribute.value \ "regexp").as[String]
      )
    }
  }


  object PersistenceTypes {

    val Basic = AType("Basic", "persistence")
    val Column = AType("Column", "persistence")
    val Entity = AType("Entity", "persistence")
    val GeneratedValue = AType("GeneratedValue", "persistence")

    val GenerationType = AType("GenerationType", "persistence")

    val Id = AType("Id", "persistence")

    val MappedSuperclass = AType("MappedSuperclass", "persistence")

    val PrePersist = AType("PrePersist", "persistence")

    val PreUpdate = AType("PreUpdate", "persistence")

    val Temporal = AType("Temporal", "persistence")

    val TemporalType = AType("TemporalType", "persistence")

    val Version = AType("Version", "persistence")

    val Table = AType("Table", "persistence")

    val ManyToOne = AType("ManyToOne", "persistence")


    val OneToMany = AType("OneToMany", "persistence")

    val OneToOne = AType("OneToOne", "persistence")

    val CascadeType = AType("CascadeType", "persistence")

    val JoinColumn = AType("JoinColumn", "persistence")

    val JoinTable = AType("JoinTable", "persistence")

    val AttributeConverter = AType("AttributeConverter", "persistence")

    val Converter = AType("Converter", "persistence")

    val Convert = AType("Convert", "persistence")
  }

  object PersistenceAnnotations {

    def Converter(springVersion: SpringVersion, autoApply: Boolean) = AnnotationSpec.builder(PersistenceTypes.Converter.toClassName(springVersion))
      .addMember("autoApply", "$L", if (autoApply) "true" else "false")
      .build()

    def Convert(springVersion: SpringVersion, converterClassName: ClassName) = AnnotationSpec.builder(PersistenceTypes.Convert.toClassName(springVersion))
      .addMember("converter", "$T.class", converterClassName)
      .build()

    def JoinColumn(springVersion: SpringVersion, field: Field) = AnnotationSpec.builder(PersistenceTypes.JoinColumn.toClassName(springVersion))
      .addMember("name", "$S", Text.camelToSnakeCase(field.name) + "_id")
      .build()

    def JoinColumn(springVersion: SpringVersion, columnName: String) = AnnotationSpec.builder(PersistenceTypes.JoinColumn.toClassName(springVersion))
      .addMember("name", "$S", columnName)
      .build()

    def JoinColumn(springVersion: SpringVersion, service: Service, field: Field) = {
      val isModel = JavaPojoUtil.isModelType(service, field)
      val isList = JavaPojoUtil.isListOfModeslType(service, field)
      val spec = AnnotationSpec.builder(PersistenceTypes.JoinColumn.toClassName(springVersion))
      if (isModel) {
        spec.addMember("name", "$S", Text.camelToSnakeCase(field.name) + "_id")
      }
      if (isList) {
        val fieldRef = FieldRef.fromField(field)
        spec.addMember("name", "$S", Text.camelToSnakeCase(if (fieldRef.isDefined) fieldRef.get.field else ""))
      }
      spec.build()
    }

    def JoinTable(springVersion: SpringVersion, service: Service, className: ClassName, field: Field) = {

      val fromTableName = Text.camelToSnakeCase(JPA.toTableName(className));
      val fromColumnName = Text.camelToSnakeCase(JPA.toColumnName(field));
      val toTableName = Text.camelToSnakeCase(JavaPojoUtil.getArrayType(field.`type`));

      AnnotationSpec.builder(JavaxPersistanceTypes.JoinTable)
        .addMember("name", "$S", s"${fromTableName}__map__${fromColumnName}__to__${toTableName}")
        .addMember("joinColumns", "$L", JoinColumn(springVersion, fromTableName))
        .addMember("inverseJoinColumns", "$L", JoinColumn(springVersion, toTableName))
        .build()
    }

    def ManyToOne(springVersion: SpringVersion) = AnnotationSpec.builder(PersistenceTypes.ManyToOne.toClassName(springVersion)).build()

    def OneToMany(springVersion: SpringVersion) = AnnotationSpec.builder(PersistenceTypes.OneToMany.toClassName(springVersion)).build()

    def Basic(springVersion: SpringVersion, optional: Boolean) = AnnotationSpec.builder(PersistenceTypes.Basic.toClassName(springVersion))
      .addMember("optional", "$L", optional.toString)
      .build()

    def Id(springVersion: SpringVersion) = AnnotationSpec.builder(PersistenceTypes.Id.toClassName(springVersion)).build()

    def GeneratedValue(springVersion: SpringVersion, strategy: CodeBlock) = AnnotationSpec.builder(PersistenceTypes.GeneratedValue.toClassName(springVersion))
      .addMember("strategy", strategy).build()

    def Column(springVersion: SpringVersion, fieldName: String): AnnotationSpec = {
      AnnotationSpec.builder(PersistenceTypes.Column.toClassName(springVersion))
        .addMember("name", "$S", Text.camelToSnakeCase(fieldName))
        .build()
    }

    def Column(springVersion: SpringVersion, field: Field): AnnotationSpec = {
      val isId = (field.name == "id")
      val isUUID = (field.`type` == "uuid")
      val isString = (field.`type` == "string")

      val spec = AnnotationSpec.builder(PersistenceTypes.Column.toClassName(springVersion))
        .addMember("name", "$S", JPA.toColumnName(field))
        .addMember("nullable", "$L", (!field.required).toString)
        .addMember("unique", "$L", (isId || field.annotations.find(_ == "unique").isDefined).toString)

      if (isString && field.maximum.isDefined) {
        spec.addMember("length", "$L", JavaPojos.toMaxFieldStaticFieldName(field))
      }

      if (isUUID && isId) {
        spec.addMember("updatable", "$L", false.toString)
          .addMember("insertable", "$L", true.toString)
      }
      spec.build()
    }

    def Table(springVersion: SpringVersion, model: Model): AnnotationSpec = {
      AnnotationSpec.builder(PersistenceTypes.Table.toClassName(springVersion)).addMember("name", "$S", JPA.toTableName(model)).build()
    }

    def Entity(springVersion: SpringVersion) = AnnotationSpec.builder(PersistenceTypes.Entity.toClassName(springVersion)).build()

    def Version(springVersion: SpringVersion) = AnnotationSpec.builder(PersistenceTypes.Version.toClassName(springVersion)).build()

    def TemporalTIMESTAMP(springVersion: SpringVersion) = AnnotationSpec.builder(PersistenceTypes.Temporal.toClassName(springVersion)).addMember("value", "$T.TIMESTAMP", PersistenceTypes.TemporalType.toClassName(springVersion)).build()

    def GeneratedValue(springVersion: SpringVersion, generator: String) = AnnotationSpec.builder(PersistenceTypes.GeneratedValue.toClassName(springVersion))
      .addMember("generator", "$S", generator)
      .build()
  }

}
