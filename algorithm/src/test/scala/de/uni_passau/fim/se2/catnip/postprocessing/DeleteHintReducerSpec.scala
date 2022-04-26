package de.uni_passau.fim.se2.catnip.postprocessing

import de.uni_passau.fim.se2.catnip.common.FileFinder
import de.uni_passau.fim.se2.catnip.common.defs.UnitSpec
import de.uni_passau.fim.se2.catnip.hint_generation.model.HintGenerationResult
import de.uni_passau.fim.se2.catnip.model.{
  DeletionHint,
  InsertionHint,
  SolutionProgram,
  StudentNode,
  StudentProgram
}
import de.uni_passau.fim.se2.catnip.util.NodeListVisitor
import de.uni_passau.fim.se2.litterbox.ast.model.Program
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.{
  IfElseStmt,
  IfThenStmt
}

class DeleteHintReducerSpec extends UnitSpec {
  "The Delete Hint Reducer" should "remove delete hints if a delete hint for a parent exists" in {
    val (program, outerIf, innerIf) = getProgramOuterIfInnerIf
    val (outerDel, innerDel) =
      (DeletionHint(StudentNode(outerIf)), DeletionHint(StudentNode(innerIf)))

    {
      val hintGenResult = HintGenerationResult(
        StudentProgram(program),
        SolutionProgram(program),
        List(outerDel, innerDel)
      )

      val actual = DeleteHintReducer.process(hintGenResult)
      actual.hints should contain only outerDel
    }

    // should also work with hints in different order
    {
      val hintGenResult = HintGenerationResult(
        StudentProgram(program),
        SolutionProgram(program),
        List(innerDel, outerDel)
      )

      val actual = DeleteHintReducer.process(hintGenResult)
      actual.hints should contain only outerDel
    }
  }

  it should "remove an insert hint if a delete hint for a parent exists" in {
    val (program, outerIf, innerIf) = getProgramOuterIfInnerIf
    val outerDel                    = DeletionHint(StudentNode(outerIf))
    val innerIns = InsertionHint(StudentNode(innerIf.getParentNode), innerIf, 0)

    val hintGenResult = HintGenerationResult(
      StudentProgram(program),
      SolutionProgram(program),
      List(outerDel, innerIns)
    )

    val actual = DeleteHintReducer.process(hintGenResult)
    actual.hints should contain only outerDel
  }

  it should "not remove two independent delete hints" in {
    val program = FileFinder.loadProgramFromFile(
      "example_programs/simple/simple_ifElse_if.sb3"
    )
    val ifElse = NodeListVisitor(program, _.isInstanceOf[IfElseStmt]).head
      .asInstanceOf[IfElseStmt]

    val del1 = DeletionHint(StudentNode(ifElse.getStmtList))
    val del2 = DeletionHint(StudentNode(ifElse.getElseStmts))

    val hintGenResult = HintGenerationResult(
      StudentProgram(program),
      SolutionProgram(program),
      List(del1, del2)
    )

    val actual = DeleteHintReducer.process(hintGenResult)
    actual.hints should contain theSameElementsAs List(del1, del2)
  }

  def getProgramOuterIfInnerIf: (Program, IfThenStmt, IfThenStmt) = {
    val program =
      FileFinder.loadProgramFromFile("example_programs/simple/simple_if_if.sb3")
    val ifs = NodeListVisitor(program, _.isInstanceOf[IfThenStmt])
      .map(_.asInstanceOf[IfThenStmt])

    (program, ifs.head, ifs.last)
  }
}
