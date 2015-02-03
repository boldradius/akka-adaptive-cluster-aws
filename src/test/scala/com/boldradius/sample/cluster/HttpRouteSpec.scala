package com.boldradius.sample.cluster

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import akka.actor.{ Actor, ActorRef, Props }
import akka.http.model._
import akka.http.testkit._
import org.scalatest._
import scala.concurrent.duration._
import SampleClusterDefs._
import akka.cluster.NodeMetrics
import akka.actor.Address
import akka.testkit.TestActorRef

@RunWith(classOf[JUnitRunner])
class HttpRouteSpec extends FlatSpecLike
  with ScalatestRouteTest
  with MustMatchers
  with BeforeAndAfterAll
  with SampleClusterHttpRoute {

  override def afterAll() {
    system.shutdown()
  }

  /* Should be set!
  ** Otherwise returns:
  ** 'Request was neither completed nor rejected within 1 second'.
  */
  implicit val routeTestTimeout = RouteTestTimeout(5.second)

  trait TestFrontend {
    val testResp = Metrics(address = "test", timestamp = 1L, cpu = 0.1)
    val testFrontendActor = TestActorRef(Props(new Actor {
      def receive = {
        case c: GetActorMetrics => sender() ! testResp
      }
    }))
  }

  "/cluster " should "return metrics" in new TestFrontend {

    Get(Uri("/cluster")) ~> route(testFrontendActor) ~> check {
      
      responseAs[Any] must be(Map("status" -> "success", "details" -> Map("metrics" -> Map("address" -> "test", "timestamp" -> 1, "cpu" -> 0.1))))
    }

  }
}
