package bml.util.java.client.util

import bml.util.AnotationUtil.LombokAnno
import bml.util.NameSpaces
import bml.util.java.ClassNames.{JavaTypes, SpringTypes}
import bml.util.java.JavaPojoUtil
import com.squareup.javapoet.{ClassName, CodeBlock, FieldSpec, MethodSpec, ParameterSpec, ParameterizedTypeName, TypeSpec}
import io.apibuilder.spec.v0.models.{Parameter, ParameterLocation, Service}
import javax.lang.model.element.Modifier.{FINAL, PROTECTED, PUBLIC, STATIC}
import org.checkerframework.checker.units.qual.A

class Params(val service: Service, val nameSpaces: NameSpaces, val containerClass: ClassName, params: Seq[Parameter], val uriParamClass: UriParam) {

  import collection.JavaConverters._

  val typeName = ClassName.get(containerClass.canonicalName(), "Params")

  val uriParams = params.filter(p => (p.location == ParameterLocation.Path || p.location == ParameterLocation.Query)).seq

  def makeType(): TypeSpec = {
    val typeSpec = TypeSpec
      .classBuilder(typeName)
      .addJavadoc("A helper class for building uri's in a way that makes code generation cleaner.")
      .addModifiers(PROTECTED, STATIC)
    uriParams.foreach(
      param => {
        val fieldName = JavaPojoUtil.toStaticFieldName(param.name)
        val paramType = JavaPojoUtil.dataTypeFromField(service, param.`type`, nameSpaces.model)
        val fieldType = ParameterizedTypeName.get(uriParamClass.uriParamTypeName, paramType)
        val fieldSpec = FieldSpec.builder(fieldType, fieldName, PUBLIC, STATIC, FINAL)
          .initializer({

            val uriParamType = param.location match {
              case ParameterLocation.Query => uriParamClass.paramTypeEnum.query;
              case ParameterLocation.Path => uriParamClass.paramTypeEnum.path;
              case ParameterLocation.Header => uriParamClass.paramTypeEnum.header;
            }

            val code = CodeBlock.builder()
              .add("$T.<$T>builder()", uriParamClass.uriParamTypeName, paramType)
              .add(".$L($S)", uriParamClass.nameField, param.name)
              .add(".$L($L)", uriParamClass.requiredField, if (param.required) java.lang.Boolean.TRUE else java.lang.Boolean.FALSE)
              .add(".$L($L.class)", uriParamClass.typeField, paramType)
              .add(
                ".$L($L.$L)",
                uriParamClass.uriParamTypeField,
                uriParamClass.paramTypeEnum.typeName,
                uriParamType
              )

            if (param.default.isDefined) {
              param.`type` match {
                case "string" => code.add(".$L($T.of($S))", uriParamClass.defaultValueField, JavaTypes.Optional, param.default.get)
                case "integer" => code.add(".$L($T.of($T.valueOf($S)))", uriParamClass.defaultValueField, JavaTypes.Optional, JavaTypes.Integer, param.default.get)
                case "boolean" => code.add(".$L($T.of($T.valueOf($S)))", uriParamClass.defaultValueField, JavaTypes.Optional, JavaTypes.`Boolean`, param.default.get)
              }
            }
            code.add(".build()")
            code.build()
          }
          )
        typeSpec.addField(fieldSpec.build())
      }
    )
    //      )
    typeSpec.build()
  }
}
