package de.uni_passau.fim.se2.catnip.normalisation

import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.{
  And,
  BiggerThan,
  Or
}
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.*
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.VariableInfo
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor
import org.scalatest.matchers.should

/** Checks that the program is fully normalised.
  */
class NormalisedCheckVisitor(val originalVariableMap: Map[String, VariableInfo])
    extends ScratchVisitor
    with should.Matchers {
  override def visit(node: BiggerThan): Unit = {
    throw new Exception("BiggerThan should not be in a normalised program")
  }

  override def visit(node: Add): Unit = {
    val newNode = NodeNormaliser(node)
    newNode match {
      case n: Add =>
        node.getOperand1 should be(n.getOperand1)
        node.getOperand2 should be(n.getOperand2)
      case n =>
        fail(
          s"Unexpected result of normalisation: $n, (parent: ${node.getParentNode})"
        )
    }

    (node.getOperand1, node.getOperand2) match {
      case (l: NumberLiteral, r: NumberLiteral) =>
        fail(
          s"Addition of two constants ${l.getValue} and ${r.getValue} should have been reduced to a single constant (parent: ${node.getParentNode})"
        )
      case _ =>
    }

    super.visit(node)
  }

  override def visit(node: Mult): Unit = {
    val newNode = NodeNormaliser(node)
    newNode match {
      case n: Mult =>
        node.getOperand1 should be(n.getOperand1)
        node.getOperand2 should be(n.getOperand2)
      case n =>
        fail(
          s"Unexpected result of normalisation: $n, (parent: ${node.getParentNode})"
        )
    }

    (node.getOperand1, node.getOperand2) match {
      case (l: NumberLiteral, r: NumberLiteral) =>
        fail(
          s"Multiplication of two constants ${l.getValue} and ${r.getValue} should have been reduced to a single constant (parent: ${node.getParentNode})"
        )
      case _ =>
    }

    super.visit(node)
  }

  override def visit(node: Minus): Unit = {
    val newNode = NodeNormaliser(node)
    newNode should be(node)

    (node.getOperand1, node.getOperand2) match {
      case (l: NumberLiteral, r: NumberLiteral) =>
        fail(
          s"Subtraction of two constants ${l.getValue} and ${r.getValue} should have been reduced to a single constant (parent: ${node.getParentNode})"
        )
      case _ =>
    }

    super.visit(node)
  }

  override def visit(node: Div): Unit = {
    val newNode = NodeNormaliser(node)
    newNode should be(node)

    (node.getOperand1, node.getOperand2) match {
      case (l: NumberLiteral, r: NumberLiteral) if r.getValue != 0.0 =>
        fail(
          s"Division of two constants ${l.getValue} and ${r.getValue} should have been reduced to a single constant (parent: ${node.getParentNode})"
        )
      case _ =>
    }

    super.visit(node)
  }

  override def visit(node: Mod): Unit = {
    val newNode = NodeNormaliser(node)
    newNode should be(node)

    (node.getOperand1, node.getOperand2) match {
      case (l: NumberLiteral, r: NumberLiteral) if r.getValue != 0.0 =>
        fail(
          s"Modulo of two constants ${l.getValue} and ${r.getValue} should have been reduced to a single constant (parent: ${node.getParentNode})"
        )
      case _ =>
    }

    super.visit(node)
  }

  override def visit(node: Round): Unit = {
    val newNode = NodeNormaliser(node)
    newNode should be(node)

    node.getOperand1 match {
      case op: NumberLiteral =>
        fail(
          s"Rounding a single constant ${op.getValue} should have been evaluated (parent: ${node.getParentNode})"
        )
      case _ =>
    }

    super.visit(node)
  }

  override def visit(node: And): Unit = {
    val newNode = NodeNormaliser(node)
    node.getOperand1 should be(newNode.getOperand1)
    node.getOperand2 should be(newNode.getOperand2)

    super.visit(node)
  }

  override def visit(node: Or): Unit = {
    val newNode = NodeNormaliser(node)
    node.getOperand1 should be(newNode.getOperand1)
    node.getOperand2 should be(newNode.getOperand2)

    super.visit(node)
  }

  override def visit(node: IfThenStmt): Unit = {
    val newNode = NodeNormaliser(node)
    node.getBoolExpr should be(newNode.getBoolExpr)
    node.getThenStmts should be(newNode.getThenStmts)

    super.visit(node)
  }

  override def visit(node: StrId): Unit = {
    if (originalVariableMap.contains(node.getName)) {
      node.getName should fullyMatch regex """[blns]\d+""".r
    }

    super.visit(node)
  }
}
