package de.uni_passau.fim.se2.catnip.postprocessing

import de.uni_passau.fim.se2.catnip.model.ReorderHint

private[postprocessing] case class ReorderHintDetailed(
    r: ReorderHint,
    startPos: Int,
    targetPos: Int
)

private[postprocessing] object ReorderHintDetailed {
  def apply(r: ReorderHint): ReorderHintDetailed = {
    ReorderHintDetailed(
      r,
      r.parent.filteredChildren.indexOf(r.node),
      r.newPosition
    )
  }
}
