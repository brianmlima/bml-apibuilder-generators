package bml.util.java.client.util

import bml.util.AnotationUtil.LombokAnno
import bml.util.attribute.Hibernate
import bml.util.java.ClassNames.{JavaTypes, SpringTypes}
import com.squareup.javapoet.{ClassName, FieldSpec, MethodSpec, ParameterSpec, TypeSpec}
import io.apibuilder.spec.v0.models.Model
import javax.lang.model.element.Modifier.{FINAL, PROTECTED, PUBLIC, STATIC}


class UriParam(val containerClass: ClassName, val paramTypeEnum: ParamTypeEnum) {
  val valueToStringFieldName = "valueToString"

  val nameField = "name";
  val uriParamTypeField = "uriParamType"
  val typeField = "valueType"
  val requiredField = "required";
  val defaultValueField = "defaultValue"

  val uriParamTypeName = ClassName.get(containerClass.canonicalName(), "UriParam")
  val uriParamType = TypeSpec.classBuilder(uriParamTypeName)
    .addJavadoc("A helper class for building uri's in a way that makes code generation cleaner.  ")
    .addModifiers(PROTECTED, STATIC)
    .addTypeVariable(JavaTypes.T)
    .addAnnotation(LombokAnno.AllArgsConstructor)
    .addAnnotation(LombokAnno.Builder)
    .addAnnotation(LombokAnno.Getter)
    .addAnnotation(LombokAnno.AccessorFluent)
    .addField(
      FieldSpec.builder(JavaTypes.String, nameField, FINAL)
        //          .addJavadoc("The name of the parameter used in a query or path.")
        .build()
    )
    .addField(
      FieldSpec.builder(paramTypeEnum.typeName, uriParamTypeField, FINAL)
        //          .addJavadoc("The name of the parameter used in a query or path.")
        .build()
    )
    .addField(
      FieldSpec.builder(JavaTypes.Class(JavaTypes.T), typeField, FINAL)
        //          .addJavadoc("The type of the parameter value. This is used to chose an encoding handler. ")
        .build()
    )
    .addField(
      FieldSpec.builder(JavaTypes.`Boolean`, requiredField, FINAL)
        //          .addJavadoc("The type of the parameter value. This is used to chose an encoding handler. ")
        .addAnnotation(LombokAnno.BuilderDefault)
        .initializer("$T.TRUE", JavaTypes.`Boolean`)
        .build()
    )
    .addField(
      FieldSpec.builder(JavaTypes.Function(JavaTypes.T, JavaTypes.String), valueToStringFieldName, FINAL)
        .addAnnotation(LombokAnno.BuilderDefault)
        .initializer("(o) -> o.toString()")
        .build()
    )
    .addField(
      FieldSpec.builder(JavaTypes.Optional(JavaTypes.T), defaultValueField, FINAL)
        .addAnnotation(LombokAnno.BuilderDefault)
        .initializer("$T.empty()", JavaTypes.Optional)
        .build()
    )
    .addSuperinterface(JavaTypes.BiFunction(SpringTypes.UriBuilder, JavaTypes.Optional(JavaTypes.T), SpringTypes.UriBuilder))
    .addMethod(
      MethodSpec.methodBuilder("apply")
        .addModifiers(PUBLIC)
        .returns(SpringTypes.UriBuilder)
        .addParameter(ParameterSpec.builder(SpringTypes.UriBuilder, "uriBuilder", FINAL).build())
        .addParameter(ParameterSpec.builder(JavaTypes.Optional(JavaTypes.T), "valueOpt", FINAL).build())
        .addStatement("return uriBuilder.queryParamIfPresent($L,valueOpt.or(() -> $L()).map($L))", nameField, defaultValueField, valueToStringFieldName)
        .build()
    )
    .build()


}
