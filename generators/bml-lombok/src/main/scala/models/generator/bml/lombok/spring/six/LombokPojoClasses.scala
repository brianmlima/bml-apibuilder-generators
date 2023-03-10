package models.generator.bml.lombok.spring.six

import bml.generator.pojo.PojoGenerator
import bml.util.spring.SpringVersion
import io.apibuilder.generator.v0.models.{File, InvocationForm}
import lib.generator.CodeGenerator
import play.api.Logger


object LombokPojoClasses
  extends CodeGenerator {
  def getJavaDocFileHeader() = "WARNING: not all features (notably unions) and data types work with the java generator yet. \nplease contact brianmlima@gmail.com"

  val springVersion = SpringVersion.SIX;
  val logger: Logger = Logger.apply(this.getClass())

  override def invoke(form: InvocationForm): Either[Seq[String], Seq[File]] = invoke(form, addHeader = true)

  def invoke(form: InvocationForm, addHeader: Boolean = false): Either[Seq[String], Seq[File]] = generateCode(form, addHeader)

  private def generateCode(form: InvocationForm, addHeader: Boolean = true): Either[Seq[String], Seq[File]] = {
    val header =
      if (addHeader) Some(new ApidocComments(form.service.version, form.userAgent).forClassFile)
      else None
    val apiDocComments = {
      val s = getJavaDocFileHeader + "\n"
      header.fold(s)(_ + "\n" + s)
    }
    new PojoGenerator(springVersion, apiDocComments, form.service, header).generateSourceFiles()
  }

}
