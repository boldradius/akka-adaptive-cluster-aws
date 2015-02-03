package com.boldradius.sample.cluster

import scala.concurrent.Future
import com.typesafe.config.ConfigFactory
import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorSystem
import akka.actor.Props
import scala.util.Success
import scala.util.Failure
import akka.pattern.pipe
import akka.cluster.Cluster
import akka.cluster.ClusterEvent.ClusterMetricsChanged
import akka.cluster.ClusterEvent.CurrentClusterState
import akka.cluster.NodeMetrics
import SampleClusterDefs._

class SampleClusterBackend extends Actor with ActorLogging {

  val selfAddress = Cluster(context.system).selfAddress
  
  override def preStart(): Unit =
    Cluster(context.system).subscribe(self, classOf[ClusterMetricsChanged])
  override def postStop(): Unit =
    Cluster(context.system).unsubscribe(self)
    
    
  def receive2(nodeMetrics: NodeMetrics): Receive = {
    case c: GetActorMetrics =>
      sender() ! Metrics(address = nodeMetrics.address.toString, timestamp = nodeMetrics.timestamp, cpu = nodeMetrics.metric("cpu-combined").map(_.value.doubleValue()).getOrElse(-1.0))
    case ClusterMetricsChanged(clusterMetrics) =>
      clusterMetrics.filter(_.address == selfAddress) foreach { newNodeMetrics =>
        context.become(receive2(newNodeMetrics))
      }
    case state: CurrentClusterState => // ignore
  }  

  def receive = {
    case c: GetActorMetrics => // didn't get first metrics
      sender() ! NoMetricsPresent
    case ClusterMetricsChanged(clusterMetrics) =>
      clusterMetrics.filter(_.address == selfAddress) foreach { newNodeMetrics =>
        context.become(receive2(newNodeMetrics))
      }
    case state: CurrentClusterState => // ignore
  }
}

object SampleClusterBackend {
  def main(args: Array[String]): Unit = {
    val port = if (args.isEmpty) "0" else args(0)
    val config = ConfigFactory.parseString(s"akka.remote.netty.tcp.port=$port").
      withFallback(ConfigFactory.parseString(s"akka.cluster.roles = [$BackendActorRoleName]"))
      .withFallback(ConfigFactory.load("application"))

    val system = ActorSystem(ActorSystemName, config)
    system.actorOf(Props(classOf[SampleClusterBackend]), name = BackendActorName)
  }
}