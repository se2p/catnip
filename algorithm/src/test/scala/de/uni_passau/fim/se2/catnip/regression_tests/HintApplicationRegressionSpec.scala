package de.uni_passau.fim.se2.catnip.regression_tests

import de.uni_passau.fim.se2.catnip.HintPipeline
import de.uni_passau.fim.se2.catnip.common.FileFinder
import de.uni_passau.fim.se2.catnip.common.defs.UnitSpec
import de.uni_passau.fim.se2.catnip.hint_application.StructuralHintApplicator
import de.uni_passau.fim.se2.catnip.model.ReplaceFieldHint
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.Touching
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfElseStmt

class HintApplicationRegressionSpec extends UnitSpec {
  "The Insert Hint Applicator" should "insert the condition in the IfElseStmt in 01_insert_hint_application" in {
    val solution = FileFinder.loadExampleProgramFromFile(
      "regression/01_insert_hint_application/horse_solution.sb3"
    )
    val student = FileFinder.loadExampleProgramFromFile(
      "regression/01_insert_hint_application/horse_unfinished.sb3"
    )

    val hintGen = HintPipeline.defaultHintPipeline
    hintGen.addSolutions(solution)
    val hints = hintGen.generateHints(student)
    hints.hints should have length 2

    val replaceFieldHints = hints.hints.collect {
      case i @ ReplaceFieldHint(_, _, _: Touching) => i
    }
    replaceFieldHints should have length 1

    val hint = replaceFieldHints.head
    val res = StructuralHintApplicator.apply(
      hint.parent.node.asInstanceOf[IfElseStmt],
      hint
    )
    res.getBoolExpr shouldBe a[Touching]
  }
}
