package bml.util.java

import bml.util.java
import com.squareup.javapoet.{ClassName, ParameterizedTypeName}

sealed trait JavaDataType {

  val apiBuilderType: String

  val javaClassName: ClassName

  def setterValueAssignment(paramName: String) = {
    paramName
  }

}


object JavaDataTypes {

  sealed trait NativeDatatype extends JavaDataType

  case object Boolean extends NativeDatatype {
    override val apiBuilderType = "boolean"
    override val javaClassName = ClassName.get("", "Boolean")
  }

  case object Double extends NativeDatatype {
    override val apiBuilderType = "double"
    override val javaClassName = ClassName.get("", "Double")
  }

  case object Integer extends NativeDatatype {
    override val apiBuilderType = "integer"
    override val javaClassName = ClassName.get("", "Integer")
  }

  case object Long extends NativeDatatype {
    override val apiBuilderType = "long"
    override val javaClassName = ClassName.get("", "Long")
  }

  case object DateIso8601 extends NativeDatatype {
    override val apiBuilderType = "date-iso8601"
    override val javaClassName = ClassName.get("java.time", "LocalDate")
    //override def setterValueAssignment(paramName: String) = s"if( ${paramName} != nulll )? new Date(${paramName}.getTime()) : null"
  }

  case object DateTimeIso8601 extends NativeDatatype {
    override val apiBuilderType = "date-time-iso8601"
    override val javaClassName = ClassName.get("java.time", "LocalDate")
    //override def setterValueAssignment(paramName: String) = s"if( ${paramName} != nulll )? new Date(${paramName}.getTime()) : null"
  }

  case object Decimal extends NativeDatatype {
    override val apiBuilderType = "decimal"
    override val javaClassName = ClassName.get("java.math", "BigDecimal")
  }

  case object Object extends NativeDatatype {
    override val apiBuilderType = "object"

    //    override val javaClassName =
    //      ParameterizedTypeName.get(
    //        ClassName.get("java.util", "Map"),
    //        ClassName.get("java.lang", "String"),
    //        ClassName.get("java.lang", "Object")
    //      )


    override val javaClassName = ClassName.get("java.util", "Map<java.lang.String, java.lang.Object>")
  }

  case object JsonValue extends NativeDatatype {
    override val apiBuilderType = "json"
    override val javaClassName = ClassName.get("", "Object")
  }

  case object String extends NativeDatatype {
    override val apiBuilderType = "string"
    override val javaClassName = ClassName.get("", "String")
  }

  case object Uuid extends NativeDatatype {
    override val apiBuilderType = "uuid"
    override val javaClassName = ClassName.get("java.util", "UUID")
  }

}

object JavaDataType {

  def apply(apiBuilderType: String) = {
    apiBuilderType match {
      case JavaDataTypes.Boolean.apiBuilderType => JavaDataTypes.Boolean
      case JavaDataTypes.Double.apiBuilderType => JavaDataTypes.Double
      case JavaDataTypes.Integer.apiBuilderType => JavaDataTypes.Integer
      case JavaDataTypes.Long.apiBuilderType => JavaDataTypes.Long
      case JavaDataTypes.DateIso8601.apiBuilderType => JavaDataTypes.DateIso8601
      case JavaDataTypes.DateTimeIso8601.apiBuilderType => JavaDataTypes.DateTimeIso8601
      case JavaDataTypes.Decimal.apiBuilderType => JavaDataTypes.Decimal
      case JavaDataTypes.Object.apiBuilderType => JavaDataTypes.Object
      case JavaDataTypes.JsonValue.apiBuilderType => JavaDataTypes.JsonValue
      case JavaDataTypes.String.apiBuilderType => JavaDataTypes.String
      case JavaDataTypes.Uuid.apiBuilderType => JavaDataTypes.Uuid
    }


  }


}




