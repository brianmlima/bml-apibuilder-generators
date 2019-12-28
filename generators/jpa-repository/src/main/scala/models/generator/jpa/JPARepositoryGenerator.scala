package models.generator.jpa

import bml.util.AnotationUtil.JavaxAnnotations.JavaxValidationAnnotations
import bml.util.attribute.Hibernate
import bml.util.java.ClassNames.SpringTypes.SpringDataTypes
import bml.util.java.ClassNames.{JavaTypes, SpringTypes}
import bml.util.java.{JavaCommonClasses, JavaPojoUtil, JavaPojos}
import bml.util.jpa.JPA
import bml.util.{GeneratorFSUtil, NameSpaces, SpecValidation}
import com.squareup.javapoet._
import io.apibuilder.generator.v0.models.{File, InvocationForm}
import io.apibuilder.spec.v0.models.Service
import javax.lang.model.element.Modifier
import lib.generator.CodeGenerator
import play.api.Logger

class JPARepositoryGenerator extends CodeGenerator {
  val logger: Logger = Logger.apply(this.getClass())


  def getJavaDocFileHeader() = "WARNING: not all features (notably unions) and data types work with the java generator yet. \nplease contact brianmlima@gmail.com"

  override def invoke(form: InvocationForm): Either[Seq[String], Seq[File]] = invoke(form, addHeader = true)

  def invoke(form: InvocationForm, addHeader: Boolean = false): Either[Seq[String], Seq[File]] = generateCode(form, addHeader)

  private def generateCode(form: InvocationForm, addHeader: Boolean = true): Either[Seq[String], Seq[File]] = {
    val header =
      if (addHeader) Some(new ApidocComments(form.service.version, form.userAgent).forClassFile)
      else None
    new Generator(form.service, header).generateSourceFiles()
  }

  class Generator(service: Service, header: Option[String]) {
    private val nameSpaces = new NameSpaces(service)

    def generateSourceFiles(): Either[Seq[String], Seq[File]] = {
      logger.info(s"Processing Application ${service.name}")
      val errors = SpecValidation.validate(service: Service, header: Option[String])
      if (errors.isDefined) {
        return Left(errors.get)
      }
      val baseRepoSimpleName = "BaseRepository"
      val baseRepoClassName = ClassName.get(nameSpaces.jpa.nameSpace, baseRepoSimpleName)
      val baseEntitySimpleName = "BaseEntity"
      val baseEntityClassName = ClassName.get(nameSpaces.jpa.nameSpace, baseEntitySimpleName)


      Right(
        generateJPARepositories() ++
          Seq(
            GeneratorFSUtil.makeFile(baseEntityClassName.simpleName(), nameSpaces.jpa, JavaCommonClasses.baseEntity(nameSpaces.jpa.nameSpace)),
            GeneratorFSUtil.makeFile(baseRepoClassName.simpleName(), nameSpaces.jpa, JavaCommonClasses.baseRepository(nameSpaces.jpa.nameSpace)))
      )
    }

    def generateJPARepositories(): Seq[File] = {

      service.models.filter(Hibernate.fromModel(_).use).map(
        model => {
          val className = JPA.toRepositoryClassName(nameSpaces.jpa, model)
          val entityClassName = JavaPojoUtil.toClassName(nameSpaces.model, model)

          val idField = model.fields.filter(_.name == "id").last
          val idType = JavaPojoUtil.dataTypeFromField(idField, nameSpaces.model)


          def saveMethod(): MethodSpec = {
            MethodSpec.methodBuilder("save").returns(entityClassName).addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
              .addJavadoc(
                Seq[String](
                  s"Saves a given ${entityClassName.simpleName()}. Use the returned instance for further operations as the save operation might have changed the",
                  s"${entityClassName.simpleName()} instance completely.",
                  "",
                  s"@param entity an ${entityClassName.simpleName()} to be saved. Must not be {@literal null}.",
                  s"@return the saved ${entityClassName.simpleName()}. will never be {@literal null}."
                ).mkString("\n")
              )
              .addParameter(
                ParameterSpec.builder(entityClassName, "entity")
                  .addAnnotation(JavaxValidationAnnotations.NotNull)
                  .build()
              )
              .build()
          }

          def saveAll(): MethodSpec = {
            MethodSpec.methodBuilder("saveAll").returns(JavaTypes.List(entityClassName)).addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
              .addJavadoc(
                Seq[String](
                  "Saves all given entities.",
                  "",
                  "@param entities must not be {@literal null}.",
                  "@return the saved entities will never be {@literal null}.",
                  "@throws IllegalArgumentException in case the given model is {@literal null} or an empty Itterable.",
                ).mkString("\n")
              )
              .addParameter(
                ParameterSpec.builder(JavaTypes.Iterable(entityClassName), "entities")
                  .addAnnotation(JavaxValidationAnnotations.NotNull)
                  .addAnnotation(JavaxValidationAnnotations.NotEmpty)
                  .build()
              )
              .build()
          }


          def findById(): MethodSpec = {
            val javaDoc = Seq[String](
              "Retrieves an model by its id.",
              "",
              Seq[String](
                "@param id must not be {@literal null}",
                if (idField.`type` == "string") s" and must be between ${idField.minimum.get} and ${idField.maximum.get} characters" else "",
                "."
              ).mkString,
              "@return the model with the given id or {@literal Optional#empty()} if none found",
              "@throws IllegalArgumentException if {@code id} is {@literal null}."
            ).mkString("\n")

            val parameterSpec = ParameterSpec.builder(idType, "id")
              .addAnnotation(JavaxValidationAnnotations.NotNull)
            if (idField.`type` == "string") {
              val option = JavaPojos.handleSizeAttribute(entityClassName, idField)
              if (option.isDefined)
                parameterSpec.addAnnotation(option.get)
            }


            MethodSpec.methodBuilder("findById").returns(JavaTypes.Optional(entityClassName)).addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
              .addJavadoc(javaDoc)
              .addParameter(parameterSpec.build())
              .build()
          }


          val repoSpec = TypeSpec.interfaceBuilder(className).addModifiers(Modifier.PUBLIC)
            .addSuperinterface(
              ParameterizedTypeName.get(
                SpringTypes.Repository,
                entityClassName,
                JavaPojoUtil.dataTypeFromField(idField, nameSpaces.model)
              )
            )
            .addAnnotation(JavaxValidationAnnotations.Validated)
            .addAnnotation(SpringDataTypes.Repository)
            .addMethod(saveMethod())
            .addMethod(saveAll())
            .addMethod(findById())

          GeneratorFSUtil.makeFile(className.simpleName(), nameSpaces.jpa, repoSpec)
        }

      )

    }

  }

}
