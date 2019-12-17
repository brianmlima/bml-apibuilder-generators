package bml.util.java

import bml.util.java.ClassNames.JavaxTypes.JavaxValidationTypes
import bml.util.java.ClassNames.{LombokTypes, SpringTypes}
import com.fasterxml.jackson.annotation.JsonIgnore
import com.squareup.javapoet._
import javax.lang.model.element.Modifier._
import org.javers.spring.annotation.JaversAuditable
import org.springframework.data.repository.NoRepositoryBean
import org.springframework.validation.annotation.Validated

import scala.collection.JavaConverters._

object JavaCommonClasses {

  def serialVersionUID(): FieldSpec = FieldSpec.builder(TypeName.LONG, "serialVersionUID").addModifiers(PRIVATE, STATIC, FINAL).initializer("$LL", "1").addJavadoc("Standard serialVersionUID.").build()

  private def abstractGetThis(): MethodSpec = MethodSpec.methodBuilder("getThis")
    .addModifiers(PUBLIC, ABSTRACT)
    .returns(TypeVariableName.get("T"))
    .addJavadoc(
      Seq(
        "Allows generic with type builder functionality in a super class.",
        "Also ensures with functionality modified the data object and avoids",
        "accidentally working with a proxy in the case of Hibernate.",
        "Im not sure this can happen with later versions of hibernate but better",
        "safe than sorry in this case as those bugs take days to track down.",
        "",
        "@return T the instance the method is called on as its type"
      ).mkString("\n")
    ).build()

  def getThisMethod(className: ClassName): MethodSpec = MethodSpec.methodBuilder("getThis")
    .addModifiers(PUBLIC)
    .returns(className)
    .addCode("return this;\n")
    .addAnnotation(AnnotationSpec.builder(classOf[JsonIgnore]).build())
    .addAnnotation(AnnotationSpec.builder(classOf[Override]).build())
    .build()


  def getThisMethod(className: String): MethodSpec = getThisMethod(ClassName.get("", className))

  def getTypeMethod(className: ClassName): MethodSpec = MethodSpec.methodBuilder("getType")
    .addModifiers(PUBLIC)
    .returns(classOf[String])
    .addCode("return getThis().getClass().getCanonicalName();\n")
    .addAnnotation(AnnotationSpec.builder(classOf[JsonIgnore]).build())
    .addAnnotation(AnnotationSpec.builder(classOf[Override]).build())
    .build()

  def getTypeMethod(className: String): MethodSpec = getTypeMethod(ClassName.get("", className))


  def getIdentifierMethod(idFieldName: String): MethodSpec = MethodSpec.methodBuilder("getIdentifier")
    .addModifiers(PUBLIC)
    .returns(classOf[String])
    .addCode(s"return this.${
      idFieldName
    }.toString();\n")
    .addAnnotation(AnnotationSpec.builder(classOf[JsonIgnore]).build())
    .addAnnotation(AnnotationSpec.builder(classOf[Override]).build())
    .build()


  def baseEntity(packageName: String): TypeSpec.Builder = TypeSpec.classBuilder("BaseEntity")
    .addModifiers(PUBLIC, ABSTRACT)
    .addTypeVariable(TypeVariableName.get("T", ClassName.get("", "BaseEntity")))
    .addSuperinterface(SpringTypes.ObjectIdentity)
    .addAnnotation(ClassNames.mappedSuperclass)
    .addAnnotation(SpringTypes.Validated)
    .addAnnotation(LombokTypes.Slf4j)
    .addField(serialVersionUID())
    .addMethod(abstractGetThis())

  def baseRepository(packageName: String): TypeSpec.Builder = {

    val notNull = AnnotationSpec.builder(JavaxValidationTypes.NotNull).build()
    val javersAuditable = AnnotationSpec.builder(classOf[JaversAuditable]).build()

    case class TypeHelper(packagePath: String, className: String) {
      def typeVar() = TypeVariableName.get(className)

      def listType() = ParameterizedTypeName.get(ClassName.get("java.util", "List"), clazzName())

      def collectionType() = ParameterizedTypeName.get(ClassName.get("java.util", "List"), clazzName())

      def iterableType() = ParameterizedTypeName.get(ClassName.get("java.lang", "Iterable"), clazzName())

      def optionalType() = ParameterizedTypeName.get(ClassName.get("java.util", "Optional"), clazzName())

      def sliceType() = ClassNames.slice(clazzName())

      def param(parmaName: String) = ParameterSpec.builder(clazzName(), parmaName)

      def param(parmaName: String, annotationSpecs: AnnotationSpec*): ParameterSpec.Builder = {
        param(parmaName).addAnnotations(annotationSpecs.asJava)
      }

      def collectionParam(paramName: String) = ParameterSpec.builder(collectionType(), paramName)

      def collectionParam(paramName: String, annotationSpecs: AnnotationSpec*): ParameterSpec.Builder = {
        collectionParam(paramName).addAnnotations(annotationSpecs.asJava)
      }

      def iterableParam(paramName: String) = ParameterSpec.builder(iterableType(), paramName)

      def iterableParam(paramName: String, annotationSpecs: AnnotationSpec*): ParameterSpec.Builder = {
        iterableParam(paramName).addAnnotations(annotationSpecs.asJava)
      }

      def listParam(parmaName: String) = ParameterSpec.builder(listType(), parmaName)

      def clazzName() = ClassName.get(packagePath, className)
    }

    val repo = TypeHelper("org.springframework.data.repository", "Repository")
    val pageable = TypeHelper("org.springframework.data.domain", "Pageable")
    val t = TypeHelper("", "T")
    val s = TypeHelper("", "S")
    val id = TypeHelper("", "ID")


    val classBuilder = TypeSpec.interfaceBuilder("BaseRepository")
      .addModifiers(PUBLIC)
      .addSuperinterface(
        ParameterizedTypeName.get(
          repo.clazzName(),
          t.typeVar(),
          id.typeVar())
      )
      .addTypeVariable(t.typeVar().withBounds(ParameterizedTypeName.get(ClassName.get("", "BaseEntity"), t.typeVar())))
      .addTypeVariable(id.typeVar().withBounds(classOf[java.io.Serializable]))
      .addAnnotation(classOf[NoRepositoryBean])
      .addAnnotation(classOf[Validated])
      .addJavadoc(
        Seq(
          "Base repository methods common to all Entity classes.",
          "",
          " @param <T> the Entity type",
          " @param <ID> the Entity id type"
        ).mkString("\n")
      )


    ////////////////////////////////////////////////////////////////////////////////////////////////////
    //<S extends T> S save(@NotNull S entity);
    classBuilder.addMethod(
      MethodSpec.methodBuilder("save")
        .addModifiers(PUBLIC, ABSTRACT)
        .addTypeVariable(s.typeVar().withBounds(t.typeVar()))
        .returns(s.typeVar())
        .addParameter(s.param("entity").addAnnotation(notNull).build())
        .addAnnotation(javersAuditable)
        .addJavadoc(
          Seq(
            "Saves a given model. Use the returned instance for further operations as the save operation might have changed the",
            "model instance completely.",
            "",
            " @param <S> saved impl",
            " @param entity must not be { @literal null}.",
            " @return the saved model will never be { @literal null}."
          ).mkString("\n")
        ).build()
    )
    ////////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    //   <S extends T> List<S> saveAll(@NotNull Iterable<T> entities);
    classBuilder.addMethod(
      MethodSpec.methodBuilder("saveAll")
        .addModifiers(PUBLIC, ABSTRACT)
        .addTypeVariable(s.typeVar().withBounds(t.typeVar()))
        .returns(s.listType())
        .addParameter(s.iterableParam("entities").addAnnotation(notNull).build())
        //.addParameter(s.collectionParam("entities").addAnnotation(notNull).build())
        .addAnnotation(javersAuditable)
        .addJavadoc(Seq(
          "Saves all given entities.",
          "",
          " @param <S> saved impl",
          "@param entities must not be { @literal null}.",
          "@return the saved entities will never be { @literal null}.",
          "@throws IllegalArgumentException in case the given model is { @literal null}.",
        ).mkString("\n")
        ).build()
    )
    ////////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // void flush();
    classBuilder.addMethod(
      MethodSpec.methodBuilder("flush")
        .addModifiers(PUBLIC, ABSTRACT)
        .addJavadoc(Seq(
          "Flushes all pending changes to the database.",
        ).mkString("\n")
        ).build()
    )
    ////////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    //  <S extends T> S saveAndFlush(@NotNull S entity);
    classBuilder.addMethod(
      MethodSpec.methodBuilder("saveAndFlush")
        .addModifiers(PUBLIC, ABSTRACT)
        .addTypeVariable(s.typeVar().withBounds(t.typeVar()))
        .returns(s.typeVar())
        .addParameter(s.param("entity", notNull).build())
        .addAnnotation(javersAuditable)
        .addJavadoc(
          Seq(
            "Saves an model and flushes changes instantly.",
            "",
            "@param <S> an extension of BaseEntity",
            "@param entity the model instance to save",
            "@return the saved model"
          ).mkString("\n")
        ).build()
    )
    ////////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // Optional<T> findById(@NotNull ID id);
    classBuilder.addMethod(
      MethodSpec.methodBuilder("findById")
        .addModifiers(PUBLIC, ABSTRACT)
        //.addTypeVariable(id.typeVar().withBounds(t.typeVar()))
        .returns(t.optionalType())
        .addParameter(id.param("id", notNull).build())
        .addJavadoc(
          Seq(
            "Retrieves an model by its id.",
            "",
            "@param id must not be {@literal null}.",
            "@return the model with the given id or {@literal Optional#empty()} if none found",
            "@throws IllegalArgumentException if {@code id} is {@literal null}."
          ).mkString("\n")
        ).build()
    )
    ////////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    //T getOne(@NotNull ID id);
    classBuilder.addMethod(
      MethodSpec.methodBuilder("getOne")
        .addModifiers(PUBLIC, ABSTRACT)
        .returns(t.typeVar())
        .addParameter(id.param("id", notNull).build())
        .addJavadoc(
          Seq(
            "Returns a reference to the model with the given identifier.",
            "",
            "@param id must not be {@literal null}.",
            "@return a reference to the model with the given identifier.",
            "@throws javax.persistence.EntityNotFoundException if no model exists for given {@code id}.",
            "@see javax.persistence.EntityManager#getReference(Class, Object)"
          ).mkString("\n")
        ).build()
    )
    ////////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    //boolean existsById(@NotNull ID id);
    classBuilder.addMethod(
      MethodSpec.methodBuilder("existsById")
        .addModifiers(PUBLIC, ABSTRACT)
        .returns(TypeName.BOOLEAN)
        .addParameter(id.param("id", notNull).build())
        .addJavadoc(
          Seq(
            "Returns whether an model with the given id exists.",
            "",
            "@param id must not be {@literal null}.",
            "@return {@literal true} if an model with the given id exists, {@literal false} otherwise.",
            "@throws IllegalArgumentException if {@code id} is {@literal null}."
          ).mkString("\n")
        ).build()
    )
    ////////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    //Slice<T> findAll(@NotNull Pageable pageable);
    classBuilder.addMethod(
      MethodSpec.methodBuilder("findAll")
        .addModifiers(PUBLIC, ABSTRACT)
        .returns(t.sliceType())
        .addParameter(pageable.param("pageable", notNull).build())
        .addJavadoc(
          Seq(
            "Provides the ability to {@link Slice} (paging without the expensive",
            "total count) through findAll results with a given {@link Pageable}.",
            "",
            "@param pageable a pageing object",
            "@return a page of T"
          ).mkString("\n")
        ).build()
    )
    ////////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    //List<T> findAllById(@NotNull Iterable<ID> ids);
    classBuilder.addMethod(
      MethodSpec.methodBuilder("findAllById")
        .addModifiers(PUBLIC, ABSTRACT)
        .returns(t.listType())
        .addParameter(id.iterableParam("ids", notNull).build())
        .addJavadoc(
          Seq(
            "Returns all instances of the type with the given IDs.",
            "",
            "@param ids an Iterable of ids to find entities for",
            "@return a list of T"
          ).mkString("\n")
        ).build()
    )
    ////////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    //long count();
    classBuilder.addMethod(
      MethodSpec.methodBuilder("count")
        .addModifiers(PUBLIC, ABSTRACT)
        .returns(TypeName.LONG)
        .addJavadoc(
          Seq(
            "Returns the number of entities available.",
            "",
            "@return the number of entities"
          ).mkString("\n")
        ).build()
    )

    classBuilder
  }


}
