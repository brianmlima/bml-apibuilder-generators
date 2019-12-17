package bml.util.java

import java.time.LocalDate
import java.util
import java.util.concurrent.ThreadLocalRandom
import java.util.{Locale, Random, UUID}

import bml.util.JavaNameSpace
import bml.util.java.ClassNames.HamcrestTypes.MatcherAssert
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

  val java = new Object {
    val `override` = classOf[Override]
  }
  //####################################################################################################################
  // BEGIN JAVA CORE ###################################################################################################
  //val string = ClassName.get(classOf[String])
  //  val integer = ClassName.get(classOf[Integer])
  //  val localDate = ClassName.get(classOf[LocalDate])

  //  val random = ClassName.get(classOf[Random])
  //  val threadLocalRandom = ClassName.get(classOf[ThreadLocalRandom])

  val locale = ClassName.get(classOf[Locale])
  val supplier = ClassName.get("java.util.function", "Supplier")
  val uuid = ClassName.get(classOf[UUID])

  def supplier(className: ClassName): ParameterizedTypeName = {
    ParameterizedTypeName.get(supplier, className)
  }

  def supplier(typeName: TypeName): ParameterizedTypeName = {
    ParameterizedTypeName.get(supplier, typeName)
  }

  val list = ClassName.get("java.util", "List")

  def list(className: ClassName): ParameterizedTypeName = {
    ParameterizedTypeName.get(list, className)
  }

  def list(typeName: TypeName): ParameterizedTypeName = {
    ParameterizedTypeName.get(list, typeName)
  }


  val linkedList = ClassName.get("java.util", "LinkedList")

  def linkedList(className: ClassName): ParameterizedTypeName = {
    ParameterizedTypeName.get(linkedList, className)
  }

  def linkedList(typeName: TypeName): ParameterizedTypeName = {
    ParameterizedTypeName.get(linkedList, typeName)
  }

  //val arrays = ClassName.get("java.util", "Arrays")
  val math = ClassName.get(classOf[Math])

  val illegalArgumentException = ClassName.get(classOf[IllegalArgumentException])

  val `boolean` = ClassName.get("java.lang", "Boolean")

  val `class` = ClassName.get("", "Class")

  val invocationTargetException = ClassName.get("java.lang.reflect", "InvocationTargetException")
  val method = ClassName.get("java.lang.reflect", "Method")


  object JavaTypes {
    val String = ClassName.get(classOf[String])
    val Collections = ClassName.bestGuess("java.util.Collections")
    val Arrays = ClassName.get("java.util", "Arrays")
    val Supplier = ClassName.get("java.util.function", "Supplier")
    val Integer = ClassName.get(classOf[Integer])
    val LocalDate = ClassName.get(classOf[LocalDate])

    val Random = ClassName.get(classOf[Random])
    val ThreadLocalRandom = ClassName.get(classOf[ThreadLocalRandom])


    def supplier(className: ClassName): ParameterizedTypeName = {
      ParameterizedTypeName.get(Supplier, className)
    }

    def supplier(typeName: TypeName): ParameterizedTypeName = {
      ParameterizedTypeName.get(Supplier, typeName)
    }

    def Collectors = ClassName.get("java.util.stream", "Collectors")

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

  val stringBuilder = ClassName.get("java.lang", "StringBuilder")

  //####################################################################################################################
  // BEGIN Spring ######################################################################################################
  val responseEntity: ClassName = ClassName.get("org.springframework.http", "ResponseEntity")

  def responseEntity(className: ClassName): ParameterizedTypeName = ParameterizedTypeName.get(responseEntity, className)

  val responseBody = ClassName.bestGuess("org.springframework.web.bind.annotation.ResponseBody")
  val responseStatus = ClassName.bestGuess("org.springframework.web.bind.annotation.ResponseStatus")
  val responseEntityOfObject: ParameterizedTypeName = responseEntity(ClassName.OBJECT)
  val springApplication = ClassName.get("org.springframework.boot", "SpringApplication")
  val springBootApplication = ClassName.get("org.springframework.boot.autoconfigure", "SpringBootApplication")
  val controller = ClassName.get("org.springframework.stereotype", "Controller")
  val springBootTest = ClassName.get("org.springframework.boot.test.context", "SpringBootTest")
  val autowired = ClassName.bestGuess("org.springframework.beans.factory.annotation.Autowired")
  val getMapping = ClassName.bestGuess("org.springframework.web.bind.annotation.GetMapping")
  val postMapping = ClassName.bestGuess("org.springframework.web.bind.annotation.PostMapping")
  val putMapping = ClassName.bestGuess("org.springframework.web.bind.annotation.PutMapping")
  val requestMapping = ClassName.bestGuess("org.springframework.web.bind.annotation.RequestMapping")
  val httpStatus = ClassName.bestGuess("org.springframework.http.HttpStatus")
  val methodArgumentNotValidException = ClassName.bestGuess("org.springframework.web.bind.MethodArgumentNotValidException")
  val exceptionHandler = ClassName.bestGuess("org.springframework.web.bind.annotation.ExceptionHandler")
  val controllerAdvice = ClassName.bestGuess("org.springframework.web.bind.annotation.ControllerAdvice")
  val fieldError = ClassName.bestGuess("org.springframework.validation.FieldError")
  val bindingResult = ClassName.bestGuess("org.springframework.validation.BindingResult")
  val ordered = ClassName.bestGuess("org.springframework.core.Ordered")
  val order = ClassName.bestGuess("org.springframework.core.annotation.Order")
  val pathVariable = ClassName.bestGuess("org.springframework.web.bind.annotation.PathVariable")
  val requestHeader = ClassName.bestGuess("org.springframework.web.bind.annotation.RequestHeader")
  val requestParam = ClassName.bestGuess("org.springframework.web.bind.annotation.RequestParam")
  val objectIdentity = ClassName.bestGuess("org.springframework.security.acls.model.ObjectIdentity")
  val noRepositoryBean = ClassName.bestGuess("org.springframework.data.repository.NoRepositoryBean")
  val validated = ClassName.bestGuess("org.springframework.validation.annotation.Validated")
  val restController = ClassName.bestGuess("org.springframework.web.bind.annotation.RestController")

  object SpringTypes {

    val Configuration = ClassName.get("org.springframework.context.annotation", "Configuration")
    val Bean = ClassName.get("org.springframework.context.annotation", "Bean")

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
  val test = ClassName.get("org.junit.jupiter.api", "Test")
  val displayName: ClassName = ClassName.get("org.junit.jupiter.api", "DisplayName")

  def displayName(testNAme: String): AnnotationSpec = AnnotationSpec.builder(displayName).addMember("value", "$S", testNAme).build()

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
