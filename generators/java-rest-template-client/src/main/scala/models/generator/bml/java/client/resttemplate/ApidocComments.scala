package models.generator.bml.java.client.resttemplate


import lib.Constants

class ApidocComments(version: String, userAgent: Option[String]) {

  private val elements = Seq(
    Some(s"Generated by API Builder - ${Constants.ApibuilderUrl}"),
    Some(s"Service version: $version"),
    Some("Do Not modify this or any generated files. They will be overwritten on update."),
    userAgent
  ).flatten
  val forClassFile: String = elements.mkString("", "\n", "\n")
}