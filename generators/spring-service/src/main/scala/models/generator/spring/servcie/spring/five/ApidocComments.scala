package models.generator.spring.servcie.spring.five

import lib.Constants

class ApidocComments(version: String, userAgent: Option[String]) {

  private val elements = Seq(
    Some(s"Generated by API Builder - ${Constants.ApibuilderUrl}"),
    Some(s"Service version: $version"),
    userAgent
  ).flatten
  val forClassFile: String = elements.mkString("", "\n", "\n")
}
