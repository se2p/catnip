import sbt._

object ScalacOptions {
  private val scalac2_options = Seq(
    "-explaintypes",
    "-Wunused",
    "-Xlint",
    "-Xsource:3",
    "-Ytasty-reader",
    "-Ywarn-dead-code"
  )

  private val scalac3_options = Seq(
    "-noindent",
    "-old-syntax"
    // "-source", "future" // ToDo: enable when metals and IntelliJ can deal with `*`-imports
  )

  private val commonOptions = Seq(
    "-deprecation",
    "-feature",
    "-unchecked",
    "-Xfatal-warnings"
  )

  def scalacOpt(version: String): Seq[String] = {
    val specificOpts = CrossVersion.partialVersion(version) match {
      case Some((2, _)) => scalac2_options
      case _            => scalac3_options
    }

    commonOptions ++ specificOpts
  }
}
