package models.generator.java.persistence.sql.generators

import bml.util.java.JavaPojoUtil
import com.squareup.javapoet.{AnnotationSpec, FieldSpec}
import javax.lang.model.element.Modifier.PROTECTED
import models.generator.java.persistence.sql.{GenUtils, ModelData}

class ClassGenerator {

}

object ClassGenerator extends JavaPojoUtil {

  /**
    * Generates models that are not marked for persistence.
    *
    * @return
    */
  def generateNonPersistenceModel(modelData: ModelData): ModelData = {
    //Get normalized class name
    //val className = toClassName(model.name)
    //Create the JavaPoet builder tool for the java file and add the default javadoc header
    val classBuilder = GeneratorsCommon.commonClassBuilder(modelData)
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Setup Class Annotations
    Seq[AnnotationSpec](
      //Add anotation specs specific to persistance classes here
    ).foreach(classBuilder.addAnnotation(_))
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Setup Common methods and implementations

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Process the models fields
    modelData.model.fields.foreach(field => {
      //Get the data type of the field.
      val javaDataType = dataTypeFromField(field.`type`, modelData.config.modelsNameSpace)
      //create the basic builder
      val fieldBuilder = FieldSpec.builder(javaDataType, toParamName(field.name, true)).addModifiers(PROTECTED)
      //Set @NotNull if required
      if (field.required) {
        fieldBuilder.addAnnotation(GenUtils.notNull)
        //if the field is a String add NotBlank
        if (field.`type` == "string") {
          fieldBuilder.addAnnotation(GenUtils.notBlank)
        }
      }
      //Deal with javadocs
      GenUtils.javaDoc(fieldBuilder, field)
      // Deal with individual known attributes.
      GeneratorsCommon.processCommonAttributes(classBuilder, field, fieldBuilder)
      // Add the field to the current class
      classBuilder.addField(fieldBuilder.build)
    })
    //      makeFile(modelData.simpleName, classBuilder)
    modelData.classBuilder = Some(classBuilder)
    modelData
  }
}