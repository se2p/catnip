package de.uni_passau.fim.se2.catnip.hint_application

import de.uni_passau.fim.se2.catnip.model.{
  DeletionHint,
  InsertInFieldHint,
  InsertionHint,
  MissingActorHint,
  MoveInFieldHint,
  MoveToPosHint,
  ReorderHint,
  ReplaceFieldHint,
  ReplaceStmtHint,
  ScratchProgram,
  StructuralHint
}
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode
import de.uni_passau.fim.se2.litterbox.ast.visitor.CloneVisitor
import de.uni_passau.fim.se2.catnip.util.ASTNodeExtTyped
import org.slf4j.LoggerFactory

object StructuralHintApplicator {
  private val logger = LoggerFactory.getLogger(this.getClass)

  /** Applies all changes suggested by the hints to the program.
    *
    * Applies them strictly in the same order as they appear in the `hints`
    * list. Applies the changes to a deep-copy of the program, leaving the
    * original `program` unchanged.
    * @param program
    *   a Scratch program.
    * @param hints
    *   a list of structural change hints that should be applied.
    * @return
    *   a new program with all the changes applied.
    */
  def apply(
      program: ScratchProgram,
      hints: List[StructuralHint]
  ): ScratchProgram = {
    val prog = program.accept(new CloneVisitor)

    ScratchProgram(hints.foldLeft(prog) { case (p, hint) =>
      applyHintDirectly(p, hint)
    })
  }

  /** Applies a change suggested by the hint to the program.
    *
    * Applies the changes to a deep-copy of the program.
    * @param program
    *   a Scratch program.
    * @param hint
    *   a structural change hint that should be applied.
    * @return
    *   a new program with the suggested change applied.
    */
  def apply(program: ScratchProgram, hint: StructuralHint): ScratchProgram = {
    val p = program.accept(new CloneVisitor)
    ScratchProgram(applyHintDirectly(p, hint))
  }

  /** Applies a change suggested by the hint in the subtree of `node`.
    *
    * Applies the changes to a deep-copy of the node.
    * @param node
    *   the root of the subtree in which a change should be made
    * @param hint
    *   a structural change hint that should be applied.
    * @tparam A
    *   the concrete type of the `node`.
    * @return
    *   a deep copy of `node` but with the change applied.
    */
  def apply[A <: ASTNode](node: A, hint: StructuralHint): A = {
    val n = node.cloned
    applyHintDirectly(n, hint)
  }

  /** Applies the hint directly to `program` without making a deep-copy first.
    * @param rootNode
    *   the subtree in which a change should be made.
    * @param hint
    *   the hint changing the program.
    * @tparam A
    *   the concrete type of the root in which the change should be made.
    * @return
    *   the same `node` but with the change applied.
    */
  private def applyHintDirectly[A <: ASTNode](
      rootNode: A,
      hint: StructuralHint
  ): A = {
    logger.debug(s"Applying structural hint $hint.")

    hint match {
      case DeletionHint(node) => DeletionVisitor(rootNode, node.node)
      case InsertionHint(parent, node, position) =>
        InsertionVisitor(rootNode, parent.node, node, position)
      case InsertInFieldHint(parent, newValue, fieldName) =>
        InsertionInFieldVisitor(rootNode, parent.node, newValue, fieldName)
      case MoveToPosHint(newParent, node, newPosition) =>
        MoveVisitor(rootNode, newParent.node, node.node, newPosition)
      case MoveInFieldHint(newParent, node, fieldName) =>
        MoveInFieldVisitor(rootNode, newParent.node, node.node, fieldName)
      case ReorderHint(parent, node, newPosition) =>
        ReorderVisitor(rootNode, parent.node, node.node, newPosition)
      case ReplaceFieldHint(parent, fieldName, newNode) =>
        ReplacementVisitor(rootNode, parent.node, fieldName, newNode)
      case ReplaceStmtHint(parent, _, newNode, index) =>
        InsertionVisitor(rootNode, parent.node, newNode, index)
      case MissingActorHint(_, actor) =>
        MissingActorHintApplicator(rootNode, actor)
    }
  }
}
