package de.uni_passau.fim.se2.catnip.util

import de.uni_passau.fim.se2.catnip.hint_generation.model.distance.{
  Distance,
  DistanceMetric
}

/** Computes the Levenshtein Distance between two strings.
  */
object Levenshtein extends DistanceMetric[String, String] {

  /** Calculates a distance between `from` and `to`.
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
  override def distance(from: String, to: String): Distance[String, String] = {
    if (from.isEmpty || to.isEmpty) {
      Distance(from, to, math.max(from.length, to.length))
    } else {
      val fromLen = from.length
      val toLen   = to.length

      val matrix = Array.fill[Int](fromLen + 1, toLen + 1)(0)

      for (i <- 0 to fromLen) matrix(i)(0) = i
      for (j <- 0 to toLen) matrix(0)(j) = j

      for {
        j <- 0 until toLen
        i <- 0 until fromLen
      } {
        val cost = if (from(i) == to(j)) 0 else 1
        matrix(i + 1)(j + 1) = Seq(
          matrix(i)(j + 1) + 1,
          matrix(i + 1)(j) + 1,
          matrix(i)(j) + cost
        ).fold(Int.MaxValue)(math.min)
      }

      Distance(from, to, matrix(fromLen)(toLen))
    }
  }
}
