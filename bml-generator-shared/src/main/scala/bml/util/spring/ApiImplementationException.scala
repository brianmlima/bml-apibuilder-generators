package bml.util.spring

import bml.util.GeneratorFSUtil.makeFile
import bml.util.NameSpaces
import bml.util.java.ClassNames.JavaTypes
import bml.util.spring.SpringVersion.SpringVersion
import com.squareup.javapoet.{ClassName, MethodSpec, ParameterSpec, TypeSpec}
import io.apibuilder.generator.v0.models.File
import io.apibuilder.spec.v0.models.Service
import javax.lang.model.element.Modifier._


object ApiImplementationException {

  val name = "ApiImplementationException"

  def getClassName(nameSpaces: NameSpaces): ClassName = {
    ClassName.get(nameSpaces.controller.nameSpace, name)
  }

  def getClassName(service: Service, springVersion: SpringVersion, nameSpaces: NameSpaces): ClassName = {
    ClassName.get(nameSpaces.controller.nameSpace, name)
  }

  def getType(service: Service, springVersion: SpringVersion, nameSpaces: NameSpaces): TypeSpec = {
    TypeSpec.classBuilder(getClassName(nameSpaces))
      .superclass(JavaTypes.RuntimeException)
      .addModifiers(PUBLIC)
      .addMethod(
        MethodSpec.constructorBuilder().addModifiers(PUBLIC)
          .addCode("super();")
          .build()
      )
      .addMethod(
        MethodSpec.constructorBuilder().addModifiers(PUBLIC)
          .addParameter(ParameterSpec.builder(JavaTypes.String, "message").addModifiers(FINAL).build())
          .addCode("super(message);")
          .build()
      )
      .build()
  }


  def getType(nameSpaces: NameSpaces): TypeSpec = {

    TypeSpec.classBuilder(getClassName(nameSpaces))
      .superclass(JavaTypes.RuntimeException)
      .addModifiers(PUBLIC)
      .addMethod(
        MethodSpec.constructorBuilder().addModifiers(PUBLIC)
          .addCode("super();")
          .build()
      )
      .addMethod(
        MethodSpec.constructorBuilder().addModifiers(PUBLIC)
          .addParameter(ParameterSpec.builder(JavaTypes.String, "message").addModifiers(FINAL).build())
          .addCode("super(message);")
          .build()
      )
      .build()
  }


  def getFile(nameSpaces: NameSpaces): Seq[File] = {


    Seq(makeFile(name, nameSpaces.controller, getType(nameSpaces).toBuilder))


  }


}
