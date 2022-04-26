addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.8.15")

addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.9.3")

addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.4.6")
addSbtPlugin("ch.epfl.scala" % "sbt-scalafix" % "0.10.0")
// ToDo: figure out how to enable only for Scala 2
// addSbtPlugin("com.sksamuel.scapegoat" %% "sbt-scapegoat" % "1.1.1")

addSbtPlugin("com.eed3si9n"     % "sbt-assembly"        % "1.2.0")
addSbtPlugin("org.scala-native" % "sbt-scala-native"    % "0.4.4")
addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.8.1")
addSbtPlugin("org.scalameta"    % "sbt-native-image"    % "0.3.2")

addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.6.2")

addSbtPlugin("io.spray" % "sbt-revolver" % "0.9.1")
