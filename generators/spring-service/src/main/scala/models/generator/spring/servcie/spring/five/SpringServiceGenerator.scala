package models.generator.spring.servcie.spring.five

import bml.util.java.JavaPojoUtil
import bml.util.spring.SpringVersion.SpringVersion
import bml.util.spring._
import bml.util.{NameSpaces, SpecValidation}
import io.apibuilder.generator.v0.models.{File, InvocationForm}
import io.apibuilder.spec.v0.models.Service
import lib.generator.{CodeGenerator, GeneratorUtil}
import play.api.Logger

object SpringService extends SpringServiceGenerator {

}

class SpringServiceGenerator extends CodeGenerator with JavaPojoUtil {

  val springVersion: SpringVersion = SpringVersion.FIVE

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
    //Resolves data types for built in types and models
    private val datatypeResolver = GeneratorUtil.datatypeResolver(service)

    //Run Generation
    def generateSourceFiles(): Either[Seq[String], Seq[File]] = {


      logger.info(s"Processing Application ${service.name}")
      val errors = SpecValidation.validate(service: Service, header: Option[String])
      if (errors.isDefined) {
        return Left(errors.get)
      }
      Right(
        generateServices(service) ++
          generateControllers(service) ++
          ApiImplementationException.getFile(nameSpaces) ++
          MethodArgumentNotValidExceptionHandler.get(service, nameSpaces) ++
          //          SwaggerUiConfig.generate(nameSpaces, service) ++
          //        SpringBootApps.foo(nameSpaces, service) ++
          StringToEnumConverters.enumConverters(service, nameSpaces, service.enums) ++
          generateBaseConfigration(service) ++
          generateServiceOperationResponseContainer(service)
      )
    }

    //Generates Services from Resources
    def generateControllers(service: Service): Seq[File] = {
      service.resources.flatMap(SpringControllers.generateController(springVersion, service, nameSpaces, _))
    }

    //Generates Services from Resources
    def generateServices(service: Service): Seq[File] = {
      service.resources.flatMap(SpringServices.generateService(springVersion, service, nameSpaces, _))
    }

    def generateBaseConfigration(service: Service): Seq[File] = {
      SpringServices.generateBaseConfiguration(nameSpaces, service)
    }

    def generateServiceOperationResponseContainer(service: Service): Seq[File] = {
      SpringServices.generateServiceOperationResponseContainer(service, nameSpaces)
    }


  }

}