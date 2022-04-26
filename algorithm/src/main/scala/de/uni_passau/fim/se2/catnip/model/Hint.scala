package de.uni_passau.fim.se2.catnip.model

import de.uni_passau.fim.se2.catnip.common.FieldSetter
import de.uni_passau.fim.se2.catnip.util.{
  ActorDefinitionExt,
  ASTNodeExt,
  ASTNodeExtTyped
}
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinitionList

import scala.util.Failure
import scala.util.Success
import scala.util.Try
import scala.jdk.CollectionConverters.*

/** Base class for other hint types.
  * @param references
  *   the node the hint wants to change/has additional information for.
  */
sealed abstract class Hint(val references: StudentNode) {

  /** The Id of the actor `references` belongs to.
    */
  val actorId: Option[String] =
    references.node.actorParent.map(_.getIdent.getName)
}

/** Types:
  *   - Insertion
  *   - Deletion
  *   - Moving
  *   - Reordering
  *   - Replacement
  * @param node
  *   the block this hint references.
  */
sealed abstract class StructuralHint(node: StudentNode) extends Hint(node)

sealed trait InsertHint extends StructuralHint {
  def parent: StudentNode
}

sealed trait MoveHint extends StructuralHint {
  def newParent: StudentNode
}

sealed trait ReplaceHint extends StructuralHint {}

/** A hint that suggests deleting `node` from the program.
  * @param node
  *   the block this hint references.
  */
final case class DeletionHint(node: StudentNode) extends StructuralHint(node)

/** A hint that suggests adding a new `node` to the children list of `parent`.
  * @param parent
  *   the node that should receive `node` as a new child.
  * @param node
  *   the block this hint references.
  * @param position
  *   the index in the children list of `parent` at which `node` should be
  *   inserted.
  */
final case class InsertionHint private (
    parent: StudentNode,
    node: ASTNode,
    position: Int
) extends StructuralHint(parent)
    with InsertHint

object InsertionHint {
  def apply(
      parent: StudentNode,
      node: ASTNode,
      position: Int
  ): InsertionHint = {
    new InsertionHint(parent, node.cloned, position)
  }
}

/** A hint that suggests adding a new `node` into a certain attribute of
  * `parent`.
  * @param parent
  *   an existing node of the program that should get the new child `node`.
  * @param node
  *   the block this hint references.
  * @param fieldName
  *   the name of the attribute of `parent` in which `node` should be placed.
  */
final case class InsertInFieldHint private (
    parent: StudentNode,
    node: ASTNode,
    fieldName: String
) extends StructuralHint(parent)
    with InsertHint

object InsertInFieldHint {
  def apply(
      parent: StudentNode,
      node: ASTNode,
      fieldName: String
  ): InsertInFieldHint = {
    new InsertInFieldHint(parent, node.cloned, fieldName)
  }
}

/** A hint that suggests to move an existing `node` in the program to a new
  * parent’s children list.
  * @param newParent
  *   the new parent of `node`.
  * @param node
  *   the block this hint references.
  * @param position
  *   the index at which `node` should be added into the children list of
  *   `newParent`.
  */
final case class MoveToPosHint private (
    newParent: StudentNode,
    node: StudentNode,
    position: Int
) extends StructuralHint(node)
    with MoveHint

object MoveToPosHint {
  def apply(
      newParent: StudentNode,
      node: StudentNode,
      position: Int
  ): MoveToPosHint = {
    require(position >= 0)
    new MoveToPosHint(newParent, StudentNode(node.node.cloned), position)
  }
}

/** A hint that suggests to move an existing `node` in the program to a new
  * parent.
  * @param newParent
  *   the new parent `node` should be added to as a child.
  * @param node
  *   the block this hint references.
  * @param fieldName
  *   the name of the attribute of `newParent` in which `node` should be placed.
  */
final case class MoveInFieldHint private (
    newParent: StudentNode,
    node: StudentNode,
    fieldName: String
) extends StructuralHint(node)
    with MoveHint

object MoveInFieldHint {
  def apply(
      newParent: StudentNode,
      node: StudentNode,
      fieldName: String
  ): MoveInFieldHint = {
    new MoveInFieldHint(newParent, StudentNode(node.node.cloned), fieldName)
  }
}

/** A hint that suggests a different place for `node` in the list of children of
  * `parent`.
  * @param parent
  *   the parent of `node`.
  * @param node
  *   the block this hint references.
  * @param newPosition
  *   the new index of `node` in the list of children of `parent`.
  */
final case class ReorderHint(
    parent: StudentNode,
    node: StudentNode,
    newPosition: Int
) extends StructuralHint(node)

/** A hint that suggests to look for `oldNode` and replace it by `newNode`.
  * @param parent
  *   the parent of the node that should be replaced.
  * @param fieldName
  *   the name of the field in which `newNode` should be placed.
  * @param newNode
  *   the new child that should replace `oldNode` as child of `oldNode.parent`.
  */
final case class ReplaceFieldHint private (
    parent: StudentNode,
    fieldName: String,
    newNode: ASTNode
) extends StructuralHint(parent)
    with ReplaceHint

object ReplaceFieldHint {

  /** Generates a new hint that suggests replacing `oldNode` with `newNode`.
    * @param oldNode
    *   that should be replaced by `newNode`.
    * @param newNode
    *   that should be inserted to replace `oldNode`.
    * @return
    *   a hint, if `oldNode` has a parent which contains a field that contains
    *   `oldNode`; nothing otherwise.
    */
  def apply(
      oldNode: StudentNode,
      newNode: ASTNode
  ): Option[ReplaceFieldHint] = {
    for {
      parent <- oldNode.parentNode
      field  <- FieldSetter.getFieldNameWithValue(parent.node, oldNode.node)
    } yield new ReplaceFieldHint(parent, field, newNode.cloned)
  }

  def apply(
      parent: StudentNode,
      fieldName: String,
      newNode: ASTNode
  ): ReplaceFieldHint = {
    new ReplaceFieldHint(parent, fieldName, newNode)
  }
}

final case class ReplaceStmtHint private (
    parent: StudentNode,
    oldNode: StudentNode,
    newNode: ASTNode,
    index: Int
) extends StructuralHint(parent)
    with ReplaceHint

object ReplaceStmtHint {
  def apply(
      parent: StudentNode,
      oldNode: StudentNode,
      newNode: ASTNode,
      index: Int
  ): Try[ReplaceStmtHint] = {
    parent match {
      case StudentNode(s: StmtList)
          if index >= 0 && index < s.getStmts.size() =>
        Success(new ReplaceStmtHint(parent, oldNode, newNode.cloned, index))
      case StudentNode(_: StmtList) =>
        Failure(
          new IllegalArgumentException(
            "Cannot create a replacement hint if the index is out-of-bounds!"
          )
        )
      case _ =>
        Failure(
          new IllegalArgumentException(
            "Cannot create a replacement hint for a child if the parent is not a StmtList!"
          )
        )
    }
  }
}

final case class MissingActorHint private (
    actorList: StudentNode,
    actor: ActorDefinition
) extends StructuralHint(actorList)
    with InsertHint {
  override def parent: StudentNode = actorList
}

object MissingActorHint {

  /** Creates a MissingActorHint if it is possible to do so.
    *
    * # Failures
    *   - `actorList` is not actually an `ActorDefinitionList`.
    *   - the program already contains an actor with the same name as `actor`.
    *
    * @param actorList
    *   the actors of the program which the new actor should be added to.
    * @param actor
    *   the actor that should be added to the program.
    * @return
    *   a [[MissingActorHint]] if the prerequisites as defined above are met.
    */
  def apply(
      actorList: StudentNode,
      actor: ActorDefinition
  ): Try[MissingActorHint] = {
    actorList match {
      case StudentNode(al: ActorDefinitionList) =>
        val alreadyHasActorSameName =
          al.getDefinitions.asScala.exists(_.same(actor))

        if (alreadyHasActorSameName) {
          Failure(
            new IllegalArgumentException(
              s"Cannot create a MissingActorHint if an actor with the same name '${actor.getIdent.getName}' already exists!"
            )
          )
        } else {
          Success(new MissingActorHint(actorList, actor))
        }
      case _ =>
        Failure(
          new IllegalArgumentException(
            "Cannot create a MissingActorHint if the actorList is not of type ActorDefinitionList!"
          )
        )
    }
  }
}
