package de.uni_passau.fim.se2.catnip.util

import de.uni_passau.fim.se2.catnip.common.FileFinder
import de.uni_passau.fim.se2.catnip.common.defs.UnitSpec
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.Add
import de.uni_passau.fim.se2.litterbox.ast.model.literals.{
  NumberLiteral,
  StringLiteral
}
import de.uni_passau.fim.se2.litterbox.ast.parser.Scratch3Parser

import scala.jdk.CollectionConverters.*

class APTEDTreeDistanceSpec extends UnitSpec {
  "The APTED node converter" should
    "convert a leaf to its simple class name" in {
      val n1 = new NumberLiteral(1.0)

      val res = APTEDTreeDistance.toClassNameTree(n1)
      res.getChildren should be(empty)
      res.getNodeData.getLabel should be("NumberLiteral")
    }

  it should "convert a NonDataBlockMetadata node to just `Metadata`" in {
    val m = NodeGen.generateNonDataBlockMetadata()

    APTEDTreeDistance.toClassNameTree(m).getNodeData.getLabel should
      be("Metadata")
  }

  it should "convert an ADD operation to still have its children" in {
    val n1  = new NumberLiteral(1.0)
    val n2  = new NumberLiteral(2.0)
    val add = new Add(n1, n2, NodeGen.generateNonDataBlockMetadata())

    val actual = APTEDTreeDistance.toClassNameTree(add)
    actual.getNodeData.getLabel should be("Add")
    actual.getChildren.asScala.map(_.getNodeData.getLabel) should
      contain theSameElementsInOrderAs
      List("NumberLiteral", "NumberLiteral", "Metadata")
    forAll(actual.getChildren.asScala) { _.getChildren should be(empty) }
  }

  "The APTED Tree Distance" should
    "be zero for two leaves with the same class" in {
      val n1 = new NumberLiteral(1.0)
      val n2 = new NumberLiteral(2.0)

      APTEDTreeDistance(n1, n2) should be(0.0)
    }

  it should "be non-zero for two leaves with different classes" in {
    val n1 = new NumberLiteral(1.0)
    val n2 = new StringLiteral("some")

    APTEDTreeDistance(n1, n2) should be > 0.0
  }

  it should "be zero for a complex program" in {
    val parser = new Scratch3Parser
    val f = FileFinder
      .matchingSb3Files(
        "example_programs",
        "num_expr_normalise_test_control_and_operators".r
      )
      .head

    val program = parser.parseSB3File(f.toFile)

    APTEDTreeDistance(program, program) should be(0.0)
  }

  it should "be non-zero for two different complex programs" in {
    val parser = new Scratch3Parser
    val f = FileFinder
      .matchingSb3Files(
        "example_programs",
        "num_expr_normalise_test_control_and_operators".r
      )
      .head
    val f2 = FileFinder
      .matchingSb3Files(
        "example_programs",
        "num_expr_normalise_test_graphic_blocks".r
      )
      .head

    val program  = parser.parseSB3File(f.toFile)
    val program2 = parser.parseSB3File(f2.toFile)

    APTEDTreeDistance(program, program2) should be > 0.0
  }
}
