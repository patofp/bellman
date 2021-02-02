name := "bellman-algebra"
organization := "com.github.gsk-aiops"
version := "0.1.12-SNAPSHOT"

scalaVersion := "2.12.12"

libraryDependencies += "org.apache.jena" % "apache-jena-libs" % "3.17.0" pomOnly()
libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.3" % Test
libraryDependencies += "com.lihaoyi" %% "fastparse" % "2.3.0"

scalastyleFailOnWarning := true

artifactName := { (sv: ScalaVersion, module: ModuleID, artifact: Artifact) =>
  artifact.name + "_" + sv.binary + "-" + module.revision + "." + artifact.extension
}

publishTo := sonatypePublishToBundle.value
sonatypeProfileName := "com.github.gsk-aiops"
publishMavenStyle := true
isSnapshot := true
licenses := Seq("APL2" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt"))

import xerial.sbt.Sonatype._
sonatypeProjectHosting := Some(GitHubHosting("gsk-aiops", "bellman-algebra-parser", "johnhuntergskatgmail.com"))

developers := List(
  Developer(id="JNKHunter", name="John Hunter", email="johnhuntergskatgmail.com",
    url=url("https://gsk.com"))
)
