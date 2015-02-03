package com.boldradius.sample.cluster

import scala.language.implicitConversions

object SampleClusterDefs {

  case class GetActorMetrics()
  
  trait MetricsT
  case class NoMetricsPresent() extends MetricsT
  case class Metrics(address: String, timestamp: Long, cpu: Double) extends MetricsT
  
  case class SimpleClusterResponse(status: String, details: ResponseDetails)
  
  sealed trait ResponseDetails
  case class MetricsDetails(metrics: MetricsT) extends ResponseDetails
  implicit def Metrics2ResponseDetails(metrics: MetricsT) = MetricsDetails(metrics)
  case class ErrorDetails(message: String) extends ResponseDetails
  implicit def String2ResponseDetails(error: Throwable) = ErrorDetails(error.getMessage())

  val ActorSystemName = "ClusterSystem"
  val FrontendActorName = "sampleClusterFrontend"
  val FrontendActorRoleName = "frontend"
  val BackendRouterName = "sampleClusterBackendRouter"
  val BackendActorName = "sampleClusterBackend"
  val BackendActorRoleName = "backend"
}