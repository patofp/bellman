sonatypeProfileName := "com.github.gsk-aiops"
publishMavenStyle := true

publishTo := sonatypePublishToBundle.value
isSnapshot := false

licenses := Seq("APL2" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt"))

import xerial.sbt.Sonatype._
sonatypeProjectHosting := Some(GitHubHosting("gsk-aiops", "bellman-algebra-parser", "johnhuntergskatgmail.com"))

developers := List(
  Developer(id="JNKHunter", name="John Hunter", email="johnhuntergskatgmail.com",
    url=url("https://gsk.com"))
)