package de.uni_passau.fim.se2.catnip.hint_application

import de.uni_passau.fim.se2.catnip.common.FieldSetter
import de.uni_passau.fim.se2.catnip.model.NodeRootPath
import de.uni_passau.fim.se2.litterbox.ast.model.{ASTNode, StmtList}
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor

import java.util

private class InsertionVisitor(
    val parent: ASTNode,
    val newNode: ASTNode,
    val index: Int
) extends ScratchVisitor {
  private val parentPath = NodeRootPath(parent)

  override def visit(node: StmtList): Unit = {
    val path = NodeRootPath(node)
    if (parentPath == path) {
      val idx = math.min(index, node.getChildren.size())

      val newStmtList = new util.LinkedList[ASTNode](node.getStmts)
      newStmtList.add(idx, newNode)
      FieldSetter.setField(node, "stmts", newStmtList)

      val children = new java.util.ArrayList[ASTNode](node.getChildren)
      children.add(idx, newNode)
      FieldSetter.setField(node, "children", children)

      newNode.setParentNode(node)
    } else {
      super.visit(node)
    }
  }
}

object InsertionVisitor {

  /** Inserts the given node at the specified position.
    *
    * Does not create a deep-copy of the program but instead changes `program`
    * directly.
    * @param rootNode
    *   the node in the subtree of which the node should be added.
    * @param parent
    *   the parent of the node to insert.
    * @param newNode
    *   the node to insert as a child of `parent`.
    * @param index
    *   the position at which the new node should be inserted into the children
    *   list.
    * @return
    *   the same `rootNode` as in the input but with the new node inserted.
    */
  def apply[A <: ASTNode](
      rootNode: A,
      parent: ASTNode,
      newNode: ASTNode,
      index: Int
  ): A = {
    val v = new InsertionVisitor(parent, newNode, index)
    rootNode.accept(v)
    rootNode
  }
}
