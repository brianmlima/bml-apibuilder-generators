package bml.util.java

import java.time.LocalDate
import java.util
import java.util.concurrent.ThreadLocalRandom
import java.util.{Locale, Random, UUID}

import bml.util.JavaNameSpace
import bml.util.java.poet.{StaticImport, StaticImportMethod}
import com.squareup.javapoet.{ClassName, ParameterizedTypeName, TypeName}
import lombok.Getter
import lombok.experimental.{FieldNameConstants, UtilityClass}
import lombok.extern.slf4j.Slf4j

object ClassNames {

  def toClassName(namespace: JavaNameSpace, className: String): ClassName = {
    ClassName.get(namespace.nameSpace, className);
  }

  //####################################################################################################################
  // BEGIN JAVA CORE ###################################################################################################
  val `override` = classOf[Override]
  val string = ClassName.get(classOf[String])
  val integer = ClassName.get(classOf[Integer])
  val localDate = ClassName.get(classOf[LocalDate])

  val random = ClassName.get(classOf[Random])
  val threadLocalRandom = ClassName.get(classOf[ThreadLocalRandom])

  val collections = ClassName.bestGuess("java.util.Collections")
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

  val arrays = ClassName.get("java.util", "Arrays")
  val math = ClassName.get(classOf[Math])

  val illegalArgumentException = ClassName.get(classOf[IllegalArgumentException])

  val `boolean` = ClassName.get("java.lang", "Boolean")

  val `class` = ClassName.get("", "Class")

  val invocationTargetException = ClassName.get("java.lang.reflect", "InvocationTargetException")
  val method = ClassName.get("java.lang.reflect", "Method")


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

  // END Spring ########################################################################################################
  //####################################################################################################################

  val mock = ClassName.get("org.mockito", "Mock")


  //####################################################################################################################
  // BEGIN Lombok ######################################################################################################
  val fieldNameConstants = ClassName.get(classOf[FieldNameConstants])
  val slf4j = ClassName.get(classOf[Slf4j])
  val builder = ClassName.get(classOf[lombok.Builder])
  val allArgsConstructor = ClassName.get(classOf[lombok.AllArgsConstructor])
  val noArgsConstructor = ClassName.get(classOf[lombok.NoArgsConstructor])
  val utilityClass = ClassName.get(classOf[UtilityClass])
  val getter = ClassName.get(classOf[Getter])
  val builderDefault = ClassName.get(classOf[lombok.Builder.Default])


  // END Lombok ########################################################################################################
  //####################################################################################################################

  //####################################################################################################################
  // BEGIN Junit5 ######################################################################################################
  val test = ClassName.get("org.junit.jupiter.api", "Test")

  // END Junit5 ########################################################################################################
  //####################################################################################################################

  //####################################################################################################################
  // BEGIN Hamcrest ####################################################################################################


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
