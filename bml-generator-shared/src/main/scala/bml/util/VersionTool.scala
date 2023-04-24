package bml.util

import io.apibuilder.spec.v0.models.Service

object VersionTool {

  def majorVersion(service: Service): String = service.version.split("\\.")(0)

}
