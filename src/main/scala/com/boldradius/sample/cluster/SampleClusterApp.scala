package com.boldradius.sample.cluster

import com.typesafe.config.ConfigFactory

object SampleClusterApp {
  def main(args: Array[String]): Unit = {

    // starting 3 backend nodes and 1 frontend node
    SampleClusterBackend.main(Seq("2551").toArray)
    SampleClusterBackend.main(Seq("2552").toArray)
    SampleClusterBackend.main(Array.empty)
    SampleClusterFrontend.main(Array.empty)
  }

}