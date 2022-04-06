package bml.util.java

import java.io.IOException
import java.time.{LocalDate, LocalDateTime, ZoneOffset}
import java.util.concurrent.ThreadLocalRandom
import java.util.{Locale, Random, UUID}

import bml.util.JavaNameSpace
import bml.util.java.poet.StaticImportMethod
import com.fasterxml.jackson.annotation.{JsonIgnore, JsonInclude, JsonValue}
import com.squareup.javapoet._
import lombok.Builder.Default
import lombok._
import lombok.experimental.{FieldNameConstants, UtilityClass}
import lombok.extern.slf4j.Slf4j


object ClassNames {

  def toClassName(namespace: JavaNameSpace, className: String): ClassName = {
    ClassName.get(namespace.nameSpace, className);
  }


  val T = TypeVariableName.get("T")

  //####################################################################################################################
  // BEGIN Hibernate ###################################################################################################

  object HibernateTypes {
    val Generated = ClassName.get("org.hibernate.annotations", "Generated")
    val CreationTimestamp = ClassName.get("org.hibernate.annotations", "CreationTimestamp")
    val UpdateTimestamp = ClassName.get("org.hibernate.annotations", "UpdateTimestamp")


    val GenerationTime = ClassName.get("org.hibernate.annotations", "GenerationTime")
    val GenericGenerator = ClassName.get("org.hibernate.annotations", "GenericGenerator")


  }

  // END Hibernate #####################################################################################################
  //####################################################################################################################


  //####################################################################################################################
  // BEGIN JAVA CORE ###################################################################################################

  object JavaTypes {
    val Override = ClassName.get("", "Override")
    val String = ClassName.get("", "String")
    val SuppressWarnings = ClassName.get("", "SuppressWarnings")


    val Collections = ClassName.bestGuess("java.util.Collections")

    val StandardCharsets = ClassName.bestGuess("java.nio.charset.StandardCharsets")


    val RuntimeException = ClassName.get("", "RuntimeException")


    val Arrays = ClassName.get("java.util", "Arrays")
    val Set = ClassName.get("java.util", "Set")

    val Long = ClassName.get("", "Long")


    def Set(typeName: TypeName): ParameterizedTypeName = {
      ParameterizedTypeName.get(Set, typeName)
    }

    val Supplier = ClassName.get("java.util.function", "Supplier")


    def Supplier(className: ClassName): ParameterizedTypeName = {
      ParameterizedTypeName.get(Supplier, className)
    }

    def Supplier(typeName: TypeName): ParameterizedTypeName = {
      ParameterizedTypeName.get(Supplier, typeName)
    }


    val Stream = ClassName.get("java.util.stream", "Stream")


    def Stream(className: ClassName): ParameterizedTypeName = {
      ParameterizedTypeName.get(Stream, className)
    }

    val Integer = ClassName.get(classOf[Integer])
    val LocalDate = ClassName.get(classOf[LocalDate])
    val ZoneOffset = ClassName.get(classOf[ZoneOffset])
    val LocalDateTime = ClassName.get(classOf[LocalDateTime])
    val Random = ClassName.get(classOf[Random])
    val ThreadLocalRandom = ClassName.get(classOf[ThreadLocalRandom])
    val Locale = ClassName.get(classOf[Locale])
    val UUID = ClassName.get(classOf[UUID])
    val Math = ClassName.get(classOf[Math])

    val URI = ClassName.get("java.net", "URI")

    val IllegalArgumentException = ClassName.get(classOf[IllegalArgumentException])

    val IOException = ClassName.get(classOf[IOException])


    val `Boolean` = ClassName.get("", "Boolean")
    val `Class` = ClassName.get("", "Class")
    val InvocationTargetException = ClassName.get("java.lang.reflect", "InvocationTargetException")
    val StringBuilder = ClassName.get("", "StringBuilder")


    val Optional = ClassName.get("java.util", "Optional")

    def Optional(className: ClassName): ParameterizedTypeName = {
      ParameterizedTypeName.get(Optional, className)
    }

    def Collectors = ClassName.get("java.util.stream", "Collectors")

    def joining = StaticImportMethod(Collectors, "joining")


    def supplier(className: ClassName): ParameterizedTypeName = {
      ParameterizedTypeName.get(Supplier, className)
    }

    def supplier(typeName: TypeName): ParameterizedTypeName = {
      ParameterizedTypeName.get(Supplier, typeName)
    }


    def toList = StaticImportMethod(Collectors, "toList")


    val Iterable = ClassName.get("", "Iterable")

    def Iterable(className: ClassName): ParameterizedTypeName = {
      ParameterizedTypeName.get(Iterable, className)
    }

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


    val LinkedHashMap = ClassName.get("java.util", "LinkedHashMap")

    def LinkedList(keyClassName: ClassName, valueClassName: ClassName): ParameterizedTypeName = {
      ParameterizedTypeName.get(LinkedHashMap, keyClassName, valueClassName)
    }


    val Field = ClassName.get("java.lang.reflect", "Field")
    val Modifier = ClassName.get("java.lang.reflect", "Modifier")

    val ArrayList = ClassName.get("java.util", "ArrayList")

    def ArrayList(className: ClassName): ParameterizedTypeName = {
      ParameterizedTypeName.get(ArrayList, className)
    }

    def ArrayList(typeName: TypeName): ParameterizedTypeName = {
      ParameterizedTypeName.get(ArrayList, typeName)
    }

    def Method = ClassName.get("java.lang.reflect", "Method")

    def Exception = ClassName.get("", "Exception")


    val URLEncoder = ClassName.bestGuess("java.net.URLEncoder")
    val UnsupportedEncodingException = ClassName.bestGuess("java.io.UnsupportedEncodingException")

    val URISyntaxException = ClassName.bestGuess("java.net.URISyntaxException")

  }


  // END JAVA CORE #####################################################################################################
  //####################################################################################################################

  //####################################################################################################################
  // BEGIN COMMONS LANG ################################################################################################
  val randomUtils = ClassName.get("org.apache.commons.lang3", "RandomUtils")

  object CommonsLangTypes {

    val StringUtils = ClassName.bestGuess("org.apache.commons.lang3.StringUtils")
    val RandomUtils = ClassName.get("org.apache.commons.lang3", "RandomUtils")


  }

  object CommonsTextTypes {
    val StringEscapeUtils = ClassName.get("org.apache.commons.text", "StringEscapeUtils")

  }


  // END COMMONS LANG ##################################################################################################
  //####################################################################################################################


  //####################################################################################################################
  // BEGIN Spring ######################################################################################################


  object SpringTypes {

    val Configuration = ClassName.get("org.springframework.context.annotation", "Configuration")
    val Bean = ClassName.get("org.springframework.context.annotation", "Bean")
    val ResponseEntity: ClassName = ClassName.get("org.springframework.http", "ResponseEntity")
    val Primary = ClassName.bestGuess("org.springframework.context.annotation.Primary")
    val Value = ClassName.bestGuess("org.springframework.beans.factory.annotation.Value")

    def Value(value: String): AnnotationSpec = {
      AnnotationSpec.builder(Value)
        .addMember("value", "$S", value)
        .build()
    }


    //    val RequestEntity: ClassName = ClassName.get("org.springframework.http", "RequestEntity")

    val HttpClientErrorException = ClassName.bestGuess("org.springframework.web.client.HttpClientErrorException")

    val HttpServerErrorException = ClassName.bestGuess("org.springframework.web.client.HttpServerErrorException")


    //    org.springframework.web.client.HttpClientErrorException


    val DefaultResponseErrorHandler = ClassName.bestGuess("org.springframework.web.client.DefaultResponseErrorHandler")

    def ResponseEntity(className: ClassName): ParameterizedTypeName = ParameterizedTypeName.get(ResponseEntity, className)


    val HttpHeaders = ClassName.bestGuess("org.springframework.http.HttpHeaders")
    //    val HttpStatus = ClassName.bestGuess("org.springframework.http.HttpStatus")


    val ResponseBody = ClassName.bestGuess("org.springframework.web.bind.annotation.ResponseBody")

    val RequestBody = ClassName.bestGuess("org.springframework.web.bind.annotation.RequestBody")


    val ResponseStatus = ClassName.bestGuess("org.springframework.web.bind.annotation.ResponseStatus")
    val ResponseEntityOfObject: ParameterizedTypeName = ResponseEntity(ClassName.OBJECT)
    val SpringApplication = ClassName.get("org.springframework.boot", "SpringApplication")
    val SpringBootApplication = ClassName.get("org.springframework.boot.autoconfigure", "SpringBootApplication")
    val Controller = ClassName.get("org.springframework.stereotype", "Controller")
    val SpringBootTest = ClassName.get("org.springframework.boot.test.context", "SpringBootTest")
    val Autowired = ClassName.bestGuess("org.springframework.beans.factory.annotation.Autowired")
    val GetMapping = ClassName.bestGuess("org.springframework.web.bind.annotation.GetMapping")

    val DeleteMapping = ClassName.bestGuess("org.springframework.web.bind.annotation.DeleteMapping")


    val PostMapping = ClassName.bestGuess("org.springframework.web.bind.annotation.PostMapping")
    val PutMapping = ClassName.bestGuess("org.springframework.web.bind.annotation.PutMapping")
    val RequestMapping = ClassName.bestGuess("org.springframework.web.bind.annotation.RequestMapping")
    val HttpStatus = ClassName.bestGuess("org.springframework.http.HttpStatus")

    val I_AM_A_TEAPOT = StaticImportMethod.apply(HttpStatus, "I_AM_A_TEAPOT")

    val HttpMethod = ClassName.bestGuess("org.springframework.http.HttpMethod")

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
    val RestController = ClassName.bestGuess("org.springframework.web.bind.annotation.RestController")
    val Repository = ClassName.bestGuess("org.springframework.data.repository.Repository")

    val Converter = ClassName.bestGuess("org.springframework.core.convert.converter.Converter")

    val FormatterRegistry = ClassName.bestGuess("org.springframework.format.FormatterRegistry")

    val RestTemplate = ClassName.bestGuess("org.springframework.web.client.RestTemplate")

    val WebClient = ClassName.bestGuess("org.springframework.web.reactive.function.client.WebClient")

    val Headers = ClassName.bestGuess("org.springframework.web.reactive.function.client.ClientResponse.Headers")
    val MediaType = ClassName.bestGuess("org.springframework.http.MediaType")
    val BodyInserters = ClassName.bestGuess("org.springframework.web.reactive.function.BodyInserters")
    val ClientResponse = ClassName.bestGuess("org.springframework.web.reactive.function.client.ClientResponse")
    val Mono = ClassName.bestGuess("reactor.core.publisher.Mono")


    val RequestEntity = ClassName.bestGuess("org.springframework.http.RequestEntity")


    object SpringValidationTypes {
      val Validated = ClassName.get("org.springframework.validation.annotation", "Validated")
    }

    object SpringTestTypes {
      val SpringExtension = ClassName.get("org.springframework.test.context.junit.jupiter", "SpringExtension")
      val SpringBootTest = ClassName.get("org.springframework.boot.test.context", "SpringBootTest")
      val SpringJUnitConfig = ClassName.get("org.springframework.test.context.junit.jupiter", "SpringJUnitConfig")
    }

    object MockitoTypes {
      val Mockito = ClassName.get("org.mockito", "Mockito")
      val spy = StaticImportMethod.apply(Mockito, "spy")
      val mock = StaticImportMethod.apply(Mockito, "mock")
      val when = StaticImportMethod.apply(Mockito, "when")
      val ArgumentMatchers = ClassName.get("org.mockito", "ArgumentMatchers")
      val any = StaticImportMethod.apply(ArgumentMatchers, "any")
      val anyString = StaticImportMethod.apply(ArgumentMatchers, "anyString")
    }


    object SpringDataTypes {

      val Repository = ClassName.get("org.springframework.stereotype", "Repository")

      val Query = ClassName.bestGuess("org.springframework.data.jpa.repository.Query")

      val Pageable = ClassName.bestGuess("org.springframework.data.domain.Pageable")
      val Slice = ClassName.bestGuess("org.springframework.data.domain.Slice")

      val Param = ClassName.bestGuess("org.springframework.data.repository.query.Param")


      val Page = ClassName.bestGuess("org.springframework.data.domain.Page")

      def Page(typeName: TypeName): ParameterizedTypeName = {
        ParameterizedTypeName.get(Page, typeName)
      }

      def Slice(typeName: TypeName): ParameterizedTypeName = {
        ParameterizedTypeName.get(Slice, typeName)
      }

      val EntityScan = ClassName.bestGuess("org.springframework.boot.autoconfigure.domain.EntityScan")
      val EnableJpaRepositories = ClassName.bestGuess("org.springframework.data.jpa.repository.config.EnableJpaRepositories")

    }

  }


  // END Spring ########################################################################################################
  //####################################################################################################################

  val mock = ClassName.get("org.mockito", "Mock")


  //####################################################################################################################
  // BEGIN Lombok ######################################################################################################
  val FieldNameConstants = ClassName.get(classOf[FieldNameConstants])
  val slf4j = ClassName.get(classOf[Slf4j])
  val builder = ClassName.get(classOf[Builder])
  val AllArgsConstructor = ClassName.get(classOf[AllArgsConstructor])
  val NoArgsConstructor = ClassName.get(classOf[NoArgsConstructor])
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
    val JsonIgnore = ClassName.get(classOf[JsonIgnore])
    val JsonValue = ClassName.get(classOf[JsonValue])
    val Singular = ClassName.get(classOf[Singular])
    val Generated = ClassName.get(classOf[Generated])

    val EqualsAndHashCode = ClassName.get(classOf[EqualsAndHashCode])
    val Data = ClassName.get(classOf[Data])
    val `val` = ClassName.get("lombok", "val")


  }

  object FakerTypes {
    val FakeValuesService = ClassName.get("com.github.javafaker.service", "FakeValuesService")
    val RandomService = ClassName.get("com.github.javafaker.service", "RandomService")
  }


  // END Lombok ########################################################################################################
  //####################################################################################################################

  //####################################################################################################################
  // BEGIN Junit5 ######################################################################################################

  def displayName(testNAme: String): AnnotationSpec = AnnotationSpec.builder(JunitTypes.DisplayName).addMember("value", "$S", testNAme).build()

  object JunitTypes {
    val Test = ClassName.get("org.junit.jupiter.api", "Test")
    val ExtendWith = ClassName.get("org.junit.jupiter.api.extension", "ExtendWith")
    val DisplayName: ClassName = ClassName.get("org.junit.jupiter.api", "DisplayName")
    val Assertions = ClassName.get("org.junit.jupiter.api", "Assertions")
    val assertThrows = StaticImportMethod(Assertions, "assertThrows")
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
    val is = StaticImportMethod(Matchers, "is")

  }


  val matcherAssert = ClassName.get("org.hamcrest", "MatcherAssert")

  val assertThat = StaticImportMethod(matcherAssert, "assertThat")
  val matchers = ClassName.get("org.hamcrest", "Matchers")
  val notNullValue = StaticImportMethod(matchers, "notNullValue")

  // END Hamcrest ######################################################################################################
  //####################################################################################################################


  //####################################################################################################################
  // BEGIN Javax.Validation ############################################################################################

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
      val ConstraintViolation = ClassName.get("javax.validation", "ConstraintViolation")


      def ConstraintViolation(typeName: TypeName): ParameterizedTypeName = {
        ParameterizedTypeName.get(ConstraintViolation, typeName)
      }
    }

    object JavaxPersistanceTypes {
      val Basic = ClassName.get("javax.persistence", "Basic")
      val Column = ClassName.get("javax.persistence", "Column")
      val Entity = ClassName.get("javax.persistence", "Entity")
      val GeneratedValue = ClassName.get("javax.persistence", "GeneratedValue")
      val GenerationType = ClassName.get("javax.persistence", "GenerationType")
      val Id = ClassName.get("javax.persistence", "Id")
      val MappedSuperclass = ClassName.get("javax.persistence", "MappedSuperclass")
      val PrePersist = ClassName.get("javax.persistence", "PrePersist")
      val PreUpdate = ClassName.get("javax.persistence", "PreUpdate")
      val Temporal = ClassName.get("javax.persistence", "Temporal")
      val TemporalType = ClassName.get("javax.persistence", "TemporalType")
      val Version = ClassName.get("javax.persistence", "Version")
      val Table = ClassName.get("javax.persistence", "Table")

      val ManyToOne = ClassName.get("javax.persistence", "ManyToOne")

      val OneToMany = ClassName.get("javax.persistence", "OneToMany")


      val JoinColumn = ClassName.get("javax.persistence", "JoinColumn")


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
  val JavaTimeModule = ClassName.get("com.fasterxml.jackson.datatype.jsr310", "JavaTimeModule")
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


  object JacksonTypes {
    val ObjectMapper = ClassName.get("com.fasterxml.jackson.databind", "ObjectMapper")
    val JavaTimeModule = ClassName.get("com.fasterxml.jackson.datatype.jsr310", "JavaTimeModule")
    val YAMLFactory = ClassName.get("com.fasterxml.jackson.dataformat.yaml", "YAMLFactory")
    val YAMLGenerator = ClassName.get("com.fasterxml.jackson.dataformat.yaml", "YAMLGenerator")

    val JsonIgnoreProperties = ClassName.get("com.fasterxml.jackson.annotation", "JsonIgnoreProperties")

    val DeserializationFeature = ClassName.get("com.fasterxml.jackson.databind", "DeserializationFeature")
    val SerializationFeature = ClassName.get("com.fasterxml.jackson.databind", "SerializationFeature")

    val JavaType = ClassName.bestGuess("com.fasterxml.jackson.databind.JavaType")
    val TypeFactory = ClassName.bestGuess("com.fasterxml.jackson.databind.type.TypeFactory")
    val Converter = ClassName.bestGuess("com.fasterxml.jackson.databind.util.Converter")
    val JsonDeserialize = ClassName.bestGuess("com.fasterxml.jackson.databind.annotation.JsonDeserialize")
    val JsonSerialize = ClassName.bestGuess("com.fasterxml.jackson.databind.annotation.JsonSerialize")


    val JsonInclude = ClassName.get(classOf[JsonInclude])

    val TypeReference = ClassName.bestGuess("com.fasterxml.jackson.core.type.TypeReference")


  }


  //val  = ClassName.get("","")
  //val  = ClassName.bestGuess("")


}
