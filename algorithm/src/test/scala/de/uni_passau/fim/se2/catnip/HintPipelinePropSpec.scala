package de.uni_passau.fim.se2.catnip

import de.uni_passau.fim.se2.catnip.common.FileFinder
import de.uni_passau.fim.se2.catnip.common.defs.PropSpec
import de.uni_passau.fim.se2.catnip.hint_generation.StructuralHintGenerator
import de.uni_passau.fim.se2.litterbox.ast.parser.Scratch3Parser

import java.nio.file.Path

class HintPipelinePropSpec extends PropSpec {
  val EXAMPLES_FOLDER = "example_programs"

  property(
    "The pipeline should create empty hints for student program = solution program"
  ) {
    val simpleFiles = Table("path") ++
      FileFinder.matchingSb3Files(EXAMPLES_FOLDER, "simple_.*".r)
    simpleFiles.length should be > 0
    forAll(simpleFiles) { path => checkHintsEmpty(path) }
  }

  property(
    "The pipeline without post-processors should create the same hints as the hint generator directly for all programs"
  ) {
    val f = FileFinder.matchingSb3Files(EXAMPLES_FOLDER, ".*".r)
    val simpleFiles =
      Table(("student", "solution")) ++ (for {
        a <- f
        b <- f
      } yield (a, b))
    simpleFiles.length should be > 0
    forAll(simpleFiles) { (student, solution) =>
      checkHintsIdentical(student, solution)
    }
  }

  def checkHintsEmpty(path: Path): Unit = {
    val parser          = new Scratch3Parser
    val solutionProgram = parser.parseSB3File(path.toFile)
    val studentProgram  = parser.parseSB3File(path.toFile)
    val sc              = new StructuralHintGenerator(List(solutionProgram))
    val pipeline        = new HintPipeline(sc)

    val scHints = sc.generateHints(studentProgram)
    val plHints = pipeline.generateHints(studentProgram)

    plHints.hints should contain theSameElementsAs scHints.hints
    plHints.hints should be(empty)
  }

  def checkHintsIdentical(studentPath: Path, solutionPath: Path): Unit = {
    val parser          = new Scratch3Parser
    val studentProgram  = parser.parseSB3File(studentPath.toFile)
    val studentProgram2 = parser.parseSB3File(studentPath.toFile)
    val solutionProgram = parser.parseSB3File(solutionPath.toFile)

    val sc       = new StructuralHintGenerator(List(solutionProgram))
    val pipeline = new HintPipeline(sc)
    val scHints  = sc.generateHints(studentProgram)

    val plHints = pipeline.generateHints(studentProgram2)

    plHints.hints should contain theSameElementsAs scHints.hints
  }
}
