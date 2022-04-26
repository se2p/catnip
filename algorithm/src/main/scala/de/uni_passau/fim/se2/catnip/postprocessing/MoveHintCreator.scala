package de.uni_passau.fim.se2.catnip.postprocessing

import de.uni_passau.fim.se2.catnip.common.FieldSetter
import de.uni_passau.fim.se2.catnip.hint_generation.model.HintGenerationResult
import de.uni_passau.fim.se2.catnip.model.{
  DeletionHint,
  Hint,
  InsertInFieldHint,
  InsertionHint,
  MoveHint,
  MoveInFieldHint,
  MoveToPosHint,
  ReplaceFieldHint,
  ReplaceStmtHint,
  StructuralHint,
  StudentNode
}
import de.uni_passau.fim.se2.catnip.postprocessing
import de.uni_passau.fim.se2.catnip.util.{
  ASTNodeExt,
  ASTNodeSimilarity,
  NodeListVisitor
}
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode

import scala.collection.mutable.ListBuffer

final private case class MoveHintParts(
    deleteNode: ASTNode,
    insertNode: ASTNode,
    delHint: Option[StructuralHint],
    insHint: StructuralHint
) {
  def intoMoveHint: Option[MoveHint] = {
    insHint match {
      case InsertionHint(insParent, _, insPos) =>
        Some(MoveToPosHint(insParent, StudentNode(deleteNode), insPos))
      case InsertInFieldHint(insParent, _, insFieldName) =>
        Some(
          MoveInFieldHint(insParent, StudentNode(deleteNode), insFieldName)
        )
      case ReplaceStmtHint(parent, _, _, index) =>
        Some(MoveToPosHint(parent, StudentNode(deleteNode), index))
      case ReplaceFieldHint(parent, fieldName, _) =>
        Some(MoveInFieldHint(parent, StudentNode(deleteNode), fieldName))
      case _ => None
    }
  }
}

/** Combines delete, insert, and replace hints with nodes of the same structure
  * into a single move hint.
  */
object MoveHintCreator extends HintPostprocessor {
  override val name: String = "MoveHintCreator"

  /** Combines delete, insert, and replace hints with nodes of the same
    * structure into a single move hint.
    * @param hints
    *   the hint generation result the processing should be applied to.
    * @return
    *   an updated result with a new list of hints.
    */
  override def process(hints: HintGenerationResult): HintGenerationResult = {
    val del          = nodesToBeDeleted(hints.hints)
    val ins          = nodesToBeInserted(hints.hints)
    val replaced     = nodesToBeReplaced(hints.hints)
    val moveHintPrep = findHintPairs(del, replaced, ins)

    val moveHints = buildMoveHints(moveHintPrep)

    val filteredHints = ListBuffer.from(hints.hints)
    moveHints.usedInCreation.foreach { h =>
      val idx = filteredHints.indexOf(h)
      if (idx > -1) filteredHints.remove(idx)
    }

    val newHintList = moveHints.hints ++ filteredHints
    hints.copy(hints = newHintList)
  }

  case class MoveHints(
      hints: List[MoveHint],
      usedInCreation: Set[StructuralHint]
  )

  private def buildMoveHints(
      parts: Iterable[MoveHintParts]
  ): MoveHints = {
    val (moveHints, usedHints) = parts
      .flatMap { p =>
        p.intoMoveHint match {
          case Some(moveHint) =>
            Some((moveHint, List(Some(p.insHint), p.delHint).flatten))
          case None => None
        }
      }
      .foldLeft((List.empty[MoveHint], Set.empty[StructuralHint])) {
        case ((allMoveHints, allUsedHints), (newMoveHint, newUsedHints)) =>
          (newMoveHint :: allMoveHints, allUsedHints ++ newUsedHints)
      }

    MoveHints(moveHints, usedHints)
  }

  private def findHintPairs(
      toBeDeleted: Map[ASTNode, Option[DeletionHint]],
      toBeReplaced: List[ASTNode],
      toBeInserted: Map[ASTNode, StructuralHint]
  ): Iterable[MoveHintParts] = {
    val removeNodes = toBeDeleted ++ toBeReplaced.map(_ -> None)
    for {
      (ins, insHint) <- toBeInserted
      (del, delHint) <- removeNodes
      if ins != del && ins.structurallyEqual(del)
    } yield postprocessing.MoveHintParts(del, ins, delHint, insHint)
  }

  private def nodesToBeInserted(
      hints: List[Hint]
  ): Map[ASTNode, StructuralHint] = {
    hints.collect {
      case i: InsertionHint     => i.node    -> i
      case i: InsertInFieldHint => i.node    -> i
      case r: ReplaceStmtHint   => r.newNode -> r
      case r: ReplaceFieldHint  => r.newNode -> r
    }.toMap
  }

  private def nodesToBeReplaced(
      hints: List[Hint]
  ): List[ASTNode] = {
    hints.collect {
      case r: ReplaceStmtHint => r.oldNode.node
      case r: ReplaceFieldHint =>
        FieldSetter.getFieldValue(r.parent.node, r.fieldName).get
    }
  }

  private def nodesToBeDeleted(
      hints: List[Hint]
  ): Map[ASTNode, Option[DeletionHint]] = {
    val directDelete = hints.collect { case d: DeletionHint =>
      d.node.node -> Some(d)
    }.toMap

    // find all transitive children of nodes that are deleted
    val childDelete = directDelete
      .flatMap { case (d, _) => d.filteredChildren }
      .flatMap { node => NodeListVisitor(node) }
      .map(_ -> None)

    directDelete ++ childDelete
  }
}
