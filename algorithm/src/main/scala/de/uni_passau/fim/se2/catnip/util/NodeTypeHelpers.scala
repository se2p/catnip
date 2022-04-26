package de.uni_passau.fim.se2.catnip.util

import de.uni_passau.fim.se2.catnip.common.FieldSetter
import de.uni_passau.fim.se2.litterbox.ast.model.*
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt
import org.slf4j.LoggerFactory

import java.util
import scala.jdk.CollectionConverters.*

/** Provides helper methods to modify LitterBox `ASTNode`s.
  */
object NodeTypeHelpers {
  private val logger = LoggerFactory.getLogger(this.getClass)

  /** Removes a statement from a statement list.
    *
    * Also removes it from the list of children of `parent`.
    * @param parent
    *   of `oldChild`.
    * @param oldChild
    *   the statement that should be removed.
    */
  def deleteStmt(parent: StmtList, oldChild: Stmt): Unit = {
    val stmts = new util.ArrayList(parent.getStmts)
    stmts.remove(oldChild)
    FieldSetter.setField(parent, "stmts", stmts)

    deleteFromChildren(parent, oldChild)
    oldChild.setParentNode(null)
  }

  /** Removes a script from a script list.
    *
    * Also removes it from the list of children of `parent`.
    * @param parent
    *   of `oldChild`.
    * @param oldChild
    *   the script that should be deleted.
    */
  def deleteScript(parent: ScriptList, oldChild: Script): Unit = {
    val scripts = new util.ArrayList(parent.getScriptList)
    scripts.remove(oldChild)
    FieldSetter.setField(parent, "scriptList", scripts)

    deleteFromChildren(parent, oldChild)
    oldChild.setParentNode(null)
  }

  /** Replaces the child `oldChild` of `parent` with `newChild`.
    *
    * Performs the replacement in the children list and if it is directly stored
    * inside an attribute of `parent`.
    * @param parent
    *   the parent of `oldChild`, i.e. `oldChild.getParentNode` must return
    *   `parent`.
    * @param oldChild
    *   the child that should be replaced.
    * @param newChild
    *   the node that should replace `oldChild` as child.
    */
  def replaceChild(
      parent: ASTNode,
      oldChild: ASTNode,
      newChild: ASTNode
  ): Unit = {
    require(
      oldChild.parentNode.isEmpty
        || oldChild.parentNode.flatMap(_.blockIdOpt) == parent.blockIdOpt
    )

    (parent, oldChild, newChild) match {
      case (s: StmtList, o: Stmt, n: Stmt) => replaceChildStmtList(s, o, n)
      case (s: ScriptList, o: Script, n: Script) =>
        replaceChildScriptList(s, o, n)
      case _ => replaceChildField(parent, oldChild, newChild)
    }

    replaceInChildren(parent, oldChild, newChild)

    newChild.setParentNode(parent)
  }

  private def replaceChildStmtList(
      parent: StmtList,
      oldChild: Stmt,
      newChild: Stmt
  ): Unit = {
    val stmts = new util.ArrayList[Stmt](parent.getStmts)
    val idx   = stmts.asScala.indexWhere(_.structurallyEqual(oldChild))
    if (idx >= 0) {
      stmts.set(idx, newChild)
      FieldSetter.setField(parent, "stmts", stmts)
    } else {
      logger.warn(s"Could not replace statement $oldChild in $parent.")
    }
  }

  private def replaceChildScriptList(
      parent: ScriptList,
      oldChild: Script,
      newChild: Script
  ): Unit = {
    val scripts = new util.ArrayList[Script](parent.getScriptList)
    val idx     = scripts.asScala.indexWhere(_.structurallyEqual(oldChild))
    scripts.set(idx, newChild)
    FieldSetter.setField(parent, "scriptList", scripts)
  }

  /** Replaces `oldChild` in the children field of `node` by `newChild`.
    * @param parent
    *   the ASTNode which child should be changed.
    * @param oldChild
    *   the child to replace.
    * @param newChild
    *   the node to replace `oldChild` with.
    */
  private def replaceInChildren(
      parent: ASTNode,
      oldChild: ASTNode,
      newChild: ASTNode
  ): Unit = {
    val idx = parent.getChildren.indexOf(oldChild)
    if (idx > -1) {
      replaceInChildren(parent, newChild, idx)
    }
  }

  private def deleteFromChildren(parent: ASTNode, oldChild: ASTNode): Unit = {
    val children = new util.ArrayList(parent.getChildren)
    children.remove(oldChild)
    FieldSetter.setField(parent, "children", children)
  }

  /** Replaces the child at `index` in the children list of `node` by
    * `newChild`.
    * @param parent
    *   the ASTNode which child should be changed.
    * @param newChild
    *   the node to insert as replacement at `index`.
    * @param index
    *   the position at which `newChild` should be placed.
    */
  private def replaceInChildren(
      parent: ASTNode,
      newChild: ASTNode,
      index: Int
  ): Unit = {
    if (index >= 0 && index < parent.getChildren.size()) {
      val children = new java.util.ArrayList[ASTNode](parent.getChildren)
      children.set(index, newChild)
      FieldSetter.setField(parent, "children", children)
    }
  }

  /** Finds a field of `parent` that contains `oldChild` and replaces its value
    * with `newChild`.
    *
    * If no field with value `oldChild` is found, then no change is made.
    * @param parent
    *   the parent node of `oldChild`.
    * @param oldChild
    *   the child of `parent` that should be replaced.
    * @param newChild
    *   the value to store in the attribute of `parent` instead of `oldChild`.
    */
  private def replaceChildField(
      parent: ASTNode,
      oldChild: ASTNode,
      newChild: ASTNode
  ): Unit = {
    FieldSetter
      .getFieldNameWithValue(parent, oldChild)
      .foreach(FieldSetter.setField(parent, _, newChild))
  }
}
