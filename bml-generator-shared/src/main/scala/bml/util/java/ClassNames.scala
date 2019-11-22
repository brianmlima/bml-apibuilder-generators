package bml.util.java

import com.squareup.javapoet.{ClassName, ParameterizedTypeName}

object ClassNames {

  val collections = ClassName.bestGuess("java.util.Collections")

  val string = ClassName.get(classOf[String])

  val springBootTest = ClassName.get("org.springframework.boot.test.context", "SpringBootTest")

  val mock = ClassName.get("org.mockito", "Mock")

  val responseEntity: ClassName = ClassName.get("org.springframework.http", "ResponseEntity")

  def responseEntity(className: ClassName): ParameterizedTypeName = ParameterizedTypeName.get(responseEntity, className)

  val responseBody = ClassName.bestGuess("org.springframework.web.bind.annotation.ResponseBody")

  val responseStatus = ClassName.bestGuess("org.springframework.web.bind.annotation.ResponseStatus")

  val responseEntityOfObject: ParameterizedTypeName = responseEntity(ClassName.OBJECT)

  val springApplication = ClassName.get("org.springframework.boot", "SpringApplication")

  val springBootApplication = ClassName.get("org.springframework.boot.autoconfigure", "SpringBootApplication")

  val controller = ClassName.get("org.springframework.stereotype", "Controller")

  val slf4j = ClassName.bestGuess("lombok.extern.slf4j.Slf4j")

  val notNull = ClassName.bestGuess("javax.validation.constraints.NotNull")

  val notBlank = ClassName.bestGuess("javax.validation.constraints.NotBlank")

  val `override` = classOf[Override]

  val autowired = ClassName.bestGuess("org.springframework.beans.factory.annotation.Autowired")

  val mediaType = ClassName.bestGuess("org.springframework.http.MediaType")

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

  //def = ClassName.bestGuess("")
  val pathVariable = ClassName.bestGuess("org.springframework.web.bind.annotation.PathVariable")

  val requestHeader = ClassName.bestGuess("org.springframework.web.bind.annotation.RequestHeader")

  val requestParam = ClassName.bestGuess("org.springframework.web.bind.annotation.RequestParam")

  val objectIdentity = ClassName.bestGuess("org.springframework.security.acls.model.ObjectIdentity")

  val noRepositoryBean = ClassName.bestGuess("org.springframework.data.repository.NoRepositoryBean")

  val validated = ClassName.bestGuess("org.springframework.validation.annotation.Validated")

  val restController = ClassName.bestGuess("org.springframework.web.bind.annotation.RestController")


  val pattern = ClassName.bestGuess("javax.validation.constraints.Pattern")

  val size = ClassName.bestGuess("javax.validation.constraints.Size")

  val email = ClassName.bestGuess("javax.validation.constraints.Email")

  val valid = ClassName.bestGuess("javax.validation.Valid")

  val mappedSuperclass = ClassName.bestGuess("javax.persistence.MappedSuperclass")

  val slice: ClassName = ClassName.get("org.springframework.data.domain", "Slice")

  def slice(className: ClassName): ParameterizedTypeName = ParameterizedTypeName.get(slice, className)

  val repository = ClassName.get("org.springframework.data.repository", "Repository")

  val pageable: ClassName = ClassName.get("org.springframework.data.domain", "Pageable")

}
