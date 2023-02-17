package bml.util.java

import akka.http.scaladsl.model.headers.LinkParams.`type`
import bml.util.java.ClassNames.HValidatorTypes
import bml.util.{GeneratorFSUtil, JavaNameSpace, NameSpaces}
import com.squareup.javapoet._
import io.apibuilder.spec.v0.models.{Enum, Field, Model, Parameter, Service}
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

  def toClassName(enum: Enum): String = {
    toClassName(enum.name)
  }

  def toClassName(javaNameSpace: JavaNameSpace, enum: Enum): ClassName = {
    toClassName(javaNameSpace, enum.name)
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

  def toClassName(javaNameSpace: JavaNameSpace, `type`: String): ClassName = {
    // We don't support upper case class names so if a word is upper case then make it lower case
    def checkForUpperCase(word: String): String =
      if (word == word.toUpperCase) word.toLowerCase
      else word

    val cleanedName = `type`.split("\\.").last
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
    //    println(s"toClassName('${modelName}')")
    // We don't support upper case class names so if a word is upper case then make it lower case
    def checkForUpperCase(word: String): String =
      if (word == word.toUpperCase) word.toLowerCase
      else word

    val cleanedName = modelName.split("\\.").last

    if (isModelNameWithPackage(cleanedName)) {
      replaceEnumsPrefixWithModels(capitalizeModelNameWithPackage(cleanedName))
      //      replaceEnumsPrefixWithModels(capitalizeModelNameWithPackage(cleanedName))
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

  def islistOfModelNameWithPackage(modelName: String): Boolean = {
    if (!isParameterArray(modelName)) {
      false
    } else {
      modelName.contains(".")
    }
    // modelName.toLowerCase.equals(modelName) && modelName.contains(".")
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

  /**
   * Gets the subtype of an array as a ClassName.
   */
  def getListType(`type`: String, nameSpace: JavaNameSpace): ClassName = {
    val listSubType = getArrayType(`type`);
    JavaPojoUtil.toClassName(nameSpace, listSubType)
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
    "boolean" -> ClassName.get("", "Boolean"),
    "date-iso8601" -> ClassName.get("java.time", "LocalDate"),
    "date-time-iso8601" -> ClassName.get("java.time", "LocalDateTime"),
    "decimal" -> ClassName.get("java.math", "BigDecimal"),
    "double" -> ClassName.get("", "Double"),
    "integer" -> ClassName.get("", "Integer"),
    "long" -> ClassName.get("", "Long"),
    //    "object" -> ClassName.get("java.util", "Map"),

    "object" -> ParameterizedTypeName.get(
      ClassName.get("java.util", "Map"),
      ClassName.get("java.lang", "String"),
      ClassName.get("java.lang", "Object")
    ),


    "json" -> ClassName.get("", "Object"),
    "string" -> ClassName.get("", "String"),
    "unit" -> ClassName.get("", "Void"),
    "uuid" -> ClassName.get("java.util", "UUID"),
    "guid" -> ClassName.get("java.util", "UUID")
  )


  def dataTypeFromField(service: Service, `type`: String, nameSpace: JavaNameSpace): TypeName = {
    dataTypeFromField(service, `type`, nameSpace.nameSpace)
  }

  def dataTypeFromField(service: Service, `type`: String, modelsNameSpace: String): TypeName = {
    dataTypes.get(`type`).getOrElse {
      //Helps with external mapped classes IE imports
      val hasNamespace = `type`.contains(".")
      var nameSpace = if (hasNamespace) `type`.split("\\.").dropRight(1).mkString(".") else modelsNameSpace

      //keep enums in the model name space
      nameSpace = nameSpace.replace(".enums", ".models")


      val name = toParamName(`type`, false)
      if (isParameterArray(`type`)) {
        ParameterizedTypeName.get(ClassName.get("java.util", "List"), dataTypeFromField(service, getArrayType(`type`), nameSpace))
      } else if (isParameterMap(`type`)) {
        ParameterizedTypeName.get(ClassName.get("java.util", "Map"), ClassName.get("java.lang", "String"), dataTypeFromField(service, getMapType(`type`), nameSpace))
      } else {

        ClassName.get(nameSpace, name)
      }

    }
  }

  def dataTypeFromFieldArraySupport(service: Service, field: Field, modelsNameSpace: String): TypeName = {
    dataTypeFromFieldArraySupport(service, field, field.`type`, modelsNameSpace)
  }

  def dataTypeFromFieldArraySupport(service: Service, field: Field, `type`: String, modelsNameSpace: String): TypeName = {
    dataTypes.get(`type`).getOrElse {
      //Helps with external mapped classes IE imports
      val hasNamespace = `type`.contains(".")
      var nameSpace = if (hasNamespace) `type`.split("\\.").dropRight(1).mkString(".") else modelsNameSpace

      //keep enums in the model name space
      nameSpace = nameSpace.replace(".enums", ".models")


      val name = toParamName(`type`, false)
      if (isParameterArray(`type`)) {
        if (`type` == "[string]") {
          ParameterizedTypeName.get(
            ClassName.get("java.util", "List"),
            dataTypeFromField(service,
              getArrayType(`type`),
              nameSpace).annotated(
              AnnotationSpec.builder(HValidatorTypes.Length).addMember("min", "$L", JavaPojos.toMinStringValueLengthStaticFieldName(field.name)).addMember("max", "$L", JavaPojos.toMaxStringValueLengthStaticFieldName(field.name)).build()
            )
          )
        } else {
          ParameterizedTypeName.get(ClassName.get("java.util", "List"), dataTypeFromField(service, getArrayType(`type`), nameSpace))
        }
      } else if (isParameterMap(`type`)) {
        ParameterizedTypeName.get(ClassName.get("java.util", "Map"), ClassName.get("java.lang", "String"), dataTypeFromField(service, getMapType(`type`), nameSpace))
      } else {

        ClassName.get(nameSpace, name)
      }

    }
  }

  def dataTypeFromField(service: Service, field: Field, nameSpace: JavaNameSpace): TypeName = {
    dataTypeFromField(service, field.`type`, nameSpace.nameSpace)
  }

  def dataTypeFromField(service: Service, field: Field, modelsNameSpace: String): TypeName = {
    dataTypeFromField(service, field.`type`, modelsNameSpace)
  }


  def externalNameSpaceFromType(`type`: String): NameSpaces = {
    new NameSpaces(`type`.split("\\.").dropRight(1).dropRight(1).mkString("."))
  }


  def toFieldName(field: Field): String = {
    toParamName(field.name, true)
  }

  def toIdFieldName(field: Field): String = {
    toParamName(field.name + "_id", true)
  }

  def toFieldName(fieldName: String): String = {
    toParamName(fieldName, true)
  }

  def toParamName(parameter: Parameter, startingWithLowercase: Boolean = true): String = {
    toParamName(parameter.name, startingWithLowercase)
  }

  def toParamName(modelName: String, startingWithLowercase: Boolean): String = {
    var paramStartingWithUppercase = if (isParameterArray(modelName)) {
      toClassName(modelName.tail.reverse.tail.reverse)
    } else {
      toClassName(modelName)
    }

    if (paramStartingWithUppercase.equals("Default")) {
      paramStartingWithUppercase = "DefaultValue"
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
    !service.enums.filter(_.name == field.`type`).isEmpty | field.`type`.contains(".enums.")
  }

  def isEnumType(service: Service, `type`: String): Boolean = {
    !service.enums.filter(_.name == `type`).isEmpty | `type`.contains(".enums.")
  }

  def isListOfEnumlType(service: Service, field: Field): Boolean = {
    if (!isParameterArray(field)) {
      return false
    }
    val clean = field.`type`.replaceAll("[\\[\\]]", "")
    isEnumType(service, clean)
  }

  def isListOfEnumlType(service: Service, `type`: String): Boolean = {
    if (!isParameterArray(`type`)) {
      return false
    }
    val clean = `type`.replaceAll("[\\[\\]]", "")
    isEnumType(service, clean)
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

  def isListOfModeslType(service: Service, `type`: String): Boolean = {
    if (!isParameterArray(`type`)) {
      return false
    }
    val clean = `type`.replaceAll("[\\[\\]]", "")
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

  def findIdField(service: Service, `type`: String): Option[Field] = {
    val modelOption = service.models.find(_.name == `type`)
    if (modelOption.isEmpty) {
      return None
    }
    val model = modelOption.get
    model.fields.find(_.name == "id")
  }

  def isDateOrTime(field: Field): Boolean = {
    if (field.`type` == "date-iso8601") return true;
    if (field.`type` == "date-time-iso8601") return true;
    false
  }


}

object JavaPojoUtil extends JavaPojoUtil
