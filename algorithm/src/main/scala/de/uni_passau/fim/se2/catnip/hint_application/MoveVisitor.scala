package de.uni_passau.fim.se2.catnip.hint_application

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode

object MoveVisitor {

  /** Deletes the node as child of `oldParent` and inserts it as child of
    * `newParent`.
    *
    * Does not create a deep-copy of the program but instead changes `program`
    * directly.
    * @param rootNode
    *   the node in the subtree of which the change should be made.
    * @param newParent
    *   the new parent of `node`.
    * @param node
    *   that should be moved to the new parent.
    * @param position
    *   the position in the list of statements/children the node should be
    *   inserted at.
    * @return
    *   the same `rootNode` with the described change applied.
    */
  def apply[A <: ASTNode](
      rootNode: A,
      newParent: ASTNode,
      node: ASTNode,
      position: Int
  ): A = {
    val del = DeletionVisitor(rootNode, node)
    InsertionVisitor(del, newParent, node, position)
  }
}
