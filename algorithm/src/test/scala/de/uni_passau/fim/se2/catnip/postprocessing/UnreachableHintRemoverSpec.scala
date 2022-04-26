package de.uni_passau.fim.se2.catnip.postprocessing

import de.uni_passau.fim.se2.catnip.common.FileFinder
import de.uni_passau.fim.se2.catnip.common.defs.UnitSpec
import de.uni_passau.fim.se2.catnip.hint_generation.model.HintGenerationResult
import de.uni_passau.fim.se2.catnip.model.{
  DeletionHint,
  MoveInFieldHint,
  MoveToPosHint,
  ReplaceFieldHint,
  ScratchProgram,
  SolutionProgram,
  StudentNode,
  StudentProgram
}
import de.uni_passau.fim.se2.catnip.util.{NodeGen, NodeListVisitor}
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.And
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.Round
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorsound.StopAllSounds
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.WaitSeconds
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.Hide

class UnreachableHintRemoverSpec extends UnitSpec {
  "The unreachable hint remove postprocessor" should "not remove a delete hint referencing a reachable block" in {
    val p1 = ScratchProgram(
      FileFinder.loadProgramFromFile(
        "example_programs/simple/simple_and_or.sb3"
      )
    )

    val and = NodeListVisitor(p1, _.isInstanceOf[And])
      .map(_.asInstanceOf[And])
      .map(StudentNode(_))
      .map(DeletionHint(_))

    val expected =
      HintGenerationResult(StudentProgram(p1), SolutionProgram(p1), and)
    val actual = UnreachableHintRemover.process(expected)

    actual.hints should contain theSameElementsAs and
  }

  it should "remove a delete hint referencing an unreachable ASTNode" in {
    val p1 = ScratchProgram(
      FileFinder.loadProgramFromFile(
        "example_programs/unreachable/unreachable_stop_all_sounds.sb3"
      )
    )
    val stop_all_sounds = NodeListVisitor(p1, _.isInstanceOf[StopAllSounds])
      .map(_.asInstanceOf[StopAllSounds])
      .map(StudentNode(_))
      .map(DeletionHint(_))

    val input = HintGenerationResult(
      StudentProgram(p1),
      SolutionProgram(p1),
      stop_all_sounds
    )
    val actual = UnreachableHintRemover.process(input)

    actual.hints shouldBe empty
  }

  it should "remove a ReplaceFieldHint referencing an unreachable block" in {
    val p1 = ScratchProgram(
      FileFinder.loadProgramFromFile(
        "example_programs/unreachable/unreachable_stop_all_sounds.sb3"
      )
    )
    val replaceWith = new Hide(NodeGen.generateNonDataBlockMetadata())
    val stop_all_sounds = NodeListVisitor(p1, _.isInstanceOf[Round])
      .map(_.asInstanceOf[Round])
      .map(StudentNode(_))
      .map(ReplaceFieldHint(_, replaceWith).get)

    val input = HintGenerationResult(
      StudentProgram(p1),
      SolutionProgram(p1),
      stop_all_sounds
    )
    val actual = UnreachableHintRemover.process(input)

    actual.hints shouldBe empty
  }

  it should "remove a MoveHint of the target is unreachable" in {
    val p1 = ScratchProgram(
      FileFinder.loadProgramFromFile(
        "example_programs/unreachable/unreachable_two_unreachable_scripts.sb3"
      )
    )
    val target = NodeListVisitor(p1, _.isInstanceOf[StopAllSounds])
      .map(_.getParentNode.asInstanceOf[StmtList])
      .head
    val node = NodeListVisitor(p1, _.isInstanceOf[WaitSeconds])
      .map(_.asInstanceOf[WaitSeconds])
      .head

    val moveHint = MoveToPosHint(StudentNode(target), StudentNode(node), 1)
    val input = HintGenerationResult(
      StudentProgram(p1),
      SolutionProgram(p1),
      List(moveHint)
    )
    UnreachableHintRemover.process(input).hints shouldBe empty
  }

  it should "remove a MoveInFieldHint of the target is unreachable" in {
    val p1 = ScratchProgram(
      FileFinder.loadProgramFromFile(
        "example_programs/unreachable/unreachable_two_unreachable_scripts.sb3"
      )
    )
    val target = NodeListVisitor(p1, _.isInstanceOf[StopAllSounds])
      .map(_.getParentNode.asInstanceOf[StmtList])
      .head
    val node = NodeListVisitor(p1, _.isInstanceOf[WaitSeconds])
      .map(_.asInstanceOf[WaitSeconds])
      .head

    val moveHint =
      MoveInFieldHint(StudentNode(target), StudentNode(node), "children")
    val input = HintGenerationResult(
      StudentProgram(p1),
      SolutionProgram(p1),
      List(moveHint)
    )
    UnreachableHintRemover.process(input).hints shouldBe empty
  }

  it should "not remove a MoveHint if the target is reachable" in {
    val p1 = ScratchProgram(
      FileFinder.loadProgramFromFile(
        "example_programs/unreachable/unreachable_stop_all_sounds.sb3"
      )
    )
    val target = NodeListVisitor(p1, _.isInstanceOf[StmtList])
      .map(_.asInstanceOf[StmtList])
      .filter(_.getParentNode.isInstanceOf[IfThenStmt])
      .head
    val stop_all_sounds = NodeListVisitor(p1, _.isInstanceOf[StopAllSounds])
      .map(_.asInstanceOf[StopAllSounds])
      .head
    val moveHint =
      MoveToPosHint(StudentNode(target), StudentNode(stop_all_sounds), 1)

    val input = HintGenerationResult(
      StudentProgram(p1),
      SolutionProgram(p1),
      List(moveHint)
    )
    UnreachableHintRemover.process(input).hints should contain only moveHint
  }

  it should "not remove a MoveInFieldHint if the target is reachable" in {
    val p1 = ScratchProgram(
      FileFinder.loadProgramFromFile(
        "example_programs/unreachable/unreachable_stop_all_sounds.sb3"
      )
    )
    val target = NodeListVisitor(p1, _.isInstanceOf[IfThenStmt])
      .map(_.asInstanceOf[IfThenStmt])
      .head
    val stop_all_sounds = NodeListVisitor(p1, _.isInstanceOf[StopAllSounds])
      .map(_.asInstanceOf[StopAllSounds])
      .head
    val moveHint = MoveInFieldHint(
      StudentNode(target),
      StudentNode(stop_all_sounds),
      "boolExpr"
    )

    val input = HintGenerationResult(
      StudentProgram(p1),
      SolutionProgram(p1),
      List(moveHint)
    )
    UnreachableHintRemover.process(input).hints should contain only moveHint
  }
}
