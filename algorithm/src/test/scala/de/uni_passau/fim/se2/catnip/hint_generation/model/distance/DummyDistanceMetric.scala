package de.uni_passau.fim.se2.catnip.hint_generation.model.distance

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode

class DummyDistanceMetric extends NodeDistanceMetric {

  /** Calculates a distance between `from` and `to`.
    *
    * If `from` and `to` are equal, the distance should be zero. The distance
    * should always be greater or equal to zero.
    *
    * @param from
    *   an [[ASTNode]].
    * @param to
    *   another [[ASTNode]].
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
    val distance = if (from == to) {
      0
    } else {
      1
    }

    new Distance[ASTNode, ASTNode](from, to, distance)
  }
}
