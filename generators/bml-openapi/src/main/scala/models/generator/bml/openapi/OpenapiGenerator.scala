package models.generator.bml.openapi

import java.lang.Object
import java.util

import akka.http.scaladsl
import akka.http.scaladsl.model
import akka.http.scaladsl.model.headers.LinkParams.`type`
import akka.util.HashCode
import bml.util.java.JavaPojoUtil
import bml.util.java.client.JavaClients
import bml.util.openapi.Info.License
import bml.util.openapi.{Components, Info, Items, OpenApi, Property, Ref, Schema, Type}
import bml.util.{NameSpaces, SpecValidation}
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.{BeanProperty, ObjectMapper, SerializationFeature}
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.google.googlejavaformat.java.Formatter
import com.squareup.javapoet.ClassName
import io.apibuilder.generator.v0.models.{File, InvocationForm}
import io.apibuilder.spec.v0.models.{Field, Operation, Response, ResponseCode, ResponseCodeInt, Service}
import lib.generator.CodeGenerator
import lombok.{Data, Getter}
import lombok.experimental.Accessors
import org.springframework.beans.factory.config.YamlMapFactoryBean
import play.api.Logger

import scala.beans
import scala.collection.mutable
import lib.Text._


class OpenapiGenerator extends CodeGenerator {

  import collection.JavaConverters._

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
      Right(generateClient())
    }

    //
    //    def toLocalComponentRef(field:Field): Unit ={
    //
    //    }


    def toRef(service: Service, typeIn: String): String = {
      if (JavaPojoUtil.isModelNameWithPackage(typeIn)) {
        val modelName = typeIn
        val fileName = typeIn.split("\\.").drop(2).dropRight(3).map(_.capitalize).mkString("") + ".yaml"
        return s"${fileName}#/components/schemas/${modelName}"
      }
      s"#/components/schemas/${nameSpaces.model.nameSpace}.${typeIn}"
    }


    def generateClient(): Seq[File] = {

      val name = JavaPojoUtil.toClassName(service.name);






      val schemas = service.models.map(
        modelIn => {
          val schemaOut = Schema.builder().description(modelIn.description.getOrElse(null))
          modelIn.fields.foreach(
            fieldIn => {
              val propertyOut = Property.builder()
              if (fieldIn.description.isDefined) propertyOut.description(fieldIn.description.get)
              if (JavaPojoUtil.isModelNameWithPackage(fieldIn.`type`)) {
                propertyOut.oneOf(Ref.builder().ref(fieldIn.`type`).build())
              } else if (JavaPojoUtil.isModelType(service, fieldIn)) {
                propertyOut.oneOf(Ref.builder().ref(toRef(service, fieldIn.`type`)).build())
              } else if (fieldIn.`type`.startsWith("[") && fieldIn.`type`.endsWith("]")) {
                propertyOut.`type`("array")
                val itemType = fieldIn.`type`.toCharArray.drop(1).dropRight(1).mkString("")
                propertyOut.items(Items.builder().ref(toRef(service, itemType)).build())
              } else {
                propertyOut.`type`(Type.`object`.name())
                if (fieldIn.`type` == "uuid") {
                  propertyOut.format("uuid")
                } else if (fieldIn.`type` == "date-iso8601")
                  propertyOut.`type`("string")
                    .format("date-time")
              }
              schemaOut.property(fieldIn.name, propertyOut.build())
            }
          )
          nameSpaces.model.nameSpace + "." + modelIn.name -> schemaOut.build()
        }
      ).toMap ++ service.enums.map(
        enumIn => {
          val schemaOut = Schema.builder().description(enumIn.description.getOrElse(null))
          schemaOut.`type`(Type.string)
          schemaOut.enums(
            enumIn.values.map(_.name).asJava
          )
          nameSpaces.model.nameSpace + "." + enumIn.name -> schemaOut.build()
        }
      )

      val components = Components.builder().schemas(schemas.asJava).build()

      def toOperationId(operation: Operation): String = {
        s"${operation.method.toString.toLowerCase()}-${operation.path.replace("/", "-")}"
      }

      val paths = new mutable.LinkedHashMap[String, Object].asJava
      service.resources.foreach(
        resourceIn => {
          resourceIn.operations.foreach(
            operationIn => {
              val DESCRIPTION = "description"
              val pathOut = new mutable.LinkedHashMap[String, Object].asJava
              val operationOut = new mutable.LinkedHashMap[String, Object].asJava
              pathOut.put(operationIn.method.toString.toLowerCase(), operationOut)
              operationOut.put(DESCRIPTION, operationIn.description.getOrElse(null))
              operationOut.put("operationId", toOperationId(operationIn))
              val parameters = new util.LinkedList[Map[String, Object]]()
              operationOut.put("parameters", parameters)
              val responsesOut = new mutable.LinkedHashMap[String, Object].asJava
              operationOut.put("responses", responsesOut)

              //Do response codes
              operationIn.responses.foreach(
                responseIn => {
                  val responseOut = mutable.LinkedHashMap[String, Object]().asJava
                  if (responseIn.description.isDefined) {
                    responseOut.put(DESCRIPTION, responseIn.description.get)
                  }
                  if (responseIn.`type` == "unit") {

                  } else if (JavaPojoUtil.isModelNameWithPackage(responseIn.`type`) || JavaPojoUtil.isModelType(service, responseIn.`type`)) {
                    val contentOut = new mutable.LinkedHashMap[String, Object].asJava
                    val contentTypeOut = new mutable.LinkedHashMap[String, Object].asJava
                    contentOut.put("application/json", contentTypeOut)
                    val schemaOut = new mutable.LinkedHashMap[String, Object].asJava
                    schemaOut.put("$ref", toRef(service, responseIn.`type`))
                    contentTypeOut.put("schema", schemaOut)
                    responseOut.put("content", contentOut)
                  }
                  //                  else if(JavaPojoUtil.isModelType(service,responseIn.`type`)){
                  //
                  //
                  //                    val contentTypeOut = new mutable.LinkedHashMap[String,Object].asJava
                  //                    val schemaOut = new mutable.LinkedHashMap[String,Object].asJava
                  //                    schemaOut.put("$ref",toRef(service,responseIn.`type`))
                  //                    contentTypeOut.put("schema",schemaOut)
                  //                    responseOut.put("content",contentTypeOut)
                  //                  }
                  responsesOut.put(responseIn.code.asInstanceOf[ResponseCodeInt].value.toString, responseOut)
                }
              )

              paths.put(operationIn.path, pathOut)
            }
          )
        }
      )

      val openapi = OpenApi.builder().components(components)
        .paths(paths)
      openapi.info(
        Info.builder()
          .contact(Info.Contact.builder().build())
          .license(License.builder().name("").build())
          .termsOfService("foo.com")
          //.server(Servers.builder().url("foo.com").build())
          .title(service.name)
          .version(service.version)
          .build()
      )
      val mapper = new ObjectMapper(new YAMLFactory()).disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)


      Seq(
        File(name = s"${name}.yaml", contents = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(openapi.build()))
      )
    }
  }


}
