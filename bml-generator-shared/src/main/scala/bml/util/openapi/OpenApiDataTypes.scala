package bml.util.openapi

object OpenApiDataTypes {

  def isPrimitive(typeIn: String): Boolean = {
    typeIn match {
      case "uuid" => true
      case "string" => true
      case "integer" => true
      case "date-iso8601" => true
      case default => false
    }
  }

  def toPrimitive(typeIn: String): Option[String] = {
    typeIn match {
      case "uuid" => Some("string")
      case "string" => Some("string")
      case "integer" => Some("integer")
      case "date-iso8601" => Some("string")
      case default => None
    }
  }

  /**
   * Tells if a primitive data type has an accompanying format.
   *
   * @param typeIn a type
   * @return true if the data type has a format false otherwise
   */
  def hasFormat(typeIn: String): Boolean = {
    typeIn match {
      case "uuid" => true
      case "date-iso8601" => true
      case default => false
    }
  }

  /**
   * Returns an openapi format optional if one exists for the type passed.
   *
   * @param typeIn a type
   * @return some format or none if one does not exist
   */
  def getFormat(typeIn: String): Option[String] = {
    typeIn match {
      case "uuid" => Some("uuid")
      case "date-iso8601" => Some("date-time")
      case default => None
    }
  }


}
