package models.generator.java.persistence.sql

import com.squareup.javapoet.ClassName
import io.apibuilder.spec.v0.models.Service

case class GenConfig(service: Service,
                     nameSpace: String,
                     modelsNameSpace: String,
                     jpaNameSpace: String,
                     modelsDirectoryPath: String,
                     jpaDirectoryPath: String,
                     resourceNameSpace: String,
                     resourceDirectoryPath: String,
                     apiDocComments: String
                    ) {

  val baseRepoSimpleName = "BaseRepository"
  val baseRepoClassName = ClassName.get(jpaNameSpace, baseRepoSimpleName)

  val baseEntitySimpleName = "BaseEntity"
  val baseEntityClassName = ClassName.get(jpaNameSpace, baseEntitySimpleName)

}
