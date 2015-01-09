
name := "transaction-example"

version := "1.0"

scalaVersion := "2.10.4"

libraryDependencies ++= {
  val phantomVersion = "1.4.0"

  Seq(
    "org.apache.spark"       %% "spark-core"        % "1.1.1"        % "provided",
    "com.datastax.spark"     %% "spark-cassandra-connector" % "1.1.1",
    "org.apache.cassandra"    % "cassandra-all"     % "2.1.2"        % "test",
    "com.websudos"           %% "phantom-dsl"       % phantomVersion % "test",
    "com.websudos"           %% "phantom-testing"   % phantomVersion % "test",
    "com.websudos"           %% "phantom-zookeeper" % phantomVersion % "test",

    // Overrides necessary for all dependencies to play nice
    "org.apache.zookeeper"    % "zookeeper"         % "3.3.4"        % "test",
    "com.google.guava"        % "guava"             % "16.0",
    "log4j"                   % "log4j"             % "1.2.15"
      exclude("com.sun.jdmk", "jmxtools") exclude("com.sun.jmx", "jmxri")
  )
}

// Needed to get Phantom DSL Zookeeper Connector working
dependencyOverrides += "org.apache.zookeeper" % "zookeeper" % "3.3.4"

net.virtualvoid.sbt.graph.Plugin.graphSettings

