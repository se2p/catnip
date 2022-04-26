package de.uni_passau.fim.se2.catnip.postprocessing

import de.uni_passau.fim.se2.catnip.common.FileFinder
import de.uni_passau.fim.se2.catnip.common.defs.UnitSpec
import de.uni_passau.fim.se2.catnip.hint_generation.StructuralHintGenerator
import de.uni_passau.fim.se2.catnip.model.ReplaceFieldHint
import de.uni_passau.fim.se2.catnip.util.NodeGen
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.LessThan
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral

class CommutativeOpHintReducerSpec extends UnitSpec {
  "The remove of unnecessary hints for commutative operations" should "remove both hints when the two operators are switched" in {
    val solution = FileFinder.loadExampleProgramFromFile(
      "regression/02_commutative_operation/fruits_solution_03.sb3"
    )
    val student = FileFinder.loadExampleProgramFromFile(
      "regression/02_commutative_operation/fruits_partial_01.sb3"
    )

    val hintGen = new StructuralHintGenerator(List(solution))
    val hints   = hintGen.generateHints(student)
    hints.hints should have length 2

    val reduced = CommutativeOpHintReducer.process(hints)
    reduced.hints shouldBe empty
  }

  it should "not affect other generated hints" in {
    val solution = FileFinder.loadExampleProgramFromFile(
      "regression/01_insert_hint_application/horse_solution.sb3"
    )
    val student = FileFinder.loadExampleProgramFromFile(
      "regression/01_insert_hint_application/horse_unfinished.sb3"
    )

    val hintGen = new StructuralHintGenerator(List(solution))
    val hints   = hintGen.generateHints(student)
    hints.hints should have length 2

    val reduced = CommutativeOpHintReducer.process(hints)
    reduced.hints should contain theSameElementsAs hints.hints
  }

  it should "not affect another replace hint if the structure is different" in {
    val solution = FileFinder.loadExampleProgramFromFile(
      "regression/02_commutative_operation/fruits_solution_03.sb3"
    )
    val student = FileFinder.loadExampleProgramFromFile(
      "regression/02_commutative_operation/fruits_partial_01.sb3"
    )

    val hintGen = new StructuralHintGenerator(List(solution))
    val hints   = hintGen.generateHints(student)
    hints.hints should have length 2

    val newNode = new LessThan(
      new NumberLiteral(12),
      new NumberLiteral(34579),
      NodeGen.generateNonDataBlockMetadata()
    )
    val newHint =
      ReplaceFieldHint(hints.hints.head.references, "operand1", newNode)

    val updatedHints = hints.copy(hints = newHint :: hints.hints)

    val reduced = CommutativeOpHintReducer.process(updatedHints)
    reduced.hints should contain only newHint
  }
}
