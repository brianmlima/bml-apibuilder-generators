package bml.util

import com.google.common.base.CaseFormat

object Text {

  def camelToSnakeCase(string: String) = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, string)

}
