package models.generator.java.persistence.sql

import com.squareup.javapoet._

import scala.collection.JavaConverters._

case class TypeHelper(packagePath: String, className: String) {
  def typeVar() = TypeVariableName.get(className)

  def listType() = ParameterizedTypeName.get(ClassName.get("java.util", "List"), clazzName())

  def collectionType() = ParameterizedTypeName.get(ClassName.get("java.util", "List"), clazzName())

  def iterableType() = ParameterizedTypeName.get(ClassName.get("java.util", "Iterable"), clazzName())

  def optionalType() = ParameterizedTypeName.get(ClassName.get("java.util", "Optional"), clazzName())

  def sliceType() = ParameterizedTypeName.get(ClassName.get("org.springframework.data.domain", "Slice"), clazzName())

  def responseEntityType() = ParameterizedTypeName.get(ClassName.get("org.springframework.http", "ResponseEntity"), clazzName())

  def param(parmaName: String) = ParameterSpec.builder(typeVar(), parmaName)

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
