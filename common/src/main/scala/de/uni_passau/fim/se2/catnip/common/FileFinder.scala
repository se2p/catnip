package de.uni_passau.fim.se2.catnip.common

import de.uni_passau.fim.se2.litterbox.ast.model.Program
import de.uni_passau.fim.se2.litterbox.ast.parser.Scratch3Parser

import java.nio.file.{Files, FileVisitOption, Path}
import scala.util.matching.Regex
import scala.jdk.CollectionConverters.*

object FileFinder {
  def matchingSb3Files(
      path: String,
      filenamePattern: Regex,
      includeRegressionTestFiles: Boolean = false
  ): List[Path] = {
    Files
      .walk(
        Path.of(this.getClass.getClassLoader.getResource(path).toURI),
        FileVisitOption.FOLLOW_LINKS
      )
      .iterator()
      .asScala
      .filter(p =>
        if (!includeRegressionTestFiles) {
          !p.toString.contains("regression")
        } else {
          true
        }
      )
      .filter(p =>
        Files.isRegularFile(p) &&
          p.getFileName.toString.matches(s"$filenamePattern\\.sb3")
      )
      .toList
  }

  def loadExampleProgramFromFile(path: String): Program = {
    loadProgramFromFile(s"example_programs/$path")
  }

  def loadProgramFromFile(path: String): Program = {
    val file = Path.of(getClass.getClassLoader.getResource(path).toURI).toFile
    val s    = new Scratch3Parser
    s.parseFile(file)
  }
}
