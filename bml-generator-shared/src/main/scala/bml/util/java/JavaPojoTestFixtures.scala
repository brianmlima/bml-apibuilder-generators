package bml.util.java

import java.util.Locale

import akka.http.scaladsl
import akka.http.scaladsl.model
import akka.http.scaladsl.model.headers.CacheDirectives.public
import bml.util.GeneratorFSUtil.makeFile
import bml.util.java.ClassNames.{illegalArgumentException, math, randomUtils, string, stringBuilder, supplier}
import bml.util.{AnotationUtil, NameSpaces, java}
import com.squareup.javapoet.TypeName.INT
import com.squareup.javapoet._
import io.apibuilder.generator.v0.models.File
import io.apibuilder.spec.v0.models.{Field, Model}
import javax.lang.model.element.Modifier._
import org.checkerframework.checker.units.qual.min

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


  def languagesClassName(nameSpaces: NameSpaces): ClassName = {
    ClassName.get(nameSpaces.tool.nameSpace, "Languages")
  }

  def makeLanguages(nameSpaces: NameSpaces): File = {
    val className = languagesClassName(nameSpaces)
    val typeBuilder = TypeSpec.classBuilder(className)
      .addField(
        FieldSpec.builder(TypeName.DOUBLE, "ENGLISH_AVG_WORD_LENGTH", PUBLIC, STATIC)
          .initializer("4.7")
          .build()
      )
    makeFile(className.simpleName(), nameSpaces.tool, typeBuilder)
  }


  //  public static Supplier<String> stringRangeSupplier(Locale locale, int min, int max) {
  //    if (max < min) {
  //      throw new IllegalArgumentException(String.format("max param can not be less than min param. min={} max={}", min, max));
  //    }
  //    if (min == 0 && max == 0) {
  //      throw new IllegalArgumentException("max param and min param can not both be 0");
  //    }
  //    if (min < 0) {
  //      throw new IllegalArgumentException(String.format("min param can not be less than 0. min={} max={}", min, max));
  //    }
  //    if (max < 0) {
  //      throw new IllegalArgumentException(String.format("max param can not be less than 0. min={} max={}", min, max));
  //    }
  //
  //    return () -> {
  //      int requestWordCount = (int) Math.ceil(max / Languages.ENGLISH_AVG_WORD_LENGTH);
  //      final String[] words = LoremTool.getWords(locale, requestWordCount).split("[ ]");
  //      //log.trace("Words={}", words);
  //      final int randStringLength = RandomUtils.nextInt(min, max);
  //      final StringBuilder buff = new StringBuilder();
  //
  //      for (String word : words) {
  //        //log.trace("Buffer Length={} randStringLength={} wordCount={} requestWordCount={}", buff.length(), randStringLength, words.length, requestWordCount);
  //        if (buff.length() <= randStringLength) {
  //          buff.append(word);
  //        } else {
  //          break;
  //        }
  //      }
  //      String returnValue = (buff.length() > randStringLength) ? buff.toString().substring(0, randStringLength).trim() : buff.toString();
  //      log.debug("Returning Lorem String length={}", returnValue.length());
  //      return returnValue;
  //    };
  //  }


}
