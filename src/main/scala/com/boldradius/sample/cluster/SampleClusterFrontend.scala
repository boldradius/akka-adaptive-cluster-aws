package com.boldradius.sample.cluster

import scala.concurrent.duration._
import com.typesafe.config.ConfigFactory
import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorSystem
import akka.actor.Props
import akka.routing.FromConfig
import akka.actor.ReceiveTimeout
import akka.http.Http
import akka.http.server.Directives._
import akka.stream.FlowMaterializer
import scala.concurrent.ExecutionContext.Implicits.global
import akka.util.Timeout
import akka.actor.ActorRef
import akka.actor.ActorContext
import akka.cluster.Cluster
import scala.language.postfixOps
import SampleClusterDefs._

trait BackendRouterProvider {
  def newBackendRouter(context: ActorContext): ActorRef
}

trait BackendRouterProviderImpl extends BackendRouterProvider {
  def newBackendRouter(context: ActorContext) = context.actorOf(FromConfig.props(), name = BackendRouterName)
}

class SampleClusterFrontend() extends Actor with ActorLogging {

  this: BackendRouterProvider =>

  implicit val timeout = Timeout(5 seconds)

  val backendRouter = newBackendRouter(context)

  def receive = {
    case c: GetActorMetrics =>
      backendRouter.forward(c)
    case ReceiveTimeout =>
      log.info("Timeout")
  }
}

object SampleClusterFrontend extends SampleClusterHttpRoute {
  def main(args: Array[String]): Unit = {

    val host = if (args.isEmpty) "localhost" else args(0)
    val port = if (args.isEmpty || args.length == 1) 8080 else args(1).toInt

    val config = ConfigFactory.parseString(s"akka.cluster.roles = [$FrontendActorRoleName]")
      .withFallback(ConfigFactory.load("application"))

    implicit val system = ActorSystem(ActorSystemName, config)
    system.log.info("Sample Cluster System will start when there is a minimum number of backend members in the cluster.")

    Cluster(system) registerOnMemberUp {
      val frontEnd = system.actorOf(Props(new SampleClusterFrontend with BackendRouterProviderImpl), name = FrontendActorName)
      implicit val materializer = FlowMaterializer()
      Http().bind(host, port) startHandlingWith {
        logRequestResult("A") {
          route(frontEnd)
        }
      }

    }

  }
}