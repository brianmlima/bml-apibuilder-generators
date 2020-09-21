package bml.util.java.testing

import bml.util.GeneratorFSUtil.makeFile
import bml.util.NameSpaces
import bml.util.java.{ClassNames, JavaPojoTestFixtures, JavaPojoUtil}
import bml.util.java.ClassNames.{HamcrestTypes, JunitTypes, LombokTypes}
import bml.util.java.poet.StaticImport
import com.squareup.javapoet.{ClassName, CodeBlock, MethodSpec, TypeSpec}
import io.apibuilder.generator.v0.models.File
import io.apibuilder.spec.v0.models.Service
import javax.lang.model.element.Modifier

object ExcersizeEnums extends JavaPojoUtil {
  def excersiseEnumTestClass(service: Service, nameSpaces: NameSpaces): File = {

    import scala.collection.JavaConverters._

    val className = ClassName.get(nameSpaces.base.nameSpace, "ExerciseEnumTests")


    val staticImports = Seq[StaticImport](
      ClassNames.assertThat.staticImport,
      ClassNames.notNullValue.staticImport,
      HamcrestTypes.is.staticImport

    )


    val typeBuilder = TypeSpec.classBuilder(className).addModifiers(Modifier.PUBLIC)
      .addAnnotation(LombokTypes.Slf4j)
      .addMethods(
        service
          .enums
          .map(
            enumIn => {
              val enumClassName = toClassName(enumIn)
              val enumClass = toClassName(nameSpaces.model, enumIn)
              MethodSpec.methodBuilder(s"exercise${enumClassName}Enum").addAnnotation(JunitTypes.Test)
                .addAnnotation(ClassNames.displayName(s"Exercising ${enumClassName} Enum, Checking..."))
                .addException(ClassName.get("", "Exception"))
                .addCode(
                  CodeBlock.builder()
                    .addStatement("$T.Converters.ToString toStringConverter = new $T.Converters.ToString()", enumClass, enumClass)
                    .addStatement("$T.Converters.ToEnum toEnumConverter = new $T.Converters.ToEnum()", enumClass, enumClass)
                    .build()
                )
                .addCode(
                  CodeBlock.builder()
                    .beginControlFlow("for ( $T value : $T.values() )", enumClass, enumClass)
                    .addStatement("assertThat(value.apiValue(), notNullValue())")
                    .addStatement("assertThat(value.apiValue().isEmpty(), is(false))")
                    .addStatement("assertThat(value.toString(), notNullValue())")
                    .addStatement("assertThat(value.toString().isEmpty(), is(false))")
                    .addStatement("assertThat(value.toString(), is(value.apiValue()))")

                    .addStatement("assertThat($T.fromApiValue(value.apiValue()), is(value))", enumClass)
                    .addStatement("assertThat(toStringConverter.convert(value),is(value.apiValue()))")
                    .addStatement("assertThat(toEnumConverter.convert(value.apiValue()),is(value))")
                    .endControlFlow()
                    .build()
                )


                .build()


            }
          ).asJava
      )


    makeFile(className.simpleName(), nameSpaces.model, typeBuilder, staticImports: _*)

  }
}
