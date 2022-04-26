package de.uni_passau.fim.se2.catnip.postprocessing

import de.uni_passau.fim.se2.catnip.hint_generation.model.HintGenerationResult
import de.uni_passau.fim.se2.catnip.model.{
  DeletionHint,
  InsertHint,
  MoveHint,
  ReorderHint,
  ReplaceFieldHint,
  ReplaceStmtHint,
  StudentNode
}
import de.uni_passau.fim.se2.catnip.util.NodeListVisitor

/** Removes hints from the list if a delete hints exists for a (transitive)
  * parent of that node.
  *
  * For [[MoveHint]] s the hint is removed if the target node is to be deleted.
  */
object DeleteHintReducer extends HintPostprocessor {
  override val name: String = "DeleteHintReducer"

  /** Applies some rules to change the generated hints into a new list of hints.
    *
    * @param hints
    *   the hint generation result the processing should be applied to.
    * @return
    *   an updated result with a new list of hints.
    */
  override def process(hints: HintGenerationResult): HintGenerationResult = {
    val nodesToBeDeleted = hints.hints
      .collect { case d: DeletionHint => d }
      .flatMap(d => NodeListVisitor(d.node.node))
      .toSet

    // referenced node not to be deleted => keep the hint
    val chk = (node: StudentNode) => !nodesToBeDeleted.contains(node.node)
    val filteredHints = hints.hints.collect {
      case d @ DeletionHint(node)
          if !node.parentNode.exists(p => nodesToBeDeleted.contains(p.node)) =>
        d
      case i: InsertHint if chk(i.parent)                      => i
      case m: MoveHint if chk(m.newParent)                     => m
      case r @ ReorderHint(parent, _, _) if chk(parent)        => r
      case r @ ReplaceFieldHint(parent, _, _) if chk(parent)   => r
      case r @ ReplaceStmtHint(parent, _, _, _) if chk(parent) => r
    }

    hints.copy(hints = filteredHints)
  }
}
