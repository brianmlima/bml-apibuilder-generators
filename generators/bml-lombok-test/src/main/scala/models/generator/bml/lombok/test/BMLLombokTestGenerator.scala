package models.generator.bml.lombok.test

import bml.util.NameSpaces
import bml.util.java.testing.ExercisePojos
import bml.util.java.{JavaPojoTestFixtures, JavaPojoUtil, LoremTooling, ProbabilityTools, TestSuppliers}
import io.apibuilder.generator.v0.models.{File, InvocationForm}
import io.apibuilder.spec.v0.models.Service
import lib.generator.{CodeGenerator, GeneratorUtil}
import org.slf4j.LoggerFactory


class BMLLombokTestGenerator extends CodeGenerator with JavaPojoUtil {
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
    val log = LoggerFactory.getLogger(classOf[Generator])
    private val nameSpaces = new NameSpaces(service)
    //Resolves data types for built in types and models
    private val datatypeResolver = GeneratorUtil.datatypeResolver(service)

    //Run Generation
    def generateSourceFiles(): Seq[File] = {
      generateFixtureBuilders() ++
        Seq[File](
          LoremTooling.generateLoremTool(nameSpaces),
          TestSuppliers.testSuppliers(nameSpaces),
          JavaPojoTestFixtures.makeLanguages(nameSpaces: NameSpaces),
          ProbabilityTools.probabilityTool(nameSpaces),
          ExercisePojos.excersisePojoTestClass(service, nameSpaces)
        )
    }


    //Generates Services from Resources
    def generateFixtureBuilders(): Seq[File] = {
      //      Seq[File]()
      service.models.map(JavaPojoTestFixtures.generateMockFactory(service, nameSpaces, _))
    }


  }

}
