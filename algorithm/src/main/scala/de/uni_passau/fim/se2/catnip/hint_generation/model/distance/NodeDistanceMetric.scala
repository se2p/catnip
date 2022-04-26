package de.uni_passau.fim.se2.catnip.hint_generation.model.distance

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode

/** Defines the operations needed to calculate the distance between two ASTNodes
  * of a program.
  */
trait NodeDistanceMetric extends DistanceMetric[ASTNode, ASTNode] {

  /** Calculates a distance between `from` and `to`.
    *
    * @param from
    *   an ASTNode.
    * @param to
    *   another ASTNode.
    * @return
    *   a distance between `from` and `to` in range <math
    *   xmlns="http://www.w3.org/1998/Math/MathML"
    *   display="inline"><mo>[</mo><mn>0.0</mn><mo>,</mo><mi
    *   mathvariant="normal">∞</mi><mo>)</mo></math>.
    */
  def distance(from: ASTNode, to: ASTNode): Distance[ASTNode, ASTNode]
}
