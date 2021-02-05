import xerial.sbt.Sonatype._

lazy val Versions = Map(
  "kind-projector" -> "0.11.3",
  "cats"           -> "2.0.0",
  "jena"           -> "3.17.0",
  "scalatest"      -> "3.2.3",
  "fastparse"      -> "2.1.2",
  "cats"           -> "2.0.0",
  "scala212"       -> "2.12.12",
  "scala211"       -> "2.11.12"
)

inThisBuild(List(
  organization := "com.github.gsk-aiops",
  homepage := Some(url("https://github.com/gsk-aiops/bellman-algebra-parser")),
  licenses := Seq("APL2" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt")),
  developers := List(
    Developer(
      id = "JNKHunter",
      name = "John Hunter",
      email = "johnhuntergskatgmail.com",
      url = url("https://gsk.com")
    )
  )
))

lazy val buildSettings = Seq(
  scalaVersion := Versions("scala212"),
  crossScalaVersions :=  List(Versions("scala211"), Versions("scala212")),
  sonatypeProjectHosting := Some(GitHubHosting("gsk-aiops", "bellman-algebra-parser", "johnhuntergskatgmail.com")),
  sonatypeProfileName := "com.github.gsk-aiops",
  artifactName := { (sv: ScalaVersion, module: ModuleID, artifact: Artifact) =>
    artifact.name + "_" + sv.binary + "-" + module.revision + "." + artifact.extension
  },
  scalastyleFailOnWarning := true
)

lazy val noPublishSettings = Seq(
  publish := {},
  publishLocal := {},
  publishArtifact := false,
  skip in publish := true
)

lazy val compilerPlugins = Seq(
  libraryDependencies ++= Seq(
    compilerPlugin("org.typelevel" %% "kind-projector" % Versions("kind-projector") cross CrossVersion.full)
  )
)

lazy val commonDependencies = Seq(
  libraryDependencies ++= Seq(
    "org.scalatest" %% "scalatest" % Versions("scalatest") % Test,
  )
)

lazy val root = project
  .in(file("."))
  .settings(buildSettings)
  .settings(noPublishSettings)
  .dependsOn(`bellman-algebra-parser`, `bellman-spark-engine`)
  .aggregate(`bellman-algebra-parser`, `bellman-spark-engine`)

lazy val `bellman-algebra-parser` = project
  .in(file("modules/parser"))
  .settings(moduleName := "bellman-algebra-parser")
  .settings(buildSettings)
  .settings(commonDependencies)
  .settings(compilerPlugins)
  .settings(
    libraryDependencies ++= Seq(
      "org.apache.jena" % "apache-jena-libs" % Versions("jena") pomOnly(),
      "com.lihaoyi" %% "fastparse" % Versions("fastparse"),
    )
  )

lazy val `bellman-spark-engine` = project
  .in(file("modules/engine"))
  .settings(moduleName := "bellman-spark-engine")
  .settings(buildSettings)
  .settings(commonDependencies)
  .settings(compilerPlugins)
  .settings(
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-core" % Versions("cats")
    )
  )

addCommandAlias("ci-test", ";scalastyle ;+test")
