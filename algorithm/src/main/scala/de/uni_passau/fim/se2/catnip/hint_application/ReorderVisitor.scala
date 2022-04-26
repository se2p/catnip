package de.uni_passau.fim.se2.catnip.hint_application

import de.uni_passau.fim.se2.catnip.common.FieldSetter
import de.uni_passau.fim.se2.catnip.model.NodeRootPath
import de.uni_passau.fim.se2.litterbox.ast.model.{ASTNode, StmtList}
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor

import java.util

private class ReorderVisitor(
    val parent: ASTNode,
    val newNode: ASTNode,
    val newPosition: Int
) extends ScratchVisitor {
  private val parentPath = NodeRootPath(parent)

  override def visit(node: StmtList): Unit = {
    val path = NodeRootPath(node)
    if (parentPath == path) {
      val newStmtList = new util.LinkedList[ASTNode](node.getStmts)
      newStmtList.remove(newNode)
      newStmtList.add(newPosition, newNode)
      FieldSetter.setField(node, "stmts", newStmtList)

      val children = new util.LinkedList[ASTNode](node.getChildren)
      children.remove(newNode)
      children.add(newPosition, newNode)
      FieldSetter.setField(node, "children", children)

      newNode.setParentNode(node)
    } else {
      super.visit(node)
    }
  }
}

object ReorderVisitor {

  /** Moves the given node to the specified position within the children list of
    * the unchanged parent.
    *
    * Does not create a deep-copy of the program but instead changes `program`
    * directly.
    * @param rootNode
    *   the node in the subtree of which the node should be moved.
    * @param parent
    *   the parent of the node to move.
    * @param node
    *   the node to insert as a child of `parent`.
    * @param newPosition
    *   the position at which the node should be inserted into the children
    *   list.
    * @return
    *   the same `rootNode` as in the input but with the node moved to the new
    *   position.
    */
  def apply[A <: ASTNode](
      rootNode: A,
      parent: ASTNode,
      node: ASTNode,
      newPosition: Int
  ): A = {
    val v = new ReorderVisitor(parent, node, newPosition)
    rootNode.accept(v)
    rootNode
  }
}
