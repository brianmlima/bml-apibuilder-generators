package models.generator.java.persistence.sql.generators

import bml.util.java.{JavaCommonClasses, JavaPojoUtil}
import com.squareup.javapoet.{AnnotationSpec, ClassName, FieldSpec, ParameterizedTypeName}
import io.apibuilder.spec.v0.models.Service
import javax.lang.model.element.Modifier.PROTECTED
import javax.persistence.{JoinColumn, ManyToOne}
import models.generator.java.persistence.sql.{GenUtils, ModelData}

class PersistanceClassGenerator {

}

object PersistanceClassGenerator extends JavaPojoUtil {

  /**
   * Generate Models that are marked for persistence.
   *
   * @return
   */
  def generatePersistenceModel(service: Service, modelData: ModelData): ModelData = {

    //Create the JavaPoet builder tool for the java file and add the default javadoc header
    val classBuilder = GeneratorsCommon.commonClassBuilder(modelData)
    //Assign it to the rite place in the ModelData
    modelData.classBuilder = Some(classBuilder)

    // Setup Extension and Implements
    classBuilder
      //Extend AuditableBaseEntity
      .superclass(ParameterizedTypeName.get(ClassName.get(modelData.config.jpaNameSpace, "BaseEntity"), modelData.className))
    Seq(
      //Add anotation specs specific to persistance classes here
      GenUtils.entity,
      GenUtils.table(modelData.model)
    ).foreach(classBuilder.addAnnotation(_))

    val jpaIdField = modelData.getIdField().get


    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Setup Common methods
    Seq(
      JavaCommonClasses.getThisMethod(modelData.simpleName),
      JavaCommonClasses.getTypeMethod(modelData.simpleName),
      JavaCommonClasses.getIdentifierMethod(toParamName(jpaIdField.name, true))
    ).foreach(classBuilder.addMethod(_))

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Process the models fields
    modelData.model.fields.foreach(field => {
      //Get the data type of the field.
      val javaDataType = dataTypeFromField(service, field.`type`, modelData.config.modelsNameSpace)

      //create the basic builder
      val fieldBuilder = FieldSpec.builder(javaDataType, toParamName(field.name, true)).addModifiers(PROTECTED)

      if (true) {
        fieldBuilder.addAnnotation(GenUtils.jsonProperty)
      }
      if (field == jpaIdField) {
        fieldBuilder.addAnnotation(GenUtils.id)
        fieldBuilder.addAnnotation(GenUtils.generatedValueAuto)
      }

      //Set @NotNull if required
      if (field.required) {
        fieldBuilder.addAnnotation(GenUtils.notNull)
        fieldBuilder.addAnnotation(GenUtils.basicOptional(false))
        //if the field is a String add NotBlank
        if (field.`type` == "string") {
          fieldBuilder.addAnnotation(GenUtils.notBlank)
        }
      }

      if (field.minimum.isDefined && field.maximum.isDefined) {
        GenUtils.size(
          classBuilder,
          fieldBuilder,
          field.name,
          field.minimum.get.toInt,
          field.maximum.get.toInt)
      }

      //Handle enumeration type.
      //@Enumerated(EnumType.STRING)
      if (isEnumType(modelData.config.service, field)) {
        fieldBuilder.addAnnotation(GenUtils.enumerated())
      }
      //Add column for types that are not models.
      if (!isModelType(modelData.config.service, field)) {
        fieldBuilder.addAnnotation(GenUtils.column(field))
      }

      if (isModelType(modelData.config.service, field)) {
        fieldBuilder
          .addAnnotation(classOf[ManyToOne])
          .addAnnotation(AnnotationSpec.builder(classOf[JoinColumn]).addMember("name", "$S", field.name + "_id").build())
      }

      ///////////////////////////////////////
      //Deal with javadocs
      GenUtils.javaDoc(fieldBuilder, field)
      ///////////////////////////////////////
      // Deal with individual known attributes.
      GeneratorsCommon.processCommonAttributes(classBuilder, field, fieldBuilder)
      classBuilder.addField(fieldBuilder.build)
    })
    modelData
    //makeFile(modelData.simpleName, classBuilder)
  }

}
