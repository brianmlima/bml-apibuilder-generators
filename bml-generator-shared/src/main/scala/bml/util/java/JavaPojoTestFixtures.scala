package bml.util.java

import java.util.Locale

import akka.http.scaladsl
import akka.http.scaladsl.model
import akka.http.scaladsl.model.headers.CacheDirectives.public
import akka.http.scaladsl.model.headers.LinkParams.`type`
import bml.util.GeneratorFSUtil.makeFile
import bml.util.java.ClassNames.{illegalArgumentException, math, randomUtils, string, stringBuilder, supplier}
import bml.util.java.JavaPojoTestFixtures.defaultSupplierMethodName
import bml.util.{AnotationUtil, NameSpaces, java}
import com.squareup.javapoet
import com.squareup.javapoet.TypeName.INT
import com.squareup.javapoet._
import io.apibuilder.generator.v0.models.File
import io.apibuilder.spec.v0.models.{Field, Model, Service}
import javax.lang.model.element.Modifier
import javax.lang.model.element.Modifier._
import org.checkerframework.checker.units.qual.min
import org.slf4j.{Logger, LoggerFactory}

import collection.JavaConverters._

object JavaPojoTestFixtures extends JavaPojoUtil {

  private val LOG: Logger = LoggerFactory.getLogger(this.getClass)


  def mockFactoryClassName(nameSpace: String, name: String): ClassName = {
    ClassName.get(nameSpace, toClassName(name) + "MockFactory")
  }

  def mockFactoryClassName(nameSpaces: NameSpaces, name: String): ClassName = {
    mockFactoryClassName(nameSpaces.modelFactory.nameSpace, name)
  }

  def mockFactoryClassName(nameSpaces: NameSpaces, model: Model): ClassName = {
    mockFactoryClassName(nameSpaces, model.name)
  }

  def getFunction(model: Model, nameSpaces: NameSpaces): MethodSpec = {
    val targetClassName = ClassName.get(nameSpaces.model.nameSpace, toClassName(model))
    val targetClassBuilderName = toBuilderClassName(targetClassName)
    MethodSpec.methodBuilder(generateMethodName)
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

  val generateMethodName = "get"


  def generateMockFactory(service: Service, nameSpaces: NameSpaces, model: Model): File = {
    val className = mockFactoryClassName(nameSpaces, model)
    val targetClassName = ClassName.get(nameSpaces.model.nameSpace, toClassName(model))
    val targetClassBuilderName = toBuilderClassName(targetClassName)
    val typeBuilder = TypeSpec.classBuilder(className).addModifiers(Modifier.PUBLIC)
      .addAnnotation(ClassNames.builder)
      .addAnnotation(AnotationUtil.fluentAccessor)
      .addSuperinterface(ClassNames.supplier(targetClassName))
      .addField(FieldSpec.builder(TypeName.INT, "MAX_GENERATED_LIST_SIZE", PUBLIC, STATIC).initializer("$L", "50").build())
      .addFields(
        model.fields
          .map(
            (field) => {
              LOG.info("Service={} Model={} Field.name={} Field.type={}", service.name, model.name, field.name, field.`type`)

              val fieldName = toFieldName(field)
              val fieldType = dataTypeFromField(field, nameSpaces.model)
              val supplierType = ClassNames.supplier(fieldType)
              val fiedlSpec = FieldSpec.builder(supplierType, fieldName, PRIVATE).addAnnotation(ClassNames.getter)
              if (field.`type` == "boolean") {
                fiedlSpec
                  .addAnnotation(ClassNames.builderDefault)
                  .initializer("$L()", defaultSupplierMethodName(field))
              }

              if (field.`type` == "uuid") {
                fiedlSpec
                  .addAnnotation(ClassNames.builderDefault)
                  .initializer("$L()", defaultSupplierMethodName(field))
              }
              if (field.`type` == "date-iso8601") {
                fiedlSpec
                  .addAnnotation(ClassNames.builderDefault)
                  .initializer("$L()", defaultSupplierMethodName(field))
              }
              if (field.`type` == "string") {
                fiedlSpec
                  .addAnnotation(ClassNames.builderDefault)
                  .initializer("$L()", defaultSupplierMethodName(field))
              }
              if (field.`type` == "integer") {
                fiedlSpec
                  .addAnnotation(ClassNames.builderDefault)
                  .initializer("$L()", defaultSupplierMethodName(field))
              }
              if (JavaPojoUtil.isEnumType(service, field)) {
                fiedlSpec
                  .addAnnotation(ClassNames.builderDefault)
                  .initializer("$L()", defaultSupplierMethodName(field))
              }
              if (JavaPojoUtil.isParameterArray(field)) {
                fiedlSpec
                  .addAnnotation(ClassNames.builderDefault)
                  .initializer("$L()", defaultSupplierMethodName(field))
              }

              if (JavaPojoUtil.isModelType(service, field)) {
                fiedlSpec
                  .addAnnotation(ClassNames.builderDefault)
                  .initializer(
                    "$T.$L()",
                    mockFactoryClassName(nameSpaces.modelFactory.nameSpace, field.`type`),
                    defaultObjectSupplierMethodName
                  )
              }
              if (JavaPojoUtil.isModelNameWithPackage(field.`type`)) {
                val externalNameSpace = JavaPojoUtil.externalNameSpaceFromType(field.`type`)
                fiedlSpec
                  .addAnnotation(ClassNames.builderDefault)
                  .initializer(
                    "$T.$L()",
                    mockFactoryClassName(externalNameSpace.modelFactory.nameSpace, field.`type`),
                    defaultObjectSupplierMethodName
                  )

              }

              fiedlSpec.build()
            }
          ).asJava
      )
      .addField(
        FieldSpec.builder(ClassName.get("", className.simpleName()), defaultFactoryStaticParamName, Modifier.PUBLIC, Modifier.STATIC)
          .initializer("builder().build()")
          .build()
      )
      .addMethod(
        MethodSpec.methodBuilder(defaultObjectSupplierMethodName)
          .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
          .returns(ClassNames.supplier(targetClassName))
          .addStatement("return ()-> $L.$L()", defaultFactoryStaticParamName, generateMethodName)
          .build()
      )
      .addMethods(
        model.fields.map(generateDefaultSupplier(service, nameSpaces, model, _)).filter(_.isDefined).map(_.get).asJava
      )
      .addMethod(getFunction(model, nameSpaces))
    makeFile(className.simpleName(), nameSpaces.modelFactory, typeBuilder)
  }

  val defaultFactoryStaticParamName = "DEFAULT_FACTORY"
  val defaultObjectSupplierMethodName = "defaultObjectSupplier"

  def defaultSupplierMethodName(field: Field) = toFieldName(field) + "DefaultSupplier"


  def generateDefaultSupplier(service: Service, nameSpaces: NameSpaces, model: Model, field: Field): Option[MethodSpec] = {
    val fieldName = defaultSupplierMethodName(field)
    val fieldType = dataTypeFromField(field, nameSpaces.model)
    val supplierType = ClassNames.supplier(fieldType)
    val testSuppliers = TestSuppliers.className(nameSpaces)


    val booleanTypeReturn = CodeBlock.of("$T.$L()", TestSuppliers.className(nameSpaces), TestSuppliers.methods.booleanSupplier)
    val integerTypeReturn = CodeBlock.of("$T.$L()", TestSuppliers.className(nameSpaces), TestSuppliers.methods.integerSupplier)

    val uuidTypeRequiredReturn = CodeBlock.of("$T.$L()", TestSuppliers.className(nameSpaces), TestSuppliers.methods.uuidSupplier)

    val dateIso8601TypeReturn = CodeBlock.of("$T.$L()", testSuppliers, TestSuppliers.methods.localDateSupplier)

    val stringTypeRequiredReturn = CodeBlock.of(
      "$T.$L($T.ENGLISH,$T.$L,$T.$L)",
      TestSuppliers.className(nameSpaces),
      TestSuppliers.methods.stringRangeSupplier,
      ClassNames.locale,
      ClassNames.toClassName(nameSpaces.model, toClassName(model)),
      JavaPojos.toMinFieldStaticFieldName(field),
      ClassNames.toClassName(nameSpaces.model, toClassName(model)),
      JavaPojos.toMaxFieldStaticFieldName(field)
    )

    def enumTypeReturn(dataTypeOverride: TypeName = fieldType) = {


      CodeBlock
        .builder()
        .add("$L",
          TypeSpec.anonymousClassBuilder("")
            .addSuperinterface(ParameterizedTypeName.get(ClassNames.supplier, dataTypeOverride))
            .addField(
              FieldSpec.builder(ArrayTypeName.of(dataTypeOverride), "values")
                .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                .initializer("$T.values()", dataTypeOverride).build())
            .addField(
              FieldSpec.builder(ClassNames.random, "random")
                .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                .initializer("new $T()", ClassNames.random).build())
            .addMethod(
              MethodSpec.methodBuilder("get").returns(dataTypeOverride).addModifiers(Modifier.PUBLIC)
                .addStatement("return values[random.nextInt(values.length)]")
                .build())
            .build()
        ).build()
    }

    def modelTypeReturn(`type`: String = field.`type`) = CodeBlock.of(
      "$T.$L",
      ClassName.get("", mockFactoryClassName(nameSpaces, `type`).simpleName()),
      defaultFactoryStaticParamName
    )


    def stdWithNullable(requiredCodeBlock: CodeBlock): MethodSpec = {
      val spec = MethodSpec.methodBuilder(fieldName).addModifiers(PUBLIC, STATIC);
      if (field.required) {
        spec
          .returns(supplierType)
          .addStatement(
            CodeBlock.builder().add("return ").add(requiredCodeBlock).build()
          )
      } else {
        spec
          .returns(supplierType)
          .addStatement(CodeBlock.builder().add("return ").add(
            CodeBlock.of(
              "$T.$L($L,$L)",
              TestSuppliers.className(nameSpaces),
              TestSuppliers.methods.wrapProbNull,
              requiredCodeBlock,
              "50")
          ).build())
      }
      spec.build()
    }


    field.`type` match {
      case "boolean" => return Some(stdWithNullable(booleanTypeReturn))
      case "integer" => return Some(stdWithNullable(integerTypeReturn))
      case "uuid" => return Some(stdWithNullable(uuidTypeRequiredReturn))
      case "date-iso8601" => return Some(stdWithNullable(dateIso8601TypeReturn))
      case "string" => return Some(stdWithNullable(stringTypeRequiredReturn))
      case _ =>
    }

    if (JavaPojoUtil.isModelType(service, field)) return Some(stdWithNullable(modelTypeReturn()))
    if (JavaPojoUtil.isEnumType(service, field)) return Some(stdWithNullable(enumTypeReturn()))


    def wrapList(codeBlock: CodeBlock): CodeBlock = {

      CodeBlock.builder()
        .add("$T.$L($L,50)", TestSuppliers.className(nameSpaces), TestSuppliers.methods.listSupplier, codeBlock)
        .build()
    }


    if (JavaPojoUtil.isParameterArray(field)) {
      val memberType = JavaPojoUtil.getArrayType(field)
      memberType match {
        case "boolean" => return Some(stdWithNullable(wrapList(booleanTypeReturn)))
        case "integer" => return Some(stdWithNullable(wrapList(integerTypeReturn)))
        case "uuid" => return Some(stdWithNullable(wrapList(uuidTypeRequiredReturn)))
        case "date-iso8601" => return Some(stdWithNullable(wrapList(dateIso8601TypeReturn)))
        case "string" => return Some(stdWithNullable(wrapList(stringTypeRequiredReturn)))
        case _ =>
      }
      if (JavaPojoUtil.isModelType(service, memberType)) return Some(stdWithNullable(wrapList(modelTypeReturn(memberType))))
      if (JavaPojoUtil.isEnumType(service, memberType)) return Some(
        stdWithNullable(
          wrapList(
            enumTypeReturn(
              dataTypeFromField(memberType, nameSpaces.model.nameSpace)
            )
          )
        )
      )
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

}
