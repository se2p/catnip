package de.uni_passau.fim.se2.catnip.postprocessing

import de.uni_passau.fim.se2.catnip.common.FileFinder
import de.uni_passau.fim.se2.catnip.common.defs.UnitSpec
import de.uni_passau.fim.se2.catnip.hint_generation.model.HintGenerationResult
import de.uni_passau.fim.se2.catnip.model.{
  DeletionHint,
  InsertionHint,
  MoveToPosHint,
  ReorderHint,
  ScratchProgram,
  SolutionProgram,
  StudentNode,
  StudentProgram
}
import de.uni_passau.fim.se2.catnip.util.{NodeGen, NodeListVisitor}
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList
import de.uni_passau.fim.se2.litterbox.ast.model.literals.{
  NumberLiteral,
  StringLiteral
}
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorsound.StopAllSounds
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.{
  IfThenStmt,
  UntilStmt
}
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.SayForSecs
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.{
  PointInDirection,
  SetXTo,
  SetYTo
}
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.StopAll
import org.scalatest.prop.TableDrivenPropertyChecks

class HintSorterSpec extends UnitSpec with TableDrivenPropertyChecks {
  "The HintSorter" should "sort a MoveHint to the start of the list" in {
    val program = ScratchProgram(
      FileFinder.loadProgramFromFile(
        "example_programs/move_hint_test/move_hint_repeat_until.sb3"
      )
    )

    val ifNode = NodeListVisitor(program, _.isInstanceOf[IfThenStmt])
      .map(_.asInstanceOf[IfThenStmt])
      .head
    val repeatUntil = NodeListVisitor(program, _.isInstanceOf[UntilStmt])
      .map(_.asInstanceOf[UntilStmt])
      .head

    val deleteHint = DeletionHint(StudentNode(ifNode))
    val moveHint = MoveToPosHint(
      StudentNode(ifNode.getThenStmts),
      StudentNode(repeatUntil),
      0
    )

    {
      val hints = HintGenerationResult(
        StudentProgram(program),
        SolutionProgram(program),
        List(deleteHint, moveHint)
      )
      val actual = HintSorter.process(hints)
      actual.hints should contain theSameElementsInOrderAs List(
        moveHint,
        deleteHint
      )
    }

    {
      val hints = HintGenerationResult(
        StudentProgram(program),
        SolutionProgram(program),
        List(moveHint, deleteHint)
      )
      val actual = HintSorter.process(hints)
      actual.hints should contain theSameElementsInOrderAs List(
        moveHint,
        deleteHint
      )
    }
  }

  it should "not change positions for different MoveHints" in {
    val program = ScratchProgram(
      FileFinder.loadProgramFromFile(
        "example_programs/move_hint_test/move_hint_repeat_until.sb3"
      )
    )

    val ifNode = NodeListVisitor(program, _.isInstanceOf[IfThenStmt])
      .map(_.asInstanceOf[IfThenStmt])
      .head
    val repeatUntil = NodeListVisitor(program, _.isInstanceOf[UntilStmt])
      .map(_.asInstanceOf[UntilStmt])
      .head

    val deleteHint = DeletionHint(StudentNode(ifNode))
    val moveHint1 = MoveToPosHint(
      StudentNode(repeatUntil),
      StudentNode(ifNode.getThenStmts),
      0
    )
    val moveHint2 = MoveToPosHint(
      StudentNode(ifNode.getThenStmts),
      StudentNode(repeatUntil),
      0
    )

    {
      val hints = HintGenerationResult(
        StudentProgram(program),
        SolutionProgram(program),
        List(deleteHint, moveHint1, moveHint2)
      )
      val actual = HintSorter.process(hints)
      actual.hints should contain theSameElementsInOrderAs List(
        moveHint1,
        moveHint2,
        deleteHint
      )
    }
    {
      val hints = HintGenerationResult(
        StudentProgram(program),
        SolutionProgram(program),
        List(moveHint1, deleteHint, moveHint2)
      )
      val actual = HintSorter.process(hints)
      actual.hints should contain theSameElementsInOrderAs List(
        moveHint1,
        moveHint2,
        deleteHint
      )
    }
  }

  it should "not change positions of different DeleteHints" in {
    val program = FileFinder.loadProgramFromFile(
      "example_programs/move_hint_test/move_hint_setx_sety.sb3"
    )
    val p1 = StudentProgram(ScratchProgram(program))
    val p2 = SolutionProgram(ScratchProgram(program))

    val setx =
      NodeListVisitor(program, _.isInstanceOf[SetXTo]).head.asInstanceOf[SetXTo]
    val sety =
      NodeListVisitor(program, _.isInstanceOf[SetYTo]).head.asInstanceOf[SetYTo]

    val del1 = DeletionHint(StudentNode(setx))
    val del2 = DeletionHint(StudentNode(sety))
    val reorder =
      ReorderHint(StudentNode(setx.getParentNode), StudentNode(setx), 1)

    {
      val hints  = HintGenerationResult(p1, p2, List(del1, del2, reorder))
      val actual = HintSorter.process(hints)
      actual.hints should contain theSameElementsInOrderAs List(
        reorder,
        del1,
        del2
      )
    }
    {
      val hints  = HintGenerationResult(p1, p2, List(del2, reorder, del1))
      val actual = HintSorter.process(hints)
      actual.hints should contain theSameElementsInOrderAs List(
        reorder,
        del2,
        del1
      )
    }
  }

  it should "order positions of different ReorderHints for the same parent by target index" in {
    val program = FileFinder.loadProgramFromFile(
      "example_programs/move_hint_test/move_hint_setx_sety.sb3"
    )
    val p1 = StudentProgram(ScratchProgram(program))
    val p2 = SolutionProgram(ScratchProgram(program))

    val setx =
      NodeListVisitor(program, _.isInstanceOf[SetXTo]).head.asInstanceOf[SetXTo]
    val sety =
      NodeListVisitor(program, _.isInstanceOf[SetYTo]).head.asInstanceOf[SetYTo]

    val del = DeletionHint(StudentNode(setx))
    val reorder1 =
      ReorderHint(StudentNode(setx.getParentNode), StudentNode(setx), 1)
    val reorder2 =
      ReorderHint(StudentNode(sety.getParentNode), StudentNode(sety), 0)

    {
      val hints  = HintGenerationResult(p1, p2, List(reorder1, reorder2, del))
      val actual = HintSorter.process(hints)
      actual.hints should contain theSameElementsInOrderAs List(
        reorder2,
        reorder1,
        del
      )
    }
    {
      val hints  = HintGenerationResult(p1, p2, List(reorder2, del, reorder1))
      val actual = HintSorter.process(hints)
      actual.hints should contain theSameElementsInOrderAs List(
        reorder2,
        reorder1,
        del
      )
    }
  }

  it should "not change order of different ReorderHints for different parents" in {
    val program = FileFinder.loadProgramFromFile(
      "example_programs/move_hint_test/move_hint_sety_point.sb3"
    )
    val p1 = StudentProgram(ScratchProgram(program))
    val p2 = SolutionProgram(ScratchProgram(program))

    val setx =
      NodeListVisitor(program, _.isInstanceOf[SetXTo]).head.asInstanceOf[SetXTo]
    val point =
      NodeListVisitor(program, _.isInstanceOf[PointInDirection]).head
        .asInstanceOf[PointInDirection]

    val del = DeletionHint(StudentNode(setx))
    val reorder1 =
      ReorderHint(StudentNode(setx.getParentNode), StudentNode(setx), 1)
    val reorder2 =
      ReorderHint(StudentNode(point.getParentNode), StudentNode(point), 0)

    {
      val hints  = HintGenerationResult(p1, p2, List(reorder1, reorder2, del))
      val actual = HintSorter.process(hints)
      actual.hints should contain theSameElementsInOrderAs List(
        reorder1,
        reorder2,
        del
      )
    }
    {
      val hints  = HintGenerationResult(p1, p2, List(reorder2, del, reorder1))
      val actual = HintSorter.process(hints)
      actual.hints should contain theSameElementsInOrderAs List(
        reorder2,
        reorder1,
        del
      )
    }
  }

  it should "order positions of different MoveToPosHints by target index" in {
    val program = FileFinder.loadProgramFromFile(
      "example_programs/move_hint_test/move_hint_setx_sety.sb3"
    )
    val p1 = StudentProgram(ScratchProgram(program))
    val p2 = SolutionProgram(ScratchProgram(program))

    val setx =
      NodeListVisitor(program, _.isInstanceOf[SetXTo]).head.asInstanceOf[SetXTo]
    val sety =
      NodeListVisitor(program, _.isInstanceOf[SetYTo]).head.asInstanceOf[SetYTo]

    val del = DeletionHint(StudentNode(setx))
    val reorder1 =
      MoveToPosHint(StudentNode(setx.getParentNode), StudentNode(setx), 1)
    val reorder2 =
      MoveToPosHint(StudentNode(sety.getParentNode), StudentNode(sety), 0)

    {
      val hints  = HintGenerationResult(p1, p2, List(reorder1, reorder2, del))
      val actual = HintSorter.process(hints)
      actual.hints should contain theSameElementsInOrderAs List(
        reorder2,
        reorder1,
        del
      )
    }
    {
      val hints  = HintGenerationResult(p1, p2, List(reorder2, del, reorder1))
      val actual = HintSorter.process(hints)
      actual.hints should contain theSameElementsInOrderAs List(
        reorder2,
        reorder1,
        del
      )
    }
  }

  it should "sort in order MoveHints, ReorderHints, others, DeleteHints" in {
    val program = FileFinder.loadProgramFromFile(
      "example_programs/move_hint_test/move_hint_sety_point.sb3"
    )
    val p1 = StudentProgram(ScratchProgram(program))
    val p2 = SolutionProgram(ScratchProgram(program))

    val setx =
      NodeListVisitor(program, _.isInstanceOf[SetXTo]).head.asInstanceOf[SetXTo]
    val sety =
      NodeListVisitor(program, _.isInstanceOf[SetYTo]).head.asInstanceOf[SetYTo]
    val point = NodeListVisitor(program, _.isInstanceOf[PointInDirection]).head
      .asInstanceOf[PointInDirection]

    val reorder =
      ReorderHint(StudentNode(setx.getParentNode), StudentNode(setx), 1)
    val move =
      MoveToPosHint(StudentNode(setx.getParentNode), StudentNode(point), 0)
    val insert = InsertionHint(
      StudentNode(point.getParentNode),
      new SayForSecs(
        new StringLiteral("s"),
        new NumberLiteral(1),
        NodeGen.generateNonDataBlockMetadata()
      ),
      1
    )
    val delete = DeletionHint(StudentNode(sety))

    val expectedOrder = List(move, reorder, insert, delete)

    forAll(Table("Hints") ++ expectedOrder.permutations) { hintOrder =>
      val hints  = HintGenerationResult(p1, p2, hintOrder)
      val actual = HintSorter.process(hints)
      withClue(s"Actual order: ${actual.hints.map(_.getClass.getSimpleName)}") {
        actual.hints should contain theSameElementsInOrderAs expectedOrder
      }
    }
  }

  it should "sort multiple insert statement hints for the same parent by index" in {
    val program = FileFinder.loadProgramFromFile(
      "example_programs/move_hint_test/move_hint_sety_point.sb3"
    )
    val p1 = StudentProgram(ScratchProgram(program))
    val p2 = SolutionProgram(ScratchProgram(program))

    val stmtList = StudentNode(
      NodeListVisitor(program, _.isInstanceOf[SetXTo]).head.getParentNode
        .asInstanceOf[StmtList]
    )

    val i1 = InsertionHint(
      stmtList,
      new SetXTo(new NumberLiteral(1), NodeGen.generateNonDataBlockMetadata()),
      1
    )
    val i2 = InsertionHint(
      stmtList,
      new StopAllSounds(NodeGen.generateNonDataBlockMetadata()),
      2
    )
    val i3 = InsertionHint(
      stmtList,
      new StopAll(NodeGen.generateNonDataBlockMetadata()),
      4
    )

    val expectedOrder = List(i1, i2, i3)

    val table = Table("perms") ++ List(i1, i2, i3).permutations
    forAll(table) { perm =>
      val hints  = HintGenerationResult(p1, p2, perm)
      val actual = HintSorter.process(hints)
      actual.hints should contain theSameElementsInOrderAs expectedOrder
    }
  }
}
