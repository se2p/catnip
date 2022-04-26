package de.uni_passau.fim.se2.catnip.hint_generation.model.distance

import de.uni_passau.fim.se2.catnip.common.defs.UnitSpec
import de.uni_passau.fim.se2.catnip.util.NodeGen
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.{
  Add,
  Minus,
  Mult
}
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId
import de.uni_passau.fim.se2.litterbox.ast.model.literals.{
  NumberLiteral,
  StringLiteral
}
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.SetVariableTo

import scala.jdk.CollectionConverters.*

class StructuralDistanceSpec extends UnitSpec {
  "The structural distance" should "be zero for leaves with same type" in {
    val n1 = new NumberLiteral(1.0)
    val n2 = new NumberLiteral(2.0)

    StructuralDistance.distance(n1, n1) should be(Distance(n1, n1, 0.0))
    StructuralDistance.distance(n1, n2) should be(Distance(n1, n2, 0.0))
  }

  it should "be one if the types of from and to differ" in {
    val n1 = new NumberLiteral(1.0)
    val b1 = new StringLiteral("s")
    StructuralDistance.distance(n1, b1) should be(Distance(n1, b1, 1.0))

    val n2   = new NumberLiteral(2.0)
    val add1 = new Add(n1, n2, NodeGen.generateNonDataBlockMetadata())
    val minus =
      new Mult(n1, n2, NodeGen.generateNonDataBlockMetadata(opcode = "minus"))
    StructuralDistance.distance(add1, minus) should
      be(Distance(add1, minus, 1.0))

    StructuralDistance.distance(n1, add1) should be(Distance(n1, add1, 1.0))
  }

  it should "be zero for two nodes like Add(NumberLiteral, NumberLiteral)" in {
    val n1   = new NumberLiteral(1.0)
    val n2   = new NumberLiteral(2.0)
    val add1 = new Add(n1, n2, NodeGen.generateNonDataBlockMetadata())

    val n3 = new NumberLiteral(3.0)
    val n4 = new NumberLiteral(4.0)
    val add2 =
      new Add(n3, n4, NodeGen.generateNonDataBlockMetadata(opcode = "add2"))

    StructuralDistance.distance(add1, add2) should be(Distance(add1, add2, 0.0))
  }

  it should "be 0.5 if the types of children of from and to differ" in {
    val n1 = new NumberLiteral(1.0)
    val minus = new Minus(
      new NumberLiteral(2.0),
      new NumberLiteral(3.0),
      NodeGen.generateNonDataBlockMetadata("minus")
    )
    val add1 = new Add(n1, minus, NodeGen.generateNonDataBlockMetadata())

    val n3 = new NumberLiteral(3.0)
    val n4 = new NumberLiteral(4.0)
    val add2 =
      new Add(n3, n4, NodeGen.generateNonDataBlockMetadata(opcode = "add2"))

    StructuralDistance.distance(add1, add2) should be(Distance(add1, add2, 0.5))
  }

  it should "be 0.5 if the number of children of from and to differ" in {
    val s1 = new SetVariableTo(
      new StrId("x"),
      new NumberLiteral(1.0),
      NodeGen.generateNonDataBlockMetadata(opcode = "set")
    )
    val n1 = new StmtList(List[Stmt](s1, s1, s1).asJava)
    val n2 = new StmtList(List[Stmt](s1, s1).asJava)

    StructuralDistance.distance(n1, n2) should be(Distance(n1, n2, 0.5))
  }
}
