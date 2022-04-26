package de.uni_passau.fim.se2.catnip.postprocessing

import de.uni_passau.fim.se2.catnip.common.FileFinder
import de.uni_passau.fim.se2.catnip.common.defs.UnitSpec
import de.uni_passau.fim.se2.catnip.hint_generation.model.HintGenerationResult
import de.uni_passau.fim.se2.catnip.model.{
  DeletionHint,
  InsertionHint,
  MoveToPosHint,
  ReorderHint,
  SolutionProgram,
  StudentNode,
  StudentProgram
}
import de.uni_passau.fim.se2.catnip.util.{NodeGen, NodeListVisitor}
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.{
  ChangeXBy,
  ChangeYBy,
  IfOnEdgeBounce,
  SetXTo,
  SetYTo
}
import org.scalatest.prop.TableDrivenPropertyChecks

class StatementListReorderReducerSpec
    extends UnitSpec
    with TableDrivenPropertyChecks {
  "The StatementListReorderHintReducer" should "remove one reorder hint of two nodes swap places" in {
    val program = FileFinder.loadExampleProgramFromFile(
      "move_hint_test/move_hint_setx_sety.sb3"
    )
    val setx     = NodeListVisitor(program, _.isInstanceOf[SetXTo]).head
    val sety     = NodeListVisitor(program, _.isInstanceOf[SetYTo]).head
    val stmtList = setx.getParentNode

    val r1 = ReorderHint(StudentNode(stmtList), StudentNode(setx), 1)
    val r2 = ReorderHint(StudentNode(stmtList), StudentNode(sety), 0)

    val res = HintGenerationResult(
      StudentProgram(program),
      SolutionProgram(program),
      List(r1, r2)
    )
    val actual = StatementListReorderReducer.process(res)

    actual.hints should contain only r1
  }

  it should "remove one reorder hint each if two node swaps occur in one statement list" in {
    val program = FileFinder.loadExampleProgramFromFile(
      "reorder_hints/changex_setx_changey_sety.sb3"
    )
    val changex  = NodeListVisitor(program, _.isInstanceOf[ChangeXBy]).head
    val setx     = NodeListVisitor(program, _.isInstanceOf[SetXTo]).head
    val changey  = NodeListVisitor(program, _.isInstanceOf[ChangeYBy]).head
    val sety     = NodeListVisitor(program, _.isInstanceOf[SetYTo]).head
    val stmtList = StudentNode(setx.getParentNode)

    val r1 = ReorderHint(stmtList, StudentNode(changex), 3) // 0 -> 3
    val r2 = ReorderHint(stmtList, StudentNode(setx), 2)    // 1 -> 2
    val r3 = ReorderHint(stmtList, StudentNode(changey), 1) // 2 -> 1
    val r4 = ReorderHint(stmtList, StudentNode(sety), 0)    // 3 -> 0

    val i1 = InsertionHint(
      stmtList,
      new IfOnEdgeBounce(NodeGen.generateNonDataBlockMetadata()),
      4
    )

    val res = HintGenerationResult(
      StudentProgram(program),
      SolutionProgram(program),
      List(r1, r2, r3, r4, i1)
    )
    val actual = StatementListReorderReducer.process(res)

    actual.hints should have length 3
    actual.hints should contain theSameElementsInOrderAs List(i1, r2, r1)

    val table = Table("perms") ++ List(r1, r2, r3, r4, i1).permutations
    forAll(table) { perm =>
      val result = HintGenerationResult(
        StudentProgram(program),
        SolutionProgram(program),
        perm
      )
      val processed = StatementListReorderReducer.process(result)

      processed.hints should have length 3
      processed.hints should contain theSameElementsInOrderAs List(i1, r2, r1)
    }
  }

  it should "remove the move one back reorder hints if one node is inserted at the start" in {
    val program = FileFinder.loadExampleProgramFromFile(
      "move_hint_test/move_hint_setx_sety.sb3"
    )
    val setx     = NodeListVisitor(program, _.isInstanceOf[SetXTo]).head
    val sety     = NodeListVisitor(program, _.isInstanceOf[SetYTo]).head
    val stmtList = StudentNode(setx.getParentNode)

    val i1 = InsertionHint(
      stmtList,
      new IfOnEdgeBounce(NodeGen.generateNonDataBlockMetadata()),
      0
    )
    val r1 = ReorderHint(stmtList, StudentNode(setx), 1) // 0 -> 1
    val r2 = ReorderHint(stmtList, StudentNode(sety), 2) // 1 -> 2

    val res = HintGenerationResult(
      StudentProgram(program),
      SolutionProgram(program),
      List(r1, r2, i1)
    )
    val actual = StatementListReorderReducer.process(res)
    actual.hints should contain only i1

    val perms = Table("perms") ++ List(r1, r2, i1).permutations
    forAll(perms) { hints =>
      val result = HintGenerationResult(
        StudentProgram(program),
        SolutionProgram(program),
        hints
      )
      val processed = StatementListReorderReducer.process(result)
      processed.hints should contain only i1
    }
  }

  it should "remove the move one to the front reorder hints if one node is deleted at the start" in {
    val program = FileFinder.loadExampleProgramFromFile(
      "reorder_hints/changex_setx_changey_sety.sb3"
    )
    val changex  = NodeListVisitor(program, _.isInstanceOf[ChangeXBy]).head
    val setx     = NodeListVisitor(program, _.isInstanceOf[SetXTo]).head
    val changey  = NodeListVisitor(program, _.isInstanceOf[ChangeYBy]).head
    val sety     = NodeListVisitor(program, _.isInstanceOf[SetYTo]).head
    val stmtList = StudentNode(setx.getParentNode)

    val r1 = ReorderHint(stmtList, StudentNode(setx), 0)    // 1 -> 0
    val r2 = ReorderHint(stmtList, StudentNode(changey), 1) // 2 -> 1
    val r3 = ReorderHint(stmtList, StudentNode(sety), 2)    // 3 -> 2
    val d1 = DeletionHint(StudentNode(changex))             // delete 0

    val res = HintGenerationResult(
      StudentProgram(program),
      SolutionProgram(program),
      List(r1, r2, r3, d1)
    )
    val actual = StatementListReorderReducer.process(res)
    actual.hints should contain only d1

    val perms = Table("perms") ++ List(r1, r2, r3, d1).permutations
    forAll(perms) { hints =>
      val result = HintGenerationResult(
        StudentProgram(program),
        SolutionProgram(program),
        hints
      )
      val processed = StatementListReorderReducer.process(result)
      processed.hints should contain only d1
    }
  }

  it should "remove the two back reorder hints if two nodes are inserted at the start" in {
    val program = FileFinder.loadExampleProgramFromFile(
      "reorder_hints/changex_setx_changey_sety.sb3"
    )
    val changex  = NodeListVisitor(program, _.isInstanceOf[ChangeXBy]).head
    val setx     = NodeListVisitor(program, _.isInstanceOf[SetXTo]).head
    val changey  = NodeListVisitor(program, _.isInstanceOf[ChangeYBy]).head
    val sety     = NodeListVisitor(program, _.isInstanceOf[SetYTo]).head
    val stmtList = StudentNode(setx.getParentNode)

    val r1 = ReorderHint(stmtList, StudentNode(changey), 0) // 2 -> 0
    val r2 = ReorderHint(stmtList, StudentNode(sety), 1)    // 3 -> 1
    val d1 = DeletionHint(StudentNode(changex))             // delete 0
    val d2 = DeletionHint(StudentNode(setx))                // delete 1

    val perms = Table("perms") ++ List(r1, r2, d1, d2).permutations
    forAll(perms) { hints =>
      val result = HintGenerationResult(
        StudentProgram(program),
        SolutionProgram(program),
        hints
      )
      val processed = StatementListReorderReducer.process(result)
      processed.hints should contain theSameElementsAs List(d1, d2)
    }
  }

  it should "not remove a reorder hint over two places with one delete inbetween" in {
    val program = FileFinder.loadExampleProgramFromFile(
      "reorder_hints/changex_setx_changey_sety.sb3"
    )
    val setx     = NodeListVisitor(program, _.isInstanceOf[SetXTo]).head
    val changey  = NodeListVisitor(program, _.isInstanceOf[ChangeYBy]).head
    val sety     = NodeListVisitor(program, _.isInstanceOf[SetYTo]).head
    val stmtList = StudentNode(setx.getParentNode)

    val d1 = DeletionHint(StudentNode(changey))
    val r1 = ReorderHint(stmtList, StudentNode(sety), 1)

    val res = HintGenerationResult(
      StudentProgram(program),
      SolutionProgram(program),
      List(r1, d1)
    )
    val actual = StatementListReorderReducer.process(res)

    actual.hints should contain theSameElementsAs List(r1, d1)
  }

  it should "treat a moveToPos hint like a insertion hint" in {
    val program = FileFinder.loadExampleProgramFromFile(
      "move_hint_test/move_hint_setx_sety.sb3"
    )
    val setx     = NodeListVisitor(program, _.isInstanceOf[SetXTo]).head
    val sety     = NodeListVisitor(program, _.isInstanceOf[SetYTo]).head
    val stmtList = StudentNode(setx.getParentNode)

    val m1 = MoveToPosHint(
      stmtList,
      StudentNode(new IfOnEdgeBounce(NodeGen.generateNonDataBlockMetadata())),
      0
    )
    val r1 = ReorderHint(stmtList, StudentNode(setx), 1) // 0 -> 1
    val r2 = ReorderHint(stmtList, StudentNode(sety), 2) // 1 -> 2

    val res = HintGenerationResult(
      StudentProgram(program),
      SolutionProgram(program),
      List(r1, r2, m1)
    )
    val actual = StatementListReorderReducer.process(res)
    actual.hints should contain only m1

    val perms = Table("perms") ++ List(r1, r2, m1).permutations
    forAll(perms) { hints =>
      val result = HintGenerationResult(
        StudentProgram(program),
        SolutionProgram(program),
        hints
      )
      val processed = StatementListReorderReducer.process(result)
      processed.hints should contain only m1
    }
  }

  it should "remove superfluous reorder hints between another reorder hint" ignore {
    val program = FileFinder.loadExampleProgramFromFile(
      "reorder_hints/changex_setx_changey_sety.sb3"
    )
    val changex  = NodeListVisitor(program, _.isInstanceOf[ChangeXBy]).head
    val setx     = NodeListVisitor(program, _.isInstanceOf[SetXTo]).head
    val changey  = NodeListVisitor(program, _.isInstanceOf[ChangeYBy]).head
    val stmtList = StudentNode(setx.getParentNode)

    val r1 = ReorderHint(stmtList, StudentNode(changex), 2) // 0 -> 2
    val r2 = ReorderHint(stmtList, StudentNode(setx), 0)    // 1 -> 0
    val r3 = ReorderHint(stmtList, StudentNode(changey), 1) // 2 -> 1

    val res = HintGenerationResult(
      StudentProgram(program),
      SolutionProgram(program),
      List(r1, r2, r3)
    )
    val actual = StatementListReorderReducer.process(res)
    actual.hints should contain only r1
  }
}
