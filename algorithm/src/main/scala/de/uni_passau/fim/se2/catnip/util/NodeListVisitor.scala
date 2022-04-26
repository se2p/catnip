package de.uni_passau.fim.se2.catnip.util

import de.uni_passau.fim.se2.catnip.model.ScratchProgram
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor
import NodeListVisitor.NodeFilter

private class NodeListVisitor(val filter: NodeFilter) extends ScratchVisitor {
  private val nodes = scala.collection.mutable.ListBuffer[ASTNode]()

  /** Gets the list of all visited nodes.
    * @return
    *   the list of all visited nodes.
    */
  def getNodes: List[ASTNode] = nodes.toList

  override def visit(node: ASTNode): Unit = {
    if (filter(node)) {
      nodes += node
    }
    super.visit(node)
  }
}

/** Visits the subtree of the AST with root `node` and builds a list of all
  * visited nodes.
  */
object NodeListVisitor {
  type NodeFilter = ASTNode => Boolean

  /** The default [[NodeFilter]] does not ignore any visited nodes.
    */
  val defaultFilter: NodeFilter = (_: ASTNode) => true

  def apply(node: ASTNode): List[ASTNode] = {
    NodeListVisitor(node, defaultFilter)
  }

  def apply(node: ASTNode, filter: NodeFilter): List[ASTNode] = {
    val v = new NodeListVisitor(filter)
    node.accept(v)
    v.getNodes
  }

  def apply(program: ScratchProgram): List[ASTNode] = {
    NodeListVisitor(program, defaultFilter)
  }

  def apply(program: ScratchProgram, filter: NodeFilter): List[ASTNode] = {
    val v = new NodeListVisitor(filter)
    program.accept(v)
    v.getNodes
  }
}
