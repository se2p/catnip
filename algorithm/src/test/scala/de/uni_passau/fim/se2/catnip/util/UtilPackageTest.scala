package de.uni_passau.fim.se2.catnip.util

import de.uni_passau.fim.se2.catnip.common.FileFinder
import de.uni_passau.fim.se2.catnip.common.defs.UnitSpec
import de.uni_passau.fim.se2.catnip.model.StudentNode
import de.uni_passau.fim.se2.litterbox.ast.model.Script
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.{Add, Round}
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral

class UtilPackageTest extends UnitSpec {
  "Removing transitive children" should "keep a singular element" in {
    val n1 = StudentNode(new NumberLiteral(1.0))

    removeTransitiveChildren(List(n1)) should contain theSameElementsAs List(n1)
  }

  it should "keep independent nodes" in {
    val n1 = StudentNode(new NumberLiteral(1.0))
    val n2 = StudentNode(new NumberLiteral(2.0))

    removeTransitiveChildren(
      List(n1, n2)
    ) should contain theSameElementsAs List(n1, n2)
  }

  it should "keep an ADD and remove its operators" in {
    val n1 =
      new Round(new NumberLiteral(1.3), NodeGen.generateNonDataBlockMetadata())
    val n2 = new NumberLiteral(345)
    val and =
      StudentNode(new Add(n1, n2, NodeGen.generateNonDataBlockMetadata()))

    removeTransitiveChildren(
      List(and, StudentNode(n1), StudentNode(n2))
    ) should contain only and
  }

  it should "keep the program node and remove all others" in {
    val p =
      FileFinder.loadProgramFromFile("example_programs/simple/simple_if.sb3")
    val nodes = NodeListVisitor(p).map(StudentNode(_))

    removeTransitiveChildren(nodes) should contain only StudentNode(p)
  }

  it should "keep multiple independent scripts and remove their children" in {
    val p =
      FileFinder.loadProgramFromFile("example_programs/simple/simple_if.sb3")
    val scripts = NodeListVisitor(p, _.isInstanceOf[Script])
    val nodes   = scripts.flatMap(NodeListVisitor(_)).map(StudentNode(_))

    removeTransitiveChildren(nodes) should contain theSameElementsAs scripts
      .map(StudentNode(_))
  }
}
