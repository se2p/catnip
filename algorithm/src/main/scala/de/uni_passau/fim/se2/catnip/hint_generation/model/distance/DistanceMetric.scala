package de.uni_passau.fim.se2.catnip.hint_generation.model.distance

/** Defines the operations needed to calculate the distance between two
  * elements.
  */
trait DistanceMetric[A, B] {

  /** Calculates a distance between `from` and `to`.
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
  def distance(from: A, to: B): Distance[A, B]
}
