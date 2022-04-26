package de.uni_passau.fim.se2.catnip.hint_generation.model.distance

/** Used to store a calculated distance between two elements.
  *
  * If `from == to`, then the distance must be 0. Otherwise the distance has to
  * be greater than 0.
  *
  * @param from
  *   a T.
  * @param to
  *   another T.
  * @param distance
  *   the distance between `from` and `to`.
  */
final case class Distance[A, B](from: A, to: B, distance: Double) {
  require(distance >= 0, "The distance has to be >= 0!")
}
