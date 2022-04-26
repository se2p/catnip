import com.typesafe.sbt.packager.docker.{
  Cmd,
  DockerChmodType,
  DockerPermissionStrategy,
  ExecCmd
}
import Dependencies._
import Util.Cctt
import ScalacOptions.scalacOpt

lazy val scala2 = "2.13.8"
lazy val scala3 = "3.1.2"

ThisBuild / organization := "de.uni_passau.fim.se2"
ThisBuild / version      := "1.0.0"
ThisBuild / scalaVersion := scala2

// ToDo: figure out how to enable only for Scala 2
// ThisBuild / scapegoatVersion := "1.4.9"
// ThisBuild / scapegoatIgnoredFiles := Seq(
//   ".*/serve/target/.*.scala" // ignore Play generated files
// )

Test / testOptions ++= Seq(
  Tests.Argument(TestFrameworks.ScalaTest, "-u", "target/test-reports"),
  Tests.Argument(TestFrameworks.ScalaTest, "-h", "target/test-reports"),
  Tests.Argument(TestFrameworks.ScalaTest, "-oD")
)
ThisBuild / coverageOutputCobertura := true

lazy val root = project
  .in(file("."))
  .disablePlugins(AssemblyPlugin)
  .aggregate(common, algorithm, serve)

lazy val common = project
  .in(file("common"))
  .disablePlugins(AssemblyPlugin)
  .settings(
    name               := "common",
    crossScalaVersions := List(scala2, scala3),
    scalacOptions ++= scalacOpt(scalaVersion.value),
    libraryDependencies ++= commonProjectDependencies
  )

lazy val algorithm = project
  .in(file("algorithm"))
  .dependsOn(common % Cctt)
  .disablePlugins(AssemblyPlugin)
  .settings(
    name               := "algorithm",
    crossScalaVersions := List(scala2, scala3),
    scalacOptions ++= scalacOpt(scalaVersion.value),
    libraryDependencies ++= algorithmProjectDependencies,
    // stops the Sb3Parser from failing
    Test / parallelExecution := false
  )

lazy val serve = project
  .in(file("serve"))
  .dependsOn(algorithm % Cctt, common % Cctt)
  .enablePlugins(PlayScala)
  .settings(
    name       := "hintgen-serve",
    maintainer := "fein@fim.uni-passau.de",
    scalacOptions ++= Seq(
      "-deprecation",
      "-feature",
      "-unchecked",
      "-Xlint:infer-any,doc-detached,private-shadow",
      "-Xlint:strict-unsealed-patmat",
      "-Xsource:3",
      "-Ytasty-reader"
    ),
    AssemblySettings.assemblySettings(name),
    libraryDependencies ++= serveProjectDependencies,
    libraryDependencies ++= Seq(
      guice,
      "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0" % "test"
    ),
    projectDependencies := {
      Seq(
        (algorithm / projectID).value.excludeAll(ExclusionRule("org.slf4j")),
        (common / projectID).value.excludeAll(ExclusionRule("org.slf4j"))
      )
    },
    dockerBaseImage          := "ubuntu:latest",
    dockerExposedPorts       := Seq(9000),
    dockerChmodType          := DockerChmodType.UserGroupWriteExecute,
    dockerPermissionStrategy := DockerPermissionStrategy.CopyChown,
    dockerCommands ++= Seq(
      Cmd("USER", "root"),
      Cmd("ENV", "DEBIAN_FRONTEND=noninteractive"),
      ExecCmd("RUN", "apt-get", "update"),
      ExecCmd(
        "RUN",
        "apt-get",
        "install",
        "--no-install-recommends",
        "-y",
        "git",
        "nodejs",
        "openjdk-17-jre-headless",
        // needed for headless chrome for Whisker
        "libcairo-gobject2",
        "libnss3",
        "libatk1.0-0",
        "libatk-bridge2.0-0",
        "libx11-xcb1",
        "libxkbcommon0",
        "libxcomposite1",
        "libxdamage1",
        "libxfixes3",
        "libxrandr2",
        "libcups2",
        "libdrm2",
        "libgtk-3-0",
        "libgbm1",
        "libasound2",
        "libxshmfence1"
      ),
      ExecCmd("RUN", "rm", "-rf", "/var/lib/apt/lists/*"),
      ExecCmd(
        "RUN",
        "bash",
        "-c",
        s"mkdir -p /tmp/whiskerTmp /receivedSolutions && chown -R ${(Docker / daemonUser).value} /tmp/whiskerTmp /receivedSolutions"
      ),
      Cmd("USER", (Docker / daemonUser).value)
    )
  )
  .settings(
    jlinkOptions ++= Seq(
      "--compress=2",
      "--no-man-pages",
      "--strip-debug",
      "--no-header-files"
    ),
    jlinkModules ++= Seq(
      "jdk.crypto.ec",
      "jdk.unsupported"
    ),
    jlinkIgnoreMissingDependency := JlinkIgnore.only(
      "ch.qos.logback.classic"        -> "javax.servlet.http",
      "ch.qos.logback.classic.boolex" -> "groovy.lang",
      "ch.qos.logback.classic.boolex" -> "org.codehaus.groovy.control",
      "ch.qos.logback.classic.boolex" -> "org.codehaus.groovy.reflection",
      "ch.qos.logback.classic.boolex" -> "org.codehaus.groovy.runtime",
      "ch.qos.logback.classic.boolex" -> "org.codehaus.groovy.runtime.callsite",
      "ch.qos.logback.classic.boolex" -> "org.codehaus.groovy.runtime.typehandling",
      "ch.qos.logback.classic.gaffer" -> "groovy.lang",
      "ch.qos.logback.classic.gaffer" -> "org.codehaus.groovy.control",
      "ch.qos.logback.classic.gaffer" -> "org.codehaus.groovy.control.customizers",
      "ch.qos.logback.classic.gaffer" -> "org.codehaus.groovy.reflection",
      "ch.qos.logback.classic.gaffer" -> "org.codehaus.groovy.runtime",
      "ch.qos.logback.classic.gaffer" -> "org.codehaus.groovy.runtime.callsite",
      "ch.qos.logback.classic.gaffer" -> "org.codehaus.groovy.runtime.typehandling",
      "ch.qos.logback.classic.gaffer" -> "org.codehaus.groovy.runtime.wrappers",
      "ch.qos.logback.classic.gaffer" -> "org.codehaus.groovy.transform",
      "ch.qos.logback.classic.helpers"          -> "javax.servlet",
      "ch.qos.logback.classic.helpers"          -> "javax.servlet.http",
      "ch.qos.logback.classic.selector.servlet" -> "javax.servlet",
      "ch.qos.logback.classic.servlet"          -> "javax.servlet",
      "ch.qos.logback.core.boolex"              -> "org.codehaus.janino",
      "ch.qos.logback.core.joran.conditional" -> "org.codehaus.commons.compiler",
      "ch.qos.logback.core.joran.conditional" -> "org.codehaus.janino",
      "ch.qos.logback.core.net"               -> "javax.mail",
      "ch.qos.logback.core.net"               -> "javax.mail.internet",
      "ch.qos.logback.core.status"            -> "javax.servlet",
      "ch.qos.logback.core.status"            -> "javax.servlet.http",
      "dev.ludovic.netlib.blas"               -> "jdk.incubator.foreign",
      "dev.ludovic.netlib.blas"               -> "jdk.incubator.vector",
      "io.jsonwebtoken.impl"                  -> "android.util",
      "io.jsonwebtoken.impl.crypto"           -> "org.bouncycastle.jce",
      "io.jsonwebtoken.impl.crypto"           -> "org.bouncycastle.jce.spec",
      "javax.activation"        -> "com.sun.activation.registries",
      "javax.transaction"       -> "javax.enterprise.context",
      "javax.transaction"       -> "javax.enterprise.util",
      "javax.transaction"       -> "javax.interceptor",
      "org.joda.time"           -> "org.joda.convert",
      "org.joda.time.base"      -> "org.joda.convert",
      "pl.edu.icm.jlargearrays" -> "com.sun.xml.internal.ws.encoding.soap",
      "shapeless"               -> "scala.reflect.macros.contexts",
      "shapeless"               -> "scala.tools.nsc",
      "shapeless"               -> "scala.tools.nsc.ast",
      "shapeless"               -> "scala.tools.nsc.typechecker"
    )
  )
  .enablePlugins(DockerPlugin)
  .enablePlugins(JlinkPlugin)
