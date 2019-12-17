package bml.util.java.testing

import bml.util.GeneratorFSUtil.makeFile
import bml.util.NameSpaces
import bml.util.java.ClassNames.{HamcrestTypes, JavaTypes, LombokTypes}
import bml.util.java.{ClassNames, JavaPojoTestFixtures, JavaPojoUtil, JavaPojos}
import bml.util.java.poet.StaticImport
import com.squareup.javapoet.{ClassName, CodeBlock, MethodSpec, ParameterSpec, TypeSpec}
import io.apibuilder.generator.v0.models.File
import io.apibuilder.spec.v0.models.Service
import javax.lang.model.element.Modifier

import scala.collection.JavaConverters._

object ExercisePojos {


  def excersisePojoTestClass(service: Service, nameSpaces: NameSpaces): File = {

    import bml.util.java.JavaPojoTestFixtures.generateMethodName


    val className = ClassName.get(nameSpaces.base.nameSpace, "ExercisePojosTests")

    val staticImports = Seq[StaticImport](
      ClassNames.assertThat.staticImport,
      ClassNames.notNullValue.staticImport
    )


    def foo = CodeBlock.join(
      service
        .models
        .map(_.name)
        .map(JavaPojoUtil.toClassName)
        .map(
          name =>
            CodeBlock.of(
              "excersizePojo($T.class,$T.class);",
              ClassName.get(nameSpaces.model.nameSpace, name),
              JavaPojoTestFixtures.mockFactoryClassName(nameSpaces, name)
            )
        ).asJava, "\n"
    )


    val typeBuilder = TypeSpec.classBuilder(className).addModifiers(Modifier.PUBLIC)
      .addAnnotation(LombokTypes.Slf4j)
      .addMethods(

        service
          .models
          .map(_.name)
          .map(JavaPojoUtil.toClassName)
          .map(
            name =>
              MethodSpec.methodBuilder(s"exercise${name}Pojo").addAnnotation(ClassNames.test)
                .addAnnotation(ClassNames.displayName(s"Exercising ${name} Pojo, Checking mock factory,default builders, and checking required fields"))
                .addException(ClassName.get("", "Exception"))
                .addCode(
                  CodeBlock.of(
                    "excersizePojo($T.class,$T.class);",
                    ClassName.get(nameSpaces.model.nameSpace, name)
                    , JavaPojoTestFixtures.mockFactoryClassName(nameSpaces, name)
                  )
                )
                .build()
          ).asJava
      )
      .addMethod(
        MethodSpec.methodBuilder("excersizePojo").addModifiers(Modifier.PUBLIC, Modifier.STATIC)
          .addException(ClassName.get("", "Exception"))
          .addParameter(ParameterSpec.builder(JavaTypes.`Class`, "objectClass", Modifier.FINAL).build())
          .addParameter(ParameterSpec.builder(JavaTypes.`Class`, "factoryClass", Modifier.FINAL).build())
          .addStatement("final Object factoryBuilder = factoryClass.getDeclaredMethod(\"builder\", null).invoke(null, null)")
          .addStatement("final Object factory = factoryBuilder.getClass().getDeclaredMethod(\"build\", null).invoke(factoryBuilder, null)")

          .addCode(
            CodeBlock.builder()
              .addStatement("$T $L = null", JavaTypes.Method, generateMethodName)
              .beginControlFlow("try")
              .addStatement("$L = factory.getClass().getDeclaredMethod(\"$L\", null)", generateMethodName, generateMethodName)
              .endControlFlow()
              .beginControlFlow("catch ($T e)", JavaTypes.Exception)
              .addStatement("log.error(\"{} caught msg={}\",e.getClass().getSimpleName(),e.getMessage(),e)")
              .addStatement("throw e")
              .endControlFlow()
              .build()

          )

          .addStatement(
            "assertThat($T.format(\"{}\",$L.getClass().getSimpleName()),$L,$L())",
            JavaTypes.String,
            generateMethodName,
            generateMethodName,
            HamcrestTypes.notNullValue.methodName
          )
          .addStatement("log.debug(\"Using {}.{}()\",factory.getClass().getSimpleName(),$S)", generateMethodName)
          .addStatement("Object anObject = $L.invoke(factory, null)", generateMethodName)
          .addStatement("" +
            "$L($T.format(\"%s should not be null\", anObject.getClass()), anObject, $L())",
            ClassNames.assertThat.methodName,
            JavaTypes.String,
            ClassNames.notNullValue.methodName)
          .addStatement(
            "$T requiredFields = ($T) objectClass.getDeclaredField(\"$L\").get(null)" +
              "", JavaTypes.List(JavaTypes.String), JavaTypes.List(JavaTypes.String), JavaPojos.requiredFieldsFieldName)
          .addCode(
            CodeBlock.builder()
              .beginControlFlow("for ($T field : requiredFields)", JavaTypes.String)
              .addStatement("$T fieldGetter = anObject.getClass().getMethod(field)", JavaTypes.Method)
              .add("assertThat(")
              .add("String.format(\"%s.%s should not be null\", anObject.getClass().getName(), field),")
              .add("fieldGetter.invoke(anObject),")
              .add("notNullValue()")
              .addStatement(")")
              .endControlFlow()
              .build()
          )
          .build()
      )
    makeFile(className.simpleName(), nameSpaces.model, typeBuilder, staticImports: _*)
  }

}
