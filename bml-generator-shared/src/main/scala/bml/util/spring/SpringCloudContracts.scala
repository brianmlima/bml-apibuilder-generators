package bml.util.spring

import bml.util.java.ClassNames
import bml.util.{GeneratorFSUtil, NameSpaces}
import com.squareup.javapoet.{AnnotationSpec, FieldSpec, TypeSpec}
import io.apibuilder.generator.v0.models.File
import io.apibuilder.spec.v0.models.Service
import javax.lang.model.element.Modifier.{PUBLIC,PROTECTED}
import lib.Text

object SpringCloudContracts {



  def baseTestClass(nameSpaces: NameSpaces,service: Service):Seq[File]={

    val name = "GeneratedSpringCloudContractTestBaseClass"
    val builder = TypeSpec.classBuilder(name)
      .addModifiers(PUBLIC)
        .addAnnotation(AnnotationSpec.builder(ClassNames.extendWith).addMember("value","$T.class",ClassNames.springExtension).build())
        .addAnnotation(
          AnnotationSpec.builder(ClassNames.springBootTest)
            .addMember("webEnvironment","$T.WebEnvironment.NONE", ClassNames.springBootTest)
            .build()
        )
        .addAnnotation(ClassNames.dirtiesContext)
        .addAnnotation(ClassNames.autoConfigureMessageVerifier)

        val services = service.resources
          .map(SpringServices.toServiceClassName(nameSpaces,_))

    services.map((v)=>{
      builder.addField(FieldSpec.builder(v,Text.initLowerCase(v.simpleName()),PROTECTED).addAnnotation(ClassNames.autowired).build())
    })












    Seq(GeneratorFSUtil.makeFile(name, nameSpaces.base, builder))

  }



}
