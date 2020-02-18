package models.generator.bml.openapi

import java.lang.Object

import akka.http.scaladsl
import akka.http.scaladsl.model
import akka.http.scaladsl.model.headers.LinkParams.`type`
import akka.util.HashCode
import bml.util.java.JavaPojoUtil
import bml.util.java.client.JavaClients
import bml.util.openapi.Info.License
import bml.util.openapi.{Components, Info, OpenApi, Property, Ref, Schema, Type}
import bml.util.{NameSpaces, SpecValidation}
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.{BeanProperty, ObjectMapper, SerializationFeature}
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.google.googlejavaformat.java.Formatter
import io.apibuilder.generator.v0.models.{File, InvocationForm}
import io.apibuilder.spec.v0.models.{Field, Service}
import lib.generator.CodeGenerator
import lombok.{Data, Getter}
import lombok.experimental.Accessors
import org.springframework.beans.factory.config.YamlMapFactoryBean
import play.api.Logger

import scala.beans


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


    def toLocalComponentRef(field:Field): Unit ={

    }



    def toRef(service: Service,field: Field):String ={

      if(JavaPojoUtil.isModelNameWithPackage(field.`type`)){
        return s"FILENAME#/components/schemas/${field.`type`}"
      }
      s"#/components/schemas/${field.`type`}"
    }


    def generateClient(): Seq[File] = {

      val name = JavaPojoUtil.toClassName(service.name);

      val schemas = service.models.map( model => {

        val schema = Schema.builder().description(model.description.getOrElse(null))

        model.fields.foreach( field => {
          val property = Property.builder()
          if (JavaPojoUtil.isModelNameWithPackage(field.`type`)) {
//            logger.info(s"${model.name}.${field.name} isModelNameWithPackage = true")
            if(field.description.isDefined){
              property.description(field.description.get)
            }
            property.oneOf(Ref.builder().ref(field.`type`).build())
          }else if (JavaPojoUtil.isModelType(service, field)) {
//            logger.info(s"${model.name}.${field.name} isModelType = true")
            if(field.description.isDefined){
              property.description(field.description.get)
            }
            property.oneOf(Ref.builder().ref(toRef(service,field)).build())
          } else {
            if(field.description.isDefined){
                property.description(field.description.get)
            }
            property.`type`(Type.`object`.name())
            if(field.`type` == "uuid"){
              property.format("uuid")
            }
          }
          schema.property(field.name,property.build())
        }
        )
          model.name -> schema.build()
      }
      ).toMap
      val components = Components.builder().schemas(schemas.asJava).build()
      val openapi = OpenApi.builder().components(components)
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
        File(name = s"${name}.yaml", contents =  mapper.writerWithDefaultPrettyPrinter().writeValueAsString(openapi.build()))
      )
    }
  }


}
