package bml.util
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.squareup.javapoet.{AnnotationSpec, CodeBlock, TypeSpec}
import javax.validation.constraints.{Pattern, Size}
import lombok.experimental.Accessors
import io.apibuilder.spec.v0.models.Attribute
import javax.persistence.Table
import lombok.EqualsAndHashCode
import org.springframework.validation.annotation.Validated

object AnotationUtil {

  def fluentAccessor = AnnotationSpec
    .builder(classOf[Accessors])
    .addMember("fluent",CodeBlock.builder().add("true").build).build()

  def notNull = classOf[javax.validation.constraints.NotNull]


  def size(min: Int,max:Int):AnnotationSpec ={
    AnnotationSpec.builder(classOf[Size])
      .addMember("min","$L",new Integer(min))
      .addMember("max","$L",new Integer(max))
      .build()
  }

  def size(attribute: Attribute): AnnotationSpec ={
    size((attribute.value \ "min").as[Int],(attribute.value \ "max").as[Int])
  }

  def pattern(regexp: String):AnnotationSpec ={
      AnnotationSpec.builder(classOf[Pattern])
        .addMember("regexp","$S",regexp)
        .build()
  }

  def pattern(attribute: Attribute):AnnotationSpec ={
    pattern((attribute.value \ "regexp").as[String])
  }

  def table(tableName: String):AnnotationSpec ={
    AnnotationSpec.builder(classOf[Table])
      .addMember("name","$S",tableName)
      .build()
  }

  def validated():AnnotationSpec = AnnotationSpec.builder(classOf[Validated]).build()

  def equalsAndHashCode(onlyExplicitlyIncluded : Boolean): AnnotationSpec ={
      AnnotationSpec.builder(classOf[EqualsAndHashCode]).addMember("onlyExplicitlyIncluded","$L",onlyExplicitlyIncluded.toString).build()
    }


  def addDataClassAnnotations(builder: TypeSpec.Builder) {
    builder.addAnnotation(AnnotationSpec.builder(classOf[Accessors])
      .addMember("fluent", CodeBlock.builder().add("true").build).build())
      .addAnnotation(classOf[lombok.Builder])
      .addAnnotation(classOf[lombok.AllArgsConstructor])
      .addAnnotation(classOf[lombok.NoArgsConstructor])
      .addAnnotation(
        AnnotationSpec.builder(classOf[JsonIgnoreProperties])
          .addMember("ignoreUnknown", CodeBlock.builder().add("true").build).build()
      )
  }
}