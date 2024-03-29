package bml.util.java.testing

import bml.util.AnotationUtil.{JunitAnno, LombokAnno}
import bml.util.java.ClassNames.{CommonsTextTypes, HamcrestTypes, JavaTypes}
import bml.util.java.ClassNames.JavaxTypes.JavaxValidationTypes
import bml.util.{AnotationUtil, NameSpaces}
import bml.util.java.{ClassNames, JavaPojoTestFixtures, JavaPojoUtil, TestSuppliers}
import bml.util.java.poet.StaticImport
import bml.util.persist.SpringVariableTypes.ValidationTypes
import bml.util.spring.SpringVersion.SpringVersion
import com.squareup.javapoet.{ClassName, FieldSpec, MethodSpec, ParameterSpec, TypeSpec}
import io.apibuilder.generator.v0.models.File
import io.apibuilder.spec.v0.models.{Model, Service}
import org.checkerframework.checker.units.qual.s
import org.springframework.data.mapping.PropertyPath

object ExcersicePojoSpringValidation {

  import bml.util.GeneratorFSUtil.makeFile
  import com.squareup.javapoet.MethodSpec.methodBuilder
  import com.squareup.javapoet.TypeSpec.classBuilder
  import javax.lang.model.element.Modifier._
  import scala.collection.JavaConverters._


  /** Provides a namespaced ClassName for TestSuppliers.
   *
   * @param nameSpaces NameSpaces for the service
   * @return a namespaced ClassName for TestSuppliers
   */
  def className(nameSpaces: NameSpaces) = ClassName.get(nameSpaces.base.nameSpace, "ExercisePojoValidationTests")

  def excersisePojoTestClass(springVersion: SpringVersion, service: Service, nameSpaces: NameSpaces): File = {
    val theClassName = className(nameSpaces)

    val staticImports = Seq[StaticImport](
      JavaTypes.joining.staticImport,
      HamcrestTypes.assertThat.staticImport,
      HamcrestTypes.notNullValue.staticImport,
      HamcrestTypes.is.staticImport
    )

    object methods {
      val testValidValidator = "testValidValidator"
      val validationTest = "testValidation"
      val constraintViolationsToLogString = "constraintViolationsToLogString"
    }
    object fields {
      val validator = "validator"
    }


    def validationCheckMethod() = {
      methodBuilder(methods.testValidValidator)
        .addAnnotation(JunitAnno.Test)
        .addAnnotation(JunitAnno.DisplayName("Testing class validator is not null. This is required for all other tests in this class."))
        .addStatement("$T($L, $T())", HamcrestTypes.assertThat, fields.validator, HamcrestTypes.notNullValue)
        .addStatement("log.trace(\"Confirmed validator is not null\")")
    }

    def validationTestMethod(): MethodSpec = {
      methodBuilder(methods.validationTest).addModifiers(PRIVATE)
        .build()
    }

    def pojoValidationTestMethodName(model: Model): String = {
      "test" + JavaPojoUtil.toClassName(model) + "Validation"
    }

    def pojoValidationTestDisplayName(model: Model): String = {
      "Testing Validation on " + JavaPojoUtil.toClassName(model) + " provided by " + JavaPojoTestFixtures.mockFactoryClassName(nameSpaces, model)
    }


    def constraintViolationsToLogStringMethod(): MethodSpec = {
      MethodSpec.methodBuilder(methods.constraintViolationsToLogString).addModifiers(PRIVATE)
        .addTypeVariable(ClassNames.T)
        .addParameter(ParameterSpec.builder(
          JavaTypes.Set(ValidationTypes.ConstraintViolation.withTypeParameter(springVersion, ClassNames.T)),
          "constraintViolations",
          FINAL
        )
          .build()
        )
        .returns(JavaTypes.String)
        .addCode("return constraintViolations.stream().map(")
        .addCode("(violation) ->")
        .addCode("String.format(")
        .addCode("\"{Package:'%s', Object:'%s' PropertyPath:'%s' Value:'%s' message:'%s'}\",")
        .addCode("$T.escapeJson(violation.getRootBeanClass().getSimpleName()),", CommonsTextTypes.StringEscapeUtils)
        .addCode("$T.escapeJson(violation.getRootBeanClass().getSimpleName()),", CommonsTextTypes.StringEscapeUtils)
        .addCode("$T.escapeJson(violation.getPropertyPath().toString()),", CommonsTextTypes.StringEscapeUtils)
        .addCode("$T.escapeJson(violation.getInvalidValue().toString()),", CommonsTextTypes.StringEscapeUtils)
        .addCode("$T.escapeJson(violation.getMessage())", CommonsTextTypes.StringEscapeUtils)
        .addCode(")")
        .addCode(").collect($L(\", \"));", JavaTypes.joining.methodName)
        .build()
    }

    def buildValidationTest(model: Model): MethodSpec = {

      val mockFactoryClassName = JavaPojoTestFixtures.mockFactoryClassName(nameSpaces, model)
      val modelClassName = ClassName.get(nameSpaces.model.nameSpace, JavaPojoUtil.toClassName(model))

      methodBuilder(pojoValidationTestMethodName(model)).addModifiers(PUBLIC)
        .addAnnotation(JunitAnno.Test)
        .addAnnotation(JunitAnno.DisplayName(pojoValidationTestDisplayName(model)))
        .addStatement("$T factory = $T.$L", mockFactoryClassName, mockFactoryClassName, JavaPojoTestFixtures.defaultFactoryStaticParamName)
        .addStatement("$L(\" factory instance of $T should not be null\",factory,$L())", HamcrestTypes.assertThat.methodName, mockFactoryClassName, HamcrestTypes.notNullValue.methodName)
        .addStatement("$T object = factory.get()", modelClassName)
        .addStatement("$T constraintViolations", JavaTypes.Set(ValidationTypes.ConstraintViolation.withTypeParameter(springVersion, modelClassName)))
        .addCode("try{")
        .addStatement("constraintViolations = validator.validate(object)")
        .addCode("}catch($T e){ ", JavaTypes.Exception)
        .addCode("log.error(\"{} caught while validating instance of {} msg={}\",e.getClass().getSimpleName(),$T.class.getName(),e.getMessage(),e); throw e;}", modelClassName)
        .addStatement("$T constraintViolationErrors = $L(constraintViolations)", JavaTypes.String, methods.constraintViolationsToLogString)
        .addStatement(
          "$L(String.format(\"ConstraintViolations should be empty Found=%s Errors=%s\", constraintViolations.size(),constraintViolationErrors), constraintViolations.isEmpty(), $L(true))",
          HamcrestTypes.assertThat.methodName,
          HamcrestTypes.is.methodName
        )
        .build()
    }

    val typeSpec = classBuilder(theClassName).addModifiers(PUBLIC)
      .addAnnotation(LombokAnno.Generated)
      .addAnnotation(LombokAnno.Slf4j)
      .addField(
        FieldSpec.builder(ValidationTypes.Validator.toClassName(springVersion), fields.validator, PRIVATE, FINAL)
          .initializer("$T.buildDefaultValidatorFactory().getValidator()", ValidationTypes.Validation.toClassName(springVersion))
          .build()
      )
      .addMethod(constraintViolationsToLogStringMethod)
      .addMethods(service.models.map(buildValidationTest).asJava)
      .addMethod(validationTestMethod())

    makeFile(theClassName.simpleName(), nameSpaces.model, typeSpec, staticImports: _*)
  }

}
