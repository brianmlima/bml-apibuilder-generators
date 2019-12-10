package bml.util.java

import java.util.{Locale, Random, UUID}

import com.squareup.javapoet.{ClassName, ParameterizedTypeName, TypeName}
import lombok.Getter
import lombok.experimental.{FieldNameConstants, UtilityClass}
import lombok.extern.slf4j.Slf4j

object ClassNames {


  //####################################################################################################################
  // BEGIN JAVA CORE ###################################################################################################
  val `override` = classOf[Override]
  val string = ClassName.get(classOf[String])
  val random = ClassName.get(classOf[Random])
  val collections = ClassName.bestGuess("java.util.Collections")
  val locale = ClassName.get(classOf[Locale])
  val supplier = ClassName.get("java.util.function", "Supplier")
  val uuid = ClassName.get(classOf[UUID])

  def supplier(className: ClassName): ParameterizedTypeName = {
    ParameterizedTypeName.get(ClassNames.supplier, className)
  }

  def supplier(typeName: TypeName): ParameterizedTypeName = {
    ParameterizedTypeName.get(ClassNames.supplier, typeName)
  }

  val illegalArgumentException = ClassName.get(classOf[IllegalArgumentException])

  // END JAVA CORE #####################################################################################################
  //####################################################################################################################

  val randomUtils = ClassName.get("org.apache.commons.lang3", "RandomUtils")


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
  // END Lombok ########################################################################################################
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


  val list = ClassName.get("java.util", "List")

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
