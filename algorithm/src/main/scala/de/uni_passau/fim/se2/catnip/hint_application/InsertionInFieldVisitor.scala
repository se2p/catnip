package de.uni_passau.fim.se2.catnip.hint_application

import de.uni_passau.fim.se2.catnip.common.FieldSetter
import de.uni_passau.fim.se2.catnip.util.{ASTNodeExt, NodeTypeHelpers}
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor
import org.slf4j.LoggerFactory

private class InsertionInFieldVisitor(
    val parent: ASTNode,
    val newValue: ASTNode,
    val fieldName: String
) extends ScratchVisitor {
  private val logger = LoggerFactory.getLogger(this.getClass)

  private val parentId = parent.blockId

  override def visit(node: ASTNode): Unit = {
    if (parentId == node.blockId) {
      FieldSetter.getFieldValue(node, fieldName) match {
        case Some(oldValue) =>
          NodeTypeHelpers.replaceChild(node, oldValue, newValue)
          newValue.setParentNode(node)
        case None =>
          logger.warn(
            s"Could not insert in field of $node, as there was no attribute named $fieldName!"
          )
      }
    } else {
      super.visit(node)
    }
  }
}

object InsertionInFieldVisitor {
  def apply[A <: ASTNode](
      rootNode: A,
      parent: ASTNode,
      newValue: ASTNode,
      fieldName: String
  ): A = {
    val v = new InsertionInFieldVisitor(parent, newValue, fieldName)
    rootNode.accept(v)
    rootNode
  }
}
