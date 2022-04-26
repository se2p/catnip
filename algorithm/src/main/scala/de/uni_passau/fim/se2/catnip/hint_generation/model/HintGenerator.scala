package de.uni_passau.fim.se2.catnip.hint_generation.model

import de.uni_passau.fim.se2.litterbox.ast.model.Program

trait HintGenerator {

  /** Adds example solution(s) into the pool of solutions the student’s program
    * is compared to.
    * @param solutions
    *   some example solution(s).
    */
  def addSolutions(solutions: Program*): Unit

  /** Generate the actual hints for a student’s program.
    * @param program
    *   for which the student requests hints.
    * @return
    *   structural hints for `program`.
    */
  def generateHints(program: Program): HintGenerationResult
}
