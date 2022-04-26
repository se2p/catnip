package de.uni_passau.fim.se2.catnip.postprocessing

import de.uni_passau.fim.se2.catnip.hint_generation.model.HintGenerationResult
import de.uni_passau.fim.se2.catnip.model.{
  DeletionHint,
  Hint,
  InsertionHint,
  MoveHint,
  MoveToPosHint,
  ReorderHint,
  ReplaceHint
}

object HintSorter extends HintPostprocessor {
  override val name: String = "HintSorter"

  /** Ordering from ‘smallest’ to ‘biggest’:
    *   - [[MoveHint]]
    *   - [[ReorderHint]]
    *   - others
    *   - [[DeletionHint]]
    *
    * Within the same statement list multiple [[InsertionHint InsertionHints]]
    * are sorted by ascending index of insert location.
    *
    * Results in [[MoveHint MoveHints]] being moved to the start of lists,
    * followed by [[ReorderHint ReorderHints]] and so on.
    */
  implicit val hintOrder: Ordering[Hint] = (x: Hint, y: Hint) => {
    (x, y) match {
      case (MoveToPosHint(p1, _, i1), MoveToPosHint(p2, _, i2)) if p1 == p2 =>
        i1.compare(i2)
      case (ReorderHint(p1, _, i1), ReorderHint(p2, _, i2)) if p1 == p2 =>
        i1.compare(i2)
      case (InsertionHint(p1, _, i1), InsertionHint(p2, _, i2)) if p1 == p2 =>
        i1.compare(i2)
      case (_: MoveHint, _: MoveHint)         => 0
      case (_: ReorderHint, _: ReorderHint)   => 0
      case (_: DeletionHint, _: DeletionHint) => 0
      case (_: MoveHint, _)                   => -1
      case (_, _: DeletionHint)               => -1
      case (_: ReplaceHint, _: InsertionHint) => -1
      case (_: ReorderHint, _: MoveHint)      => 1
      case (_: ReorderHint, _)                => -1
      case _                                  => 0
    }
  }

  /** Moves [[MoveHint MoveHints]] and [[ReorderHint ReorderHints]] to the start
    * of the hints list and [[DeletionHint DeletionHints]] to the end.
    *
    * @param hints
    *   the hint generation result the processing should be applied to.
    * @return
    *   an updated result with the hints in the order described above.
    */
  override def process(hints: HintGenerationResult): HintGenerationResult = {
    hints.copy(hints = hints.hints.sorted)
  }
}
