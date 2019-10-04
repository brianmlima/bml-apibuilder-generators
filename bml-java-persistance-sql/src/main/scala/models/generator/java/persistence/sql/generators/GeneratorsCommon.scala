package models.generator.java.persistence.sql.generators

import bml.util.java.JavaPojoUtil
import com.squareup.javapoet.{ClassName, FieldSpec, TypeSpec}
import io.apibuilder.spec.v0.models.{Field, Model}
import javax.lang.model.element.Modifier.PUBLIC
import javax.validation.constraints.Email
import models.generator.java.persistence.sql.{GenUtils, ModelData}

import scala.collection.JavaConverters._

class GeneratorsCommon {

}

object GeneratorsCommon extends JavaPojoUtil {

  def processCommonClassAnnotations(model: Model, builder: TypeSpec.Builder): Unit = {
    Seq(
      GenUtils.data,
      GenUtils.builder,
      GenUtils.allArgsConstructor,
      GenUtils.validated,
      GenUtils.accessorFluent,
      GenUtils.jsonIgnoreProperties
    ).foreach(builder.addAnnotation(_))
  }


  def commonClassBuilder(modelData: ModelData): TypeSpec.Builder = {
    //Create the JavaPoet builder tool for the java file and add the default javadoc header
    val classBuilder = TypeSpec.classBuilder(modelData.simpleName).addJavadoc(modelData.config.apiDocComments)
      //Is a public class
      .addModifiers(PUBLIC)
      //Implement Unions
      .addSuperinterfaces(modelData.relatedUnions.map { u => ClassName.get(modelData.config.modelsNameSpace, toClassName(u.name)) }.asJava)
    //Add api.json documentation
    modelData.model.description.map(classBuilder.addJavadoc(_))
    //Add Class level Annotations
    GeneratorsCommon.processCommonClassAnnotations(modelData.model, classBuilder)
    return classBuilder
  }

  def processCommonAttributes(classBuilder: TypeSpec.Builder, field: Field, fieldBuilder: FieldSpec.Builder): Unit = {
    field.attributes.foreach(attribute => {
      attribute.name match {
        //Handle @Size
        case "size" => GenUtils.size(
          classBuilder,
          fieldBuilder,
          field.name,
          (attribute.value \ "min").as[Int],
          (attribute.value \ "max").as[Int])
        //Handle @Pattern
        case "pattern" => fieldBuilder.addAnnotation(GenUtils.pattern(attribute))
        //Handle @Email
        case "email" => fieldBuilder.addAnnotation(classOf[Email])
        //Handle unknown
        case _ =>
      }
    })

  }


}

