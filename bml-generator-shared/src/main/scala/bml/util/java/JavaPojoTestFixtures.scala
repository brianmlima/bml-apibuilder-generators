package bml.util.java


import akka.http.scaladsl
import akka.http.scaladsl.model
import bml.util.java.ClassNames.{JavaTypes, LombokTypes}
import bml.util.{AnotationUtil, NameSpaces}
import io.apibuilder.generator.v0.models.File
import io.apibuilder.spec.v0.models.{Field, Model, Service}
import org.slf4j.{Logger, LoggerFactory}


object JavaPojoTestFixtures extends JavaPojoUtil {

  import javax.lang.model.element.Modifier._
  import bml.util.GeneratorFSUtil.makeFile
  import collection.JavaConverters._
  import com.squareup.javapoet._
  import bml.util.java.ClassNames.JavaTypes.{Locale, Random, supplier}
  import bml.util.java.ClassNames.LombokTypes.BuilderDefault

  private val log: Logger = LoggerFactory.getLogger(this.getClass)


  def mockFactoryClassName(nameSpace: String, name: String): ClassName = {
    ClassName.get(nameSpace, toClassName(name) + "MockFactory")
  }

  def mockFactoryClassName(nameSpaces: NameSpaces, name: String): ClassName = {
    mockFactoryClassName(nameSpaces.modelFactory.nameSpace, name)
  }

  def mockFactoryClassName(nameSpaces: NameSpaces, model: Model): ClassName = {
    mockFactoryClassName(nameSpaces, model.name)
  }

  def getFunction(service: Service, model: Model, nameSpaces: NameSpaces): MethodSpec = {
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
                  .addStatement("$T $L = $L.get()", JavaPojoUtil.dataTypeFromField(service, field, nameSpaces.model), fieldName + "Object", fieldName)
                  .beginControlFlow("if($L!=null)", fieldName + "Object")
                  .addStatement("builder.$L($L)", fieldName, fieldName + "Object")
                  .endControlFlow()
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
    val typeBuilder = TypeSpec.classBuilder(className).addModifiers(PUBLIC)
      .addAnnotation(LombokTypes.Builder)
      .addAnnotation(AnotationUtil.fluentAccessor)
      .addSuperinterface(JavaTypes.supplier(targetClassName))
      .addField(FieldSpec.builder(TypeName.INT, "MAX_GENERATED_LIST_SIZE", PUBLIC, STATIC).initializer("$L", "50").build())
      .addFields(
        model.fields
          .map(
            (field) => {
              log.info("Service={} Model={} Field.name={} Field.type={}", service.name, model.name, field.name, field.`type`)

              val fieldName = toFieldName(field)
              val fieldType = dataTypeFromField(service, field, nameSpaces.model)
              val supplierType = supplier(fieldType)
              val fiedlSpec = FieldSpec.builder(supplierType, fieldName, PRIVATE).addAnnotation(ClassNames.getter)
              if (field.`type` == "boolean") {
                fiedlSpec
                  .addAnnotation(BuilderDefault)
                  .initializer("$L()", defaultSupplierMethodName(field))
              }

              if (field.`type` == "uuid") {
                fiedlSpec
                  .addAnnotation(BuilderDefault)
                  .initializer("$L()", defaultSupplierMethodName(field))
              } else if (field.`type` == "date-iso8601") {
                fiedlSpec
                  .addAnnotation(BuilderDefault)
                  .initializer("$L()", defaultSupplierMethodName(field))
              } else if (field.`type` == "string") {
                fiedlSpec
                  .addAnnotation(BuilderDefault)
                  .initializer("$L()", defaultSupplierMethodName(field))
              } else if (field.`type` == "integer") {
                fiedlSpec
                  .addAnnotation(BuilderDefault)
                  .initializer("$L()", defaultSupplierMethodName(field))
              } else if (JavaPojoUtil.isEnumType(service, field)) {
                fiedlSpec
                  .addAnnotation(BuilderDefault)
                  .initializer("$L()", defaultSupplierMethodName(field))
              } else if (JavaPojoUtil.isParameterArray(field)) {
                fiedlSpec
                  .addAnnotation(BuilderDefault)
                  .initializer("$L()", defaultSupplierMethodName(field))
              } else if (JavaPojoUtil.isModelType(service, field)) {
                fiedlSpec
                  .addAnnotation(BuilderDefault)
                  .initializer(
                    "$T.$L()",
                    mockFactoryClassName(nameSpaces.modelFactory.nameSpace, field.`type`),
                    defaultObjectSupplierMethodName
                  )
              } else if (JavaPojoUtil.isModelNameWithPackage(field.`type`)) {
                val externalNameSpace = JavaPojoUtil.externalNameSpaceFromType(field.`type`)
                fiedlSpec
                  .addAnnotation(BuilderDefault)
                  .initializer(
                    "$T.$L()",
                    mockFactoryClassName(externalNameSpace.modelFactory.nameSpace, field.`type`),
                    defaultObjectSupplierMethodName
                  )
              } else {
                log.error(
                  s"Unable to create a default builder value for " +
                    s"Model=${model.name} " +
                    s"Field=${field.name} " +
                    s"Field.Type=${field.`type`} " +
                    s"isModelNameWithPackage=${JavaPojoUtil.isModelNameWithPackage(field.`type`)}")
              }

              fiedlSpec.build()
            }
          ).asJava
      )
      .addField(
        FieldSpec.builder(ClassName.get("", className.simpleName()), defaultFactoryStaticParamName, PUBLIC, STATIC)
          .initializer("builder().build()")
          .build()
      )
      .addMethod(
        MethodSpec.methodBuilder(defaultObjectSupplierMethodName)
          .addModifiers(PUBLIC, STATIC)
          .returns(JavaTypes.supplier(targetClassName))
          .addStatement("return ()-> $L.$L()", defaultFactoryStaticParamName, generateMethodName)
          .build()
      )
      .addMethods(
        model.fields.map(generateDefaultSupplier(service, nameSpaces, model, _)).filter(_.isDefined).map(_.get).asJava
      )
      .addMethod(getFunction(service, model, nameSpaces))
    makeFile(className.simpleName(), nameSpaces.modelFactory, typeBuilder)
  }

  val defaultFactoryStaticParamName = "DEFAULT_FACTORY"
  val defaultObjectSupplierMethodName = "defaultObjectSupplier"

  def defaultSupplierMethodName(field: Field) = toFieldName(field) + "DefaultSupplier"


  def generateDefaultSupplier(service: Service, nameSpaces: NameSpaces, model: Model, field: Field): Option[MethodSpec] = {
    val fieldName = defaultSupplierMethodName(field)
    val fieldType = dataTypeFromField(service, field, nameSpaces.model)
    val supplierType = supplier(fieldType)
    val testSuppliers = TestSuppliers.className(nameSpaces)


    val booleanTypeReturn = CodeBlock.of("$T.$L()", TestSuppliers.className(nameSpaces), TestSuppliers.methods.booleanSupplier)
    val integerTypeReturn = CodeBlock.of("$T.$L()", TestSuppliers.className(nameSpaces), TestSuppliers.methods.integerSupplier)

    val uuidTypeRequiredReturn = CodeBlock.of("$T.$L()", TestSuppliers.className(nameSpaces), TestSuppliers.methods.uuidSupplier)

    val dateIso8601TypeReturn = CodeBlock.of("$T.$L()", testSuppliers, TestSuppliers.methods.localDateSupplier)

    val stringTypeRequiredReturn = CodeBlock.of(
      "$T.$L($T.ENGLISH,$T.$L,$T.$L)",
      TestSuppliers.className(nameSpaces),
      TestSuppliers.methods.stringRangeSupplier,
      Locale,
      ClassNames.toClassName(nameSpaces.model, toClassName(model)),
      if (field.`type` == "[string]") JavaPojos.toMinStringValueLengthStaticFieldName(field) else JavaPojos.toMinFieldStaticFieldName(field),
      ClassNames.toClassName(nameSpaces.model, toClassName(model)),
      if (field.`type` == "[string]") JavaPojos.toMaxStringValueLengthStaticFieldName(field) else JavaPojos.toMaxFieldStaticFieldName(field)
    )

    def enumTypeReturn(dataTypeOverride: TypeName = fieldType) = {


      CodeBlock
        .builder()
        .add("$L",
          TypeSpec.anonymousClassBuilder("")
            .addSuperinterface(supplier(dataTypeOverride))
            .addField(
              FieldSpec.builder(ArrayTypeName.of(dataTypeOverride), "values")
                .addModifiers(PRIVATE, FINAL)
                .initializer("$T.values()", dataTypeOverride).build())
            .addField(
              FieldSpec.builder(Random, "random")
                .addModifiers(PRIVATE, FINAL)
                .initializer("new $T()", Random).build())
            .addMethod(
              MethodSpec.methodBuilder("get").returns(dataTypeOverride).addModifiers(PUBLIC)
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
        .add("$T.$L($L,$L)", TestSuppliers.className(nameSpaces), TestSuppliers.methods.listSupplier, codeBlock, field.maximum.getOrElse(50).toString)
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
              dataTypeFromField(service, memberType, nameSpaces.model.nameSpace)
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
