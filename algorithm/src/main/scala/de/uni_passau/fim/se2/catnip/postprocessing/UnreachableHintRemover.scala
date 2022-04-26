package de.uni_passau.fim.se2.catnip.postprocessing

import de.uni_passau.fim.se2.catnip.hint_generation.model.HintGenerationResult
import de.uni_passau.fim.se2.catnip.model.{
  DeletionHint,
  InsertInFieldHint,
  InsertionHint,
  MoveInFieldHint,
  MoveToPosHint,
  ReorderHint,
  ReplaceFieldHint,
  StudentNode
}

/** Removes all [[de.uni_passau.fim.se2.catnip.model.StructuralHint]] s for
  * blocks that are not reachable in the program.
  *
  * Those blocks do not affect correctness of the program in any way and can
  * therefore be left as is. Removing the therefore unnecessary
  * [[de.uni_passau.fim.se2.catnip.model.StructuralHint]] s has the effect of
  * uncluttering the list of hints shown to the student, making it easier for
  * them to react to the actually important ones.
  *
  * [[MoveToPosHint]] s and [[MoveInFieldHint]] s remain however, if the target
  * node is reachable. In that case an unreachable node is moved into a
  * reachable part of the program, which is useful information.
  */
object UnreachableHintRemover extends HintPostprocessor {
  override val name: String = "UnreachableHintRemover"

  /** Applies some rules to change the generated hints into a new list of hints.
    *
    * @param hints
    *   the hint generation result the processing should be applied to.
    * @return
    *   an updated result with a new list of hints.
    */
  override def process(hints: HintGenerationResult): HintGenerationResult = {
    val unreachableBlock = hints.studentProgram.program.unreachableBlocks
    val check            = (n: StudentNode) => unreachableBlock.contains(n.node)

    val newHints = hints.hints.flatMap {
      case DeletionHint(h) if check(h)            => None
      case InsertionHint(p, _, _) if check(p)     => None
      case InsertInFieldHint(p, _, _) if check(p) => None
      case MoveToPosHint(p, _, _) if check(p)     => None
      case MoveInFieldHint(p, _, _) if check(p)   => None
      case ReorderHint(p, _, _) if check(p)       => None
      case ReplaceFieldHint(p, _, _) if check(p)  => None
      case h                                      => Some(h)
    }

    hints.copy(hints = newHints)
  }
}
