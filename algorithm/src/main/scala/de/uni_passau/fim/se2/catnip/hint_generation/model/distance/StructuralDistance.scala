package de.uni_passau.fim.se2.catnip.hint_generation.model.distance

import de.uni_passau.fim.se2.litterbox.ast.model.{ASTLeaf, ASTNode}
import de.uni_passau.fim.se2.catnip.util.{ASTNodeExt, ASTNodeSimilarity}

/** Calculates the structural distance between `from` and `to`.
  *
  *   - It is zero, if `from` and `to` are structurally equal.
  *   - It is one, if `from` and `to` are of different type.
  *   - Otherwise the distance scales with the depth in the tree at which the
  *     difference occurred. That way differences close to the root of the
  *     subtree are penalised heavier.
  *
  * Examples:
  *
  *   - `from` and `to` are different: 1.0
  *   - The children of `from` and `to` are different: 0.5
  *   - The children of children are different: 0.33
  *   - Children of children of children: 0.25
  */
object StructuralDistance extends NodeDistanceMetric {

  /** Some function that compares two given node and returns true if they are
    * considered equal.
    * @tparam A
    *   the concrete type of the left-hand node of the comparison.
    * @tparam B
    *   the concrete type of the right-hand node of the comparison.
    */
  type EqualsChecker[A <: ASTNode, B <: ASTNode] = (A, B) => Boolean

  private def simpleEquality[A <: ASTNode, B <: ASTNode](
      a: A,
      b: B
  ): Boolean = {
    a.getClass.getName == b.getClass.getName || a.verySimilar(b)
  }

  /** Calculates the structural distance between `from` and `to`.
    *
    *   - It is zero, if `from` and `to` are structurally equal.
    *   - It is one, if `from` and `to` are of different type.
    *   - Otherwise the distance scales with the depth in the tree at which the
    *     difference occurred. That way differences close to the root of the
    *     subtree are penalised heavier.
    *
    * Examples:
    *
    *   - `from` and `to` are different: 1.0
    *   - The children of `from` and `to` are different: 0.5
    *   - The children of children are different: 0.33
    *   - Children of children of children: 0.25
    *
    * @param from
    *   a T.
    * @param to
    *   another T.
    * @return
    *   a distance between `from` and `to` in range <math
    *   xmlns="http://www.w3.org/1998/Math/MathML"
    *   display="inline"><mo>[</mo><mn>0.0</mn><mo>,</mo><mi
    *   mathvariant="normal">∞</mi><mo>)</mo></math>.
    */
  override def distance(
      from: ASTNode,
      to: ASTNode
  ): Distance[ASTNode, ASTNode] = {
    if (from == to) {
      Distance(from, to, 0.0)
    } else {
      distanceCustomEq(from, to)(simpleEquality)(simpleEquality)
    }
  }

  /** Works identical to [[distance]], but checks leaves of the AST not for
    * equal/similar class, but checks their actual value.
    * @param from
    *   one root of an AST.
    * @param to
    *   another root an AST.
    * @return
    *   a distance between `from` and `to` in range <math
    *   xmlns="http://www.w3.org/1998/Math/MathML"
    *   display="inline"><mo>[</mo><mn>0.0</mn><mo>,</mo><mi
    *   mathvariant="normal">∞</mi><mo>)</mo></math>.
    */
  def distanceCheckingLiteralValue(
      from: ASTNode,
      to: ASTNode
  ): Distance[ASTNode, ASTNode] = {
    if (from == to) {
      Distance(from, to, 0.0)
    } else {
      def equalityLeaf(a: ASTLeaf, b: ASTLeaf): Boolean = a.structurallyEqual(b)
      distanceCustomEq(from, to)(simpleEquality)(equalityLeaf)
    }
  }

  /** Works identical to [[distance]], but allows to give custom equality
    * definitions for ASTNodes of the tree.
    * @param from
    *   one root of an AST.
    * @param to
    *   another root an AST.
    * @param nodeEquals
    *   defines equality for two nodes of the AST.
    * @param leafEquals
    *   defines equality for two leaves of the AST.
    * @return
    *   a distance between `from` and `to` in range <math
    *   xmlns="http://www.w3.org/1998/Math/MathML"
    *   display="inline"><mo>[</mo><mn>0.0</mn><mo>,</mo><mi
    *   mathvariant="normal">∞</mi><mo>)</mo></math>.
    */
  def distanceCustomEq(from: ASTNode, to: ASTNode)(
      nodeEquals: EqualsChecker[ASTNode, ASTNode]
  )(leafEquals: EqualsChecker[ASTLeaf, ASTLeaf]): Distance[ASTNode, ASTNode] = {
    val (equals, depth) = walkTree(from, to)(nodeEquals)(leafEquals)
    distanceFromResult(from, to, equals, depth)
  }

  private def distanceFromResult(
      from: ASTNode,
      to: ASTNode,
      equals: Boolean,
      depth: Int
  ): Distance[ASTNode, ASTNode] = {
    if (equals) {
      Distance(from, to, 0.0)
    } else {
      Distance(from, to, 1.0 / (depth + 1))
    }
  }

  /** Walks through the ASTs for `rootA` and `rootB` and checks them for
    * structural equality and at which depth the differ, if they do.
    * @param rootA
    *   some root of a subtree to look at.
    * @param rootB
    *   another root of a subtree of the AST.
    * @param depth
    *   the starting depth of `rootA` in the tree.
    * @param nodeEquals
    *   a function that checks if two nodes are equal to each other.
    * @param leafEquals
    *   a function that checks if two leaves of the AST are equal to each other.
    * @return
    *   a tuple containing if the subtrees of `rootA` and `rootB` are
    *   structurally equal, and the depth where they differ if they are not.
    */
  private def walkTree(rootA: ASTNode, rootB: ASTNode, depth: Int = 0)(
      nodeEquals: EqualsChecker[ASTNode, ASTNode]
  )(leafEquals: EqualsChecker[ASTLeaf, ASTLeaf]): (Boolean, Int) = {
    (rootA, rootB) match {
      case (a: ASTLeaf, b: ASTLeaf)    => (leafEquals(a, b), depth)
      case (a, b) if !nodeEquals(a, b) => (false, depth)
      case (a, b) if a.getChildren.size() != b.getChildren.size() =>
        (false, depth + 1)
      case (a, b) =>
        a.filteredChildren
          .lazyZip(b.filteredChildren)
          .map { case (f, t) =>
            walkTree(f, t, depth + 1)(nodeEquals)(leafEquals)
          }
          .find { case (equal, _) => !equal }
          .getOrElse((true, 0))
    }
  }
}
