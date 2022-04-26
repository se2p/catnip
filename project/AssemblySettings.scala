import sbt.SettingKey
import sbtassembly.AssemblyKeys._
import sbtassembly.AssemblyPlugin.autoImport.MergeStrategy
import sbtassembly.PathList

object AssemblySettings {
  def assemblySettings(name: SettingKey[String]) = Seq(
    assembly / assemblyJarName := name.value + ".jar",
    assembly / assemblyMergeStrategy := {
      case "scala-collection-compat.properties" => MergeStrategy.last
      case PathList("dev", "ludovic", "netlib", "InstanceBuilder.class") =>
        MergeStrategy.last
      case PathList("scala", "collection", "compat", xs @ _*) =>
        MergeStrategy.last
      case PathList("scala", "util", "control", "compat", xs @ _*) =>
        MergeStrategy.last
      case PathList("cats", "kernel", xs @ _*) =>
        MergeStrategy.last
      case PathList("org", "checkerframework", xs @ _*) => MergeStrategy.last
      case PathList("com", "google", xs @ _*)           => MergeStrategy.last
      case PathList("com", "fasterxml", xs @ _*)        => MergeStrategy.last
      case PathList("org", "slf4j", "impl", xs @ _*)    => MergeStrategy.first
      case PathList("play", "reference-overrides.conf") => MergeStrategy.concat
      case manifest if manifest.contains("MANIFEST.MF") => MergeStrategy.discard
      case PathList(ps @ _*) if ps.last == "module-info.class" =>
        MergeStrategy.discard
      case x =>
        val oldStrategy = (assembly / assemblyMergeStrategy).value
        oldStrategy(x)
    }
  )
}
