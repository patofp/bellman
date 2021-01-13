name := "bellman-algebra-parser"

version := "0.1"

scalaVersion := "2.13.4"

libraryDependencies += "org.apache.jena" % "apache-jena-libs" % "3.17.0" pomOnly()
libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.3" % Test
libraryDependencies += "com.lihaoyi" %% "fastparse" % "2.3.0"