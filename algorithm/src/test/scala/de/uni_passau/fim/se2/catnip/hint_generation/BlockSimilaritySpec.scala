package de.uni_passau.fim.se2.catnip.hint_generation

import de.uni_passau.fim.se2.catnip.common.defs.UnitSpec
import de.uni_passau.fim.se2.catnip.hint_generation.model.distance.Distance
import de.uni_passau.fim.se2.catnip.util.NodeGen
import de.uni_passau.fim.se2.litterbox.ast.model.expression.Expression
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.{And, LessThan}
import de.uni_passau.fim.se2.litterbox.ast.model.expression.list.ExpressionList
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.{Add, Div}
import de.uni_passau.fim.se2.litterbox.ast.model.literals.{
  BoolLiteral,
  NumberLiteral
}

import scala.jdk.CollectionConverters.*

class BlockSimilaritySpec extends UnitSpec {
  "BlockSimilarity" should
    "return a distance of 0 for equal from and to with same parent" in {
      val parent = new ExpressionList(List[Expression]().asJava)
      val a      = new NumberLiteral(12.0)
      a.setParentNode(parent)
      val b = new NumberLiteral(12.0)
      b.setParentNode(parent)

      BlockSimilarity.distance(a, a) should be(Distance(a, a, 0.0))
      BlockSimilarity.distance(a, b) should be(Distance(a, b, 0.0))
      BlockSimilarity.distance(b, a) should be(Distance(b, a, 0.0))
    }

  it should
    "return a distance of 0.5 for equal from and to with different parents" in {
      val a = new NumberLiteral(12.0)
      val b = new NumberLiteral(12.0)

      val parent1 = new ExpressionList(List[Expression](a).asJava)
      val parent2 = new Add(a, b, NodeGen.generateNonDataBlockMetadata())

      parent1 should not equal parent2

      a.setParentNode(parent1)
      b.setParentNode(parent2)

      BlockSimilarity.distance(a, b) should be(Distance(a, b, 0.5))
      BlockSimilarity.distance(b, a) should be(Distance(b, a, 0.5))
    }

  it should
    "return a distance of >= 0.5 for different nodes with same type" in {
      val a = new NumberLiteral(12.0)
      val b = new NumberLiteral(10.0)

      BlockSimilarity.distance(a, b).distance should be >= 0.5
      BlockSimilarity.distance(b, a).distance should be >= 0.5
    }

  it should "return a distance of >= 1.5 for different nodes with very similar types" in {
    val n10 = new NumberLiteral(10.0)
    val n12 = new NumberLiteral(12.0)
    val a   = new Add(n10, n12, NodeGen.generateNonDataBlockMetadata())
    n10.setParentNode(a)
    n12.setParentNode(a)

    val n10_2 = new NumberLiteral(10.0)
    val n12_2 = new NumberLiteral(12.0)
    val b = new Div(
      n10_2,
      n12_2,
      NodeGen.generateNonDataBlockMetadata(opcode = "minus")
    )
    n10_2.setParentNode(b)
    n12_2.setParentNode(b)

    BlockSimilarity.distance(a, b).distance should be >= 1.5
    BlockSimilarity.distance(b, a).distance should be >= 1.5
  }

  it should
    "return a distance of 5.25 for different nodes with similar types" in {
      // both AND and LessThan are BoolExprs

      val b1 = new BoolLiteral(false)
      val b2 = new BoolLiteral(true)
      val a  = new And(b1, b2, NodeGen.generateNonDataBlockMetadata())
      b1.setParentNode(a)
      b2.setParentNode(a)

      val n10 = new NumberLiteral(10.0)
      val n12 = new NumberLiteral(12.0)
      val b = new LessThan(
        n10,
        n12,
        NodeGen.generateNonDataBlockMetadata(opcode = "lessThan")
      )
      n10.setParentNode(b)
      n12.setParentNode(b)

      BlockSimilarity.distance(a, b).distance should be >= 5.25
      BlockSimilarity.distance(b, a).distance should be >= 5.25
    }
}
