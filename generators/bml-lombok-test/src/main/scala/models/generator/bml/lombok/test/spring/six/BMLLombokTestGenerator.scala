package models.generator.bml.lombok.test.spring.six

import bml.util.{GeneratorFormattingException, NameSpaces}
import bml.util.java._
import bml.util.java.testing.{ExcersicePojoSpringValidation, ExcersizeEnums, ExercisePojos}
import bml.util.spring.SpringVersion
import bml.util.spring.SpringVersion.SpringVersion
import com.google.googlejavaformat.java.{Formatter, FormatterException}
import io.apibuilder.generator.v0.models.{File, InvocationForm}
import io.apibuilder.spec.v0.models.Service
import lib.generator.{CodeGenerator, GeneratorUtil}
import play.api.Logger


object BMLLombokTests extends BMLLombokTestGenerator {

}

class BMLLombokTestGenerator extends CodeGenerator with JavaPojoUtil {
  val logger: Logger = Logger.apply(this.getClass())

  def getJavaDocFileHeader() = "WARNING: not all features (notably unions) and data types work with the java generator yet. \nplease contact brianmlima@gmail.com"

  val springVersion = SpringVersion.SIX;

  override def invoke(form: InvocationForm): Either[Seq[String], Seq[File]] = invoke(form, addHeader = true)

  def invoke(form: InvocationForm, addHeader: Boolean = false): Either[Seq[String], Seq[File]] = {


    try {
      return Right(generateCode(form, addHeader))
    } catch {
      case e: GeneratorFormattingException => return Left(handleFormatterException(e));
      //      case def
    }
  }

  private def handleFormatterException(e: GeneratorFormattingException): Seq[String] = {
    Seq[String](
      "#######################################################################",
      "The generator generated code and there was an exception while formatting and checking for syntax."
      ,
      "This is almost always an issue with the generator. Please copy the rest of the message and feel free to submit a bug report " +
        "along with the section of your api.json that is associated with the following code."
      ,
      "#######################################################################",
      e.e.getMessage
      ,
      "##################",
      e.javaFileBuilder.build().toString,
      "#######################################################################"
    )
  }


  private def generateCode(form: InvocationForm, addHeader: Boolean = true): Seq[File] = {
    val header =
      if (addHeader) Some(new ApidocComments(form.service.version, form.userAgent).forClassFile)
      else None
    new Generator(springVersion, form.service, header).generateSourceFiles()
  }


  class Generator(springVersion: SpringVersion, service: Service, header: Option[String]) {
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
          ExercisePojos.excersisePojoTestClass(service, nameSpaces),
          ExcersicePojoSpringValidation.excersisePojoTestClass(springVersion, service, nameSpaces),
          ExcersizeEnums.excersiseEnumTestClass(service, nameSpaces)
        )
    }

    //Generates Services from Resources
    def generateFixtureBuilders(): Seq[File] = {
      //      Seq[File]()
      service.models.map(JavaPojoTestFixtures.generateMockFactory(service, nameSpaces, _))
    }

  }

}

