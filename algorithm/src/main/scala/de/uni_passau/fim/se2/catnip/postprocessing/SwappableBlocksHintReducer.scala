package de.uni_passau.fim.se2.catnip.postprocessing

import de.uni_passau.fim.se2.catnip.hint_generation.model.HintGenerationResult
import de.uni_passau.fim.se2.catnip.model.{
  Hint,
  ReorderHint,
  StudentNode,
  StudentNodeTyped
}
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.{
  ActorLookStmt,
  AskAndWait,
  SwitchBackdropAndWait
}
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.{
  SayForSecs,
  SpriteLookStmt,
  ThinkForSecs
}
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.{
  GlideSecsTo,
  GlideSecsToXY,
  MoveSteps,
  PointTowards,
  SpriteMotionStmt
}
import de.uni_passau.fim.se2.litterbox.ast.model.{ASTNode, StmtList}

import scala.jdk.CollectionConverters.*

object SwappableBlocksHintReducer extends HintPostprocessor {

  /** A human-readable name of the postprocessor.
    */
  override val name: String = "Swappable Blocks Hint Reducer"

  /** Removes all reorder hints from the given list if the node should only be
    * moved by one position and swapping those two node would have no difference
    * on the actual program.
    *
    * @param hints
    *   the hint generation result the processing should be applied to.
    * @return
    *   an updated result with a new list of hints.
    */
  override def process(hints: HintGenerationResult): HintGenerationResult = {
    val nonReorderHints = hints.hints.filter {
      case _: ReorderHint => false
      case _              => true
    }

    val reducedReorderHints = reorderHints(hints.hints)
      .flatMap { case (stmtList, hints) => reduceReorderHints(stmtList, hints) }
      .map(_.r)

    hints.copy(hints = nonReorderHints ++ reducedReorderHints)
  }

  /** Groups all reorder hints by the parent node of the affected node.
    * @param hints
    *   that should be filtered and grouped.
    * @return
    *   all reorder hints in `hints` grouped by the parent node of the node that
    *   should be moved.
    */
  private def reorderHints(
      hints: List[Hint]
  ): Map[StudentNodeTyped[StmtList], List[ReorderHintDetailed]] = {
    hints
      .collect { case r: ReorderHint => r }
      .collect { case r @ ReorderHint(StudentNode(p: StmtList), _, _) =>
        (StudentNodeTyped(p), r)
      }
      .groupMap(_._1) { case (_, hint) => ReorderHintDetailed(hint) }
  }

  /** Removes all reorder hints from the given list if the node should only be
    * moved by one position and swapping those two node would have no difference
    * on the actual program.
    * @param stmtList
    *   the parent node of the nodes the `hints` refer to.
    * @param hints
    *   that should be filtered.
    * @return
    *   a list of filtered hints as described above.
    */
  private def reduceReorderHints(
      stmtList: StudentNodeTyped[StmtList],
      hints: List[ReorderHintDetailed]
  ): List[ReorderHintDetailed] = {
    hints
      .map { hint =>
        val stmts = stmtList.node.getStmts
        (hint, stmts.get(hint.startPos), stmts.get(hint.targetPos))
      }
      .filterNot { case (hint, startNode, targetNode) =>
        val betweenNodesNonAffecting =
          onlyNonAffectingBlocksInBetween(stmtList, hint)
        val canBeSwappedByType = canBeSwapped(startNode, targetNode)

        betweenNodesNonAffecting && canBeSwappedByType
      }
      .map(_._1)
  }

  private def canBeSwapped(a: ASTNode, b: ASTNode): Boolean = {
    (a, b) match {
      // special cases
      case (_: SayForSecs, _) | (_, _: SayForSecs)       => false
      case (_: ThinkForSecs, _) | (_, _: ThinkForSecs)   => false
      case (_: GlideSecsTo, _) | (_, _: GlideSecsTo)     => false
      case (_: GlideSecsToXY, _) | (_, _: GlideSecsToXY) => false
      case (_: AskAndWait, _) | (_, _: AskAndWait)       => false
      case (_: SwitchBackdropAndWait, _) | (_, _: SwitchBackdropAndWait) =>
        false

      // generally looks and motions can be exchanged, as long as there is no
      // time component involved
      case (_: SpriteLookStmt, _: SpriteLookStmt)   => true
      case (_: SpriteLookStmt, _: ActorLookStmt)    => true
      case (_: ActorLookStmt, _: SpriteLookStmt)    => true
      case (_: SpriteLookStmt, _: SpriteMotionStmt) => true
      case (_: SpriteMotionStmt, _: SpriteLookStmt) => true
      case (_: ActorLookStmt, _: SpriteMotionStmt)  => true
      case (_: SpriteMotionStmt, _: ActorLookStmt)  => true

      // special case for Schifffahrt
      case (_: PointTowards, _: MoveSteps) => true
      case (_: MoveSteps, _: PointTowards) => true

      // all not explicitly mentioned blocks are non-swappable
      case _ => false
    }
  }

  /** Checks if all blocks between the starting and target position of the
    * reorder do no affect the moved block in a way that would make moving
    * impossible.
    *
    * Specialized for Schifffahrt, in general not applicable as different
    * movement blocks might cancel each other when swapped into a different
    * order. ⇒ To adapt it for the general case, a data/timing analysis between
    * the nodes in the statement list should be performed.
    * @param stmtList
    *   in which the reordering of nodes happens.
    * @param hint
    *   for the node which should be reordered.
    * @return
    *   true, if a reordering based on the statements in the `stmtList` between
    *   starting and target position is okay.
    */
  private def onlyNonAffectingBlocksInBetween(
      stmtList: StudentNodeTyped[StmtList],
      hint: ReorderHintDetailed
  ): Boolean = {
    val stmts = stmtList.node.getStmts.asScala
    val start = hint.startPos.min(hint.targetPos) + 1
    val end   = hint.startPos.max(hint.targetPos)

    stmts.slice(start, end).forall {
      _ match {
        case _: SayForSecs            => false
        case _: ThinkForSecs          => false
        case _: GlideSecsTo           => false
        case _: GlideSecsToXY         => false
        case _: AskAndWait            => false
        case _: SwitchBackdropAndWait => false
        case _: SpriteLookStmt        => true
        case _: ActorLookStmt         => true
        case _: SpriteMotionStmt      => true
        case _                        => false
      }
    }
  }
}
