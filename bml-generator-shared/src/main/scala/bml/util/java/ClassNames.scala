package bml.util.java

import com.squareup.javapoet.ClassName

object ClassNames {

  def collections = ClassName.bestGuess("java.util.Collections")

  def string = ClassName.get(classOf[String])


}
