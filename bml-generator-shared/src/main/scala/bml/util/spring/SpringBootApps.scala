package bml.util.spring


import bml.util.java.{ClassNames, JavaPojoUtil}
import bml.util.{GeneratorFSUtil, NameSpaces}
import com.squareup.javapoet.{ClassName, CodeBlock, MethodSpec, TypeSpec}
import io.apibuilder.generator.v0.models.File
import io.apibuilder.spec.v0.models.Service
import javax.lang.model.element.Modifier.{FINAL, PUBLIC, STATIC}

object SpringBootApps {

  def mainName(service: Service) = JavaPojoUtil.toClassName(service.name)

  def mainClassName(nameSpaces: NameSpaces, service: Service) = {
    ClassName.get(nameSpaces.nameSpace, mainName(service))
  }

  def foo(nameSpaces: NameSpaces, service: Service): Seq[File] = {
    val className = mainClassName(nameSpaces, service)
    val builder = TypeSpec.classBuilder(className).addAnnotation(ClassNames.springBootApplication)
      .addMethod(
        MethodSpec
          .methodBuilder("main")
          .addModifiers(PUBLIC, STATIC)
          .addParameter(classOf[Array[String]], "args", FINAL)
          .addStatement(CodeBlock.of("$T.run($T.class, args)", ClassNames.springApplication, className))
          .build()
      )
    Seq(GeneratorFSUtil.makeFile(className.simpleName(), nameSpaces.base, builder))
  }

}
