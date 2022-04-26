package de.uni_passau.fim.se2.catnip.hint_generation

import de.uni_passau.fim.se2.catnip.hint_generation.model.DistanceMatrixBuilder
import de.uni_passau.fim.se2.catnip.hint_generation.model.distance.{
  ChildDistance,
  Distance,
  NodeDistanceMetric
}
import de.uni_passau.fim.se2.catnip.model.DummyNode
import de.uni_passau.fim.se2.catnip.util.{ASTNodeExt, ASTNodeSimilarity}
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode

object BlockSimilarity extends NodeDistanceMetric {

  /** Defines by how much the distance should increase when the block types of
    * two nodes are *very* similar.
    */
  private val penaltyVerySimilarNodeType = 1

  /** Defines by how much the distance should increase when the block types of
    * two nodes are similar.
    */
  private val penaltySimilarNodeType = 5

  /** Defines by how much the distance should increase when the block types of
    * two nodes are different.
    */
  private val penaltyDifferentNodeType = 10

  /** @inheritdoc
    *
    * @param from
    *   a node of a program.
    * @param to
    *   another node of a program.
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
    val distance = if (from == to && from.getParentNode == to.getParentNode) {
      0
    } else if (from == to) {
      0.5
    } else {
      distanceFromType(from, to) + distanceFromChildren(from, to)
    }

    Distance(from, to, distance)
  }

  private def distanceFromType(from: ASTNode, to: ASTNode): Double = {
    if (from.getClass.getName == to.getClass.getName) {
      0.5
    } else if (from.verySimilar(to)) {
      penaltyVerySimilarNodeType
    } else if (from.similar(to)) {
      penaltySimilarNodeType
    } else {
      penaltyDifferentNodeType
    }
  }

  /** Computes a distance between the two nodes based on their children.
    * @param a
    *   the node of the solution program.
    * @param b
    *   another node in the student program.
    * @return
    *   a distance between a and b >= 0.
    */
  def distanceFromChildren(a: ASTNode, b: ASTNode): Double = {
    val aChildren = a.filteredChildren
    val bChildren = b.filteredChildren

    if (aChildren.isEmpty && bChildren.isEmpty) {
      0.0
    } else {
      val metric = new ChildDistance(a, b)

      val distances =
        for {
          aChild <- aChildren
          bChild <- bChildren
        } yield metric.distance(aChild, bChild)

      DistanceMatrixBuilder(1.0, distances, new DummyNode, new DummyNode)
        .build()
        .computePairingsKuhnMunkres()
        .costs / (aChildren.size + bChildren.size)
    }
  }
}
