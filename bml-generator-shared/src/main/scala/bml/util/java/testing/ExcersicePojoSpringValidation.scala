package bml.util.java.testing

import bml.util.AnotationUtil.{JunitAnno, LombokAnno}
import bml.util.java.ClassNames.HamcrestTypes
import bml.util.java.ClassNames.JavaxTypes.JavaxValidationTypes
import bml.util.{AnotationUtil, NameSpaces}
import bml.util.java.{ClassNames, TestSuppliers}
import bml.util.java.poet.StaticImport
import com.squareup.javapoet.{ClassName, FieldSpec, MethodSpec, TypeSpec}
import io.apibuilder.generator.v0.models.File
import io.apibuilder.spec.v0.models.Service

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

  def excersisePojoTestClass(service: Service, nameSpaces: NameSpaces): File = {
    val theClassName = className(nameSpaces)

    val staticImports = Seq[StaticImport](
      HamcrestTypes.assertThat.staticImport,
      HamcrestTypes.notNullValue.staticImport
    )

    object methods {
      val testValidValidator = "testValidValidator"
      val validationTest = "testValidation"
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


    val typeSpec = classBuilder(theClassName).addModifiers(PUBLIC)
      .addAnnotation(LombokAnno.Slf4j)
      .addField(
        FieldSpec.builder(JavaxValidationTypes.Validator, fields.validator, PRIVATE, FINAL)
          .initializer("$T.buildDefaultValidatorFactory().getValidator()", JavaxValidationTypes.Validation)
          .build()
      )
      .addMethod(validationTestMethod())

    makeFile(theClassName.simpleName(), nameSpaces.model, typeSpec, staticImports: _*)
  }

}
