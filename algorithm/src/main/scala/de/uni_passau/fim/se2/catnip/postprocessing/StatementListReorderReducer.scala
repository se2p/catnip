package de.uni_passau.fim.se2.catnip.postprocessing

import de.uni_passau.fim.se2.catnip.hint_generation.model.HintGenerationResult
import de.uni_passau.fim.se2.catnip.model.{
  DeletionHint,
  Hint,
  InsertionHint,
  MoveToPosHint,
  ReorderHint,
  StructuralHint,
  StudentNode
}
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt
import org.slf4j.LoggerFactory

/** Removes unnecessary reorder hints from statement lists.
  *
  * Those hints can become unnecessary if a previous insertion oder deletion
  * hint already causes the other nodes in the statement list to shift position.
  * Additionally, one hint can be removed if two nodes swap position.
  */
object StatementListReorderReducer extends HintPostprocessor {
  private val logger = LoggerFactory.getLogger(this.getClass)

  /** A human-readable name of the postprocessor.
    */
  override val name: String = "StatementlistReorderReducer"

  /** Applies some rules to change the generated hints into a new list of hints.
    *
    * @param hints
    *   the hint generation result the processing should be applied to.
    * @return
    *   an updated result with a new list of hints.
    */
  override def process(hints: HintGenerationResult): HintGenerationResult = {
    val partitioned = grouped(hints.hints)
    val updatedHintList = partitioned.toProcess.flatMap { case (_, l) =>
      optimizeSingleStatementList(l)
    }

    val updated =
      hints.copy(hints = partitioned.leaveAsIs ++ updatedHintList.toList)
    logChanges(hints, updated)
    updated
  }

  private def logChanges(
      previous: HintGenerationResult,
      after: HintGenerationResult
  ): Unit = {
    val prev = previous.sortedByActor._1
    val updd = after.sortedByActor._1

    val actors = List("Apple", "Bananas", "Bowl", "Stage")

    val program = previous.studentProgram.program.name
    logger.info(s"Program,${actors.mkString(",")}")
    val diffs = actors.map { actor =>
      (prev.getOrElse(actor, List.empty).toSet diff updd
        .getOrElse(actor, List.empty)
        .toSet).size
    }
    logger.info(s"$program,${diffs.mkString(",")}")
  }

  case class HintPartition(
      toProcess: Map[StudentNode, List[StructuralHint]],
      leaveAsIs: List[Hint]
  )

  private def grouped(
      hints: List[Hint]
  ): HintPartition = {
    val (consider, ignore) = hints.partition {
      case DeletionHint(StudentNode(_: Stmt)) => true
      case _: InsertionHint                   => true
      case _: MoveToPosHint                   => true
      case _: ReorderHint                     => true
      case _                                  => false
    }

    val groups = consider
      .collect {
        case d @ DeletionHint(s) => (StudentNode(s.node.getParentNode), d)
        case i @ InsertionHint(p, _, _) => (p, i)
        case m @ MoveToPosHint(p, _, _) => (p, m)
        case r @ ReorderHint(p, _, _)   => (p, r)
      }
      .groupMap(_._1)(_._2)

    HintPartition(groups, ignore)
  }

  private def optimizeSingleStatementList(
      hints: List[StructuralHint]
  ): List[StructuralHint] = {
    def updateReorderHints(
        reorderHints: List[ReorderHintDetailed],
        idxGreaterThan: Int,
        diff: Int
    ): List[ReorderHintDetailed] = {
      reorderHints.map { r =>
        if (r.startPos >= idxGreaterThan)
          r.copy(startPos = r.startPos + diff)
        else
          r
      }
    }

    val (reorderHints, others) = removeSwapNodesHints(hints).partitionMap {
      case r: ReorderHint => Left(ReorderHintDetailed(r))
      case h              => Right(h)
    }

    // For each insert/delete update the starting position of the nodes that are
    // somewhere after the new/deleted node.
    // At the end, remove those reorder hints where the updated starting
    // position and the target position are identical.
    val updatedReorderHints = others
      .foldLeft(reorderHints) { case (reorderHints, hint) =>
        hint match {
          case i: InsertionHint =>
            updateReorderHints(reorderHints, i.position, 1)
          case d: DeletionHint =>
            val idx = d.node.parentNode.get.filteredChildren.indexOf(d.node)
            updateReorderHints(reorderHints, idx, -1)
          case m: MoveToPosHint =>
            updateReorderHints(reorderHints, m.position, 1)
          case _ => reorderHints
        }
      }
      .filter(r => r.startPos != r.targetPos)
      .map(_.r)
      .sortBy(_.newPosition)

    /* intertwined reorder hints have to be handled separately
    val updated2 = updatedReorderHints
      .foldLeft(updatedReorderHints) { case (reorderHints, hint) =>
        reorderHints.map { r =>
          if (r.startPos > hint.startPos && r.targetPos < hint.startPos) {
            // start of r between hint start/target
            r.copy(startPos = r.startPos - 1)
          } else {
            r
          }
        }
      }
      .filter(r => r.startPos != r.targetPos)
      .map(_.r)
      .sortBy(_.newPosition)*/

    others ++ updatedReorderHints
  }

  private def removeSwapNodesHints(
      hints: List[StructuralHint]
  ): List[StructuralHint] = {
    val (consider, leave) = hints.partitionMap {
      case r: ReorderHint => Left(r)
      case h              => Right(h)
    }

    // group them together where start and target are the same, only keep the
    // first hint per found pair
    val withStartAndTarget = consider
      .map(ReorderHintDetailed(_))
      .groupBy { case ReorderHintDetailed(_, startPos, targetPos) =>
        (math.min(startPos, targetPos), math.max(startPos, targetPos))
      }
      .map { case (_, hints) => hints.minBy(_.startPos).r }
      .toList

    withStartAndTarget ++ leave
  }
}
