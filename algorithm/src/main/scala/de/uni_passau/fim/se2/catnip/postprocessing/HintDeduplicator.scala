package de.uni_passau.fim.se2.catnip.postprocessing

import de.uni_passau.fim.se2.catnip.hint_generation.model.HintGenerationResult

/** Removes duplicates from the list of hints.
  */
object HintDeduplicator extends HintPostprocessor {
  override val name: String = "HintDeduplicator"

  /** Deduplicates the given list of hints.
    *
    * @param hints
    *   the hint generation result the processing should be applied to.
    * @return
    *   an updated result with a new list of hints.
    */
  override def process(hints: HintGenerationResult): HintGenerationResult = {
    hints.copy(hints = hints.hints.distinct)
  }
}
