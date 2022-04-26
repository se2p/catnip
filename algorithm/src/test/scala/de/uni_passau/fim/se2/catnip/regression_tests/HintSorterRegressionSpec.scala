package de.uni_passau.fim.se2.catnip.regression_tests

import de.uni_passau.fim.se2.catnip.HintPipeline
import de.uni_passau.fim.se2.catnip.common.FileFinder
import de.uni_passau.fim.se2.catnip.common.defs.UnitSpec
import de.uni_passau.fim.se2.catnip.model.InsertionHint

class HintSorterRegressionSpec extends UnitSpec {
  "The hints sorter" should "sort the insert hints for the fruit bowl" in {
    val solution = FileFinder.loadExampleProgramFromFile(
      "regression/03_useless_insert_statements/fruits_solution_03.sb3"
    )
    val student = FileFinder.loadExampleProgramFromFile(
      "regression/fruits_hints_sort_01.sb3"
    )

    val hintGen = HintPipeline.defaultHintPipeline
    hintGen.addSolutions(solution)

    val hints = hintGen.generateHints(student)
    hints.hints should have length 11

    val bowlHints = hints.sortedByActor._1("Bowl")
    val hintsOfInterest = bowlHints.collect {
      case i @ InsertionHint(_, _, idx) if idx > 1 => i
    }
    hintsOfInterest should have length 3
    // all have the same parent
    hintsOfInterest
      .sliding(2)
      .foreach(l => l.head.parent shouldBe l.last.parent)

    val sorted = hintsOfInterest.sortBy(_.position)
    withClue(
      s"Index order was ${hintsOfInterest.map(_.position).mkString(", ")} for"
    ) {
      hintsOfInterest should contain theSameElementsInOrderAs sorted
    }
  }
}
