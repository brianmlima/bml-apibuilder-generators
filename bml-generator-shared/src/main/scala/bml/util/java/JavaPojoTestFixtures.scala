package bml.util.java

import bml.util.GeneratorFSUtil.makeFile
import bml.util.{AnotationUtil, NameSpaces}
import com.squareup.javapoet._
import io.apibuilder.generator.v0.models.File
import io.apibuilder.spec.v0.models.{Field, Model}
import javax.lang.model.element.Modifier._

import collection.JavaConverters._

object JavaPojoTestFixtures extends JavaPojoUtil {

  def mockFactoryClassName(nameSpaces: NameSpaces, model: Model): ClassName = {
    ClassName.get(nameSpaces.model.nameSpace, toClassName(model.name) + "MockFactory")
  }


  def generateFunction(model: Model, nameSpaces: NameSpaces): MethodSpec = {
    val targetClassName = ClassName.get(nameSpaces.model.nameSpace, toClassName(model))
    val targetClassBuilderName = toBuilderClassName(targetClassName)
    MethodSpec.methodBuilder("generate")
      .addModifiers(PUBLIC)
      .returns(targetClassName)
      .addStatement("$T builder =  $T.builder()", targetClassBuilderName, targetClassName)
      .addCode(
        CodeBlock.join(
          model.fields
            .map(
              (field) => {
                val fieldName = toFieldName(field)
                CodeBlock.builder()
                  .beginControlFlow("if ($L !=null)", fieldName)
                  .addStatement("builder.$L($L.get())", fieldName, fieldName)
                  .endControlFlow()
                  .build()
              }
            ).asJava,
          ""
        )
      ).addStatement("return builder.build()")
      .build()
  }

  def generateMockFactory(nameSpaces: NameSpaces, model: Model): File = {
    val className = mockFactoryClassName(nameSpaces, model)
    val targetClassName = ClassName.get(nameSpaces.model.nameSpace, toClassName(model))
    val targetClassBuilderName = toBuilderClassName(targetClassName)
    val typeBuilder = TypeSpec.classBuilder(className)
      .addAnnotation(ClassNames.builder)
      .addAnnotation(AnotationUtil.fluentAccessor)
      .addFields(
        model.fields
          .map(
            (field) => {
              val fieldName = toFieldName(field)
              val fieldType = dataTypeFromField(field, nameSpaces.model)
              val supplierType = ClassNames.supplier(fieldType)
              FieldSpec.builder(supplierType, fieldName, PRIVATE).addAnnotation(ClassNames.getter).build()
            }
          ).asJava
      )
      .addMethod(generateFunction(model, nameSpaces))
    makeFile(className.simpleName(), nameSpaces.modelFactory, typeBuilder)
  }

  def typeNameToClassName(typeName: TypeName): Unit = {

    //    typeName
    //    .

  }


  def generateDefaultSupplier(field: Field, nameSpaces: NameSpaces): Option[FieldSpec] = {
    val fieldName = toFieldName(field) + "DefaultSupplier"
    val fieldType = dataTypeFromField(field, nameSpaces.model)
    val supplierType = ClassNames.supplier(fieldType)


    val stringTypeName = dataTypeFromField("string", nameSpaces.model)


    if (fieldType.equals(stringTypeName)) {


      FieldSpec.builder(supplierType, fieldName, PUBLIC, STATIC)
      //.initializer()


    }
    None
  }


}
