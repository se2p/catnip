package de.uni_passau.fim.se2.catnip.normalisation

import de.uni_passau.fim.se2.catnip.common.defs.UnitSpec
import de.uni_passau.fim.se2.catnip.model.BlockId
import de.uni_passau.fim.se2.catnip.util.NodeGen
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.{
  And,
  BiggerThan,
  LessThan,
  Or
}
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.*
import de.uni_passau.fim.se2.litterbox.ast.model.literals.{
  BoolLiteral,
  NumberLiteral,
  StringLiteral
}
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.SetVariableTo
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.{
  IfElseStmt,
  IfThenStmt
}
import de.uni_passau.fim.se2.litterbox.ast.opcodes.*

import scala.jdk.CollectionConverters.*

class NodeNormaliserSpec extends UnitSpec {
  "NodeNormaliser" should "convert BiggerThan into LessThan" in {
    val metadata = NodeGen.generateNonDataBlockMetadata(
      "abcd42",
      BoolExprOpcode.operator_gt.toString
    )
    val newMetadata = NodeGen.generateNonDataBlockMetadata(
      "abcd42",
      BoolExprOpcode.operator_lt.toString
    )

    val left  = new NumberLiteral(10d)
    val right = new StringLiteral("123")

    val biggerThan = new BiggerThan(left, right, metadata)  // 10 > "123"
    val lessThan   = new LessThan(right, left, newMetadata) // "123" < 10

    NodeNormaliser(biggerThan) should be(lessThan)
  }

  // addition

  "NodeNormaliser on additions" should "evaluate one with two constants" in {
    val metadata = NodeGen
      .generateNonDataBlockMetadata("add1", NumExprOpcode.operator_add.toString)

    val left  = new NumberLiteral(20d)
    val right = new NumberLiteral(10d)

    val oldNode  = new Add(left, right, metadata) // 20 + 10
    val oldNode2 = new Add(right, left, metadata) // 10 + 20
    val newNode  = new NumberLiteral(30d)

    NodeNormaliser(oldNode) should be(newNode)
    NodeNormaliser(oldNode2) should be(newNode)
  }

  it should "leave one with two variables as operands as is" in {
    val metadata = NodeGen
      .generateNonDataBlockMetadata("add1", NumExprOpcode.operator_add.toString)

    val left  = NodeGen.generateNumberVariable("x")
    val right = NodeGen.generateNumberVariable("y")

    val oldNode = new Add(left, right, metadata) // x + y
    NodeNormaliser(oldNode) should be(oldNode)

    val alternativeNode = new Add(right, left, metadata) // y + x
    NodeNormaliser(alternativeNode) should be(alternativeNode)
  }

  it should "convert constant + variable to have the variable to the left" in {
    val metadata = NodeGen
      .generateNonDataBlockMetadata("add1", NumExprOpcode.operator_add.toString)
    val newMetadata = NodeGen
      .generateNonDataBlockMetadata("add1", NumExprOpcode.operator_add.toString)

    val left  = new NumberLiteral(10d)
    val right = NodeGen.generateNumberVariable("x")

    val oldNode = new Add(left, right, metadata)    // 10 + x
    val newNode = new Add(right, left, newMetadata) // x + 10

    NodeNormaliser(oldNode) should be(newNode)
    NodeNormaliser(newNode) should be(newNode)
  }

  it should
    "convert constant + other expression to have the constant to the left" in {
      val metadata = NodeGen.generateNonDataBlockMetadata(
        "add1",
        NumExprOpcode.operator_add.toString
      )
      val newMetadata = NodeGen.generateNonDataBlockMetadata(
        "add1",
        NumExprOpcode.operator_add.toString
      )
      val pickRandomMetadata = NodeGen.generateNonDataBlockMetadata(
        "rand1",
        NumExprOpcode.operator_random.toString
      )

      val left = new PickRandom(
        new NumberLiteral(10d),
        new NumberLiteral(100d),
        pickRandomMetadata
      )
      val right = new NumberLiteral(10d)

      val oldNode = new Add(left, right, metadata)    // rand(10, 100) + 10
      val newNode = new Add(right, left, newMetadata) // 10 + rand(10, 100)

      NodeNormaliser(oldNode) should be(newNode)
      NodeNormaliser(newNode) should be(newNode)
    }

  // multiplication

  "NodeNormaliser on multiplications" should
    "evaluate one with two constants" in {
      val metadata = NodeGen.generateNonDataBlockMetadata(
        "mult1",
        NumExprOpcode.operator_multiply.toString
      )

      val left  = new NumberLiteral(20d)
      val right = new NumberLiteral(10d)

      val oldNode  = new Mult(left, right, metadata) // 20 * 10
      val oldNode2 = new Mult(right, left, metadata) // 10 * 20
      val newNode  = new NumberLiteral(200d)

      NodeNormaliser(oldNode) should be(newNode)
      NodeNormaliser(oldNode2) should be(newNode)
    }

  it should "leave one with variables as operands as is" in {
    val metadata = NodeGen.generateNonDataBlockMetadata(
      "mult1",
      NumExprOpcode.operator_multiply.toString
    )

    val left  = NodeGen.generateNumberVariable("x")
    val right = NodeGen.generateNumberVariable("y")

    val oldNode = new Mult(left, right, metadata) // x * y
    NodeNormaliser(oldNode) should be(oldNode)

    val alternativeNode = new Mult(right, left, metadata) // y * x
    NodeNormaliser(alternativeNode) should be(alternativeNode)
  }

  it should "constant * variable to have the variable to the left" in {
    val metadata = NodeGen.generateNonDataBlockMetadata(
      "mult1",
      NumExprOpcode.operator_multiply.toString
    )
    val newMetadata = NodeGen.generateNonDataBlockMetadata(
      "mult1",
      NumExprOpcode.operator_multiply.toString
    )

    val left  = new NumberLiteral(10d)
    val right = NodeGen.generateNumberVariable("x")

    val oldNode = new Mult(left, right, metadata)    // 10 * x
    val newNode = new Mult(right, left, newMetadata) // x * 10

    NodeNormaliser(oldNode) should be(newNode)
    NodeNormaliser(newNode) should be(newNode)
  }

  it should
    "convert constant * other expression to have the constant to the left" in {
      val metadata = NodeGen.generateNonDataBlockMetadata(
        "mult1",
        NumExprOpcode.operator_multiply.toString
      )
      val newMetadata = NodeGen.generateNonDataBlockMetadata(
        "mult1",
        NumExprOpcode.operator_multiply.toString
      )
      val pickRandomMetadata = NodeGen.generateNonDataBlockMetadata(
        "rand1",
        NumExprOpcode.operator_random.toString
      )

      val left = new PickRandom(
        new NumberLiteral(10d),
        new NumberLiteral(100d),
        pickRandomMetadata
      )
      val right = new NumberLiteral(10d)

      val oldNode = new Mult(left, right, metadata)    // rand(10, 100) * 10
      val newNode = new Mult(right, left, newMetadata) // 10 * rand(10, 100)

      NodeNormaliser(oldNode) should be(newNode)
      NodeNormaliser(newNode) should be(newNode)
    }

  // subtraction

  "NodeNormaliser on subtractions" should
    "leave one with only one constant as is" in {
      val metadata = NodeGen.generateNonDataBlockMetadata(
        "sub1",
        NumExprOpcode.operator_subtract.toString
      )
      val op1 = NodeGen.generateNumberVariable("x")
      val op2 = new NumberLiteral(10d)

      val sub1 = new Minus(op1, op2, metadata)
      NodeNormaliser(sub1) should be(sub1)

      val sub2 = new Minus(op2, op1, metadata)
      NodeNormaliser(sub2) should be(sub2)
    }

  it should "evaluate constant - constant to a single constant" in {
    val metadata = NodeGen.generateNonDataBlockMetadata(
      "sub1",
      NumExprOpcode.operator_subtract.toString
    )
    val op1 = new NumberLiteral(10d)
    val op2 = new NumberLiteral(20d)

    val sub1 = new Minus(op1, op2, metadata)
    NodeNormaliser(sub1) should be(new NumberLiteral(-10d))

    val sub2 = new Minus(op2, op1, metadata)
    NodeNormaliser(sub2) should be(new NumberLiteral(10d))
  }

  // division

  "NodeNormaliser on divisions" should
    "leave one with only one constant as is" in {
      val metadata = NodeGen.generateNonDataBlockMetadata(
        "div1",
        NumExprOpcode.operator_divide.toString
      )
      val op1 = NodeGen.generateNumberVariable("x")
      val op2 = new NumberLiteral(10d)

      val div1 = new Div(op1, op2, metadata)
      NodeNormaliser(div1) should be(div1)

      val div2 = new Div(op2, op1, metadata)
      NodeNormaliser(div2) should be(div2)
    }

  it should "evaluate constant / constant to a single constant" in {
    val metadata = NodeGen.generateNonDataBlockMetadata(
      "div1",
      NumExprOpcode.operator_divide.toString
    )
    val op1 = new NumberLiteral(10d)
    val op2 = new NumberLiteral(2d)

    val div1 = new Div(op1, op2, metadata)
    NodeNormaliser(div1) should be(new NumberLiteral(5d))

    val div2 = new Div(op2, op1, metadata)
    NodeNormaliser(div2) should be(new NumberLiteral(0.2))
  }

  it should "not try to evaluate a division with divisor zero" in {
    val metadata = NodeGen.generateNonDataBlockMetadata(
      "div1",
      NumExprOpcode.operator_divide.toString
    )
    val op1 = new NumberLiteral(10d)
    val op2 = new NumberLiteral(0d)

    val div1 = new Div(op1, op2, metadata)
    NodeNormaliser(div1) should be(div1)

    val div2 = new Div(op2, op1, metadata)
    NodeNormaliser(div2) should be(new NumberLiteral(0d))
  }

  // if then

  "NodeNormaliser on IfThen(Else)" should
    "convert an if (a) { if (b) { … } } into if (a && b) { … }" in {
      val outerMetadata = NodeGen.generateNonDataBlockMetadata(
        BlockId.random().id,
        ControlStmtOpcode.control_if.toString
      )
      val innerMetadata = NodeGen.generateNonDataBlockMetadata(
        BlockId.random().id,
        ControlStmtOpcode.control_if.toString
      )

      val outerBool = new BoolLiteral(true)
      val innerBool = new BoolLiteral(false)

      val innerStmts = new StmtList(
        List(
          new SetVariableTo(
            NodeGen.generateVariable("x"),
            new NumberLiteral(3.0),
            NodeGen.generateNonDataBlockMetadata(
              BlockId.random().id,
              SetStmtOpcode.data_setvariableto.toString
            )
          )
        ).asInstanceOf[List[Stmt]].asJava
      )

      val innerIf = new IfThenStmt(innerBool, innerStmts, innerMetadata)
      val outerIf = new IfThenStmt(
        outerBool,
        new StmtList(List(innerIf).asInstanceOf[List[Stmt]].asJava),
        outerMetadata
      )

      val andMetadata = NodeGen.generateNonDataBlockMetadata(
        BlockId.random().id,
        BoolExprOpcode.operator_and.toString,
        BlockId.random().id
      )
      val newBoolExpr =
        NodeNormaliser(new And(outerBool, innerBool, andMetadata))

      val normalised = NodeNormaliser(outerIf)

      normalised.getBoolExpr should be(newBoolExpr)
      normalised.getBoolExpr.getParentNode should be(normalised)
      normalised.getThenStmts should be(innerStmts)
      normalised.getThenStmts.getParentNode should be(normalised)
      for (stmt <- normalised.getThenStmts.getStmts.asScala) {
        stmt.getParentNode should be(normalised.getThenStmts)
      }
    }

  it should "not convert an if (a) { if (b) { … }; … }" in {
    val outerMetadata = NodeGen.generateNonDataBlockMetadata(
      BlockId.random().id,
      ControlStmtOpcode.control_if.toString
    )
    val innerMetadata = NodeGen.generateNonDataBlockMetadata(
      BlockId.random().id,
      ControlStmtOpcode.control_if.toString
    )

    val outerBool = new BoolLiteral(true)
    val innerBool = new BoolLiteral(false)

    val innerStmts = new StmtList(
      List(
        new SetVariableTo(
          NodeGen.generateVariable("x"),
          new NumberLiteral(3.0),
          NodeGen.generateNonDataBlockMetadata(
            BlockId.random().id,
            SetStmtOpcode.data_setvariableto.toString
          )
        )
      ).asInstanceOf[List[Stmt]].asJava
    )

    val innerIf = new IfThenStmt(innerBool, innerStmts, innerMetadata)
    val outerIf = new IfThenStmt(
      outerBool,
      new StmtList(List(innerIf, innerIf).asInstanceOf[List[Stmt]].asJava),
      outerMetadata
    )

    NodeNormaliser(outerIf) should be(outerIf)
  }

  it should "not convert an if (a) { if (b) { … }; … } else { … }" in {
    val outerMetadata = NodeGen.generateNonDataBlockMetadata(
      "if1",
      ControlStmtOpcode.control_if.toString
    )
    val innerMetadata = NodeGen.generateNonDataBlockMetadata(
      "if2",
      ControlStmtOpcode.control_if.toString
    )

    val outerBool = new BoolLiteral(true)
    val innerBool = new BoolLiteral(false)

    val innerStmts = new StmtList(
      List(
        new SetVariableTo(
          NodeGen.generateVariable("x"),
          new NumberLiteral(3.0),
          NodeGen.generateNonDataBlockMetadata(
            "setStmt",
            SetStmtOpcode.data_setvariableto.toString
          )
        )
      ).asInstanceOf[List[Stmt]].asJava
    )

    val innerIf = new IfThenStmt(innerBool, innerStmts, innerMetadata)
    val outerIf = new IfElseStmt(
      outerBool,
      new StmtList(List(innerIf, innerIf).asInstanceOf[List[Stmt]].asJava),
      new StmtList(List(innerIf, innerIf).asInstanceOf[List[Stmt]].asJava),
      outerMetadata
    )

    NodeNormaliser(outerIf) should be(outerIf)
  }

  // and/or

  "NodeNormaliser on boolean expressions" should
    "leave and/or with two arbitrary non-and/or operands as is" in {
      val a = new BoolLiteral(true)
      val b = new BoolLiteral(false)

      val and1 = new And(
        a,
        b,
        NodeGen.generateNonDataBlockMetadata(
          "and1",
          BoolExprOpcode.operator_and.toString
        )
      )
      NodeNormaliser(and1) should be(and1)
      NodeNormaliser(NodeNormaliser(and1)) should be(and1)

      val and2 = new And(
        b,
        a,
        NodeGen.generateNonDataBlockMetadata(
          "and2",
          BoolExprOpcode.operator_and.toString
        )
      )
      NodeNormaliser(and2) should be(and2)
      NodeNormaliser(NodeNormaliser(and2)) should be(and2)

      val or1 = new Or(
        a,
        b,
        NodeGen.generateNonDataBlockMetadata(
          "or1",
          BoolExprOpcode.operator_or.toString
        )
      )
      NodeNormaliser(or1) should be(or1)
      NodeNormaliser(NodeNormaliser(or1)) should be(or1)

      val or2 = new Or(
        b,
        a,
        NodeGen.generateNonDataBlockMetadata(
          "or2",
          BoolExprOpcode.operator_or.toString
        )
      )
      NodeNormaliser(or2) should be(or2)
      NodeNormaliser(NodeNormaliser(or2)) should be(or2)
    }

  it should
    "normalise and/or with ((A or B) and/or (C and D)) to ((A or B) and/or (C and D))" in {
      val a = new BoolLiteral(true)
      val b = new BoolLiteral(false)
      val c = new BoolLiteral(false)
      val d = new BoolLiteral(true)

      val left = new Or(
        a,
        b,
        NodeGen.generateNonDataBlockMetadata(
          "or1",
          BoolExprOpcode.operator_or.toString
        )
      )
      val right = new And(
        c,
        d,
        NodeGen.generateNonDataBlockMetadata(
          "and1",
          BoolExprOpcode.operator_and.toString
        )
      )

      val andTopLevel = new And(
        left,
        right,
        NodeGen.generateNonDataBlockMetadata(
          "and2",
          BoolExprOpcode.operator_and.toString
        )
      )
      val andTopLevelNormalised = NodeNormaliser(andTopLevel)

      andTopLevelNormalised.getOperand1 should be(right)
      andTopLevelNormalised.getOperand2 should be(left)
      NodeNormaliser(andTopLevelNormalised) should be(andTopLevelNormalised)

      val orTopLevel = new Or(
        left,
        right,
        NodeGen.generateNonDataBlockMetadata(
          "or2",
          BoolExprOpcode.operator_or.toString
        )
      )
      val orTopLevelNormalised = NodeNormaliser(orTopLevel)

      orTopLevelNormalised.getOperand1 should be(right)
      orTopLevelNormalised.getOperand2 should be(left)
      NodeNormaliser(orTopLevelNormalised) should be(orTopLevelNormalised)
    }

  it should
    "normalise and/or with (A and/or (B and/or C) to ((B and/or C) and/or A)" in {
      val a = new BoolLiteral(true)
      val b = new BoolLiteral(false)
      val c = new BoolLiteral(false)

      val or = new Or(
        b,
        c,
        NodeGen.generateNonDataBlockMetadata(
          "or1",
          BoolExprOpcode.operator_or.toString
        )
      )
      val and = new And(
        b,
        c,
        NodeGen.generateNonDataBlockMetadata(
          "and1",
          BoolExprOpcode.operator_and.toString
        )
      )

      // A and (B or C)
      {
        val andTopLevel = new And(
          a,
          or,
          NodeGen.generateNonDataBlockMetadata(
            "and2",
            BoolExprOpcode.operator_and.toString
          )
        )
        val andTopLevelNormalised = NodeNormaliser(andTopLevel)

        andTopLevelNormalised.getOperand1 should be(or)
        andTopLevelNormalised.getOperand2 should be(a)
        NodeNormaliser(andTopLevelNormalised) should be(andTopLevelNormalised)
      }

      // A and (B and C)
      {
        val andTopLevel = new And(
          a,
          and,
          NodeGen.generateNonDataBlockMetadata(
            "and2",
            BoolExprOpcode.operator_and.toString
          )
        )
        val andTopLevelNormalised = NodeNormaliser(andTopLevel)

        andTopLevelNormalised.getOperand1 should be(and)
        andTopLevelNormalised.getOperand2 should be(a)
        NodeNormaliser(andTopLevelNormalised) should be(andTopLevelNormalised)
      }

      // A or (B or C)
      {
        val orTopLevel = new Or(
          a,
          or,
          NodeGen.generateNonDataBlockMetadata(
            "or2",
            BoolExprOpcode.operator_or.toString
          )
        )
        val orTopLevelNormalised = NodeNormaliser(orTopLevel)

        orTopLevelNormalised.getOperand1 should be(or)
        orTopLevelNormalised.getOperand2 should be(a)
        NodeNormaliser(orTopLevelNormalised) should be(orTopLevelNormalised)
      }

      // A or (B and C)
      {
        val orTopLevel = new Or(
          a,
          and,
          NodeGen.generateNonDataBlockMetadata(
            "or2",
            BoolExprOpcode.operator_or.toString
          )
        )
        val orTopLevelNormalised = NodeNormaliser(orTopLevel)

        orTopLevelNormalised.getOperand1 should be(and)
        orTopLevelNormalised.getOperand2 should be(a)
        NodeNormaliser(orTopLevelNormalised) should be(orTopLevelNormalised)
      }
    }

  it should "normalise (x < 50) and (a and b) to (a and b) and (x < 50)" in {
    val a     = new BoolLiteral(true)
    val b     = new BoolLiteral(true)
    val x     = NodeGen.generateNumberVariable("x")
    val fifty = new NumberLiteral(50d)

    val lt = new LessThan(
      x,
      fifty,
      NodeGen.generateNonDataBlockMetadata(
        "lt1",
        BoolExprOpcode.operator_lt.toString
      )
    )
    val and1 = new And(
      a,
      b,
      NodeGen.generateNonDataBlockMetadata(
        "and1",
        BoolExprOpcode.operator_and.toString
      )
    )

    val and2 = new And(
      lt,
      and1,
      NodeGen.generateNonDataBlockMetadata(
        "and2",
        BoolExprOpcode.operator_and.toString
      )
    )
    val normalised = NodeNormaliser(and2)

    normalised.getOperand1 should be(and1)
    normalised.getOperand2 should be(lt)
  }
}
