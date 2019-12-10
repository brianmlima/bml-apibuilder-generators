package bml.util.java

import akka.http.scaladsl.model.headers.CacheDirectives.public
import bml.util.AnotationUtil.fluentAccessor
import bml.util.GeneratorFSUtil.makeFile
import bml.util.{AnotationUtil, NameSpaces}
import bml.util.java.ClassNames.{getter, illegalArgumentException, locale, loremIpsum, randomUtils, string, supplier, utilityClass, uuid}
import com.squareup.javapoet.MethodSpec.methodBuilder
import com.squareup.javapoet.TypeName.{DOUBLE, INT}
import com.squareup.javapoet.{ClassName, CodeBlock, FieldSpec, MethodSpec, ParameterSpec, ParameterizedTypeName, TypeName, TypeSpec, TypeVariableName}
import io.apibuilder.generator.v0.models.File
import javax.lang.model.element.Modifier._
import lib.Text
import lib.Text.initCap

import collection.JavaConverters._

object TestSuppliers {

  def testSuppliersClassName(nameSpaces: NameSpaces) = ClassName.get(nameSpaces.tool.nameSpace, "TestSuppliers")

  def recallSupplierClassName(nameSpaces: NameSpaces) = ClassName.get(nameSpaces.tool.nameSpace, "RecallSupplier")


  def t = TypeVariableName.get("T")


  private class Param(val parameterSpec: ParameterSpec, javadocString: String) {
    val javadoc = s"@param ${parameterSpec.name} ${javadocString}"
    val name = parameterSpec.name
    val fieldSpec = FieldSpec.builder(parameterSpec.`type`, name, PRIVATE)
      .addJavadoc(javadocString)
      .addAnnotation(getter)
      .build()

    def fieldSpecFinal = fieldSpec.toBuilder.addModifiers(FINAL).build()

  }

  private val supplierParam = new Param(ParameterSpec.builder(ParameterizedTypeName.get(supplier, t), "supplier", FINAL).build(), "")
  private val lastValueParam = new Param(ParameterSpec.builder(t, "lastValue").build(), "")

  def testSuppliers(nameSpaces: NameSpaces): File = {
    val className = testSuppliersClassName(nameSpaces)
    val recallSupplierClass = recallSupplierClassName(nameSpaces)
    val typeBuilder = TypeSpec.classBuilder(className)
      .addMethod(
        MethodSpec.methodBuilder("wrapRecall").addModifiers(PUBLIC, STATIC)
          .addTypeVariable(t)
          .returns(ParameterizedTypeName.get(ClassName.get("", recallSupplierClass.simpleName()), t))
          .addParameter(supplierParam.parameterSpec)
          .addStatement("return new $L($L)", recallSupplierClass.simpleName(), supplierParam.name)
          .build()
      )
      .addMethod(
        MethodSpec.methodBuilder("uuidSupplier").addModifiers(PUBLIC, STATIC)
          .returns(ParameterizedTypeName.get(supplier, uuid))
          .addStatement("return () -> $T.randomUUID()", uuid)
          .build()
      )


      .addType(recallSupplier(nameSpaces))

    makeFile(className.simpleName(), nameSpaces.tool, typeBuilder)

  }


  private def recallSupplier(nameSpaces: NameSpaces): TypeSpec = {
    val className = recallSupplierClassName(nameSpaces)
    TypeSpec.classBuilder(className).addModifiers(PUBLIC, STATIC)
      .addTypeVariable(t)
      .addAnnotation(fluentAccessor)
      .addSuperinterface(ParameterizedTypeName.get(supplier, t))
      .addFields(Seq(supplierParam.fieldSpecFinal, lastValueParam.fieldSpec).asJava)
      .addMethod(
        MethodSpec.constructorBuilder().addModifiers(PUBLIC)
          .addParameter(supplierParam.parameterSpec)
          .addStatement("this.$L = $L", supplierParam.name, supplierParam.name)
          .build()
      )
      .addMethod(
        MethodSpec.methodBuilder("get").addModifiers(PUBLIC).returns(t)
          .addCode(
            CodeBlock.builder()
              .addStatement("$L = $L.get()", lastValueParam.name, supplierParam.name)
              .addStatement("return $L", lastValueParam.name)
              .build()
          ).build()
      ).build()
  }


  //  @Accessors(fluent = true)
  //  public static class RecallSupplier<T> implements Supplier<T> {
  //    public RecallSupplier(Supplier<T> supplier) {
  //      this.supplier = supplier;
  //    }
  //    @Getter
  //    private Supplier<T> supplier;
  //    @Getter
  //    private T lastValue;
  //    @Override
  //    public T get() {
  //      lastValue = supplier.get();
  //      return lastValue;
  //    }
  //  }


}
