
name := "transaction-example"

version := "1.0"

scalaVersion := "2.10.4"

libraryDependencies ++= {
  val phantomVersion = "1.4.0"

  Seq(
    "org.apache.spark"       %% "spark-core"        % "1.0.0"        % "provided",
    "com.typesafe"            % "config"            % "1.2.1",
    "com.websudos"           %% "phantom-dsl"       % phantomVersion % "test",
    "com.websudos"           %% "phantom-testing"   % phantomVersion % "test",
    "com.websudos"           %% "phantom-zookeeper" % phantomVersion % "test",
    "org.apache.cassandra"    % "cassandra-all"     % "2.0.8"        % "test" force(),
    "org.apache.zookeeper"    % "zookeeper"         % "3.3.4"        % "test"
  )
}

ivyXML :=
  <dependencies>
    <exclude org="org.slf4j" name="slf4j-jdk14" />
  </dependencies>

dependencyOverrides += "org.apache.zookeeper" % "zookeeper" % "3.3.4"

net.virtualvoid.sbt.graph.Plugin.graphSettings

