package bml.util.java.testing

import bml.util.GeneratorFSUtil.makeFile
import bml.util.{AnotationUtil, GeneratorFSUtil, NameSpaces}
import bml.util.java.{ClassNames, JavaPojoTestFixtures}
import bml.util.java.JavaPojoTestFixtures.{toBuilderClassName, toClassName}
import com.squareup.javapoet.{ClassName, TypeSpec}
import io.apibuilder.generator.v0.models.File
import io.apibuilder.spec.v0.models.Service
import javax.lang.model.element.Modifier

object MockEnums {

  def generateMockFactory(service: Service, nameSpaces: NameSpaces): Seq[File] = {
    service.enums.map(
      enum => {
        val className = JavaPojoTestFixtures.mockFactoryClassName(nameSpaces, enum.name)
        val targetClassName = ClassName.get(nameSpaces.model.nameSpace, toClassName(`enum`.name))
        val targetClassBuilderName = toBuilderClassName(targetClassName)
        val typeBuilder = TypeSpec.classBuilder(className).addModifiers(Modifier.PUBLIC)
          .addAnnotation(ClassNames.builder)
          .addAnnotation(AnotationUtil.fluentAccessor)
          .addSuperinterface(ClassNames.supplier(targetClassName))
        (className, typeBuilder)
      }
    ).map(t => GeneratorFSUtil.makeFile(t._1.simpleName(), nameSpaces.modelFactory, t._2))

  }
}