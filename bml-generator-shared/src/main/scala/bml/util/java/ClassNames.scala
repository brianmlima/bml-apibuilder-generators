package bml.util.java

import java.time.LocalDate
import java.util
import java.util.concurrent.ThreadLocalRandom
import java.util.stream.Collectors
import java.util.{Locale, Random, UUID, stream}

import bml.util.JavaNameSpace
import bml.util.java.ClassNames.HamcrestTypes.MatcherAssert
import bml.util.java.ClassNames.displayName
import bml.util.java.poet.{StaticImport, StaticImportMethod}
import com.squareup.javapoet.{AnnotationSpec, ClassName, ParameterizedTypeName, TypeName}
import lombok.Builder.Default
import lombok.{AllArgsConstructor, Builder, Getter, NoArgsConstructor}
import lombok.experimental.{FieldNameConstants, UtilityClass}
import lombok.extern.slf4j.Slf4j
import org.junit.jupiter.api.DisplayName

object ClassNames {

  def toClassName(namespace: JavaNameSpace, className: String): ClassName = {
    ClassName.get(namespace.nameSpace, className);
  }

  //####################################################################################################################
  // BEGIN JAVA CORE ###################################################################################################

  object JavaTypes {
    val `Override` = classOf[Override]
    val String = ClassName.get(classOf[String])
    val Collections = ClassName.bestGuess("java.util.Collections")
    val Arrays = ClassName.get("java.util", "Arrays")
    val Supplier = ClassName.get("java.util.function", "Supplier")
    val Integer = ClassName.get(classOf[Integer])
    val LocalDate = ClassName.get(classOf[LocalDate])
    val Random = ClassName.get(classOf[Random])
    val ThreadLocalRandom = ClassName.get(classOf[ThreadLocalRandom])
    val Locale = ClassName.get(classOf[Locale])
    val UUID = ClassName.get(classOf[UUID])
    val Math = ClassName.get(classOf[Math])
    val IllegalArgumentException = ClassName.get(classOf[IllegalArgumentException])
    val `Boolean` = ClassName.get("java.lang", "Boolean")
    val `Class` = ClassName.get("", "Class")
    val InvocationTargetException = ClassName.get("java.lang.reflect", "InvocationTargetException")
    val StringBuilder = ClassName.get("java.lang", "StringBuilder")

    def Collectors = ClassName.get("java.util.stream", "Collectors")


    def supplier(className: ClassName): ParameterizedTypeName = {
      ParameterizedTypeName.get(Supplier, className)
    }

    def supplier(typeName: TypeName): ParameterizedTypeName = {
      ParameterizedTypeName.get(Supplier, typeName)
    }


    def toList = StaticImportMethod(Collectors, "toList")

    val List = ClassName.get("java.util", "List")

    def List(className: ClassName): ParameterizedTypeName = {
      ParameterizedTypeName.get(List, className)
    }

    def List(typeName: TypeName): ParameterizedTypeName = {
      ParameterizedTypeName.get(List, typeName)
    }

    val LinkedList = ClassName.get("java.util", "LinkedList")

    def LinkedList(className: ClassName): ParameterizedTypeName = {
      ParameterizedTypeName.get(LinkedList, className)
    }

    def LinkedList(typeName: TypeName): ParameterizedTypeName = {
      ParameterizedTypeName.get(LinkedList, typeName)
    }

    val ArrayList = ClassName.get("java.util", "ArrayList")

    def ArrayList(className: ClassName): ParameterizedTypeName = {
      ParameterizedTypeName.get(ArrayList, className)
    }

    def ArrayList(typeName: TypeName): ParameterizedTypeName = {
      ParameterizedTypeName.get(ArrayList, typeName)
    }

    def Method = ClassName.get("java.lang.reflect", "Method")

    def Exception = ClassName.get("", "Exception")


  }


  // END JAVA CORE #####################################################################################################
  //####################################################################################################################

  val randomUtils = ClassName.get("org.apache.commons.lang3", "RandomUtils")


  //####################################################################################################################
  // BEGIN Spring ######################################################################################################

  object SpringTypes {

    val Configuration = ClassName.get("org.springframework.context.annotation", "Configuration")
    val Bean = ClassName.get("org.springframework.context.annotation", "Bean")
    val ResponseEntity: ClassName = ClassName.get("org.springframework.http", "ResponseEntity")

    def ResponseEntity(className: ClassName): ParameterizedTypeName = ParameterizedTypeName.get(ResponseEntity, className)

    val ResponseBody = ClassName.bestGuess("org.springframework.web.bind.annotation.ResponseBody")
    val ResponseStatus = ClassName.bestGuess("org.springframework.web.bind.annotation.ResponseStatus")
    val ResponseEntityOfObject: ParameterizedTypeName = ResponseEntity(ClassName.OBJECT)
    val SpringApplication = ClassName.get("org.springframework.boot", "SpringApplication")
    val SpringBootApplication = ClassName.get("org.springframework.boot.autoconfigure", "SpringBootApplication")
    val Controller = ClassName.get("org.springframework.stereotype", "Controller")
    val SpringBootTest = ClassName.get("org.springframework.boot.test.context", "SpringBootTest")
    val Autowired = ClassName.bestGuess("org.springframework.beans.factory.annotation.Autowired")
    val GetMapping = ClassName.bestGuess("org.springframework.web.bind.annotation.GetMapping")
    val PostMapping = ClassName.bestGuess("org.springframework.web.bind.annotation.PostMapping")
    val PutMapping = ClassName.bestGuess("org.springframework.web.bind.annotation.PutMapping")
    val RequestMapping = ClassName.bestGuess("org.springframework.web.bind.annotation.RequestMapping")
    val HttpStatus = ClassName.bestGuess("org.springframework.http.HttpStatus")
    val MethodArgumentNotValidException = ClassName.bestGuess("org.springframework.web.bind.MethodArgumentNotValidException")
    val ExceptionHandler = ClassName.bestGuess("org.springframework.web.bind.annotation.ExceptionHandler")
    val ControllerAdvice = ClassName.bestGuess("org.springframework.web.bind.annotation.ControllerAdvice")
    val FieldError = ClassName.bestGuess("org.springframework.validation.FieldError")
    val BindingResult = ClassName.bestGuess("org.springframework.validation.BindingResult")
    val Ordered = ClassName.bestGuess("org.springframework.core.Ordered")
    val Order = ClassName.bestGuess("org.springframework.core.annotation.Order")
    val PathVariable = ClassName.bestGuess("org.springframework.web.bind.annotation.PathVariable")
    val RequestHeader = ClassName.bestGuess("org.springframework.web.bind.annotation.RequestHeader")
    val RequestParam = ClassName.bestGuess("org.springframework.web.bind.annotation.RequestParam")
    val ObjectIdentity = ClassName.bestGuess("org.springframework.security.acls.model.ObjectIdentity")
    val NoRepositoryBean = ClassName.bestGuess("org.springframework.data.repository.NoRepositoryBean")
    val Validated = ClassName.bestGuess("org.springframework.validation.annotation.Validated")
    val RestController = ClassName.bestGuess("org.springframework.web.bind.annotation.RestController")

    object SpringTestTypes {
      val SpringExtension = ClassName.get("org.springframework.test.context.junit.jupiter", "SpringExtension")
      val SpringBootTest = ClassName.get("org.springframework.boot.test.context", "SpringBootTest")
      val SpringJUnitConfig = ClassName.get("org.springframework.test.context.junit.jupiter", "SpringJUnitConfig")
    }

  }


  // END Spring ########################################################################################################
  //####################################################################################################################

  val mock = ClassName.get("org.mockito", "Mock")


  //####################################################################################################################
  // BEGIN Lombok ######################################################################################################
  val fieldNameConstants = ClassName.get(classOf[FieldNameConstants])
  val slf4j = ClassName.get(classOf[Slf4j])
  val builder = ClassName.get(classOf[Builder])
  val allArgsConstructor = ClassName.get(classOf[AllArgsConstructor])
  val noArgsConstructor = ClassName.get(classOf[NoArgsConstructor])
  val utilityClass = ClassName.get(classOf[UtilityClass])
  val getter = ClassName.get(classOf[Getter])
  val builderDefault = ClassName.get(classOf[Default])

  object LombokTypes {
    val FieldNameConstants = ClassName.get(classOf[FieldNameConstants])
    val Slf4j = ClassName.get(classOf[Slf4j])
    val Builder = ClassName.get(classOf[Builder])
    val AllArgsConstructor = ClassName.get(classOf[AllArgsConstructor])
    val NoArgsConstructor = ClassName.get(classOf[NoArgsConstructor])
    val UtilityClass = ClassName.get(classOf[UtilityClass])
    val Getter = ClassName.get(classOf[Getter])
    val BuilderDefault = ClassName.get(classOf[Default])

  }


  // END Lombok ########################################################################################################
  //####################################################################################################################

  //####################################################################################################################
  // BEGIN Junit5 ######################################################################################################
  //val test = ClassName.get("org.junit.jupiter.api", "Test")
  //val displayName: ClassName = ClassName.get("org.junit.jupiter.api", "DisplayName")

  def displayName(testNAme: String): AnnotationSpec = AnnotationSpec.builder(JunitTypes.DisplayName).addMember("value", "$S", testNAme).build()

  object JunitTypes {
    val Test = ClassName.get("org.junit.jupiter.api", "Test")
    val ExtendWith = ClassName.get("org.junit.jupiter.api.extension", "ExtendWith")
    val DisplayName: ClassName = ClassName.get("org.junit.jupiter.api", "DisplayName")
  }


  // END Junit5 ########################################################################################################
  //####################################################################################################################

  //####################################################################################################################
  // BEGIN Hamcrest ####################################################################################################

  object HamcrestTypes {
    val Matchers = ClassName.get("org.hamcrest", "Matchers")
    val MatcherAssert = ClassName.get("org.hamcrest", "MatcherAssert")
    val assertThat = StaticImportMethod(MatcherAssert, "assertThat")
    val notNullValue = StaticImportMethod(Matchers, "notNullValue")
  }


  val matcherAssert = ClassName.get("org.hamcrest", "MatcherAssert")

  val assertThat = StaticImportMethod(matcherAssert, "assertThat")
  val matchers = ClassName.get("org.hamcrest", "Matchers")
  val notNullValue = StaticImportMethod(matchers, "notNullValue")

  // END Hamcrest ######################################################################################################
  //####################################################################################################################


  //####################################################################################################################
  // BEGIN Javax.Validation ############################################################################################
  val notNull = ClassName.bestGuess("javax.validation.constraints.NotNull")
  val notBlank = ClassName.bestGuess("javax.validation.constraints.NotBlank")
  val pattern = ClassName.bestGuess("javax.validation.constraints.Pattern")
  val size = ClassName.bestGuess("javax.validation.constraints.Size")
  val email = ClassName.bestGuess("javax.validation.constraints.Email")
  val valid = ClassName.bestGuess("javax.validation.Valid")

  object JavaxTypes {

    object JavaxValidationTypes {
      val NotNull = ClassName.bestGuess("javax.validation.constraints.NotNull")
      val NotBlank = ClassName.bestGuess("javax.validation.constraints.NotBlank")
      val NotEmpty = ClassName.bestGuess("javax.validation.constraints.NotEmpty")
      val Pattern = ClassName.bestGuess("javax.validation.constraints.Pattern")
      val Size = ClassName.bestGuess("javax.validation.constraints.Size")
      val Email = ClassName.bestGuess("javax.validation.constraints.Email")
      val Valid = ClassName.bestGuess("javax.validation.Valid")
      val Validation = ClassName.bestGuess("javax.validation.Validation")
      val Validator = ClassName.bestGuess("javax.validation.Validator")
    }

  }

  object HValidatorTypes {
    val Length = ClassName.get("org.hibernate.validator.constraints", "Length")


  }

  // END Javax.Validation ##############################################################################################
  //####################################################################################################################


  val loremIpsum = ClassName.get("com.thedeanda.lorem", "LoremIpsum")


  val mappedSuperclass = ClassName.bestGuess("javax.persistence.MappedSuperclass")

  val slice: ClassName = ClassName.get("org.springframework.data.domain", "Slice")

  def slice(className: ClassName): ParameterizedTypeName = ParameterizedTypeName.get(slice, className)

  val repository = ClassName.get("org.springframework.data.repository", "Repository")

  val pageable: ClassName = ClassName.get("org.springframework.data.domain", "Pageable")

  val jsonProperty = ClassName.bestGuess("com.fasterxml.jackson.annotation.JsonProperty")

  val extendWith = ClassName.bestGuess("org.junit.jupiter.api.extension.ExtendWith")

  val springExtension = ClassName.bestGuess("org.springframework.test.context.junit.jupiter.SpringExtension")

  val dirtiesContext = ClassName.bestGuess("org.springframework.test.annotation.DirtiesContext")

  val autoConfigureMessageVerifier = ClassName.bestGuess("org.springframework.cloud.contract.verifier.messaging.boot.AutoConfigureMessageVerifier")


  val objectMapper = ClassName.get("com.fasterxml.jackson.databind", "ObjectMapper")
  val yAMLFactory = ClassName.get("com.fasterxml.jackson.dataformat.yaml", "YAMLFactory")
  val immutableList = ClassName.get("com.google.common.collect", "ImmutableList")
  val bean = ClassName.get("org.springframework.context.annotation", "Bean")
  val configuration = ClassName.get("org.springframework.context.annotation", "Configuration")
  val primary = ClassName.get("org.springframework.context.annotation", "Primary")
  val mediaType = ClassName.get("org.springframework.http", "MediaType")
  val httpMessageConverter = ClassName.get("org.springframework.http.converter", "HttpMessageConverter")
  val mappingJackson2HttpMessageConverter = ClassName.get("org.springframework.http.converter.json", "MappingJackson2HttpMessageConverter")
  val enableWebSecurity = ClassName.get("org.springframework.security.config.annotation.web.configuration", "EnableWebSecurity")
  val component = ClassName.get("org.springframework.stereotype", "Component")
  val contentNegotiationConfigurer = ClassName.get("org.springframework.web.servlet.config.annotation", "ContentNegotiationConfigurer")
  val enableWebMvc = ClassName.get("org.springframework.web.servlet.config.annotation", "EnableWebMvc")
  val webMvcConfigurer = ClassName.get("org.springframework.web.servlet.config.annotation", "WebMvcConfigurer")


  //val  = ClassName.get("","")
  //val  = ClassName.bestGuess("")


}
