package de.uni_passau.fim.se2.catnip.util

import de.uni_passau.fim.se2.catnip.common.defs.PropSpec
import org.scalacheck.Prop.forAll as propForAll

class LevenshteinPropSpec extends PropSpec {
  property(
    "The Levenshtein distance between any string and itself should be zero"
  ) {
    propForAll { (a: String) =>
      Levenshtein.distance(a, a).distance == 0
    }
  }

  property(
    "The Levenshtein distance between any strings should always be <= the length of the longer string"
  ) {
    propForAll { (a: String, b: String) =>
      Levenshtein.distance(a, b).distance <= math.max(a.length, b.length)
    }
  }
}
