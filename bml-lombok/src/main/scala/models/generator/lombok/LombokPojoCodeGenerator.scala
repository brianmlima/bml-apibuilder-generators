package models.generator.lombok

import bml.generator.pojo.PojoGenerator
import bml.util.AnotationUtil.{JacksonAnno, LombokAnno}
import bml.util.GeneratorFSUtil.makeFile
import bml.util.attribute.{Converters, Hibernate, JsonName, Singular, SnakeCase}
import bml.util.java.ClassNames._
import bml.util.java.{JavaEnums, JavaPojoUtil, JavaPojos}
import bml.util.jpa.JPA
import bml.util.persist.SpringVariableTypes.{PersistenceAnnotations, PersistenceTypes, ValidationAnnotations, ValidationTypes}
import bml.util.persist.UUIDIfNullGenerator
import bml.util.spring.SpringVersion
import bml.util.spring.SpringVersion.SpringVersion
import bml.util.{FieldUtil, NameSpaces, SpecValidation}
import com.squareup.javapoet.{ClassName, TypeSpec, _}
import io.apibuilder.generator.v0.models.{File, InvocationForm}
import io.apibuilder.spec.v0.models.{Enum, Field, Model, Service, Union}
import javax.lang.model.element.Modifier._
import javax.persistence.{EnumType, Enumerated}
import lib.generator.CodeGenerator
import play.api.Logger


/**
 * Generator for Lombok based pojos for models.
 */
trait LombokPojoCodeGenerator extends CodeGenerator {

  val springVersion = SpringVersion.FIVE;

  val logger: Logger = Logger.apply(this.getClass())

  def getJavaDocFileHeader(): String //abstract

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
