package bml.util.persist

import com.squareup.javapoet.{ClassName, ParameterizedTypeName}

class PersistGenUtil {

}

object PersistGenUtil {

  def extendAuditableBaseEntity(className: String) :ParameterizedTypeName = {
    ParameterizedTypeName.get(ClassName.get("", "AuditableBaseEntity"), ClassName.get("", s"${className}"))
  }
}

