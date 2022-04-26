package de.uni_passau.fim.se2.catnip.hint_generation.model.distance

import de.uni_passau.fim.se2.catnip.common.FileFinder
import de.uni_passau.fim.se2.catnip.common.defs.UnitSpec
import de.uni_passau.fim.se2.catnip.util.{NodeGen, NodeListVisitor}
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.{Add, Minus}
import de.uni_passau.fim.se2.litterbox.ast.model.literals.{
  BoolLiteral,
  NumberLiteral
}
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfElseStmt
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.SetXTo

class RootPathDistanceSpec extends UnitSpec {
  "RootPathDistance" should "have 0 distance for two equal nodes" in {
    val n1 = new NumberLiteral(2.0)

    RootPathDistance.distance(n1, n1) should be(Distance(n1, n1, 0))
  }

  it should "have 0 distance for two identical paths" in {
    val n1  = new NumberLiteral(1.0)
    val n2  = new NumberLiteral(2.0)
    val add = new Add(n1, n2, NodeGen.generateNonDataBlockMetadata())
    n1.setParentNode(add)
    n2.setParentNode(add)

    val n3   = new NumberLiteral(1.0)
    val n4   = new NumberLiteral(2.0)
    val add2 = new Add(n3, n4, NodeGen.generateNonDataBlockMetadata())
    n3.setParentNode(add2)
    n4.setParentNode(add2)

    RootPathDistance.distance(n2, n4) should be(Distance(n2, n4, 0))
  }

  it should "have non-0 distance for different paths" in {
    val n1  = new NumberLiteral(1.0)
    val n2  = new NumberLiteral(2.0)
    val add = new Add(n1, n2, NodeGen.generateNonDataBlockMetadata())
    n1.setParentNode(add)
    n2.setParentNode(add)

    val n3 = new NumberLiteral(1.0)

    RootPathDistance.distance(n2, n3) should be(Distance(n2, n3, 1.0))
  }

  it should "have non-0 distance for longer different paths" in {
    val n1  = new NumberLiteral(1.0)
    val n2  = new NumberLiteral(2.0)
    val add = new Add(n1, n2, NodeGen.generateNonDataBlockMetadata())
    n1.setParentNode(add)
    n2.setParentNode(add)

    val n3    = new NumberLiteral(3.0)
    val minus = new Minus(n3, add, NodeGen.generateNonDataBlockMetadata())
    add.setParentNode(minus)
    n3.setParentNode(minus)

    val b1 = new BoolLiteral(true)
    val b2 = new BoolLiteral(false)
    val b3 = new BoolLiteral(true)
    b2.setParentNode(b1)
    b3.setParentNode(b2)

    RootPathDistance.distance(n3, b3) should be(Distance(n3, b3, 2.0))
  }

  it should "have distance 0.5 for the same statement in the then block of an IfElse block" in {
    val program = FileFinder.loadProgramFromFile(
      "example_programs/root_path_test/root_path_test_if_else_multi_statement.sb3"
    )

    val ifElse = NodeListVisitor(program, _.isInstanceOf[IfElseStmt]).head
      .asInstanceOf[IfElseStmt]
    val thenStmts = ifElse.getStmtList.getStmts
    thenStmts should have length 2

    val a = thenStmts.get(0)
    val b = thenStmts.get(1)

    val distance = RootPathDistance.distance(a, b)
    distance.distance shouldBe 0.58 +- 0.004
  }

  it should "add a penalty if one node is in the then block and the other in in the else block" in {
    val program = FileFinder.loadProgramFromFile(
      "example_programs/root_path_test/root_path_test_if_else.sb3"
    )

    val ifElse = NodeListVisitor(program, _.isInstanceOf[IfElseStmt]).head
      .asInstanceOf[IfElseStmt]
    val thenStmts = ifElse.getStmtList.getStmts
    val elseStmts = ifElse.getElseStmts.getStmts

    val thenStmt = thenStmts.get(0)
    thenStmt
      .asInstanceOf[SetXTo]
      .getNum
      .asInstanceOf[NumberLiteral]
      .getValue shouldBe 0.0
    val elseStmt = elseStmts.get(0)
    elseStmt
      .asInstanceOf[SetXTo]
      .getNum
      .asInstanceOf[NumberLiteral]
      .getValue shouldBe 2.0

    val distance = RootPathDistance.distance(thenStmt, elseStmt)
    distance.distance shouldBe 0.944 +- 0.004
  }
}
