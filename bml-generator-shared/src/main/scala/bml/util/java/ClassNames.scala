package bml.util.java

import java.time.{LocalDate, LocalDateTime}
import java.util.concurrent.ThreadLocalRandom
import java.util.{Locale, Random, UUID}

import bml.util.JavaNameSpace
import bml.util.java.poet.StaticImportMethod
import com.squareup.javapoet._
import lombok.Builder.Default
import lombok.experimental.{FieldNameConstants, UtilityClass}
import lombok.extern.slf4j.Slf4j
import lombok.{AllArgsConstructor, Builder, Getter, NoArgsConstructor}

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
    val `Override` = classOf[Override]
    val String = ClassName.get(classOf[String])
    val Collections = ClassName.bestGuess("java.util.Collections")
    val Arrays = ClassName.get("java.util", "Arrays")
    val Set = ClassName.get("java.util", "Set")

    val Long = ClassName.get("java.lang", "Long")


    def Set(typeName: TypeName): ParameterizedTypeName = {
      ParameterizedTypeName.get(Set, typeName)
    }

    val Supplier = ClassName.get("java.util.function", "Supplier")
    val Integer = ClassName.get(classOf[Integer])
    val LocalDate = ClassName.get(classOf[LocalDate])
    val LocalDateTime = ClassName.get(classOf[LocalDateTime])
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


    val Iterable = ClassName.get("java.lang", "Iterable")

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

  //####################################################################################################################
  // BEGIN COMMONS LANG ################################################################################################
  val randomUtils = ClassName.get("org.apache.commons.lang3", "RandomUtils")

  object CommonsLangTypes {


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

    def ResponseEntity(className: ClassName): ParameterizedTypeName = ParameterizedTypeName.get(ResponseEntity, className)

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
    val RestController = ClassName.bestGuess("org.springframework.web.bind.annotation.RestController")
    val Repository = ClassName.bestGuess("org.springframework.data.repository.Repository")

    val Converter = ClassName.bestGuess("org.springframework.core.convert.converter.Converter")

    val FormatterRegistry = ClassName.bestGuess("org.springframework.format.FormatterRegistry")


    object SpringValidationTypes {
      val Validated = ClassName.get("org.springframework.validation.annotation", "Validated")
    }

    object SpringTestTypes {
      val SpringExtension = ClassName.get("org.springframework.test.context.junit.jupiter", "SpringExtension")
      val SpringBootTest = ClassName.get("org.springframework.boot.test.context", "SpringBootTest")
      val SpringJUnitConfig = ClassName.get("org.springframework.test.context.junit.jupiter", "SpringJUnitConfig")
    }

    object SpringDataTypes {

      val Repository = ClassName.get("org.springframework.stereotype", "Repository")

      val Query = ClassName.bestGuess("org.springframework.data.jpa.repository.Query")

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


    val DeserializationFeature = ClassName.get("com.fasterxml.jackson.databind", "DeserializationFeature")
    val SerializationFeature = ClassName.get("com.fasterxml.jackson.databind", "SerializationFeature")

    val JavaType = ClassName.bestGuess("com.fasterxml.jackson.databind.JavaType")
    val TypeFactory = ClassName.bestGuess("com.fasterxml.jackson.databind.type.TypeFactory")
    val Converter = ClassName.bestGuess("com.fasterxml.jackson.databind.util.Converter")
    val JsonDeserialize = ClassName.bestGuess("com.fasterxml.jackson.databind.annotation.JsonDeserialize")
    val JsonSerialize = ClassName.bestGuess("com.fasterxml.jackson.databind.annotation.JsonSerialize")


  }


  //val  = ClassName.get("","")
  //val  = ClassName.bestGuess("")


}
