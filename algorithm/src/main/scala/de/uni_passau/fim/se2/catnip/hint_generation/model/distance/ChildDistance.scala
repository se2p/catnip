package de.uni_passau.fim.se2.catnip.hint_generation.model.distance

import de.uni_passau.fim.se2.catnip.util.ASTNodeExt
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.{
  IfElseStmt,
  IfThenStmt,
  UntilStmt
}

/** Calculates distances between children of `parent1` and `parent2`.
  *
  * Takes into account their positions in their parent’s children lists and
  * their [[StructuralDistance]].
  * @param parent1
  *   an ASTNode.
  * @param parent2
  *   another ASTNode.
  */
class ChildDistance(val parent1: ASTNode, val parent2: ASTNode)
    extends NodeDistanceMetric {
  private val parent1Children = parent1.filteredChildren
  private val parent2Children = parent2.filteredChildren

  private val largerChildrenSize: Double =
    Seq[Double](parent1Children.size, parent2Children.size, 1).fold(0d)(
      math.max
    )

  /** Calculates a distance between `from` and `to`.
    *
    * Takes into account their positions in their parent’s children lists and
    * their [[StructuralDistance]].
    * @param from
    *   a child of `parent1`.
    * @param to
    *   a child of `parent2`.
    * @return
    *   a distance between `from` and `to` in range <math
    *   xmlns="http://www.w3.org/1998/Math/MathML"
    *   display="inline"><mo>[</mo><mn>0.0</mn><mo>,</mo><mi
    *   mathvariant="normal">1.0</mi><mo>]</mo></math>.
    */
  override def distance(
      from: ASTNode,
      to: ASTNode
  ): Distance[ASTNode, ASTNode] = {
    require(from.getParentNode == parent1, "Parent of from has to be parent1.")
    require(to.getParentNode == parent2, "Parent of to has to be parent2.")

    val distStructural = distanceStructural(from, to)
    val distPos        = distancePositional(from, to)
    val distance       = (0.1 * distPos + distStructural) / 1.1
    Distance(from, to, distance)
  }

  /** Distance in range `[0,1]` based their position in the parent’s children
    * set.
    * @param from
    *   a child of `parent1`.
    * @param to
    *   a child of `parent2`.
    * @return
    *   the distance between `from` and `to`.
    */
  private def distancePositional(from: ASTNode, to: ASTNode): Double = {
    val fromIndex = parent1Children.indexOf(from)
    val toIndex   = parent2Children.indexOf(to)
    // scale to be in range [0, 1]
    math.abs(fromIndex - toIndex) / largerChildrenSize
  }

  /** Structural distance between `from` and `to`.
    * @param from
    *   a child of `parent1`.
    * @param to
    *   a child of `parent2`.
    * @return
    *   the [[StructuralDistance]] between `from` and `to`.
    */
  private def distanceStructural(from: ASTNode, to: ASTNode): Double = {
    def dist(a: ASTNode, b: ASTNode): Double = {
      StructuralDistance.distance(a, b).distance
    }
    def distVal(a: ASTNode, b: ASTNode): Double = {
      StructuralDistance.distanceCheckingLiteralValue(a, b).distance
    }

    (from, to) match {
      case (a: IfThenStmt, b: IfThenStmt)
          if distVal(a.getBoolExpr, b.getBoolExpr) == 0 =>
        0
      case (a: IfElseStmt, b: IfElseStmt)
          if distVal(a.getBoolExpr, b.getBoolExpr) == 0 =>
        0
      case (a: UntilStmt, b: UntilStmt)
          if distVal(a.getBoolExpr, b.getBoolExpr) == 0 =>
        0
      case _ => dist(from, to)
    }
  }
}
