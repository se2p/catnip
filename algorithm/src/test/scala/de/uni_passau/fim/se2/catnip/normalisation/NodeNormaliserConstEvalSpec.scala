package de.uni_passau.fim.se2.catnip.normalisation

import de.uni_passau.fim.se2.catnip.common.defs.PropSpec
import de.uni_passau.fim.se2.catnip.util.NodeGen
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.*
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral
import de.uni_passau.fim.se2.litterbox.ast.opcodes.NumExprOpcode

class NodeNormaliserConstEvalSpec extends PropSpec {
  property("All additions of two constants should be evaluated") {
    org.scalacheck.Prop.forAll { (op1: Double, op2: Double) =>
      val meta = NodeGen.generateNonDataBlockMetadata(
        "add1",
        NumExprOpcode.operator_add.toString
      )
      val add = new Add(new NumberLiteral(op1), new NumberLiteral(op2), meta)
      val normalised = NodeNormaliser(add)

      normalised == new NumberLiteral(op1 + op2)
    }
  }

  property("All multiplications of two constants should be evaluated") {
    org.scalacheck.Prop.forAll { (op1: Double, op2: Double) =>
      val meta = NodeGen.generateNonDataBlockMetadata(
        "mult1",
        NumExprOpcode.operator_multiply.toString
      )
      val mult = new Mult(new NumberLiteral(op1), new NumberLiteral(op2), meta)
      val normalised = NodeNormaliser(mult)

      normalised == new NumberLiteral(op1 * op2)
    }
  }

  property("All subtractions of two constants should be evaluated") {
    org.scalacheck.Prop.forAll { (op1: Double, op2: Double) =>
      val meta = NodeGen.generateNonDataBlockMetadata(
        "sub1",
        NumExprOpcode.operator_subtract.toString
      )
      val sub = new Minus(new NumberLiteral(op1), new NumberLiteral(op2), meta)
      val normalised = NodeNormaliser(sub)

      normalised == new NumberLiteral(op1 - op2)
    }
  }

  property(
    "All divisions of two constants should be evaluated if the divisor is not zero"
  ) {
    org.scalacheck.Prop.forAll { (op1: Double, op2: Double) =>
      val meta = NodeGen.generateNonDataBlockMetadata(
        "div1",
        NumExprOpcode.operator_divide.toString
      )
      val div = new Div(new NumberLiteral(op1), new NumberLiteral(op2), meta)
      val normalised = NodeNormaliser(div)

      if (op2 != 0.0) {
        normalised == new NumberLiteral(op1 / op2)
      } else {
        normalised == div
      }
    }
  }

  property("All modulo operations of two constants should be evaluated") {
    org.scalacheck.Prop.forAll { (op1: Double, op2: Double) =>
      val meta = NodeGen.generateNonDataBlockMetadata(
        "mod1",
        NumExprOpcode.operator_mod.toString
      )
      val mod = new Mod(new NumberLiteral(op1), new NumberLiteral(op2), meta)
      val normalised = NodeNormaliser(mod)

      if (op2 != 0.0) {
        normalised == new NumberLiteral(op1 % op2)
      } else {
        normalised == mod
      }
    }
  }

  property("All round operations on a constant should be evaluated") {
    org.scalacheck.Prop.forAll { (op1: Double) =>
      val meta = NodeGen.generateNonDataBlockMetadata(
        "round",
        NumExprOpcode.operator_round.toString
      )
      val round      = new Round(new NumberLiteral(op1), meta)
      val normalised = NodeNormaliser(round)

      normalised == new NumberLiteral(op1.round.toDouble)
    }
  }
}
