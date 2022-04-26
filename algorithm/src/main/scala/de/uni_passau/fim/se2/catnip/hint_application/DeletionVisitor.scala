package de.uni_passau.fim.se2.catnip.hint_application

import de.uni_passau.fim.se2.catnip.common.FieldSetter
import de.uni_passau.fim.se2.catnip.model.{DummyNode, NodeRootPath}
import de.uni_passau.fim.se2.catnip.util.{ASTNodeExt, NodeTypeHelpers}
import de.uni_passau.fim.se2.litterbox.ast.model.{
  ASTNode,
  Script,
  ScriptList,
  StmtList
}
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor
import org.slf4j.LoggerFactory

import java.util
import scala.jdk.CollectionConverters.*
import scala.util.{Failure, Success, Try}

private class DeletionVisitor(val shouldBeDeleted: ASTNode)
    extends ScratchVisitor {
  private val logger = LoggerFactory.getLogger(this.getClass)

  private val parentPath = NodeRootPath(shouldBeDeleted.getParentNode)

  private val shouldBeDeletedId = shouldBeDeleted.blockIdOpt

  override def visit(node: ASTNode): Unit = {
    val path = NodeRootPath(node)
    if (parentPath == path) {
      val children = new util.ArrayList(node.getChildren)
      val oldChild = children.asScala
        .find(child =>
          shouldBeDeletedId.contains(child.blockId) || child == shouldBeDeleted
        )
      oldChild.foreach(children.remove(_))
      FieldSetter.setField(node, "children", children)

      (node, oldChild) match {
        case (s: StmtList, Some(old: Stmt)) =>
          NodeTypeHelpers.deleteStmt(s, old)
        case (s: ScriptList, Some(old: Script)) =>
          NodeTypeHelpers.deleteScript(s, old)
        case (parent, Some(old: StmtList)) =>
          FieldSetter.replaceValueInField(
            parent,
            old,
            new StmtList(List.empty.asJava)
          )
        case (parent, Some(old)) =>
          FieldSetter.replaceValueInField(parent, old, new DummyNode)
        case (_, None) =>
          logger.warn(
            s"Could not delete $shouldBeDeleted as child of $node!"
          )
      }
    } else {
      super.visit(node)
    }
  }
}

object DeletionVisitor {
  private val logger = LoggerFactory.getLogger(this.getClass)

  /** Removes the given node.
    *
    * Does not create a deep-copy of the program but instead changes `program`
    * directly.
    * @param rootNode
    *   the node in the subtree of which the other node should be deleted.
    * @param shouldBeDeleted
    *   the node to delete.
    * @tparam A
    *   the concrete type of the node in the subtree of which the change should
    *   be made.
    * @return
    *   the same program as in the input but without the `shouldBeDeleted` node.
    */
  def apply[A <: ASTNode](rootNode: A, shouldBeDeleted: ASTNode): A = {
    Try {
      val v = new DeletionVisitor(shouldBeDeleted)
      rootNode.accept(v)
    } match {
      case Failure(err) =>
        logger.warn(
          s"Could not apply deletion hint for Node $shouldBeDeleted (${NodeRootPath(shouldBeDeleted)})!",
          err
        )
      case Success(_) => // do nothing
    }
    rootNode
  }
}
