package com.boldradius.sample.cluster

import akka.http.marshalling._
import akka.http.model._
import akka.http.server.Directives._
import akka.http.server.Route
import akka.stream.FlowMaterializer
import akka.util.Timeout
import akka.pattern.ask
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.language.postfixOps
import akka.actor.ActorRef
import SampleClusterDefs._
import akka.cluster.NodeMetrics
import scala.util.Success
import scala.util.Failure
import com.boldradius.util.Json4sSupport

trait SampleClusterHttpRoute extends Json4sSupport {

  implicit val timeout = Timeout(3 seconds)

  def route(frontendActor: ActorRef)(implicit fm: FlowMaterializer): Route = {

    get {
      path("cluster") {
        onComplete((frontendActor ? GetActorMetrics()).mapTo[MetricsT]) {
          case Success(metrics) =>
            complete(SimpleClusterResponse("success", metrics))
          case Failure(f) =>
            complete(SimpleClusterResponse("error", f))
        }
      }
    }
  }
}

