package models.generator.ebscoservice

import bml.util.NameSpaces
import bml.util.java.JavaPojoUtil
import bml.util.spring.{MethodArgumentNotValidExceptionHandler, SpringBootApps, SpringControllers, SpringServices}
import io.apibuilder.generator.v0.models.{File, InvocationForm}
import io.apibuilder.spec.v0.models.{Resource, Service}
import lib.generator.{CodeGenerator, GeneratorUtil}


class EbscoServiceGenerator extends CodeGenerator with JavaPojoUtil {

  def getJavaDocFileHeader() = "WARNING: not all features (notably unions) and data types work with the java generator yet. \nplease contact brianmlima@gmail.com"

  override def invoke(form: InvocationForm): Either[Seq[String], Seq[File]] = invoke(form, addHeader = true)

  def invoke(form: InvocationForm, addHeader: Boolean = false): Either[Seq[String], Seq[File]] = Right(generateCode(form, addHeader))

  private def generateCode(form: InvocationForm, addHeader: Boolean = true): Seq[File] = {
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
    def generateSourceFiles(): Seq[File] = {
      generateServices(service.resources) ++
      generateControllers(service.resources) ++
      MethodArgumentNotValidExceptionHandler.get(nameSpaces) ++
      SpringBootApps.foo(nameSpaces, service)
    }

    //Generates Services from Resources
    def generateControllers(resources: Seq[Resource]): Seq[File] = {
      resources.flatMap(SpringControllers.generateController(nameSpaces, _))
      //      Seq[File]()
    }

    //Generates Services from Resources
    def generateServices(resources: Seq[Resource]): Seq[File] = {
      resources.flatMap(SpringServices.generateService(nameSpaces, _))
      //      Seq[File]()
    }

  }

}