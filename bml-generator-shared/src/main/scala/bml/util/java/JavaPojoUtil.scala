package bml.util.java

import com.google.googlejavaformat.java.Formatter
import com.squareup.javapoet._
import io.apibuilder.generator.v0.models.File
import io.apibuilder.spec.v0.models.{Field, Service}
import lib.Text


trait JavaPojoUtil {

  private val ReservedWords = Set(
    "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class", "const", "continue",
    "default", "do", "double", "else", "enum", "extends", "final", "finally", "float", "for", "goto",
    "if", "implements", "import", "instanceof", "int", "interface", "long", "native", "new",
    "package", "private", "protected", "public", "return", "short", "static", "strictfp", "super", "switch", "synchronized",
    "this", "throw", "throws", "transient", "try", "void", "volatile", "while")

  def checkForReservedWord(word: String): String =
    if (ReservedWords.contains(word)) word + "_"
    else word

  def textToComment(text: String): String = textToComment(Seq(text))

  def textToComment(text: Seq[String]): String = {
    "/**\n * " + text.mkString("\n * ") + "\n */"
  }

  def toClassName(modelName: String): String = {
    // We don't support upper case class names so if a word is upper case then make it lower case
    def checkForUpperCase(word: String): String =
      if (word == word.toUpperCase) word.toLowerCase
      else word

    if (isModelNameWithPackage(modelName)) {
      replaceEnumsPrefixWithModels(capitalizeModelNameWithPackage(modelName))
    } else {
      Text.safeName(Text.splitIntoWords(modelName).map {
        checkForUpperCase(_).capitalize
      }.mkString)
    }
  }

  /**
    * Because we put enums models directory and package (scala client does the same), we need to replace
    * a.b.c.d.enums.EnumName with a.b.c.d.models.EnumName
    *
    * @param str
    * @return
    */

  def replaceEnumsPrefixWithModels(str: String): String = {
    val arr = str.split("\\.")
    val updatedArr = (arr.reverse.head +: (if (arr.reverse.tail.head == "enums") "models" else arr.reverse.tail.head) +: arr.reverse.tail.tail).reverse
    updatedArr.mkString(".")
  }

  def isModelNameWithPackage(modelName: String): Boolean = {
    modelName.toLowerCase.equals(modelName) && modelName.contains(".")
  }

  def capitalizeModelNameWithPackage(modelName: String): String = {
    (Seq(modelName.split("\\.").reverse.head.capitalize) ++ modelName.split("\\.").reverse.tail).reverse.mkString(".")
  }

  def isParameterArray(modelName: String): Boolean = {
    modelName.startsWith("[") && modelName.endsWith("]")
  }

  def getArrayType(modelName: String): String = {
    if (isParameterArray(modelName)) {
      modelName.replaceAll("^\\[", "").replaceAll("\\]$", "")
    } else {
      modelName
    }
  }

  def isParameterMap(modelName: String): Boolean = {
    modelName.startsWith("map[") && modelName.endsWith("]")
  }

  def getMapType(modelName: String): String = {
    if (isParameterMap(modelName)) {
      modelName.substring(modelName.indexOf("[") + 1).replaceAll("\\]$", "")
    } else {
      modelName
    }

  }

  //TODO: we can use primitives as well, but then equal method needs to become smarter, this way is ok

  val dataTypes = Map[String, TypeName](
    "boolean" -> ClassName.get("java.lang", "Boolean"),
    "date-iso8601" -> ClassName.get("java.time", "LocalDate"),
    "date-time-iso8601" -> ClassName.get("java.time", "LocalDateTime"),
    "decimal" -> ClassName.get("java.math", "BigDecimal"),
    "double" -> ClassName.get("java.lang", "Double"),
    "integer" -> ClassName.get("java.lang", "Integer"),
    "long" -> ClassName.get("java.lang", "Long"),
    "object" -> ClassName.get("java.util", "Map"),
    "json" -> ClassName.get("java.lang", "Object"),
    "string" -> ClassName.get("java.lang", "String"),
    "unit" -> ClassName.get("java.lang", "Void"),
    "uuid" -> ClassName.get("java.util", "UUID")
  )


  def dataTypeFromField(`type`: String, modelsNameSpace: String): TypeName = {
    dataTypes.get(`type`).getOrElse {
      val name = toParamName(`type`, false)
      if (isParameterArray(`type`))
        ParameterizedTypeName.get(ClassName.get("java.util", "List"), dataTypeFromField(getArrayType(`type`), modelsNameSpace))
      else if (isParameterMap(`type`))
        ParameterizedTypeName.get(ClassName.get("java.util", "Map"), ClassName.get("java.lang", "String"), dataTypeFromField(getMapType(`type`), modelsNameSpace))
      else
        ClassName.get(modelsNameSpace, name)
    }
  }

  def toParamName(modelName: String, startingWithLowercase: Boolean): String = {
    val paramStartingWithUppercase = if (isParameterArray(modelName)) {
      toClassName(modelName.tail.reverse.tail.reverse)
    } else {
      toClassName(modelName)
    }
    if (startingWithLowercase) {
      checkForReservedWord(paramStartingWithUppercase.head.toLower + paramStartingWithUppercase.tail)
    } else {
      checkForReservedWord(paramStartingWithUppercase)
    }
  }

  def toMethodName(modelName: String): String = {
    val methodName = {
      val methoNameStartingWithUpperCase = Text.splitIntoWords(modelName).map {
        _.toLowerCase.capitalize
      }.mkString
      Text.safeName(methoNameStartingWithUpperCase.head.toLower + methoNameStartingWithUpperCase.tail)
    }
    checkForReservedWord(methodName)
  }

  def toEnumName(input: String): String = {
    Text.safeName(input.replaceAll("\\.", "_").replaceAll("-", "_")).toUpperCase
  }

  def makeNameSpace(namespace: String): String = {
    namespace.split("\\.").map {
      checkForReservedWord
    }.mkString(".")
  }

  def isEnumType(service: Service, field: Field): Boolean = {
    !service.enums.filter(_.name == field.`type`).isEmpty
  }

  def isModelType(service: Service, field: Field): Boolean = {
    !service.models.filter(_.name == field.`type`).isEmpty
  }

  def isModelType(service: Service, `type`: String): Boolean = {
    !service.models.filter(_.name == `type`).isEmpty
  }


  def createDirectoryPath(namespace: String) = namespace.replace('.', '/')

  def makeFile(name: String, path: String, nameSpace: String, builder: TypeSpec.Builder): File = {
    File(s"${name}.java", Some(path), new Formatter().formatSource(JavaFile.builder(nameSpace, builder.build).build.toString))
  }

}
