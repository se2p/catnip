package de.uni_passau.fim.se2.catnip.hint_generation.model.distance

import de.uni_passau.fim.se2.catnip.common.defs.UnitSpec
import de.uni_passau.fim.se2.catnip.util.NodeGen
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.Add
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral

class ChildDistanceSpec extends UnitSpec {
  "The Child Distance" should
    "fail if the inputs are not children of parent1/2" in {
      val n1   = new NumberLiteral(1.0)
      val n2   = new NumberLiteral(2.0)
      val add1 = new Add(n1, n2, NodeGen.generateNonDataBlockMetadata())
      n1.setParentNode(add1)
      n2.setParentNode(add1)

      val n3   = new NumberLiteral(3.0)
      val n4   = new NumberLiteral(4.0)
      val add2 = new Add(n3, n4, NodeGen.generateNonDataBlockMetadata("add2"))
      n3.setParentNode(add2)
      n4.setParentNode(add2)

      val metric = new ChildDistance(add1, add2)
      the[IllegalArgumentException] thrownBy {
        metric.distance(n3, n4)
      } should have message
        "requirement failed: Parent of from has to be parent1."

      the[IllegalArgumentException] thrownBy {
        metric.distance(n2, n1)
      } should have message
        "requirement failed: Parent of to has to be parent2."
    }

  it should "be zero for identical children" in {
    val n1   = new NumberLiteral(1.0)
    val n2   = new NumberLiteral(2.0)
    val add1 = new Add(n1, n2, NodeGen.generateNonDataBlockMetadata())
    n1.setParentNode(add1)
    n2.setParentNode(add1)

    val n3   = new NumberLiteral(3.0)
    val n4   = new NumberLiteral(4.0)
    val add2 = new Add(n3, n4, NodeGen.generateNonDataBlockMetadata("add2"))
    n3.setParentNode(add2)
    n4.setParentNode(add2)

    val metric = new ChildDistance(add1, add2)
    metric.distance(n1, n3) should be(Distance(n1, n3, 0.0))
    metric.distance(n2, n4) should be(Distance(n2, n4, 0.0))
  }

  it should "be 0.045 for child list size 2 and swapped positions" in {
    val n1   = new NumberLiteral(1.0)
    val n2   = new NumberLiteral(2.0)
    val add1 = new Add(n1, n2, NodeGen.generateNonDataBlockMetadata())
    n1.setParentNode(add1)
    n2.setParentNode(add1)

    val n3   = new NumberLiteral(3.0)
    val n4   = new NumberLiteral(4.0)
    val add2 = new Add(n3, n4, NodeGen.generateNonDataBlockMetadata("add2"))
    n3.setParentNode(add2)
    n4.setParentNode(add2)

    val metric = new ChildDistance(add1, add2)
    metric.distance(n1, n4).distance shouldBe 0.045 +- 0.005
    metric.distance(n2, n3).distance shouldBe 0.045 +- 0.005
  }
}
