package de.uni_passau.fim.se2.catnip.regression_tests

import de.uni_passau.fim.se2.catnip.HintPipeline
import de.uni_passau.fim.se2.catnip.common.FileFinder
import de.uni_passau.fim.se2.catnip.common.defs.UnitSpec
import de.uni_passau.fim.se2.catnip.model.{InsertionHint, StudentNode}
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.{
  IfThenStmt,
  UntilStmt
}
import de.uni_passau.fim.se2.litterbox.ast.model.{Program, StmtList}

class HintGenerationRegressionSpec extends UnitSpec {
  def lp(path: String): Program = {
    FileFinder.loadExampleProgramFromFile(s"regression/$path")
  }

  "The Hint Generator" should "not generate a hint to insert a StmtList as child of StmtList in 01_insert_hint_application" in {
    val solution = lp("01_insert_hint_application/horse_solution.sb3")
    val student  = lp("01_insert_hint_application/horse_unfinished.sb3")

    val hintGen = HintPipeline.defaultHintPipeline
    hintGen.addSolutions(solution)

    val hints = hintGen.generateHints(student)
    hints.hints should have length 2

    val insertHints = hints.hints.collect { case i: InsertionHint => i }
    insertHints should have length 1

    val stmtListInsert = insertHints.collect {
      case i @ InsertionHint(StudentNode(_: StmtList), _: StmtList, _) => i
    }
    stmtListInsert shouldBe empty
  }

  it should "not generate superfluous insert if/until hints" in {
    val solution = lp("03_useless_insert_statements/fruits_solution_03.sb3")
    val student  = lp("03_useless_insert_statements/fruits_step_02_v02.sb3")

    val hintGen = HintPipeline.defaultHintPipeline
    hintGen.addSolutions(solution)

    val hints        = hintGen.generateHints(student)
    val bananasHints = hints.sortedByActor._1("Bananas")

    val insertIfThen = bananasHints.collect {
      case i @ InsertionHint(_, _: IfThenStmt, _) => i
    }
    val insertUntil = bananasHints.collect {
      case i @ InsertionHint(_, _: UntilStmt, _) => i
    }

    insertIfThen shouldBe empty
    insertUntil shouldBe empty
  }
}
