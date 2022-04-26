package de.uni_passau.fim.se2.catnip.hint_generation.model

import de.uni_passau.fim.se2.catnip.common.defs.UnitSpec
import de.uni_passau.fim.se2.catnip.hint_generation.model.distance.Distance
import de.uni_passau.fim.se2.catnip.model.DummyNode
import de.uni_passau.fim.se2.catnip.util.NodeGen
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral

class DistanceMatrixBuilderSpec extends UnitSpec {
  "DistanceMatrixBuilder" should "be empty initially" in {
    val builder = new DistanceMatrixBuilder(fillerA = List(), fillerB = List())
    val distanceMatrix = builder.build()

    distanceMatrix.elementsOuterIndex should be(empty)
    distanceMatrix.elementsInnerIndex should be(empty)
    distanceMatrix.matrix should be(empty)
  }

  it should "contain the distance and nodes after one insert" in {
    val n1 = new NumberLiteral(1)
    val n2 = new NumberLiteral(2)

    val builder = new DistanceMatrixBuilder[ASTNode, ASTNode](
      fillerA = new DummyNode,
      fillerB = new DummyNode
    )
    builder += Distance(n1, n2, 3)

    val distanceMatrix = builder.build()
    distanceMatrix.elementsOuterIndex should contain only n1
    distanceMatrix.elementsInnerIndex should contain only n2

    distanceMatrix.matrix should have length 1
    distanceMatrix.matrix(0)(0) should be(3)
  }

  it should "be the same after two identical inserts" in {
    val n1 = new NumberLiteral(1)
    val n2 = new NumberLiteral(2)

    val builder = new DistanceMatrixBuilder[ASTNode, ASTNode](
      fillerA = new DummyNode,
      fillerB = new DummyNode
    )

    builder += Distance(n1, n2, 3)
    val matrix1 = builder.build()

    matrix1.matrix should have length 1
    matrix1.matrix(0)(0) should be(3)

    builder += Distance(n1, n2, 3)
    val matrix2 = builder.build()

    matrix1.elementsOuterIndex should be(matrix2.elementsOuterIndex)
    matrix1.elementsInnerIndex should be(matrix2.elementsInnerIndex)

    matrix2.matrix should have length 1
    matrix2.matrix(0)(0) should be(matrix1.matrix(0)(0))
  }

  it should "not contain duplicate nodes" in {
    val n1 = new NumberLiteral(1)
    val n2 = new NumberLiteral(2)
    val n3 = NodeGen.generateNumberVariable("x")

    val builder = new DistanceMatrixBuilder[ASTNode, ASTNode](
      fillerA = new DummyNode,
      fillerB = new DummyNode
    )
    builder += Distance(n1, n2, 1)
    builder += Distance(n1, n3, 2)

    {
      val distanceMatrix = builder.build()
      distanceMatrix.elementsOuterIndex should contain theSameElementsAs Seq(
        n1,
        new DummyNode
      )
      distanceMatrix.elementsInnerIndex should contain theSameElementsAs Seq(
        n2,
        n3
      )
      distanceMatrix.matrix should have length 2

      // Matrix:
      //       | n2  | n3
      // n1    | 1   | 2
      // dummy | max | max

      distanceMatrix.matrix(0)(0) should be(1)
      distanceMatrix.matrix(0)(1) should be(2)
      distanceMatrix.matrix(1)(0) should be(12)
      distanceMatrix.matrix(1)(1) should be(12)

      val matrix2 = DistanceMatrixBuilder[ASTNode, ASTNode](
        List[Distance[ASTNode, ASTNode]](
          Distance(n1, n2, 1),
          Distance(n1, n3, 2)
        ),
        new DummyNode,
        new DummyNode
      ).build()
      matrix2.matrix should be(distanceMatrix.matrix)
    }

    {
      builder += Distance(n2, n3, 3)

      val distanceMatrix = builder.build()
      distanceMatrix.elementsOuterIndex should contain theSameElementsAs Seq(
        n1,
        n2
      )
      distanceMatrix.elementsInnerIndex should contain theSameElementsAs Seq(
        n2,
        n3
      )
      distanceMatrix.matrix should have length 2

      // Matrix:
      //    | n2  | n3
      // n1 | 1   | 2
      // n2 | max | 3

      distanceMatrix.matrix(0)(0) should be(1)
      distanceMatrix.matrix(0)(1) should be(2)
      distanceMatrix.matrix(1)(0) should be(13)
      distanceMatrix.matrix(1)(1) should be(3)
    }
  }
}
