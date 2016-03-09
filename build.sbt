// settings for sbt-dependency
filterScalaLibrary := false

organization  := "ro.mihneabaia.api"

version       := "0.1"

scalaVersion  := "2.11.6"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

libraryDependencies ++= {
  val akkaVersion = "2.3.9"
  val sprayVersion = "1.3.3"
  val slickVersion = "3.1.1"

  Seq(
    "io.spray"            %%  "spray-can"             % sprayVersion,
    "io.spray"            %%  "spray-routing"         % sprayVersion,
    "io.spray"            %%  "spray-json"            % "1.3.2",
    "com.typesafe.akka"   %%  "akka-actor"            % akkaVersion,
    "com.typesafe.slick"  %%  "slick"                 % slickVersion,
    "com.typesafe.slick"  %%  "slick-hikaricp"        % slickVersion,
    "ch.qos.logback"       %  "logback-classic"       % "1.0.9",
    "mysql"                %  "mysql-connector-java"  % "5.1.38",
    "io.spray"            %%  "spray-testkit"         % sprayVersion  % "test",
    "com.typesafe.akka"   %%  "akka-testkit"          % akkaVersion   % "test",
    "org.specs2"          %%  "specs2-core"           % "2.3.11"      % "test"
  )
}

Revolver.settings
