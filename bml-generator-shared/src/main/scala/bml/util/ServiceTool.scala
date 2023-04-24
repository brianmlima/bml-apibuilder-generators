package bml.util

import bml.util.java.JavaPojoUtil
import bml.util.spring.SpringVersion.SpringVersion
import io.apibuilder.spec.v0.models.Service

object ServiceTool {

  def versionedPrefix(service: Service) = JavaPojoUtil.toClassName(s"${service.name}-v${VersionTool.majorVersion(service)}")

  def prefix(springVersion: SpringVersion, service: Service) = {
    springVersion match {
      case bml.util.spring.SpringVersion.SIX => JavaPojoUtil.toClassName(s"${service.name}-v${VersionTool.majorVersion(service)}")
      case bml.util.spring.SpringVersion.FIVE => ""
    }
  }


}
