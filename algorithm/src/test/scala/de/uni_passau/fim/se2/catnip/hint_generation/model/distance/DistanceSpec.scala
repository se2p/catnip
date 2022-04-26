package de.uni_passau.fim.se2.catnip.hint_generation.model.distance

import de.uni_passau.fim.se2.catnip.common.defs.UnitSpec
import de.uni_passau.fim.se2.catnip.util.NodeGen

class DistanceSpec extends UnitSpec {
  "Distance" should "reject a distance < 0 if from and to are not equal" in {
    val from = NodeGen.generateNumberVariable("x")
    val to   = NodeGen.generateNumberVariable("y")

    the[IllegalArgumentException] thrownBy {
      Distance(from, to, -1)
    } should have message "requirement failed: The distance has to be >= 0!"
  }

  it should "construct valid distances" in {
    val from = NodeGen.generateNumberVariable("x")
    val to   = NodeGen.generateNumberVariable("y")

    val d = Distance(from, to, 1)
    d.distance should be(1)

    val d2 = Distance(from, from, 0)
    d2.distance should be(0)
  }
}
