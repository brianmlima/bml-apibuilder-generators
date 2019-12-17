package bml.util.java.testing

import bml.util.GeneratorFSUtil.makeFile
import bml.util.NameSpaces
import bml.util.java.{ClassNames, JavaPojoTestFixtures, JavaPojoUtil, JavaPojos}
import bml.util.java.poet.StaticImport
import com.squareup.javapoet.{ClassName, CodeBlock, MethodSpec, ParameterSpec, TypeSpec}
import io.apibuilder.generator.v0.models.File
import io.apibuilder.spec.v0.models.Service
import javax.lang.model.element.Modifier
import scala.collection.JavaConverters._

object ExercisePojos {


  def excersisePojoTestClass(service: Service, nameSpaces: NameSpaces): File = {
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
      //      .addMethod(
      //        MethodSpec.methodBuilder("exercisePojos").addAnnotation(ClassNames.test)
      //          .addException(ClassName.get("", "Exception"))
      //          .addCode(foo)
      //          .build()
      //
      //      )
      .addMethod(
        MethodSpec.methodBuilder("excersizePojo").addModifiers(Modifier.PUBLIC, Modifier.STATIC)
          .addException(ClassName.get("", "Exception"))
          .addParameter(ParameterSpec.builder(ClassNames.`class`, "objectClass", Modifier.FINAL).build())
          .addParameter(ParameterSpec.builder(ClassNames.`class`, "factoryClass", Modifier.FINAL).build())
          .addStatement("final Object factoryBuilder = factoryClass.getDeclaredMethod(\"builder\", null).invoke(null, null)")
          .addStatement("final Object factory = factoryBuilder.getClass().getDeclaredMethod(\"build\", null).invoke(factoryBuilder, null)")
          .addStatement("Object anObject = factory.getClass().getDeclaredMethod(\"$L\", null).invoke(factory, null)", JavaPojoTestFixtures.generateMethodName)
          .addStatement("" +
            "$L($T.format(\"%s should not be null\", anObject.getClass()), anObject, $L())",
            ClassNames.assertThat.methodName,
            ClassNames.string,
            ClassNames.notNullValue.methodName)
          .addStatement(
            "$T requiredFields = ($T) objectClass.getDeclaredField(\"$L\").get(null)" +
              "", ClassNames.list(ClassNames.string), ClassNames.list(ClassNames.string), JavaPojos.requiredFieldsFieldName)
          .addCode(
            CodeBlock.builder()
              .beginControlFlow("for ($T field : requiredFields)", ClassNames.string)
              .addStatement("$T fieldGetter = anObject.getClass().getMethod(field)", ClassNames.method)
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