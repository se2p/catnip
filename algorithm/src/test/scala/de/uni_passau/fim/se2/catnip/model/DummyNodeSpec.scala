package de.uni_passau.fim.se2.catnip.model

import de.uni_passau.fim.se2.catnip.common.defs.UnitSpec
import de.uni_passau.fim.se2.catnip.util.NodeListVisitor
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral
import de.uni_passau.fim.se2.litterbox.ast.visitor.CloneVisitor

class DummyNodeSpec extends UnitSpec {
  "DummyNode" should "equal any other DummyNode" in {
    new DummyNode should be(new DummyNode)
  }

  it should "have name DummyNode" in {
    new DummyNode().getUniqueName should be("DummyNode")
  }

  it should "have no metadata" in {
    new DummyNode().getMetadata should be(null)
  }

  it should "have no children" in {
    new DummyNode().getChildren should be(empty)
  }

  it should "have no parent" in {
    val n = new DummyNode
    n.setParentNode(new NumberLiteral(10.0))
    n.getParentNode should be(null)
  }

  it should "accept a ScratchVisitor" in {
    val n = new DummyNode

    NodeListVisitor(n) should be(empty)
  }

  it should "accept a CloneVisitor" in {
    val v = new CloneVisitor
    val n = new DummyNode
    n.accept(v) should be(n)
  }
}
