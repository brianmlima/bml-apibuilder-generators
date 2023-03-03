package models.generator.java.persistence.sql

import bml.util.java.{JavaCommonClasses, JavaPojoUtil}
import bml.util.spring.SpringVersion
import com.google.googlejavaformat.java.Formatter
import com.squareup.javapoet.{JavaFile, TypeSpec, _}
import io.apibuilder.generator.v0.models.{File, InvocationForm}
import io.apibuilder.spec.v0.models.{Enum, Resource, Service, Union}
import javax.lang.model.element.Modifier.PUBLIC
import lib.generator.CodeGenerator
import models.generator.java.persistence.sql.generators._

trait JavaPersistanceSqlCodeGenerator extends CodeGenerator with JavaPojoUtil {
  def getJavaDocFileHeader(): String //abstract

  val springVersion = SpringVersion.FIVE

  override def invoke(form: InvocationForm): Either[Seq[String], Seq[File]] = invoke(form, addHeader = true)

  def invoke(form: InvocationForm, addHeader: Boolean = false): Either[Seq[String], Seq[File]] = generateCode(form, addHeader)

  private def generateCode(form: InvocationForm, addHeader: Boolean = true): Either[Seq[String], Seq[File]] = {
    val header =
      if (addHeader) Some(new ApidocComments(form.service.version, form.userAgent).forClassFile)
      else None
    new Generator(form.service, header).generateSourceFiles()
  }

  class Generator(service: Service, header: Option[String]) {

    private val nameSpace = makeNameSpace(service.namespace)
    private val modelsNameSpace = nameSpace + ".models"
    private val jpaNameSpace = nameSpace + ".jpa"
    private val resourceNameSpace = nameSpace + ".controllers"

    private val modelsDirectoryPath = createDirectoryPath(modelsNameSpace)
    private val jpaDirectoryPath = createDirectoryPath(jpaNameSpace)
    private val resourceDirectoryPath = createDirectoryPath(resourceNameSpace)

    private val apiDocComments = {
      val s = getJavaDocFileHeader + "\n"
      header.fold(s)(_ + "\n" + s)
    }

    //This generation jobs config.
    //Use named params so this does not get mixed up.
    val config = GenConfig(
      service = service,
      nameSpace = nameSpace,
      modelsNameSpace = modelsNameSpace,
      jpaNameSpace = jpaNameSpace,
      modelsDirectoryPath = modelsDirectoryPath,
      jpaDirectoryPath = jpaDirectoryPath,
      resourceNameSpace = resourceNameSpace,
      resourceDirectoryPath = resourceDirectoryPath,
      apiDocComments = apiDocComments
    )

    def generateSourceFiles(): Either[Seq[String], Seq[File]] = {
      val errors = SpecValidation.validate(service: Service, header: Option[String])

      if (errors.isDefined) {
        return Left(errors.get)
      }


      //Build enum classes
      val generatedEnums: Seq[File] = service.enums.map {
        generateEnum
      }

      //Build Union Types
      val generatedUnionTypes: Seq[File] = service.unions.map {
        generateUnionType
      }

      val generatedResources: Seq[File] = generateResources(service.resources).filter(_.isDefined).map(_.get)

      val generatedModels: Seq[File] = service.models.map { model =>
        val relatedUnions = service.unions.filter(_.types.exists(_.`type` == model.name))
        val modelData = ModelData(model, relatedUnions, config)
        generateModel(modelData)
      }.flatten.filter(_.isDefined).map(_.get)

      //val generatedResolvers: Seq[File] = generateResources(service.resources).filter(_.isDefined).map(_.get)
      Right(
        Seq(makeFile(config.baseEntitySimpleName, config.jpaDirectoryPath, config.jpaNameSpace, JavaCommonClasses.baseEntity(springVersion,config.jpaNameSpace))) ++
          Seq(makeFile(config.baseRepoSimpleName, config.jpaDirectoryPath, config.jpaNameSpace, JavaCommonClasses.baseRepository(springVersion, config.jpaNameSpace))) ++
          generatedEnums ++
          generatedUnionTypes ++
          generatedModels ++
          //generatedResolvers ++
          generatedResources
      )
    }

    def generateResources(resources: Seq[Resource]): Seq[Option[File]] = {
      val resourceDatas = resources.map(ResourceData(_, config))
      val controlerFiles = resourceDatas.map(ControllerGenerator.generate(service, _)).map(_.makeControllerFile())
      val serviceFiles = resourceDatas.map(ControllerServiceGenerator.generate(service, _)).map(_.makeServiceFile())

      controlerFiles ++ serviceFiles
    }

    def generateEnum(enum: Enum): File = {
      val className = toClassName(enum.name)
      val builder =
        TypeSpec.enumBuilder(className)
          .addModifiers(PUBLIC)
          .addJavadoc(config.apiDocComments)
      enum.attributes.foreach(attribute => {
        attribute.name match {
          case _ => {}
        }
      })
      enum.description.map(builder.addJavadoc(_))
      enum.values.foreach(value => {
        builder.addEnumConstant(toEnumName(value.name))
      })
      //val nameFieldType = classOf[String]
      val constructorWithParams = MethodSpec.constructorBuilder()
      builder.addMethod(constructorWithParams.build())
      makeFile(className, config.modelsDirectoryPath, config.modelsNameSpace, builder)
    }

    def generateUnionType(union: Union): File = {
      val className = toClassName(union.name)
      val builder = TypeSpec.interfaceBuilder(className)
        .addModifiers(PUBLIC)
        .addJavadoc(config.apiDocComments)
      union.description.map(builder.addJavadoc(_))
      makeFile(className, config.modelsDirectoryPath, config.modelsNameSpace, builder)
    }

    /**
     * Delagated model generation.
     *
     */
    def generateModel(modelData: ModelData): Seq[Option[File]] = {
      if (modelData.isPersistedModel())
        Seq(
          PersistanceClassGenerator.generatePersistenceModel(service, modelData).makeClassFile(),
          RepositoryGenerator.generate(service, modelData).makeRepoFile()
        )
      else
        Seq(
          ClassGenerator.generateNonPersistenceModel(service, modelData).makeClassFile()
        )
    }


    def makeFile(name: String, filePath: String, nameSpace: String, builder: TypeSpec.Builder): File = {
      File(s"${name}.java", Some(filePath), new Formatter().formatSource(JavaFile.builder(nameSpace, builder.build).build.toString))
    }

  }

}
