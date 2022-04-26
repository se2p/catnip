import sbt._

object Dependencies {
  // Java Dependencies
  private val apacheCommons = "org.apache.commons" % "commons-lang3" % "3.12.0"
  private val fasterxml = {
    val fasterXmlVersion = "2.12.5"
    Seq(
      "com.fasterxml.jackson.module" %% "jackson-module-scala",
      "com.fasterxml.jackson.core"    % "jackson-databind"
    ).map(_ % fasterXmlVersion)
  }
  private val litterBox = {
    val module = "de.uni_passau.fim.se2" % "Litterbox" % "1.5"
    module from "https://github.com/se2p/LitterBox/releases/download/v1.5/Litterbox-1.5.jar"
  }
  private val slf4j = {
    val slf4jVersion = "1.7.36"
    Seq(
      "org.slf4j" % "slf4j-api",
      "org.slf4j" % "slf4j-simple"
    ).map(_ % slf4jVersion)
  }

  // Scala Dependencies

  private val breeze = "org.scalanlp" %% "breeze" % "2.0"

  private val cats       = "org.typelevel" %% "cats-core"   % "2.7.0"
  private val catsEffect = "org.typelevel" %% "cats-effect" % "3.3.11"

  private val circe = {
    val circeVersion = "0.14.1"
    Seq(
      "io.circe" %% "circe-core",
      "io.circe" %% "circe-generic",
      "io.circe" %% "circe-parser"
    ).map(_ % circeVersion)
  }

  private val scalaCSV = "com.github.tototoshi" %% "scala-csv" % "1.3.10"
  private val scalaParallelCollections =
    "org.scala-lang.modules" %% "scala-parallel-collections" % "1.0.4"
  private val scopt  = "com.github.scopt" %% "scopt"        % "4.0.1"
  private val scribe = "com.outr"         %% "scribe-slf4j" % "3.8.2"

  // Test Dependencies
  private val scalatest = Seq(
    "com.vladsch.flexmark" % "flexmark-profile-pegdown" % "0.64.0"   % "test",
    "org.scalatest"       %% "scalatest"                % "3.2.12"   % "test",
    "org.scalatestplus"   %% "scalacheck-1-16"          % "3.2.12.0" % "test"
  )

  def algorithmProjectDependencies: Seq[ModuleID] = {
    val simple = Seq(
      breeze,
      litterBox,
      scalaParallelCollections
    )
    simple ++ scalatest ++ slf4j
  }

  def algorithmRunProjectDependencies: Seq[ModuleID] = {
    val simple = Seq(
      breeze,
      litterBox,
      scopt,
      scribe
    )
    simple ++ scalatest
  }

  def commonProjectDependencies: Seq[ModuleID] = {
    val simple = Seq(
      apacheCommons,
      cats,
      catsEffect,
      litterBox
    )
    simple ++ scalatest ++ slf4j
  }

  def evaluationProjectDependencies: Seq[ModuleID] = {
    val simple = Seq(
      litterBox,
      scalaParallelCollections,
      scopt,
      scribe,
      scalaCSV
    )
    simple ++ scalatest
  }

  def serveProjectDependencies: Seq[ModuleID] = {
    val simple = Seq(litterBox)
    simple ++ circe ++ fasterxml ++ scalatest
  }
}
