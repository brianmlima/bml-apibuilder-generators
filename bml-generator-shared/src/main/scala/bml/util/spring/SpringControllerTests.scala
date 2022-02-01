package bml.util.spring

import java.util.UUID

import bml.util.java.ClassNames.JunitTypes.assertThrows
import bml.util.java.ClassNames.LombokTypes.`val`
import bml.util.java.ClassNames.SpringTypes.MockitoTypes._
import bml.util.java.ClassNames.SpringTypes.{I_AM_A_TEAPOT, ResponseEntity}
import bml.util.java.ClassNames.{JavaTypes, JunitTypes}
import bml.util.java.JavaPojoUtil
import bml.util.spring.SpringControllers.{toControllerName, toControllerOperationName}
import bml.util.spring.SpringServices.{responseCodeToString, responseToContainerMemberName, toResponseSubTypeCLassName, toServiceClassName}
import bml.util.{GeneratorFSUtil, NameSpaces}
import com.squareup.javapoet.{CodeBlock, MethodSpec, TypeSpec}
import io.apibuilder.generator.v0.models.File
import io.apibuilder.spec.v0.models.{Operation, Resource, Response, Service}
import javax.lang.model.element.Modifier.PUBLIC

import scala.collection.JavaConverters._

/**
 * Generates any tests it can for generated spring controllers. This helps evolve the service generator and helps with
 * code quality gates because you want to ensure coverage of controllers.
 */
object SpringControllerTests {

  /**
   * Entry point for this generation class.
   *
   * @param nameSpaces The namespaces object for the generation system.
   * @param service    The service we are generating for.
   * @return A sequence of generated files.
   */
  def generate(nameSpaces: NameSpaces, service: Service): Seq[File] = {
    service.resources.map(
      resource =>
        resource.operations.map(
          operation =>
            generateOperationMockTests(nameSpaces, service, resource, operation)
        ).flatten
    ).flatten
  }

  def toTestClassName(resource: Resource, operation: Operation): String = {
    toControllerName(resource) + "Operation" + JavaPojoUtil.toClassName(toControllerOperationName(operation)) + "Tests"
  }

  /**
   * Generates a test class for every controller operation.
   */
  private def generateOperationMockTests(nameSpaces: NameSpaces, service: Service, resource: Resource, operation: Operation): Seq[File] = {
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //Common ClassName objects
    //The name of the class we are generating.
    val testOperationClassName = toTestClassName(resource, operation)
    //The service we are mocking.
    val serviceClassName = toServiceClassName(nameSpaces, resource)
    //This is the class we have to mock for a return type for the service method we are testing.
    val operationResponseClassName = toResponseSubTypeCLassName(nameSpaces, operation)
    //The Controller that contains the code we are testing.
    val controllerClassName = toControllerName(nameSpaces, resource)

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //Field names we use more than once.
    //The mock service field name.
    val serviceParamName = "service"
    //The controller field name.
    val controllerParamName = "controller"
    //The mock response field name.
    val mockResponseParamName = "mockResponse"

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //Static imports we use in the generated class to increase readability.
    val staticImports = Seq(
      assertThrows.staticImport,
      spy.staticImport,
      mock.staticImport,
      when.staticImport,
      any.staticImport,
      anyString.staticImport,
      I_AM_A_TEAPOT.staticImport
    )

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //Helper methods.

    //Figures out how many methods params we have and returns a CodeBlock so we dont have to calculate them inline
    def argumentCodeBlocks(): Seq[CodeBlock] = {
      val any = CodeBlock.of("any()")
      operation.parameters.map(o => any) ++ operation.body.map(o => any)
    }

    //Mock the service call.
    def mockServiceCallCodeBlock(): CodeBlock = {
      CodeBlock.builder()
        .add("when($L.$L(", serviceParamName, toControllerOperationName(operation))
        .add(CodeBlock.join(argumentCodeBlocks().asJava, ","))
        .add("))")
        .add(".thenReturn($L)", mockResponseParamName)
        .build()
    }


    //Mock the content accessor method in the service method return class.
    def mockResponseTypeContent(response: Response): CodeBlock = {
      val returnBlock = CodeBlock.builder()
        .add("$T.status($L)", ResponseEntity, I_AM_A_TEAPOT.methodName)
      //Handle responses that have no body
      if (response.`type` == "unit") {
        returnBlock.add(".build()")
      } else if (response.`type` == "uuid") {
        returnBlock.add(".body($T.randomUUID())", JavaTypes.UUID)
      } else {
        //Handle responses that have a body
        if (JavaPojoUtil.isListOfModeslType(service, response.`type`)) {
          val innerClassName = JavaPojoUtil.getListType(response.`type`, nameSpaces.model)
          returnBlock.add(".body(new $T<$T>())", JavaTypes.LinkedList, innerClassName)
        } else if (JavaPojoUtil.isParameterMap(response.`type`)) {
          val innerClassName = JavaPojoUtil.toClassName(nameSpaces.model, JavaPojoUtil.getMapType(response.`type`))
          returnBlock.add(".body(new $T<$T,$T>())", JavaTypes.LinkedHashMap, JavaTypes.String, innerClassName)
        }
        else {
          val responseClassName = JavaPojoUtil.dataTypeFromField(service, response.`type`, nameSpaces.model)
          if (response.`type` == "string") {
            returnBlock.add(".body(\"I_AM_A_TEAPOT\")")
          } else {
            returnBlock.add(".body(mock($T.class))", responseClassName)
          }
        }
      }
      CodeBlock.builder()
        .add("when($L.$L())", mockResponseParamName, responseToContainerMemberName(response))
        .add(".thenReturn($L)", returnBlock.build())
        .build()
    }

    //Generates the assertThrows section of test methods.
    def generateAssertThrows(): CodeBlock = {
      val nullCodeBlock = CodeBlock.of("null")
      //Since we are testing any input including nulls we just pass null in the test to make it easier to generate.
      val params = operation.parameters.map(o => nullCodeBlock) ++ operation.body.map(o => nullCodeBlock)

      CodeBlock.builder()
        .add(
          "assertThrows($T.class, () -> $L.$L(",
          ApiImplementationException.getClassName(nameSpaces),
          controllerParamName,
          toControllerOperationName(operation)
        )
        //        .add(CodeBlock.join((params ++ body).asJava, ","))
        .add(CodeBlock.join((params).asJava, ","))
        .add("))").build()
    }

    /** Generates a test method for the senario where a service method returns a response object where all of the
     * allowable responses are not present.
     */
    def generateTestEmptyServiceResponse(): MethodSpec = {
      MethodSpec.methodBuilder("testControllerEmptyResponse")
        .addAnnotation(JunitTypes.Test)
        //Mock the service
        .addStatement("$T $L = mock($T.class)", `val`, serviceParamName, serviceClassName)
        //Instance the controller
        .addStatement("$T $L = new $T($L)", `val`, controllerParamName, controllerClassName, serviceParamName)
        //Mock the service return object
        .addStatement("$T $L = mock($T.class)", `val`, mockResponseParamName, operationResponseClassName)
        //Mock the service call
        .addStatement(mockServiceCallCodeBlock())
        //Run the test
        .addStatement(generateAssertThrows())
        .build()
    }

    //Generate the test class javadoc.
    def generateClassJavaDoc(): CodeBlock = {
      val codeBlock = CodeBlock.builder()
      Seq[String](
        s"Tests {@link ${controllerClassName.simpleName()}} code that throws an exception if the {@link ${serviceClassName.simpleName()}}",
        s"is not implemented according to the api specification. We generate these tests to eliminate the duplication of work that would occur if we did not."
      ).foreach(
        line =>
          codeBlock.add(line)
      )
      codeBlock.build()
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //Generate the test class.
    val implBuilder = TypeSpec.classBuilder(testOperationClassName).addModifiers(PUBLIC)
      .addJavadoc(generateClassJavaDoc())
      //Add the test methods.
      .addMethods(
        //For each response in the specification
        operation.responses.map(
          response =>
            //Create a test method
            MethodSpec.methodBuilder(toControllerOperationName(operation) + responseCodeToString(response.code))
              .addAnnotation(JunitTypes.Test)
              //Mock the service
              .addStatement("$T $L = mock($T.class)", `val`, serviceParamName, serviceClassName)
              //instance the controller
              .addStatement("$T $L = new $T($L)", `val`, controllerParamName, controllerClassName, serviceParamName)
              //Mock the service return object
              .addStatement("$T $L = mock($T.class)", `val`, mockResponseParamName, operationResponseClassName)
              //Mock the specific expected mock content
              .addStatement(mockResponseTypeContent(response))
              //Mock the service call
              .addStatement(mockServiceCallCodeBlock())
              //Run the test
              .addStatement(generateAssertThrows())
              .build()
        ).asJava
      )
      .addMethod(generateTestEmptyServiceResponse())

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //Return a sequence of generated files.
    Seq(GeneratorFSUtil.makeFile(testOperationClassName, nameSpaces.controller, implBuilder, staticImports: _*))
  }


}
