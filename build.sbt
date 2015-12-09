name := """cgif"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.6"

resolvers += "jitpack" at "https://jitpack.io"

libraryDependencies ++= Seq(
  javaJdbc,
  cache,
  javaWs,
  "com.github.jtsay362" % "chesspresso" % "36843f6",
  "com.kitfox.svg" % "svg-salamander" % "1.0",
  "com.adrianhurt" % "play-bootstrap3_2.11" % "0.4.4-P24",
  "org.webjars" % "font-awesome" % "4.4.0",
  "com.kenshoo" %% "metrics-play" % "2.4.0_0.4.1"
)

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator