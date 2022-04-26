package de.uni_passau.fim.se2.catnip.hint_application

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode

object MoveInFieldVisitor {

  /** Deletes the node as child of `oldParent` and inserts it as child of
    * `newParent`.
    * @param rootNode
    *   the node in the subtree of which the change should be made.
    * @param newParent
    *   the new parent of `node`.
    * @param node
    *   that should be moved to the new parent.
    * @param fieldName
    *   the field of `newParent` in which the `node` should be inserted.
    * @return
    *   the same `rootNode` with the change applied in its subtree.
    */
  def apply[A <: ASTNode](
      rootNode: A,
      newParent: ASTNode,
      node: ASTNode,
      fieldName: String
  ): A = {
    val d = DeletionVisitor(rootNode, node)
    InsertionInFieldVisitor(d, newParent, node, fieldName)
  }
}
