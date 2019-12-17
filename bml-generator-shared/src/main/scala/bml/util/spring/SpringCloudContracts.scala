package bml.util.spring

import bml.util.AnotationUtil.SpringAnno.SpringTestAnno
import bml.util.java.ClassNames
import bml.util.java.ClassNames.SpringTypes
import bml.util.{GeneratorFSUtil, NameSpaces}
import com.squareup.javapoet.{AnnotationSpec, FieldSpec, TypeSpec}
import io.apibuilder.generator.v0.models.File
import io.apibuilder.spec.v0.models.Service
import lib.Text

object SpringCloudContracts {

  import javax.lang.model.element.Modifier._

  def baseTestClass(nameSpaces: NameSpaces, service: Service): Seq[File] = {

    val name = "GeneratedSpringCloudContractTestBaseClass"
    val builder = TypeSpec.classBuilder(name)
      .addModifiers(PUBLIC)
      .addAnnotation(SpringTestAnno.SpringBootTest)
      .addAnnotation(
        AnnotationSpec.builder(SpringTypes.SpringBootTest)
          .addMember("webEnvironment", "$T.WebEnvironment.NONE", SpringTypes.SpringBootTest)
          .build()
      )
      .addAnnotation(ClassNames.dirtiesContext)
      .addAnnotation(ClassNames.autoConfigureMessageVerifier)

    val services = service.resources
      .map(SpringServices.toServiceClassName(nameSpaces, _))

    services.map((v) => {
      builder.addField(FieldSpec.builder(v, Text.initLowerCase(v.simpleName()), PROTECTED).addAnnotation(SpringTypes.Autowired).build())
    })


    Seq(GeneratorFSUtil.makeFile(name, nameSpaces.base, builder))

  }


}
