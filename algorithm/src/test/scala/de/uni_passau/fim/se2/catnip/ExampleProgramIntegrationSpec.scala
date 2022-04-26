package de.uni_passau.fim.se2.catnip

import de.uni_passau.fim.se2.catnip.common.FileFinder
import de.uni_passau.fim.se2.catnip.common.defs.UnitSpec
import de.uni_passau.fim.se2.catnip.hint_application.StructuralHintApplicator
import de.uni_passau.fim.se2.catnip.hint_generation.StructuralHintGenerator
import de.uni_passau.fim.se2.catnip.model.{ScratchProgram, StructuralHint}
import de.uni_passau.fim.se2.litterbox.ast.model.Program

import java.nio.file.Path

class ExampleProgramIntegrationSpec extends UnitSpec {
  "The Hint Generator" should "not crash applying the generated hints for the parrot_01 example pair" in {
    val (solution, studentProgram) = programs("parrot_01")

    val sc              = new StructuralHintGenerator(List(solution))
    val hints           = sc.generateHints(studentProgram)
    val structuralHints = hints.hints.collect { case s: StructuralHint => s }

    StructuralHintApplicator(ScratchProgram(studentProgram), structuralHints)
  }

  def programs(pair_name: String): (Program, Program) = {
    val solution       = file(List(pair_name, "sol.sb3"))
    val studentProgram = file(List(pair_name, "stud.sb3"))

    (solution, studentProgram)
  }

  def file(path: List[String]): Program = {
    val pathElements = "pairs" :: path
    val p            = Path.of("example_programs", pathElements*)
    FileFinder.loadProgramFromFile(p.toString)
  }
}
