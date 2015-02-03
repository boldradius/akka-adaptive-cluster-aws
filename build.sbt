import com.github.retronym.SbtOneJar

val akkaVersion = "2.3.8"
val akkaHttp =  "1.0-M2"

val project = Project(
  id = "akka-adaptive-cluster-aws",
  base = file("."),
  settings = Project.defaultSettings ++ SbtOneJar.oneJarSettings ++ Seq(
    name := "akka-adaptive-cluster-aws",
    version := "2.3.8",
    scalaVersion := "2.11.4",
    scalacOptions in Compile ++= Seq("-encoding", "UTF-8", "-target:jvm-1.6", "-deprecation", "-feature", "-unchecked", "-Xlog-reflective-calls", "-Xlint"),
    javacOptions in Compile ++= Seq("-source", "1.6", "-target", "1.6", "-Xlint:unchecked", "-Xlint:deprecation"),
    libraryDependencies ++= Seq(
      "com.typesafe.akka"	%% "akka-cluster" 							% akkaVersion,
      "com.typesafe.akka"   % "akka-http-experimental_2.11" 			% akkaHttp,
      "org.json4s"          %% "json4s-native"                          % "3.2.11",
      "org.fusesource" 		% "sigar" 									% "1.6.4",
      "junit" 			    % "junit" 					    			% "4.8.1" 		% "test",
      "org.scalatest"       %%  "scalatest"          					% "2.2.1"  		% "test",
      "com.typesafe.akka"   % "akka-http-testkit-experimental_2.11"     % akkaHttp 		% "test",
      "com.typesafe.akka"   %%  "akka-testkit"              			% akkaVersion   % "test"),
    javaOptions in run ++= Seq(
      "-Djava.library.path=./sigar",
      "-Xms128m", "-Xmx1024m"),
    javaOptions in test ++= Seq(
      "-Djava.library.path=./sigar"),
    Keys.fork in run := true,  
    // disable parallel tests
    parallelExecution in Test := false
  )
) 
