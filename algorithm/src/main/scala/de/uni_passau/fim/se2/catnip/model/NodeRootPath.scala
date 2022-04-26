package de.uni_passau.fim.se2.catnip.model

import de.uni_passau.fim.se2.catnip.common.FieldSetter
import de.uni_passau.fim.se2.catnip.util.{ActorDefinitionExt, ASTNodeExt}
import de.uni_passau.fim.se2.litterbox.ast.model.{
  ActorDefinition,
  ASTNode,
  Program
}

import scala.annotation.tailrec

sealed trait RootPathElement {
  def node: ASTNode
}

/** The root `node` of a [[NodeRootPath]].
  * @param node
  *   which has no parent and is therefore the root of a [[NodeRootPath]].
  */
final case class RootPathRoot(override val node: ASTNode)
    extends RootPathElement {
  require(
    node.getParentNode == null,
    "The parent of a RootPathRoot has to be null!"
  )
}

/** An element of a [[NodeRootPath]] where `node` can be found in the attribute
  * `fieldName` of its parent.
  * @param node
  *   as part of the root path.
  * @param fieldName
  *   the name of the attribute of the parent where `node` can be found.
  */
final case class RootPathElementField(
    override val node: ASTNode,
    fieldName: String
) extends RootPathElement {
  require(
    node.getParentNode != null,
    "The node of a RootPathElement has to have a parent!"
  )
}

/** An element of a [[NodeRootPath]] where `node` can be found at `index` in the
  * list of children of its parent.
  *
  * E.g. for nodes that are children of `StmtList`s.
  * @param node
  *   as part of the root path.
  * @param index
  *   at which `node` can be found in the list of children of its parent.
  */
final case class RootPathElementIndex(override val node: ASTNode, index: Int)
    extends RootPathElement {
  require(index >= 0, "The index of a child cannot be <0!")
  require(
    node.getParentNode != null,
    "The node of a RootPathElement has to have a parent!"
  )
  require(
    index < node.getParentNode.getChildren.size(),
    s"The index of a child cannot be >= than the parent’s children count (${node.getParentNode.getChildren.size()})!"
  )
}

/** The path from a node of the AST to the root (= program node) of the AST.
  *
  * The first element has to be a [[RootPathRoot]], then working down the tree.
  *
  * Uses a special definition for equality, see documentation for the overridden
  * [[equals]] method.
  * @param path
  *   as list of elements of the AST to be visited on the way from the root
  *   towards some node.
  */
final case class NodeRootPath(path: List[RootPathElement])
    extends Seq[RootPathElement] {
  require(path.nonEmpty, "The root path cannot be empty!")
  require(
    path match {
      case RootPathRoot(_) :: _ => true
      case _                    => false
    },
    "The root path has to contain a RootPathRoot as first element!"
  )

  /** Checks if two paths are equal according to the definition below.
    *
    * Two root paths are considered equal, if:
    *   - they have the same length, and
    *   - each of their [[RootPathElement]] s are equal:
    *     - if the referenced node is an `ActorDefinition`: they have the same
    *       name.
    *     - otherwise, the fieldName/index has to be equal and the class of the
    *       referenced node is the same.
    *
    * @param obj
    *   another object.
    * @return
    *   true, if `this == obj` per definition above.
    */
  override def equals(obj: Any): Boolean = {
    def pathElementEquals(
        some: RootPathElement,
        other: RootPathElement
    ): Boolean = {
      (some, other) match {
        case (
              RootPathElementIndex(a: ActorDefinition, _),
              RootPathElementIndex(b: ActorDefinition, _)
            ) =>
          a.same(b)
        case (RootPathRoot(a), RootPathRoot(b)) =>
          a.getClass.getName == b.getClass.getName
        case (RootPathElementField(a, fA), RootPathElementField(b, fB)) =>
          a.getClass.getName == b.getClass.getName && fA == fB
        case (RootPathElementIndex(a, idxA), RootPathElementIndex(b, idxB)) =>
          a.getClass.getName == b.getClass.getName && idxA == idxB
        case _ => false
      }
    }

    @tailrec
    def pathEquals(
        pathA: List[RootPathElement],
        pathB: List[RootPathElement]
    ): Boolean = {
      (pathA, pathB) match {
        case (Nil, Nil)          => true
        case (_, Nil) | (Nil, _) => false
        case (h1 :: t1, h2 :: t2) =>
          pathElementEquals(h1, h2) && pathEquals(t1, t2)
      }
    }

    obj match {
      case NodeRootPath(other) => pathEquals(this.path, other)
      case _                   => false
    }
  }

  override def apply(i: Int): RootPathElement = path(i)

  override def length: Int = path.length

  override def iterator: Iterator[RootPathElement] = path.iterator
}

object NodeRootPath {

  /** Computes the path from the root of the program to the given node.
    *
    * # Example
    *
    * ```scala
    * while (A) {
    *   if (C) {
    *     say("test");
    *   }
    * }
    * ```
    *
    * The root path for the constant "test" is [program, while, if, say].
    *
    * @param node
    *   to compute the path for.
    * @return
    *   a list of nodes on the path to the node.
    */
  def apply(node: ASTNode): NodeRootPath = {
    NodeRootPath(rootPath(List.empty, node))
  }

  /** Convenience method that forwards to apply on `ASTNode` for the inner node
    * of `node`.
    * @param node
    *   to compute the path for.
    * @return
    *   a list of nodes on the path to the node.
    */
  def apply(node: Nodes[?]): NodeRootPath = {
    apply(node.node)
  }

  @tailrec
  private def rootPath(
      acc: List[RootPathElement],
      node: ASTNode
  ): List[RootPathElement] = {
    node.parentNode match {
      case None => RootPathRoot(node) :: acc
      case Some(parent) =>
        val next = FieldSetter.getFieldNameWithValue(parent, node) match {
          case Some(fieldName) => RootPathElementField(node, fieldName)
          case None =>
            val idx = parent.getChildren.indexOf(node)
            RootPathElementIndex(node, idx)
        }
        rootPath(next :: acc, parent)
    }
  }

  /** Follows the root path as far as possible.
    *
    * The given program might not contain the `path` fully. In that case the
    * path is followed as long as there are nodes in the program that match
    * elements described in the path.
    *
    * # Example
    *
    * Root Path: GreenFlag -> If -> SetVariableTo Program:
    * ```
    * GreenFlag:
    * If (C) {
    * ChangeXBy(10);
    * ChangeYTo(120);
    * }
    * ```
    * Then this will return (GreenFlag, if) as the path could only be matched
    * this far.
    * @param program
    *   a program in which the root path should be followed.
    * @param path
    *   the path to follow.
    * @return
    *   the (partial) root path in the target program.
    */
  def followRootPath(program: Program, path: NodeRootPath): NodeRootPath = {
    val start = List(RootPathRoot(program))
    path.path match {
      case Nil       => NodeRootPath(start)
      case _ :: tail => followRootPath(program, start, tail)
    }
  }

  @tailrec
  private def followRootPath(
      currentParent: ASTNode,
      visitedPath: List[RootPathElement],
      remainingPath: List[RootPathElement]
  ): NodeRootPath = {
    remainingPath match {
      case RootPathElementField(origChild, fieldName) :: tail =>
        val parentFieldValue =
          FieldSetter.getFieldValue(currentParent, fieldName)
        // check if the current parent has a matching child in `fieldName`
        parentFieldValue match {
          case Some(c) if c.getClass.getName == origChild.getClass.getName =>
            followRootPath(
              c,
              RootPathElementField(c, fieldName) :: visitedPath,
              tail
            )
          case _ => NodeRootPath(visitedPath.reverse)
        }
      // only consider this part if the current parent even has enough
      // children to potentially have a matching child
      case RootPathElementIndex(origChild, index) :: tail
          if currentParent.getChildren.size() > index =>
        val currentChild = currentParent.getChildren.get(index)
        // check if the current parent has a matching child at `index`
        if (currentChild.getClass.getName == origChild.getClass.getName) {
          followRootPath(
            currentChild,
            RootPathElementIndex(currentChild, index) :: visitedPath,
            tail
          )
        } else {
          NodeRootPath(visitedPath.reverse)
        }
      case _ => NodeRootPath(visitedPath.reverse)
    }
  }
}
