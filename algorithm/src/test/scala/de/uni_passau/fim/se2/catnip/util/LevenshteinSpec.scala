package de.uni_passau.fim.se2.catnip.util

import de.uni_passau.fim.se2.catnip.common.defs.UnitSpec

class LevenshteinSpec extends UnitSpec {
  "The Levenshtein Distance" should "be 3 between kitten and sitting" in {
    dist("kitten", "sitting")(3)
  }

  it should "be 2 between book and back" in {
    dist("book", "back")(2)
  }

  it should "be the number of characters of the other string if one is empty" in {
    dist("some", "")(4)
    dist("", "other")(5)
  }

  private def dist(a: String, b: String)(expected: Int): Unit = {
    Levenshtein.distance(a, b).distance shouldBe expected
    Levenshtein.distance(b, a).distance shouldBe expected
  }
}
