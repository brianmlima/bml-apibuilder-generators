package bml.util.attribute

import akka.stream.TLSClientAuth.None
import io.apibuilder.spec.v0.models.Attribute

class ExampleAttribute (val stringValue: String) {

}

object ExampleAttribute{
  val attributeName = "example"
  val stringValueKey="stringValue"

  def isThisAttribute(attribute: Attribute):Boolean = {
    attribute.name.equals(attributeName)
  }

  def asThisAttribute(attribute: Attribute):Option[ExampleAttribute] = {
     if(!attribute.name.equals(attributeName)) None
    val value = attribute.value.value.get(stringValueKey).toString
      return Option.apply(new ExampleAttribute(value))
  }
}

