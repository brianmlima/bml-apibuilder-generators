package bml.util.persist

import bml.util.AnotationUtil.LombokAnno
import bml.util.GeneratorFSUtil.makeFile
import bml.util.java.ClassNames.{HibernateTypes, JavaTypes}
import bml.util.spring.SpringVersion.SpringVersion
import bml.util.{JavaNameSpace, NameSpaces}
import com.squareup.javapoet.{ClassName, CodeBlock, MethodSpec, ParameterSpec, TypeSpec}
import javax.lang.model.element.Modifier.PUBLIC
import io.apibuilder.generator.v0.models.File

class UUIDIfNullGenerator {
}

object UUIDIfNullGenerator {

  val simpleName = "UUIDIfNullGenerator"

  def className(namespaces: NameSpaces): ClassName = {
    val thePackage = nameSpace(namespaces).nameSpace
    ClassName.bestGuess(s"${thePackage}.${simpleName}");
  }

  def nameSpace(nameSpaces: NameSpaces): JavaNameSpace = {
    nameSpaces.jpa
  }

  private def getReturnType(springVersion: SpringVersion): ClassName = {
    springVersion match {
      case bml.util.spring.SpringVersion.SIX => JavaTypes.Object
      case bml.util.spring.SpringVersion.FIVE => JavaTypes.Serializable
    }
  }


  def get(springVersion: SpringVersion, nameSpaces: NameSpaces): File = {
    val classBuilder = TypeSpec.classBuilder(className(nameSpaces))
      .addModifiers(PUBLIC)
      .addJavadoc("This UUID ID generator allows passing through set ids. This allows you to import data or duplicate data across services relatively easily by making ids in forms optional.")
      .superclass(HibernateTypes.UUIDGenerator)
      .addAnnotation(LombokAnno.Slf4j)
      .addMethod(
        MethodSpec.methodBuilder("generate")
          .addAnnotation(classOf[Override])
          .addModifiers(PUBLIC)
          .addParameter(ParameterSpec.builder(HibernateTypes.SharedSessionContractImplementor, "session").build())
          .addParameter(ParameterSpec.builder(JavaTypes.Object, "object").build())
          .addException(HibernateTypes.HibernateException)
          .addCode(
            CodeBlock.builder()
              .addStatement(
                "$T id = session.getEntityPersister(null, object).getClassMetadata().getIdentifier(object, session)",
                getReturnType(springVersion)
              )
              .addStatement("return id != null ? id : super.generate(session, object)")
              .build()
          )
          .returns(getReturnType(springVersion))
          .build()
      )
    makeFile(className(nameSpaces).simpleName(), nameSpace(nameSpaces), classBuilder)
  }

}
