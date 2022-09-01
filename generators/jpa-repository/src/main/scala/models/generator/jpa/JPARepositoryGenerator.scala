package models.generator.jpa

import bml.util.AnotationUtil.JavaxAnnotations.JavaxValidationAnnotations
import bml.util.AnotationUtil.SpringDataAnno
import bml.util.attribute.{FindBy, Hibernate, Unique}
import bml.util.java.ClassNames.SpringTypes.SpringDataTypes
import bml.util.java.ClassNames.{JavaTypes, SpringTypes}
import bml.util.java.{JavaPojoUtil, JavaPojos}
import bml.util.jpa.JPA
import bml.util.{GeneratorFSUtil, NameSpaces, SpecValidation}
import com.squareup.javapoet.{ParameterSpec, _}
import io.apibuilder.generator.v0.models.{File, InvocationForm}
import io.apibuilder.spec.v0.models.{Field, Model, Service}
import javax.lang.model.element.Modifier
import lib.generator.CodeGenerator
import play.api.Logger

import scala.collection.JavaConverters._

class JPARepositoryGenerator extends CodeGenerator {
  val logger: Logger = Logger.apply(this.getClass())


  def getJavaDocFileHeader() = "WARNING: not all features (notably unions) and data types work with the java generator yet. \nplease contact brianmlima@gmail.com"

  override def invoke(form: InvocationForm): Either[Seq[String], Seq[File]] = invoke(form, addHeader = true)

  def invoke(form: InvocationForm, addHeader: Boolean = false): Either[Seq[String], Seq[File]] = generateCode(form, addHeader)

  private def generateCode(form: InvocationForm, addHeader: Boolean = true): Either[Seq[String], Seq[File]] = {
    val header =
      if (addHeader) Some(new ApidocComments(form.service.version, form.userAgent).forClassFile)
      else None
    new Generator(form.service, header).generateSourceFiles()
  }

  class Generator(service: Service, header: Option[String]) {
    private val nameSpaces = new NameSpaces(service)

    def generateSourceFiles(): Either[Seq[String], Seq[File]] = {
      logger.info(s"Processing Application ${service.name}")
      val errors = SpecValidation.validate(service: Service, header: Option[String])
      if (errors.isDefined) {
        return Left(errors.get)
      }

      Right(
        generateJPARepositories(service)
          ++
          generateRepositoryConfig(service)
      )
    }

    def generateRepositoryConfig(service: Service): Seq[File] = {
      if (service.models.filter(Hibernate.fromModel(_).use).isEmpty) return Seq[File]()
      val className = ClassName.get(nameSpaces.jpa.nameSpace, "RepositoryConfig")
      val repoConfigSpec = TypeSpec.classBuilder(className).addModifiers(Modifier.PUBLIC)
        .addAnnotation(SpringTypes.Configuration)
        .addAnnotation(SpringDataAnno.EntityScan(nameSpaces.model.nameSpace))
        .addAnnotation(SpringDataAnno.EnableJpaRepositories)
      Seq(GeneratorFSUtil.makeFile(className.simpleName(), nameSpaces.jpa, repoConfigSpec))
    }

    def generateJPARepositories(service: Service): Seq[File] = {

      service.models.filter(Hibernate.fromModel(_).use).map(
        model => {
          val className = JPA.toRepositoryClassName(nameSpaces.jpa, model)
          val entityClassName = JavaPojoUtil.toClassName(nameSpaces.model, model)

          val idField = model.fields.filter(_.name == "id").last
          val idType = JavaPojoUtil.dataTypeFromField(service, idField, nameSpaces.model)

          def saveMethod(): MethodSpec = {
            MethodSpec.methodBuilder("save").returns(entityClassName).addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
              .addJavadoc(
                Seq[String](
                  s"Saves a given ${entityClassName.simpleName()}. Use the returned instance for further operations as the save operation might have changed the",
                  s"${entityClassName.simpleName()} instance completely.",
                  "",
                  s"@param entity an ${entityClassName.simpleName()} to be saved. Must not be {@literal null}.",
                  s"@return the saved ${entityClassName.simpleName()}. will never be {@literal null}."
                ).mkString("\n")
              )
              .addParameter(
                ParameterSpec.builder(entityClassName, "entity")
                  .addAnnotation(JavaxValidationAnnotations.NotNull)
//                  .addAnnotation(JavaxValidationAnnotations.Valid)
                  .build()
              )
              .build()
          }

          def saveAndFlushMethod(): MethodSpec = {
            MethodSpec.methodBuilder("saveAndFlush").returns(entityClassName).addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
              .addJavadoc(
                Seq[String](
                  s"Saves a given ${entityClassName.simpleName()}. Use the returned instance for further operations as the save operation might have changed the",
                  s"${entityClassName.simpleName()} instance completely.",
                  "",
                  s"@param entity an ${entityClassName.simpleName()} to be saved. Must not be {@literal null}.",
                  s"@return the saved ${entityClassName.simpleName()}. will never be {@literal null}."
                ).mkString("\n")
              )
              .addParameter(
                ParameterSpec.builder(entityClassName, "entity")
                  .addAnnotation(JavaxValidationAnnotations.NotNull)
//                  .addAnnotation(JavaxValidationAnnotations.Valid)
                  .build()
              )
              .build()
          }

          def saveAll(): MethodSpec = {
            MethodSpec.methodBuilder("saveAll").returns(JavaTypes.List(entityClassName)).addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
              .addJavadoc(
                Seq[String](
                  "Saves all given entities.",
                  "",
                  "@param entities must not be {@literal null}.",
                  "@return the saved entities will never be {@literal null}.",
                  "@throws IllegalArgumentException in case the given model is {@literal null} or an empty Itterable.",
                ).mkString("\n")
              )
              .addParameter(
                ParameterSpec.builder(JavaTypes.Iterable(entityClassName), "entities")
                  .addAnnotation(JavaxValidationAnnotations.NotNull)
                  .addAnnotation(JavaxValidationAnnotations.NotEmpty)
                  .build()
              )
              .build()
          }


          def deleteById(): MethodSpec = {
            val javaDoc = Seq[String](
              "Deletes a model by its id.",
              "",
              Seq[String](
                "@param id must not be {@literal null}",
                if (idField.`type` == "string") s" and must be between ${idField.minimum.get} and ${idField.maximum.get} characters" else "",
                "."
              ).mkString,
              "@return the model deleted with the given id or {@literal Optional#empty()} if none found",
              "@throws IllegalArgumentException if {@code id} is {@literal null}."
            ).mkString("\n")

            val parameterSpec = ParameterSpec.builder(idType, "id")
              .addAnnotation(JavaxValidationAnnotations.NotNull)
            if (idField.`type` == "string") {
              val option = JavaPojos.handleSizeAttribute(entityClassName, idField)
              if (option.isDefined)
                parameterSpec.addAnnotation(option.get)
            }
            MethodSpec.methodBuilder("deleteById").returns(JavaTypes.Optional(entityClassName)).addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
              .addJavadoc(javaDoc)
              .addParameter(parameterSpec.build())
              .build()
          }


          def findById(): MethodSpec = {
            val javaDoc = Seq[String](
              "Retrieves an model by its id.",
              "",
              Seq[String](
                "@param id must not be {@literal null}",
                if (idField.`type` == "string") s" and must be between ${idField.minimum.get} and ${idField.maximum.get} characters" else "",
                "."
              ).mkString,
              "@return the model with the given id or {@literal Optional#empty()} if none found",
              "@throws IllegalArgumentException if {@code id} is {@literal null}."
            ).mkString("\n")

            val parameterSpec = ParameterSpec.builder(idType, "id")
              .addAnnotation(JavaxValidationAnnotations.NotNull)
            if (idField.`type` == "string") {
              val option = JavaPojos.handleSizeAttribute(entityClassName, idField)
              if (option.isDefined)
                parameterSpec.addAnnotation(option.get)
            }
            MethodSpec.methodBuilder("findById").returns(JavaTypes.Optional(entityClassName)).addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
              .addJavadoc(javaDoc)
              .addParameter(parameterSpec.build())
              .build()
          }

          def findAll(): MethodSpec = {
            val javaDoc = Seq[String](
              "Retrieves all models.",
              "NOTE: As of mid 2022 you need to have an open transaction for stream to work properly in jpa. The only alternative is to use ",
              "Collection and that can easily be abused and has the potential to cause stack blowouts and out of memory exceptions.",
              "So pretty much dont use this unless you are storing an enum or something known to be small.",
              "",
              "@return A Stream of all models in the database, the stream will be empty if none found",
            ).mkString("\n")
            MethodSpec.methodBuilder("findAll").returns(JavaTypes.Stream(entityClassName)).addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
              .addJavadoc(javaDoc)
              .build()
          }

          def genFind(model: Model): Seq[MethodSpec] = {

            val findBy = FindBy.fromModel(model)
            if (findBy.isEmpty) {
              return Seq[MethodSpec]()
            }
            val fields = findBy.get.indicesToFields(model)


            def doField(field: Field): MethodSpec = {

              val methodName = "findBy" + JavaPojoUtil.toClassName(field.name)
              val returnType = JavaTypes.Stream(entityClassName)
              val paramType = JavaPojoUtil.dataTypeFromField(service, field.`type`, nameSpaces.model)

              val param = ParameterSpec.builder(paramType, field.name, Modifier.FINAL).build()
              MethodSpec.methodBuilder(methodName).returns(returnType).addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .addParameter(param)
                .build()
            }

            fields.flatten.map(doField(_))
          }

          def existsById(): MethodSpec = {
            val javaDoc = Seq[String](
              "Tests for model existence by its id.",
              "",
              Seq[String](
                "@param id must not be {@literal null}",
                if (idField.`type` == "string") s" and must be between ${idField.minimum.get} and ${idField.maximum.get} characters" else "",
                "."
              ).mkString,
              "@return true if the model with the given id exists, false otherwise",
              "@throws IllegalArgumentException if {@code id} is {@literal null}."
            ).mkString("\n")

            val parameterSpec = ParameterSpec.builder(idType, "id")
              .addAnnotation(JavaxValidationAnnotations.NotNull)
            if (idField.`type` == "string") {
              val option = JavaPojos.handleSizeAttribute(entityClassName, idField)
              if (option.isDefined)
                parameterSpec.addAnnotation(option.get)
            }
            MethodSpec.methodBuilder("existsById").returns(TypeName.BOOLEAN).addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
              .addJavadoc(javaDoc)
              .addParameter(parameterSpec.build())
              .build()
          }


          def indexFieldFindBys(): Seq[MethodSpec] = {
            val fields = model.fields.filter(!_.annotations.filter(_ == "index").isEmpty)
            logger.info(s"Found ${fields.length} indexed Fields. Model=${model.name}")

            var out = Seq[MethodSpec]()


            out = out ++ fields.flatMap(
              field => {
                val fieldName = JavaPojoUtil.toFieldName(field)

                val streamMethodName = s"streamBy${fieldName.capitalize}"
                val sliceMethodName = s"sliceBy${fieldName.capitalize}"
                val pageMethodName = s"pageBy${fieldName.capitalize}"
                val `type` = JavaPojoUtil.dataTypeFromField(service, field, nameSpaces.model);
                val query = s"SELECT o FROM #{#entityName} o WHERE o.${fieldName} = ?1"

                Seq(
                  MethodSpec.methodBuilder(streamMethodName).addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                    .addJavadoc(
                      (
                        Seq[String](s" Accessor method for Stream by ${fieldName}")
                        )
                        .mkString("\n")
                    )
                    .addAnnotation(SpringDataAnno.Query(query))
                    .addParameter(`type`, fieldName)
                    .returns(JavaTypes.Stream(entityClassName))
                    .build(),
                  MethodSpec.methodBuilder(sliceMethodName).addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                    .addAnnotation(SpringDataAnno.Query(query))
                    .addParameter(`type`, fieldName)
                    .addParameter(SpringDataTypes.Pageable, "pageable")
                    .returns(SpringDataTypes.Slice(entityClassName))
                    .build(),
                  MethodSpec.methodBuilder(pageMethodName).addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                    .addAnnotation(SpringDataAnno.Query(query))
                    .addParameter(`type`, fieldName)
                    .addParameter(SpringDataTypes.Pageable, "pageable")
                    .returns(SpringDataTypes.Page(entityClassName))
                    .build()


                  //Add in Page return type
                  //                )
                  //                MethodSpec.methodBuilder(methodName).addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                  //                  .addParameter(`type`, fieldName)
                  //                  .addParameter(SpringDataTypes.Pageable,"pageable")
                  //                  .returns(SpringDataTypes.Slice(entityClassName))
                  //                  .build()
                )

              }
            )




            //            out = out ++ combinations
            //              .map(
            //                combination => {
            //                  val methodName = combination
            //                    .map(JavaPojoUtil.toFieldName)
            //                    .map(_.capitalize)
            //                    .mkString("And")
            //                  MethodSpec.methodBuilder(s"findBy${methodName}").addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            //                    .build()
            //                }
            //              ).toSeq
            //

            val uniqueIndexesOption = Unique.fromModel(model)


            if (uniqueIndexesOption.isDefined) {
              val uniqueIndexes = uniqueIndexesOption.get


              val indices = uniqueIndexes.indices
              val fieldSets = uniqueIndexes.indicesToFields(model)


              fieldSets.foreach(
                fieldSet =>
                  logger.info(s"Createing unique index for fields ${fieldSet.map(_.name).mkString(",")}")
              )

              out = out ++
                fieldSets.map(
                  fields => {


                    val modelTypeFields = fields.filter(JavaPojoUtil.isModelType(service, _)).map(a => a -> JavaPojoUtil.findIdField(service, a.`type`))(collection.breakOut).toMap

                    val methodName = fields.map(
                      aField => if (modelTypeFields.get(aField).flatten.isDefined) {
                        JavaPojoUtil.toFieldName(aField.name + " _id")
                      } else {
                        JavaPojoUtil.toFieldName(aField.name)
                      }
                    ).map(_.capitalize).mkString("And")


                    var c = 1

                    var query = "SELECT o FROM #{#entityName} o WHERE "

                    query = query + fields.map(
                      aField => {
                        val foo = modelTypeFields.get(aField).flatten
                        var out: String = ""
                        if (foo.isDefined) {
                          val idField = foo.get
                          out = s"o.${JavaPojoUtil.toFieldName(aField)}.${JavaPojoUtil.toFieldName(idField)}=?${c}"
                        } else {
                          out = s"o.${JavaPojoUtil.toFieldName(aField)}=?${c}"
                        }
                        c = c + 1
                        out
                      }
                    ).mkString(" AND ")

                    val spec = MethodSpec.methodBuilder(s"findBy${methodName}")
                      .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                      //                      .addAnnotation(SpringDataAnno.Query(query))
                      .addJavadoc(
                        //                        JavaPojoUtil.textToComment(
                        (
                          Seq[String](s"Specified by unique attribute field index = [${fields.map(_.name).mkString(",")}] .")
                            ++
                            fields.map(
                              aField => {
                                val `type` = JavaPojoUtil.dataTypeFromField(service, aField, nameSpaces.model);
                                s"@param ${JavaPojoUtil.toFieldName(aField)} ${aField.description.getOrElse("")}"
                              }
                            ) ++
                            Seq[String](s"@return An Optional of type ${entityClassName}")
                          )
                          .mkString("\n")
                        //                        )
                      )
                      //                      .addJavadoc()
                      .addParameters(
                        fields.map(
                          aField => {
                            val fieldType = JavaPojoUtil.dataTypeFromField(service, aField, nameSpaces.model)
                            val idFieldOption = JavaPojoUtil.findIdField(service, aField.`type`)
                            var parameterSpec: ParameterSpec = null
                            if (idFieldOption.isDefined) {
                              val idField = idFieldOption.get
                              val idFieldType = JavaPojoUtil.dataTypeFromField(service, idField, nameSpaces.model)
                              parameterSpec = ParameterSpec.builder(idFieldType, JavaPojoUtil.toIdFieldName(aField))
                                .addAnnotation(JavaxValidationAnnotations.NotNull)
                                .build()
                            } else {
                              parameterSpec = ParameterSpec.builder(fieldType, JavaPojoUtil.toFieldName(aField))
                                .addAnnotation(JavaxValidationAnnotations.NotNull)
                                .build()
                            }
                            parameterSpec
                          }
                        ).asJava
                      ).returns(ParameterizedTypeName.get(JavaTypes.Optional, entityClassName))
                    if (!modelTypeFields.isEmpty) {
                      spec.addAnnotation(SpringDataAnno.Query(query))
                    }
                    spec.build()
                  }
                )
            }
            out
          }


          def indexFieldExistsBys(): Seq[MethodSpec] = {
            val fields = model.fields.filter(!_.annotations.filter(_ == "index").isEmpty)
            logger.info(s"Found ${fields.length} indexed Fields. Model=${model.name}")

            var out = Seq[MethodSpec]()
            val uniqueIndexesOption = Unique.fromModel(model)
            if (uniqueIndexesOption.isDefined) {
              val uniqueIndexes = uniqueIndexesOption.get
              val indices = uniqueIndexes.indices
              val fieldSets = uniqueIndexes.indicesToFields(model)
              fieldSets.foreach(
                fieldSet =>
                  logger.info(s"Creatingunique index existsBy for fields ${fieldSet.map(_.name).mkString(",")}")
              )
              out = out ++
                fieldSets.map(
                  fields => {
                    val modelTypeFields = fields.filter(JavaPojoUtil.isModelType(service, _)).map(a => a -> JavaPojoUtil.findIdField(service, a.`type`))(collection.breakOut).toMap
                    val methodName = fields.map(
                      aField => if (modelTypeFields.get(aField).flatten.isDefined) {
                        JavaPojoUtil.toFieldName(aField.name + " _id")
                      } else {
                        JavaPojoUtil.toFieldName(aField.name)
                      }
                    ).map(_.capitalize).mkString("And")
                    var c = 1
                    var query = "SELECT (COUNT(o) = 1) FROM #{#entityName} o WHERE "

                    query = query + fields.map(
                      aField => {
                        val foo = modelTypeFields.get(aField).flatten
                        var out: String = ""
                        if (foo.isDefined) {
                          val idField = foo.get
                          out = s"o.${JavaPojoUtil.toFieldName(aField)}.${JavaPojoUtil.toFieldName(idField)}=?${c}"
                        } else {
                          out = s"o.${JavaPojoUtil.toFieldName(aField)}=?${c}"
                        }
                        c = c + 1
                        out
                      }
                    ).mkString(" AND ")

                    val spec = MethodSpec.methodBuilder(s"existsBy${methodName}")
                      .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                      //                      .addAnnotation(SpringDataAnno.Query(query))
                      .addJavadoc(
                        //                        JavaPojoUtil.textToComment(
                        (
                          Seq[String](s"Specified by unique attribute field index = [${fields.map(_.name).mkString(",")}] .")
                            ++
                            fields.map(
                              aField => {
                                val `type` = JavaPojoUtil.dataTypeFromField(service, aField, nameSpaces.model);
                                s"@param ${JavaPojoUtil.toFieldName(aField)} ${aField.description.getOrElse("")}"
                              }
                            ) ++
                            Seq[String](s"@return True if found false otherwise")
                          )
                          .mkString("\n")
                        //                        )
                      )
                      //                      .addJavadoc()
                      .addParameters(
                        fields.map(
                          aField => {
                            val fieldType = JavaPojoUtil.dataTypeFromField(service, aField, nameSpaces.model)
                            val idFieldOption = JavaPojoUtil.findIdField(service, aField.`type`)
                            var parameterSpec: ParameterSpec.Builder = null
                            if (idFieldOption.isDefined) {
                              val idField = idFieldOption.get
                              val idFieldType = JavaPojoUtil.dataTypeFromField(service, idField, nameSpaces.model)
                              parameterSpec = ParameterSpec.builder(idFieldType, JavaPojoUtil.toIdFieldName(aField))
                            } else {
                              parameterSpec = ParameterSpec.builder(fieldType, JavaPojoUtil.toFieldName(aField))
                            }
                            if (aField.required) {
                              parameterSpec.addAnnotation(JavaxValidationAnnotations.NotNull)
                            }
                            parameterSpec.build()
                          }
                        ).asJava
                      ).returns(TypeName.BOOLEAN)
                    if (!modelTypeFields.isEmpty) {
                      spec.addAnnotation(SpringDataAnno.Query(query))
                    }
                    spec.build()
                  }
                )
            }
            out
          }


          val repoSpec = TypeSpec.interfaceBuilder(className).addModifiers(Modifier.PUBLIC)
            .addSuperinterface(
              ParameterizedTypeName.get(
                SpringTypes.Repository,
                entityClassName,
                JavaPojoUtil.dataTypeFromField(service, idField, nameSpaces.model)
              )
            )
//            .addAnnotation(JavaxValidationAnnotations.Validated)
            .addAnnotation(SpringDataTypes.Repository)
            .addMethod(saveMethod())
            .addMethod(saveAndFlushMethod())
            .addMethod(saveAll())
            .addMethod(findById())
            .addMethod(findAll())
            .addMethod(existsById())
            .addMethod(deleteById())
            .addMethods(indexFieldFindBys().asJava)
            .addMethods(indexFieldExistsBys().asJava)
            .addMethods(genFind(model).asJava)


          GeneratorFSUtil.makeFile(className.simpleName(), nameSpaces.jpa, repoSpec)
        }

      )

    }

  }

}
