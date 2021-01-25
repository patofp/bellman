name := "bellman-algebra-parser"
organization := "com.github.gsk-aiops"
version := "0.1.0"

scalaVersion := "2.13.4"

libraryDependencies += "org.apache.jena" % "apache-jena-libs" % "3.17.0" pomOnly()
libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.3" % Test
libraryDependencies += "com.lihaoyi" %% "fastparse" % "2.3.0"

scalastyleFailOnWarning := true

artifactName := { (sv: ScalaVersion, module: ModuleID, artifact: Artifact) =>
  artifact.name + "_" + sv.binary + "-" + module.revision + "." + artifact.extension
}
