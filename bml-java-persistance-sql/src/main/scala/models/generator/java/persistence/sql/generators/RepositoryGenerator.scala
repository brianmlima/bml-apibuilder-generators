package models.generator.java.persistence.sql.generators

import bml.util.java.JavaPojoUtil
import com.squareup.javapoet.{ParameterizedTypeName, TypeSpec}
import io.apibuilder.spec.v0.models.Service
import javax.lang.model.element.Modifier.PUBLIC
import models.generator.java.persistence.sql.ModelData
import org.springframework.stereotype.Repository

class RepositoryGenerator {

}

object RepositoryGenerator extends JavaPojoUtil {
  def generate(service: Service, modelData: ModelData): ModelData = {
    val config = modelData.config

    //extends BaseRepository<Client, Long>
    val repoClassName = modelData.repoClassName

    //var t = TypeVariableName.get(modelData.simpleName)
    var foo = ParameterizedTypeName.get(config.baseRepoClassName, modelData.className, modelData.getIdFieldClassName(service).get)

    var builder = TypeSpec.interfaceBuilder(repoClassName)
      .addModifiers(PUBLIC)
      .addAnnotation(classOf[Repository])
      .addSuperinterface(foo)
    modelData.repoBuilder = Some(builder)
    modelData
  }
}
