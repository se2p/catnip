package de.uni_passau.fim.se2.catnip.hint_generation.model

import de.uni_passau.fim.se2.catnip.common.defs.UnitSpec
import de.uni_passau.fim.se2.catnip.model.DummyNode
import de.uni_passau.fim.se2.catnip.util.NodeGen

class DistanceMatrixSpec extends UnitSpec {
  "DistanceMatrix" should "reject a non-square matrix" in {
    val n1 = NodeGen.generateNumberVariable("x")
    val n2 = NodeGen.generateNumberVariable("y")

    the[IllegalArgumentException] thrownBy {
      DistanceMatrix(
        IndexedSeq(n1, n2),
        IndexedSeq(n1, n2),
        new DummyNode,
        new DummyNode,
        Array.fill(2, 1)(0).map(_.map(_.toDouble).toSeq).toSeq
      )
    } should have message "requirement failed: The matrix is not square!"
  }

  it should
    "reject if the number of nodes does not match matrix dimensions" in {
      val n1 = NodeGen.generateNumberVariable("x")
      val n2 = NodeGen.generateNumberVariable("y")

      the[IllegalArgumentException] thrownBy {
        DistanceMatrix(
          IndexedSeq(n1, n2),
          IndexedSeq(n1, n2),
          new DummyNode,
          new DummyNode,
          Array.fill(3, 3)(0).map(_.map(_.toDouble).toSeq).toSeq
        )
      } should have message
        "requirement failed: The number of elements in the index lists do not match matrix dimensions!"
    }

  it should "reject if the two element lists have different lengths" in {
    val n1 = NodeGen.generateNumberVariable("x")
    val n2 = NodeGen.generateNumberVariable("y")

    the[IllegalArgumentException] thrownBy {
      DistanceMatrix(
        IndexedSeq(n1, n2),
        IndexedSeq(n1),
        new DummyNode,
        new DummyNode,
        Array.fill(2, 2)(0).map(_.map(_.toDouble).toSeq).toSeq
      )
    } should have message
      "requirement failed: The number of nodes in rows and columns is not equal!"
  }

  it should "allow a square matrix with matching node count" in {
    val n1 = NodeGen.generateNumberVariable("x")
    val n2 = NodeGen.generateNumberVariable("y")

    val matrix = DistanceMatrix(
      IndexedSeq(n1, n2),
      IndexedSeq(n1, n2),
      new DummyNode,
      new DummyNode,
      Array.fill(2, 2)(0).map(_.map(_.toDouble).toSeq).toSeq
    )
    matrix.elementsOuterIndex should contain theSameElementsAs Seq(n1, n2)
    matrix.elementsInnerIndex should contain theSameElementsAs Seq(n1, n2)
  }

  it should "compute mappings for an empty matrix" in {
    val matrix = DistanceMatrix(
      IndexedSeq.empty,
      IndexedSeq.empty,
      new DummyNode,
      new DummyNode,
      Seq.empty
    )

    matrix.computePairingsKuhnMunkres() should be(
      MatchingResult(Map.empty, 0.0)
    )
  }

  it should "compute mappings" in {
    val n1 = NodeGen.generateNumberVariable("a")
    val n2 = NodeGen.generateNumberVariable("b")
    val n3 = NodeGen.generateNumberVariable("c")
    val n4 = NodeGen.generateNumberVariable("d")

    //    | n3 | n4
    // n1 |  1 |  0
    // n2 |  0 |  1
    val matrix = DistanceMatrix(
      IndexedSeq(n1, n2),
      IndexedSeq(n3, n4),
      new DummyNode,
      new DummyNode,
      Seq(Seq(1, 0), Seq(0, 1))
    )
    val mapping = matrix.computePairingsKuhnMunkres()
    mapping.costs should be(0.0)
    mapping.itemMap should contain theSameElementsAs List(n2 -> n3, n1 -> n4)
  }
}
