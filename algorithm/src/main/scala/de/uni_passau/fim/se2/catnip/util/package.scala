package de.uni_passau.fim.se2.catnip

import de.uni_passau.fim.se2.catnip.model.{BlockId, Nodes}
import de.uni_passau.fim.se2.litterbox.ast.model.event.EventAttribute
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.{
  BiggerThan,
  BoolExpr,
  LessThan
}
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.{
  Add,
  Div,
  LengthOfString,
  LengthOfVar,
  Minus,
  Mod,
  MouseX,
  MouseY,
  Mult,
  NumExpr,
  NumFunct,
  PositionX,
  PositionY
}
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.attributes.FixedAttribute
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.{
  NameNum,
  StringExpr
}
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.LocalIdentifier
import de.uni_passau.fim.se2.litterbox.ast.model.literals.{
  BoolLiteral,
  ColorLiteral,
  NumberLiteral,
  StringLiteral
}
import de.uni_passau.fim.se2.litterbox.ast.model.{
  ActorDefinition,
  ActorDefinitionList,
  ActorType,
  ASTLeaf,
  ASTNode,
  Program,
  StmtList
}
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.Metadata
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astLists.{
  FieldsMetadataList,
  InputMetadataList
}
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.{
  DataBlockMetadata,
  NonDataBlockMetadata
}
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.{
  GraphicEffect,
  SwitchBackdrop,
  SwitchBackdropAndWait
}
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorsound.SoundEffect
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.{
  ChangeVariableBy,
  SetVariableTo
}
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.{
  IfStmt,
  RepeatForeverStmt,
  RepeatTimesStmt,
  UntilStmt
}
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.{
  ForwardBackwardChoice,
  LayerChoice,
  Say,
  SayForSecs,
  Think,
  ThinkForSecs
}
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.{
  ChangeXBy,
  ChangeYBy,
  DragMode,
  GlideSecsTo,
  GlideSecsToXY,
  GoToPos,
  GoToPosXY,
  RotationStyle,
  SetXTo,
  SetYTo,
  TurnLeft,
  TurnRight
}
import de.uni_passau.fim.se2.litterbox.ast.model.timecomp.TimeComp
import de.uni_passau.fim.se2.litterbox.ast.visitor.CloneVisitor

import scala.annotation.tailrec
import scala.jdk.CollectionConverters.*
import scala.util.Try

package object util {

  /** Only keeps elements of `nodes` that are not (transitive) children of other
    * elements in the list.
    * @param nodes
    *   some list of AST nodes.
    * @return
    *   a list of elements of `nodes` that are not children of each other.
    */
  def removeTransitiveChildren[A <: Nodes[A]](nodes: List[A]): List[A] = {
    // get all nodes that are transitive children of another node
    val children = nodes
      .map(_.node)
      .flatMap(n => NodeListVisitor(n, v => v != n))
      .toSet
    // remove them from the list of nodes
    nodes.filterNot(n => children.contains(n.node))
  }

  /** Provides extension methods for the LitterBox type `ASTNode`.
    * @param node
    *   on which the methods operate.
    */
  implicit class ASTNodeExt(node: ASTNode) {
    def parentNode: Option[ASTNode] = Option(node.getParentNode)

    def blockId: BlockId = {
      node.blockIdOpt.getOrElse(BlockId.dummy)
    }

    def blockIdOpt: Option[BlockId] = {
      Try(node.getMetadata).toOption
        .flatMap {
          case meta: NonDataBlockMetadata => Some(meta.getBlockId)
          case meta: DataBlockMetadata    => Some(meta.getBlockId)
          case _                          => None
        }
        .flatMap(BlockId(_).toOption)
    }

    /** Checks if the given node is a container node that contains a list of
      * child nodes.
      *
      * Containers:
      *
      *   - StmtList
      *
      * @return
      *   true, if it is a container; false otherwise.
      */
    def isContainer: Boolean = {
      node match {
        case _: StmtList => true
        case _           => false
      }
    }

    /** Checks if the node is a metadata node.
      * @return
      *   true, if the node is metadata.
      */
    def isMetadata: Boolean = {
      node match {
        case _: Metadata           => true
        case _: InputMetadataList  => true
        case _: FieldsMetadataList => true
        case _                     => false
      }
    }

    /** Checks if the node is a leaf of the AST.
      * @return
      *   true, if the node is an ASTLeaf.
      */
    def isASTLeaf: Boolean = {
      node match {
        case _: ASTLeaf => true
        case _          => false
      }
    }

    /** Convenience method to access the children of the node without metadata.
      * @return
      *   the list of children without metadata nodes.
      */
    def filteredChildren: IndexedSeq[ASTNode] = {
      node.getChildren.asScala.filterNot(_.isMetadata).toIndexedSeq
    }

    /** Finds the actor the node transitively belongs to.
      * @return
      *   the actor definition the node belongs to. None, if the node itself is
      *   a (transitive) parent of an actor.
      */
    def actorParent: Option[ActorDefinition] = {
      @tailrec
      def inner(n: ASTNode): Option[ActorDefinition] = n match {
        case null | _: Program | _: ActorDefinitionList => None
        case a: ActorDefinition                         => Some(a)
        case _ => inner(n.getParentNode)
      }

      inner(node)
    }

    /** Finds the program the node belongs to.
      * @return
      *   the program the node belongs to. None, if some parent node pointer on
      *   the path to the root was `null`.
      */
    def program: Option[Program] = {
      @tailrec
      def inner(n: Option[ASTNode]): Option[Program] = n match {
        case None             => None
        case Some(p: Program) => Some(p)
        case Some(v)          => inner(v.parentNode)
      }

      inner(Some(node))
    }
  }

  /** Further extension methods on `ASTNodes`, but ones that benefit from
    * knowing the concrete type of the node.
    * @param node
    *   on which the methods operate.
    * @tparam N
    *   the concrete type of `node`.
    */
  implicit class ASTNodeExtTyped[N <: ASTNode](node: N) {

    /** Adds a parent to `node`.
      * @param parent
      *   that should be set in `node`.
      * @tparam A
      *   the concrete type of the parent.
      * @return
      *   `node`, but with the new parent set.
      */
    def withParentNode[A <: ASTNode](parent: A): N = {
      node.setParentNode(parent)
      node
    }

    /** Creates a deep copy of the node using a `CloneVisitor`.
      * @return
      *   a copy of `node`.
      */
    def cloned: N = {
      val newNode = node.accept(new CloneVisitor).asInstanceOf[N]
      newNode.withParentNode(node.getParentNode)
    }
  }

  /** Provides extension methods for the LitterBox type `ASTNode` for comparing
    * them to other nodes.
    * @param node
    *   on which the methods operate.
    */
  implicit class ASTNodeSimilarity(node: ASTNode) {

    /** Checks if this node and `other` are transitive children of the same
      * actor.
      * @param other
      *   some other node of the AST.
      * @return
      *   true, if the nodes are children of the same actor or neither of them
      *   belong to any actor.
      */
    def belongsToSameActor(other: ASTNode): Boolean = {
      (node.actorParent, other.actorParent) match {
        case (Some(actor1), Some(actor2)) => actor1.same(actor2)
        case (None, None)                 => true
        case _                            => false
      }
    }

    def verySimilar(other: ASTNode): Boolean = {
      (node, other) match {
        // Variables starting with the same name => part of normalisation: same type gets same identifier
        case (a: LocalIdentifier, b: LocalIdentifier) =>
          a.getName.head == b.getName.head
        case (_, _) if node.getClass.getName == other.getClass.getName => true
        case (_: IfStmt, _: IfStmt)                                    => true
        // NumExprs
        case (_: Add, _: Minus) | (_: Minus, _: Add) => true
        case (_: Mult, _: Div) | (_: Div, _: Mult)   => true
        case (_: Div, _: Mod) | (_: Mod, _: Div)     => true
        case (_: LengthOfString, _: LengthOfVar) |
            (_: LengthOfVar, _: LengthOfString) =>
          true
        case (_: MouseX, _: MouseY) | (_: MouseY, _: MouseX)             => true
        case (_: PositionX, _: PositionY) | (_: PositionY, _: PositionX) => true
        // BoolExprs
        case (_: LessThan, _: BiggerThan) | (_: BiggerThan, _: LessThan) => true
        // Others
        case (_: SwitchBackdrop, _: SwitchBackdropAndWait) |
            (_: SwitchBackdropAndWait, _: SwitchBackdrop) =>
          true
        case (_: Say, _: SayForSecs) | (_: SayForSecs, _: Say)           => true
        case (_: Think, _: ThinkForSecs) | (_: ThinkForSecs, _: Think)   => true
        case (_: ChangeXBy, _: ChangeYBy) | (_: ChangeYBy, _: ChangeXBy) => true
        case (_: SetXTo, _: SetYTo) | (_: SetYTo, _: SetXTo)             => true
        case (_: GlideSecsTo, _: GlideSecsToXY) |
            (_: GlideSecsToXY, _: GlideSecsTo) =>
          true
        case (_: GoToPos, _: GoToPosXY) | (_: GoToPosXY, _: GoToPos)   => true
        case (_: TurnLeft, _: TurnRight) | (_: TurnRight, _: TurnLeft) => true
        case (_, _)                                                    => false
      }
    }

    def similar(other: ASTNode): Boolean = {
      (node, other) match {
        case (_, _) if node.getClass.getName == other.getClass.getName => true
        case (_: IfStmt, _: IfStmt)                                    => true
        case (_: BoolExpr, _: BoolExpr)                                => true
        case (_: NumExpr, _: NumExpr)                                  => true
        case (_: StringExpr, _: StringExpr)                            => true
        case (_: Say, _: Think) | (_: Think, _: Say)                   => true
        case (_: Say, _: ThinkForSecs) | (_: ThinkForSecs, _: Say)     => true
        case (_: SayForSecs, _: Think) | (_: Think, _: SayForSecs)     => true
        case (_: ChangeVariableBy, _: SetVariableTo) |
            (_: SetVariableTo, _: ChangeVariableBy) =>
          true
        case (_: RepeatTimesStmt, _: UntilStmt) |
            (_: UntilStmt, _: RepeatTimesStmt) =>
          true
        case (_: RepeatForeverStmt, _: UntilStmt) |
            (_: UntilStmt, _: RepeatForeverStmt) =>
          true
        case (_, _) => false
      }
    }

    /** Compares the two nodes for structural equality.
      *
      * This overcomes the problem that nodes are not properly equal, as their
      * unique IDs are different.
      *
      * It is defined as:
      *
      *   - If their class is different, they cannot be equal.
      *   - If they are of the same class, they are structurally equal if all
      *     their children are pairwise structurally equal.
      *
      * @param other
      *   another node.
      * @tparam B
      *   the concrete type of the second node.
      * @return
      *   true, if `a` and `b` are structurally equal.
      */
    def structurallyEqual[B <: ASTNode](other: B): Boolean = {
      (node, other) match {
        case (a, b) if a.getClass.getName != b.getClass.getName => false
        case (a: ASTLeaf, b: ASTLeaf) => a.structurallyEqual(b)
        case (a, b) if a.getChildren.size() != b.getChildren.size() => false
        case (a, b) =>
          a.filteredChildren
            .lazyZip(b.filteredChildren)
            .forall(_.structurallyEqual(_))
      }
    }

    /** Compares the two nodes for structural equality.
      *
      * It is defined as:
      *
      *   - If their class is different, they cannot be equal.
      *   - If they contain some internal value and implement a proper equals
      *     method on that, they are equal if their internal value is equal.
      *   - Otherwise they are equal if their class is the same.
      *
      * @param other
      *   another leaf of the program AST.
      * @return
      *   true, if `a` and `b` are structurally equal.
      */
    def structurallyEqual(other: ASTLeaf): Boolean = {
      val eps = 1e-6

      (node, other) match {
        case (a, b) if a.getClass.getName != b.getClass.getName => false
        case (a: BoolLiteral, b: BoolLiteral) => a.getValue == b.getValue
        case (a: ColorLiteral, b: ColorLiteral) =>
          a.getRed == b.getRed && a.getBlue == b.getBlue && a.getGreen == b.getGreen
        case (a: NumberLiteral, b: NumberLiteral) =>
          math.abs(a.getValue - b.getValue) < eps
        case (a: StringLiteral, b: StringLiteral)   => a.getText == b.getText
        case (a: ActorType, b: ActorType)           => a == b
        case (a: EventAttribute, b: EventAttribute) => a == b
        case (a: NumFunct, b: NumFunct)             => a == b
        case (a: NameNum, b: NameNum)               => a == b
        case (a: FixedAttribute, b: FixedAttribute) => a == b
        case (a: GraphicEffect, b: GraphicEffect)   => a == b
        case (a: SoundEffect, b: SoundEffect)       => a == b
        case (a: ForwardBackwardChoice, b: ForwardBackwardChoice) => a == b
        case (a: LayerChoice, b: LayerChoice)                     => a == b
        case (a: DragMode, b: DragMode)                           => a == b
        case (a: RotationStyle, b: RotationStyle)                 => a == b
        case (a: TimeComp, b: TimeComp)                           => a == b
        case _ /* (a, b) if a.getClass == b.getClass */           => true
      }
    }
  }

  /** Provides extension methods for the LitterBox type `Program`.
    * @param program
    *   on which the methods operate.
    */
  implicit class ProgramExt(program: Program) {

    /** The identifier of the program originally obtained from the filename when
      * parsed.
      * @return
      *   the name of the program.
      */
    @inline
    def name: String = program.getIdent.getName
  }

  /** Provides extension methods for the LitterBox type `ActorDefinition`.
    * @param node
    *   on which the methods operate.
    */
  implicit class ActorDefinitionExt(node: ActorDefinition) {
    @inline
    def name: String = node.getIdent.getName

    /** Checks if the two actors can be considered equal.
      *
      * True equality cannot always be used as it is only implemented as
      * referential equality. Therefore, a check based on the type and name is
      * used.
      * @param other
      *   another actor.
      * @return
      *   true, if the two actors are considered equal.
      */
    def same(other: ActorDefinition): Boolean = {
      (node == other) || (node.getActorType == other.getActorType && node.name == other.name)
    }
  }
}
