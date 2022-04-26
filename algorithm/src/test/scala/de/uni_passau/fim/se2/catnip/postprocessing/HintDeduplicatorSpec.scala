package de.uni_passau.fim.se2.catnip.postprocessing

import de.uni_passau.fim.se2.catnip.common.FileFinder
import de.uni_passau.fim.se2.catnip.common.defs.UnitSpec
import de.uni_passau.fim.se2.catnip.hint_generation.model.HintGenerationResult
import de.uni_passau.fim.se2.catnip.model.{
  DeletionHint,
  ScratchProgram,
  SolutionProgram,
  StudentNode,
  StudentProgram
}
import de.uni_passau.fim.se2.catnip.util.NodeListVisitor
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.WaitSeconds

class HintDeduplicatorSpec extends UnitSpec {
  "The Hint Deduplicator" should "leave the hint list as is when no duplicates are present" in {
    val program = ScratchProgram(
      FileFinder.loadProgramFromFile("example_programs/simple/simple_if_if.sb3")
    )
    val hints =
      NodeListVisitor(program).distinct
        .map(StudentNode(_))
        .map(DeletionHint.apply)
    val expected = HintGenerationResult(
      StudentProgram(program),
      SolutionProgram(program),
      hints
    )

    HintDeduplicator
      .process(expected)
      .hints should contain theSameElementsAs hints
  }

  it should "remove duplicate hints" in {
    val program = ScratchProgram(
      FileFinder.loadProgramFromFile(
        "example_programs/move_hint_test/move_hint_wait.sb3"
      )
    )
    val wait = NodeListVisitor(program, _.isInstanceOf[WaitSeconds])
      .map(_.asInstanceOf[WaitSeconds])
      .head

    val del = DeletionHint(StudentNode(wait))

    val res = HintGenerationResult(
      StudentProgram(program),
      SolutionProgram(program),
      List(del, del, del, del)
    )
    val actual = HintDeduplicator.process(res)
    actual.hints should have length 1
    actual.hints should contain only del
  }
}
