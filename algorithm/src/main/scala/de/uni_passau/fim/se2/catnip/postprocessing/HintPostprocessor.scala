package de.uni_passau.fim.se2.catnip.postprocessing

import de.uni_passau.fim.se2.catnip.hint_generation.model.HintGenerationResult

/** A HintPostprocessor takes in a [[HintGenerationResult]] with a list of hints
  * stored inside and modifies this list of hints.
  *
  * It is allowed to add and remove hints to the list. It should not change the
  * [[de.uni_passau.fim.se2.catnip.model.StudentProgram]] and
  * [[de.uni_passau.fim.se2.catnip.model.SolutionProgram]] that are also part of
  * the [[HintGenerationResult]].
  */
trait HintPostprocessor {

  /** A human-readable name of the postprocessor.
    */
  val name: String

  /** Applies some rules to change the generated hints into a new list of hints.
    * @param hints
    *   the hint generation result the processing should be applied to.
    * @return
    *   an updated result with a new list of hints.
    */
  def process(hints: HintGenerationResult): HintGenerationResult
}
