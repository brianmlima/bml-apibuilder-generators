package bml.util.persist

import bml.util.spring.SpringVersion
import bml.util.spring.SpringVersion.SpringVersion
import com.squareup.javapoet.{ClassName, ParameterizedTypeName, TypeName}
import javax.persistence.{AttributeConverter, CascadeType, GeneratedValue, GenerationType, JoinColumn, JoinTable, ManyToOne, MappedSuperclass, OneToMany, PrePersist, PreUpdate, TemporalType}

object PersistTypes {


  //  val map: Map[String, Function[SpringVersion, ClassName] =
  //    Map.apply(
  //      "NotBlank" -> (springVersion: SpringVersion) => ClassName.get(s"${getBase(_)}.validation.constraints", "NotBlank")
  //
  //  def NotEmpty = ClassName.get("javax.validation.constraints", "NotEmpty")
  //
  //  def Pattern = ClassName.get("javax.validation.constraints", "Pattern")
  //
  //  def Size = ClassName.get("javax.validation.constraints", "Size")
  //
  //  def Email = ClassName.get("javax.validation.constraints", "Email")
  //
  //  def Valid = ClassName.get("javax.validation", "Valid")
  //
  //  def Validation = ClassName.get("javax.validation", "Validation")
  //
  //  def Validator = ClassName.get("javax.validation", "Validator")
  //
  //  def ConstraintViolation = ClassName.get("javax.validation", "ConstraintViolation")


  //  )


  private val javax = "javax"
  private val jakarta = "jakarta"

  private def getBase(springVersion: SpringVersion): String = {
    springVersion match {
      case bml.util.spring.SpringVersion.SIX => javax
      case bml.util.spring.SpringVersion.FIVE => jakarta
    }
  }


  object ValidationTypes {


    def NotBlank(springVersion: SpringVersion) = ClassName.get(s"${getBase(springVersion)}.validation.constraints", "NotBlank")

    def NotEmpty(springVersion: SpringVersion) = ClassName.get(s"${getBase(springVersion)}.validation.constraints", "NotEmpty")

    def Pattern(springVersion: SpringVersion) = ClassName.get(s"${getBase(springVersion)}.validation.constraints", "Pattern")

    def Size(springVersion: SpringVersion) = ClassName.get(s"${getBase(springVersion)}.validation.constraints", "Size")

    def Email(springVersion: SpringVersion) = ClassName.get(s"${getBase(springVersion)}.validation.constraints", "Email")

    def Valid(springVersion: SpringVersion) = ClassName.get(s"${getBase(springVersion)}.validation", "Valid")

    def Validation(springVersion: SpringVersion) = ClassName.get(s"${getBase(springVersion)}.validation", "Validation")

    def Validator(springVersion: SpringVersion) = ClassName.get(s"${getBase(springVersion)}.validation", "Validator")

    def ConstraintViolation(springVersion: SpringVersion) = ClassName.get(s"${getBase(springVersion)}.validation", "ConstraintViolation")
  }

  object PersistenceTypes {
    private def persist(springVersion: SpringVersion) = s"${getBase(springVersion)}.persistence"

    def Basic(springVersion: SpringVersion) = ClassName.get(persist(springVersion), "Basic")

    def Column(springVersion: SpringVersion) = ClassName.get(persist(springVersion), "Column")

    def Entity(springVersion: SpringVersion) = ClassName.get(persist(springVersion), "Entity")

    def GeneratedValue(springVersion: SpringVersion) = ClassName.get(persist(springVersion), "GeneratedValue")

    def GenerationType(springVersion: SpringVersion) = ClassName.get(persist(springVersion), "GenerationType")

    def Id(springVersion: SpringVersion) = ClassName.get(persist(springVersion), "Id")

    def MappedSuperclass(springVersion: SpringVersion) = ClassName.get(persist(springVersion), "MappedSuperclass")

    def PrePersist(springVersion: SpringVersion) = ClassName.get(persist(springVersion), "PrePersist")

    def PreUpdate(springVersion: SpringVersion) = ClassName.get(persist(springVersion), "PreUpdate")

    def Temporal(springVersion: SpringVersion) = ClassName.get(persist(springVersion), "Temporal")

    def TemporalType(springVersion: SpringVersion) = ClassName.get(persist(springVersion), "TemporalType")

    def Version(springVersion: SpringVersion) = ClassName.get(persist(springVersion), "Version")

    def Table(springVersion: SpringVersion) = ClassName.get(persist(springVersion), "Table")

    def ManyToOne(springVersion: SpringVersion) = ClassName.get(persist(springVersion), "ManyToOne")

    def OneToMany(springVersion: SpringVersion) = ClassName.get(persist(springVersion), "OneToMany")

    def OneToOne(springVersion: SpringVersion) = ClassName.get(persist(springVersion), "OneToOne")

    def CascadeType(springVersion: SpringVersion) = ClassName.get(persist(springVersion), "CascadeType")

    def JoinColumn(springVersion: SpringVersion) = ClassName.get(persist(springVersion), "JoinColumn")

    def JoinTable(springVersion: SpringVersion) = ClassName.get(persist(springVersion), "JoinTable")

    def AttributeConverter(springVersion: SpringVersion) = ClassName.get(persist(springVersion), "AttributeConverter")

    def Converter(springVersion: SpringVersion) = ClassName.get(persist(springVersion), "Converter")

    def Convert(springVersion: SpringVersion) = ClassName.get(persist(springVersion), "Convert")
  }

}
