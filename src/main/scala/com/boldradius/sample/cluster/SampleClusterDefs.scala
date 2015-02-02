package com.boldradius.sample.cluster

import scala.language.implicitConversions

object SampleClusterDefs {

  case class GetActorMetrics()

  case class ActorMetrics(id: String)
  
  case class Metrics(timestamp: Long, cpu: Double)
  
  case class Response(status: String, details: ResponseDetails)
  
  sealed trait ResponseDetails
  case class MetricsDetails(metrics: Metrics) extends ResponseDetails
  implicit def Metrics2ResponseDetails(metrics: Metrics) = MetricsDetails(metrics)
  case class ErrorDetails(message: String) extends ResponseDetails
  implicit def String2ResponseDetails(error: Throwable) = ErrorDetails(error.getMessage())

  val ActorSystemName = "ClusterSystem"
  val FrontendActorName = "sampleClusterFrontend"
  val FrontendActorRoleName = "frontend"
  val BackendRouterName = "sampleClusterBackendRouter"
  val BackendActorName = "sampleClusterBackend"
  val BackendActorRoleName = "backend"
}