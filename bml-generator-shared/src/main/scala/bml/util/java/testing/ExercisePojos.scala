package bml.util.java.testing

import java.lang.reflect.Field
import java.util

import bml.util.AnotationUtil.LombokAnno
import bml.util.GeneratorFSUtil.makeFile
import bml.util.NameSpaces
import bml.util.java.ClassNames.{HamcrestTypes, JavaTypes, JunitTypes, LombokTypes}
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
      HamcrestTypes.assertThat.staticImport,
      HamcrestTypes.notNullValue.staticImport,
      HamcrestTypes.is.staticImport
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
      .addAnnotation(LombokAnno.Generated)
      .addAnnotation(LombokTypes.Slf4j)
      .addMethods(

        service
          .models
          .map(
            modelIn => {
              val modelClassName = JavaPojoUtil.toClassName(modelIn.name)
              val modelClass = JavaPojoUtil.toClassName(nameSpaces.model, modelIn)
              val modelFactoryClass = JavaPojoTestFixtures.mockFactoryClassName(nameSpaces, modelClassName)
              val exceptionClass = ClassName.get("", "Exception")
              val methodName = s"exercise${modelClassName}Pojo"

              MethodSpec.methodBuilder(methodName)
                .addAnnotation(JunitTypes.Test)
                .addAnnotation(ClassNames.displayName(s"Exercising ${modelClassName} Pojo, Checking mock factory,default builders, and checking required fields"))
                .addException(exceptionClass)
                .addCode("excersizePojo($T.class,$T.class,$L);", modelClass, modelFactoryClass, modelIn.fields.size.toString).build()

            }
          ).asJava
      )
      .addMethod(
        MethodSpec.methodBuilder("excersizePojo")
          .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
          .addException(ClassName.get("", "Exception"))
          .addParameter(ParameterSpec.builder(JavaTypes.`Class`, "objectClass", Modifier.FINAL).build())
          .addParameter(ParameterSpec.builder(JavaTypes.`Class`, "factoryClass", Modifier.FINAL).build())
          .addParameter(ParameterSpec.builder(JavaTypes.Integer, "numFields", Modifier.FINAL).build())
          .addStatement("final Object factoryBuilder = factoryClass.getDeclaredMethod(\"builder\").invoke(null)")
          .addStatement("final Object factory = factoryBuilder.getClass().getDeclaredMethod(\"build\").invoke(factoryBuilder)")

          .addCode(
            CodeBlock.builder()
              .addStatement("$T $L = null", JavaTypes.Method, generateMethodName)
              .beginControlFlow("try")
              .addStatement("$L = factory.getClass().getDeclaredMethod(\"$L\")", generateMethodName, generateMethodName)
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
          .addStatement("Object anObject = $L.invoke(factory)", generateMethodName)
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
          .addCode(
            CodeBlock.builder()
              .addStatement("final Class fieldsClass = $T.asList(objectClass.getDeclaredClasses()).stream().filter(clazz -> clazz.getSimpleName().equals(\"Fields\")).findFirst().orElse(null)", JavaTypes.Arrays)
              .addStatement("assertThat(fieldsClass,notNullValue())")
//              .addStatement("$T[] declaredFields = Test.class.getDeclaredFields()", classOf[Field])
//              .addStatement("$T staticFields = new $T()", JavaTypes.List(JavaTypes.Field), JavaTypes.ArrayList(JavaTypes.Field))
//              .beginControlFlow("for ($T field : declaredFields)", classOf[Field])
//              .beginControlFlow("if ($T.isStatic(field.getModifiers()))", JavaTypes.Modifier)
//              .addStatement("staticFields.add(field)")
//              .endControlFlow()
//              .endControlFlow()
              //              .addStatement("assertThat(staticFields.size(), is(numFields))")
              .build()
          ).addCode(
          CodeBlock.builder()
            .addStatement("Method toBuilder = anObject.getClass().getDeclaredMethod(\"toBuilder\")")
            .addStatement("assertThat(String.format(\"{}.{}\", get.getClass().getSimpleName(), toBuilder.getName()), toBuilder, notNullValue())")
            .addStatement("Object builder = toBuilder.invoke(anObject)")
            .addStatement("assertThat(builder, notNullValue())")
            .addStatement("assertThat(builder.toString(), notNullValue())")
            .addStatement("assertThat(builder.equals(builder), is(true))")
            .addStatement("Method build = builder.getClass().getDeclaredMethod(\"build\")")
            .addStatement("assertThat(String.format(\"{}.{}\", get.getClass().getSimpleName(), build.getName()), build, notNullValue())")
            .addStatement("Object aDuplicateObject = build.invoke(builder)")
            .addStatement("assertThat(aDuplicateObject, notNullValue())")
            .addStatement("assertThat(anObject.equals(aDuplicateObject), is(true))")
            .addStatement("assertThat(anObject.hashCode(), is(aDuplicateObject.hashCode()))")
            .addStatement("assertThat(anObject.equals(new Object()), is(false))")
            .addStatement("assertThat(anObject.toString(), notNullValue())")
            .add("//Since we use random objects from factories some structures have a high potential to build the same object")
            .add("//even with randomization. IE an object that has one field that is an enum with only 2 entries has a 50% chance of ")
            .add("//the mock generator returning the same object twice. It is because of this behavior we try the equals method 50 ")
            .add("//times and break if it returns false like we want \n")
            .addStatement("boolean result = false")
            .beginControlFlow("for(int c=0; c<50;c++)")
            .beginControlFlow("if(!anObject.equals(get.invoke(factory)))")
            .addStatement("result=true")
            .addStatement("break")
            .endControlFlow()
            .endControlFlow()
            .beginControlFlow("if(!result)")
            .addStatement("assertThat(\"Equals Test failed. This could be due to object structure. See class comments.\",false,is(true))")
            .endControlFlow()

            .build()
        )


          .build()
      )
    makeFile(className.simpleName(), nameSpaces.model, typeBuilder, staticImports: _*)
  }

}
