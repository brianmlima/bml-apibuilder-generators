package models.generator.bml.java.client

import bml.util.java.client.JavaClients
import bml.util.spring.SpringVersion
import bml.util.{NameSpaces, SpecValidation}
import io.apibuilder.generator.v0.models.{File, InvocationForm}
import io.apibuilder.spec.v0.models.Service
import lib.generator.CodeGenerator
import play.api.Logger

object JavaClient extends JavaClientGenerator {

}

class JavaClientGenerator extends CodeGenerator {
  val logger: Logger = Logger.apply(this.getClass())

  val springVersion = SpringVersion.SIX;

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
      Right(generateClient())
    }

    def generateClient(): Seq[File] = {
      JavaClients.generateClient(service, nameSpaces)
    }
  }


}
