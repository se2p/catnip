package de.uni_passau.fim.se2.catnip.model

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.ElementChoice
import de.uni_passau.fim.se2.litterbox.ast.model.event.Event
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.BoolExpr
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.NumExpr
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.StringExpr
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Identifier
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata
import de.uni_passau.fim.se2.litterbox.ast.model.position.Position
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.Touchable
import de.uni_passau.fim.se2.litterbox.ast.visitor.{
  CloneVisitor,
  ScratchVisitor
}

import java.util

/** A dummy node not containing any data that can be inserted into the AST at
  * any place.
  *
  * Any two instances of [[DummyNode]] are equal to each other.
  */
class DummyNode
    extends ASTNode
    with BoolExpr
    with ElementChoice
    with Event
    with Identifier
    with NumExpr
    with Position
    with Stmt
    with StringExpr
    with Touchable {
  override def getChildren: util.List[? <: ASTNode] =
    new util.ArrayList[ASTNode]()

  /** The [[DummyNode]] does not contain any information about its parent.
    * @return
    *   always `null`.
    */
  override def getParentNode: ASTNode = null

  /** No-op.
    * @param astNode
    *   ignored.
    */
  override def setParentNode(astNode: ASTNode): Unit = {}

  override def getUniqueName: String = "DummyNode"

  /** The [[DummyNode]] does not contain any metadata information.
    * @return
    *   always `null`.
    */
  override def getMetadata: BlockMetadata = null

  /** No-op.
    */
  override def accept(scratchVisitor: ScratchVisitor): Unit = {}

  override def accept(cloneVisitor: CloneVisitor): ASTNode = new DummyNode

  override def equals(obj: Any): Boolean = obj match {
    case _: DummyNode => true
    case _            => false
  }
}
