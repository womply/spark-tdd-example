
name := "transaction-example"

version := "1.0"

scalaVersion := "2.10.4"

libraryDependencies ++= {
  val phantomVersion = "1.4.0"

  Seq(
    "org.apache.spark"       %% "spark-core"                % "1.2.0" % "provided" intransitive(),
    "com.datastax.spark"     %% "spark-cassandra-connector" % "1.1.1",
    "com.typesafe"            % "config"                    % "1.2.1",
    "com.websudos"           %% "phantom-dsl"               % phantomVersion % "test",
    "com.websudos"           %% "phantom-testing"           % phantomVersion % "test",
    "com.websudos"           %% "phantom-zookeeper"         % phantomVersion % "test"
  )
}

dependencyOverrides += "org.apache.zookeeper" % "zookeeper" % "3.3.4"

