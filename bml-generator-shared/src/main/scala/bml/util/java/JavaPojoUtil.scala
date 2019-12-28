package bml.util.java

import bml.util.java.ClassNames.HValidatorTypes
import bml.util.{GeneratorFSUtil, JavaNameSpace, NameSpaces}
import com.squareup.javapoet._
import io.apibuilder.spec.v0.models.{Field, Model, Service}
import lib.Text


trait JavaPojoUtil extends JavaNamespaceUtil {

  def checkForReservedWord(word: String): String = {
    JavaReservedWordUtil.checkForReservedWord(word)
  }

  def textToComment(text: String): String = textToComment(Seq(text))

  def textToComment(text: Seq[String]): String = {
    "/**\n * " + text.mkString("\n * ") + "\n */"
  }

  def toClassName(model: Model): String = {
    toClassName(model.name)
  }

  def toClassName(javaNameSpace: JavaNameSpace, model: Model): ClassName = {
    // We don't support upper case class names so if a word is upper case then make it lower case
    def checkForUpperCase(word: String): String =
      if (word == word.toUpperCase) word.toLowerCase
      else word

    val cleanedName = model.name.split("\\.").last
    if (isModelNameWithPackage(cleanedName)) {
      ClassName.bestGuess(replaceEnumsPrefixWithModels(capitalizeModelNameWithPackage(cleanedName)))
    } else {
      ClassName.get(
        javaNameSpace.nameSpace,
        Text.safeName(Text.splitIntoWords(cleanedName).map(checkForUpperCase(_).capitalize).mkString)
      )
    }
  }


  def toClassName(modelName: String): String = {
    // We don't support upper case class names so if a word is upper case then make it lower case
    def checkForUpperCase(word: String): String =
      if (word == word.toUpperCase) word.toLowerCase
      else word

    val cleanedName = modelName.split("\\.").last

    if (isModelNameWithPackage(cleanedName)) {
      replaceEnumsPrefixWithModels(capitalizeModelNameWithPackage(cleanedName))
    } else {
      Text.safeName(Text.splitIntoWords(cleanedName).map {
        checkForUpperCase(_).capitalize
      }.mkString)
    }
  }

  def toStaticFieldName(fieldName: String): String = {
    //Produce all caps snake case
    val cleanedName = fieldName.split("\\.").last
    Text.safeName(Text.splitIntoWords(cleanedName).map {
      _.toUpperCase()
    }.mkString("_"))
  }


  def toBuilderClassName(className: ClassName): ClassName = {
    ClassName.get(className.packageName() + '.' + className.simpleName(), className.simpleName() + "Builder")
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
    // modelName.toLowerCase.equals(modelName) && modelName.contains(".")
    modelName.contains(".")
  }

  def capitalizeModelNameWithPackage(modelName: String): String = {
    (Seq(modelName.split("\\.").reverse.head.capitalize) ++ modelName.split("\\.").reverse.tail).reverse.mkString(".")
  }

  def isParameterArray(modelName: String): Boolean = {
    modelName.startsWith("[") && modelName.endsWith("]")
  }

  def isParameterArray(field: Field): Boolean = {
    isParameterArray(field.`type`)
  }

  def getArrayType(modelName: String): String = {
    if (isParameterArray(modelName)) {
      modelName.replaceAll("^\\[", "").replaceAll("\\]$", "")
    } else {
      modelName
    }
  }

  def getArrayType(field: Field): String = {
    getArrayType(field.`type`)
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


  def dataTypeFromField(field: Field, nameSpace: JavaNameSpace): TypeName = {
    dataTypeFromField(field.`type`, nameSpace.nameSpace)
  }

  def dataTypeFromField(`type`: String, nameSpace: JavaNameSpace): TypeName = {
    dataTypeFromField(`type`, nameSpace.nameSpace)
  }


  def dataTypeFromField(`type`: String, modelsNameSpace: String): TypeName = {
    dataTypes.get(`type`).getOrElse {
      //Helps with external mapped classes IE imports
      val hasNamespace = `type`.contains(".")
      val nameSpace = if (hasNamespace) `type`.split("\\.").dropRight(1).mkString(".") else modelsNameSpace

      val name = toParamName(`type`, false)
      if (isParameterArray(`type`))
        ParameterizedTypeName.get(ClassName.get("java.util", "List"), dataTypeFromField(getArrayType(`type`), nameSpace))
      else if (isParameterMap(`type`))
        ParameterizedTypeName.get(ClassName.get("java.util", "Map"), ClassName.get("java.lang", "String"), dataTypeFromField(getMapType(`type`), nameSpace))
      else
        ClassName.get(nameSpace, name)

    }
  }

  def dataTypeFromField(field: Field, modelsNameSpace: String): TypeName = {
    dataTypes.get(field.`type`).getOrElse {
      //Helps with external mapped classes IE imports
      val hasNamespace = field.`type`.contains(".")
      val nameSpace = if (hasNamespace) field.`type`.split("\\.").dropRight(1).mkString(".") else modelsNameSpace

      val name = toParamName(field.`type`, false)
      if (isParameterArray(field.`type`))
        if (field.`type` == "[string]") {
          ParameterizedTypeName.get(
            ClassName.get("java.util", "List"),
            dataTypeFromField(
              getArrayType(field.`type`),
              nameSpace).annotated(
              AnnotationSpec.builder(HValidatorTypes.Length).addMember("min", "$L", JavaPojos.toMinStringValueLengthStaticFieldName(field)).addMember("max", "$L", JavaPojos.toMaxStringValueLengthStaticFieldName(field)).build()
            )
          )
        } else {
          ParameterizedTypeName.get(ClassName.get("java.util", "List"), dataTypeFromField(getArrayType(field.`type`), nameSpace))
        }
      else if (isParameterMap(field.`type`))
        ParameterizedTypeName.get(ClassName.get("java.util", "Map"), ClassName.get("java.lang", "String"), dataTypeFromField(getMapType(field.`type`), nameSpace))
      else
        ClassName.get(nameSpace, name)

    }
  }


  def externalNameSpaceFromType(`type`: String): NameSpaces = {
    new NameSpaces(`type`.split("\\.").dropRight(1).dropRight(1).mkString("."))
  }


  def toFieldName(field: Field): String = {
    toParamName(field.name, true)
  }

  def toFieldName(fieldName: String): String = {
    toParamName(fieldName, true)
  }

  def toParamName(modelName: String, startingWithLowercase: Boolean): String = {
    val paramStartingWithUppercase = if (isParameterArray(modelName)) {
      toClassName(modelName.tail.reverse.tail.reverse)
    } else {
      toClassName(modelName)
    }
    if (startingWithLowercase) {
      JavaReservedWordUtil.checkForReservedWord(paramStartingWithUppercase.head.toLower + paramStartingWithUppercase.tail)
    } else {
      JavaReservedWordUtil.checkForReservedWord(paramStartingWithUppercase)
    }
  }

  def toMethodName(modelName: String): String = {
    val methodName = {
      val methoNameStartingWithUpperCase = Text.splitIntoWords(modelName).map {
        _.toLowerCase.capitalize
      }.mkString
      Text.safeName(methoNameStartingWithUpperCase.head.toLower + methoNameStartingWithUpperCase.tail)
    }
    JavaReservedWordUtil.checkForReservedWord(methodName)
  }

  def toEnumName(input: String): String = {
    JavaEnums.toEnumName(input)
  }

  def isEnumType(service: Service, field: Field): Boolean = {
    !service.enums.filter(_.name == field.`type`).isEmpty
  }

  def isEnumType(service: Service, `type`: String): Boolean = {
    !service.enums.filter(_.name == `type`).isEmpty
  }

  def isModelType(service: Service, field: Field): Boolean = {
    !service.models.filter(_.name == field.`type`).isEmpty
  }

  def isListOfModeslType(service: Service, field: Field): Boolean = {
    if (!isParameterArray(field)) {
      return false
    }
    val clean = field.`type`.replaceAll("[\\[\\]]", "")
    !service.models.filter(_.name == clean).isEmpty
  }


  def isModelType(service: Service, `type`: String): Boolean = {
    !service.models.filter(_.name == `type`).isEmpty
  }

  //Legacy to keep up the contract for older stuffs.
  def createDirectoryPath(namespace: String): String = GeneratorFSUtil.createDirectoryPath(namespace)

  //  def makeFile(name: String, path: String, nameSpace: String, builder: TypeSpec.Builder): File = {
  //    GeneratorFSUtil.makeFile(name, path, nameSpace, builder)
  //  }


  def hasValidation(model: Model): Boolean = {
    model.fields.find((field) => {
      field.required || field.maximum.isDefined || field.minimum.isDefined
    }).isDefined
  }

}

object JavaPojoUtil extends JavaPojoUtil
