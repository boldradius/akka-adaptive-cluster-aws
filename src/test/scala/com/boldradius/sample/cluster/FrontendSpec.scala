package com.boldradius.sample.cluster

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.BeforeAndAfterAll
import org.scalatest.FlatSpec
import org.scalatest.Matchers
import akka.testkit.TestActorRef
import akka.testkit.TestKit
import akka.actor.ActorSystem
import org.scalatest.FlatSpecLike
import akka.pattern.ask
import scala.concurrent.duration._
import akka.util.Timeout
import scala.util.Success
import scala.util.Failure
import akka.actor.Props
import akka.actor.ActorContext
import akka.testkit.TestProbe
import akka.actor.ActorRef
import akka.actor.Actor
import SampleClusterDefs._

@RunWith(classOf[JUnitRunner])
class FrontendSpec
  extends TestKit(ActorSystem("TestSystem"))
  with FlatSpecLike
  with Matchers
  with BeforeAndAfterAll {

  override def afterAll() {
    system.shutdown()
  }

  class TestSampleClusterBackend(target: ActorRef) extends Actor {
    def receive = {
      case c: GetActorMetrics => target forward c
    }
  }

  trait TestBackend {
    val backendProbe = TestProbe()
    trait BackendRouterProviderTest extends BackendRouterProvider {
      def newBackendRouter(context: ActorContext) = context.actorOf(Props(new TestSampleClusterBackend(backendProbe.ref)))
    }
  }

  "GetActorMetrics message on frontend " should "triggers GetActorMetrics message on backend" in new TestBackend {

    val frontend = TestActorRef(Props(new SampleClusterFrontend with BackendRouterProviderTest))

    frontend ! GetActorMetrics()

    backendProbe.expectMsg(GetActorMetrics())

  }
}