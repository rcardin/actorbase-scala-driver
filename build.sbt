name := "actorbase-scala-driver"

version := "0.1"

scalaVersion := "2.12.3"

libraryDependencies ++= Seq(
  "com.typesafe.akka"  %% "akka-actor"    % "2.5.4",
  "org.scalatest"      %% "scalatest"     % "3.0.0"   % "test",
  "com.typesafe.akka"  %% "akka-testkit"  % "2.5.4"  % "test"
)
        