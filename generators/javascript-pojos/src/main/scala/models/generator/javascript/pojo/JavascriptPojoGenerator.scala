package models.generator.javascript.pojo

import bml.util.{NameSpaces, SpecValidation}
import bml.util.java.JavaPojoUtil
import io.apibuilder.generator.v0.models.{File, InvocationForm}
import io.apibuilder.spec.v0.models.Service
import lib.generator.{CodeGenerator, GeneratorUtil}

import play.api.Logger


class JavascriptPojoGenerator extends CodeGenerator with JavaPojoUtil {

  /**
   * Standard logger
   */
  val logger: Logger = Logger.apply(this.getClass())

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // BEGIN Common generator invocation
  override def invoke(form: InvocationForm): Either[Seq[String], Seq[File]] = invoke(form, addHeader = true)

  def invoke(form: InvocationForm, addHeader: Boolean = false): Either[Seq[String], Seq[File]] = generateCode(form, addHeader)

  private def generateCode(form: InvocationForm, addHeader: Boolean = true): Either[Seq[String], Seq[File]] = {
    val header =
      if (addHeader) Some(new ApidocComments(form.service.version, form.userAgent).forClassFile)
      else None
    logger.info(s"Processing Application ${form.service.name}")
    // ENSURE Spec before even creating the generator
    val errors = SpecValidation.validate(form.service, header)
    if (errors.isDefined) {
      return Left(errors.get)
    }
    //Spec passed
    Right(new Generator(form.service, header).generateSourceFiles())
  }

  class Generator(service: Service, header: Option[String]) {
    private val nameSpaces = new NameSpaces(service)

    //Run Generation
    def generateSourceFiles(): Seq[File] = {
      generateEnums()
    }

    def generateEnums(): Seq[File] = {


      val qt = '"';

      service.enums.map(
        enumIn => {

          val className = JavaPojoUtil.toClassName(enumIn)

          val buff = new StringBuilder();

          buff.append(s"const ${className} = {\n")


          buff.append(
            enumIn.values.map(
              enumValue => {
                val enumName = JavaPojoUtil.toEnumName(enumValue.name)
                val innerBuff = new StringBuilder();
                innerBuff.append(s"\t${enumName}: {\n")
                innerBuff.append(s"\t\tapiValue : ${qt}${enumValue.name}${qt},\n")
                innerBuff.append(s"\t\tdescription : ${qt}${enumValue.description.getOrElse("")}${qt},\n")
                innerBuff.append("\t}\n")
                innerBuff.mkString
              }
            ).mkString(",")
          )
          buff.append(",")

          buff.append("\tfromApiValue(apiValue){\n")
          buff.append("\t\tswitch(String(apiValue)){\n")

          buff.append(
            enumIn.values.map(
              enumValue => {
                val enumName = JavaPojoUtil.toEnumName(enumValue.name)
                val innerBuff = new StringBuilder();
                innerBuff.append(s"\t\t\tcase ${qt}${enumValue.name}${qt}:\n")
                innerBuff.append(s"\t\t\t\treturn ${className}.${enumName};\n")
                innerBuff.mkString
              }
            ).mkString("")
          )
          buff.append("\t\t\tdefault: return undefined;")


          buff.append("\t\t}\n")


          buff.append("\t}\n")


          buff.append("}\n")
          buff.append(s"export default ${className}\n")
          File(name = s"${className}.js", contents = buff.mkString)
        }
      )


    }


  }

}