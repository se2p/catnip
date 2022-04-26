package de.uni_passau.fim.se2.catnip.hint_generation

import de.uni_passau.fim.se2.catnip.common.defs.UnitSpec
import de.uni_passau.fim.se2.catnip.model.{
  ReplaceFieldHint,
  SolutionNode,
  StudentNode
}
import de.uni_passau.fim.se2.catnip.util.NodeGen
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.{
  Add,
  Minus,
  NumFunct,
  NumFunctOf,
  Round
}
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.UnspecifiedId
import de.uni_passau.fim.se2.litterbox.ast.model.literals.{
  NumberLiteral,
  StringLiteral
}
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.SetVariableTo

class StructuralReplacementHintGeneratorSpec extends UnitSpec {
  "The Structural Replacement Hint" should "be generated for leaves of different classes" in {
    val n1 = NodeGen.withActor(new NumberLiteral(1.0))
    val b1 = NodeGen.withActor(new StringLiteral("some"))
    val setVar = new SetVariableTo(
      new UnspecifiedId(),
      b1,
      NodeGen.generateNonDataBlockMetadata()
    )
    b1.setParentNode(setVar)

    val expected = ReplaceFieldHint(StudentNode(b1), n1).get
    StructuralReplacementHintGenerator(
      SolutionNode(n1),
      StudentNode(b1)
    ) should contain only expected
  }

  it should "generate no hint for identical leaves" in {
    val n1     = new NumberLiteral(1.0)
    val round1 = new Round(n1, NodeGen.generateNonDataBlockMetadata())
    n1.setParentNode(round1)

    val n2     = new NumberLiteral(1.0)
    val round2 = new Round(n2, NodeGen.generateNonDataBlockMetadata())
    n2.setParentNode(round2)

    StructuralReplacementHintGenerator(
      SolutionNode(n1),
      StudentNode(n2)
    ) shouldBe empty
  }

  it should "generate a hint for number literals with different inner values" in {
    val n1     = NodeGen.withActor(new NumberLiteral(1.0))
    val round1 = new Round(n1, NodeGen.generateNonDataBlockMetadata())
    n1.setParentNode(round1)

    val n2     = NodeGen.withActor(new NumberLiteral(2.0))
    val round2 = new Round(n2, NodeGen.generateNonDataBlockMetadata())
    n2.setParentNode(round2)

    val expected = ReplaceFieldHint(StudentNode(n2), n1).get
    StructuralReplacementHintGenerator(
      SolutionNode(n1),
      StudentNode(n2)
    ) should contain only expected
  }

  it should "generate a hint to replace the root node if they differ" in {
    val n1  = new NumberLiteral(1.0)
    val n2  = new NumberLiteral(2.0)
    val add = new Add(n1, n2, NodeGen.generateNonDataBlockMetadata())
    n1.setParentNode(add)
    n2.setParentNode(add)
    val round1 = new Round(add, NodeGen.generateNonDataBlockMetadata())
    add.setParentNode(round1)

    val n3 = new NumberLiteral(1.0)
    val n4 = new NumberLiteral(3.0)
    val minus = NodeGen.withActor(
      new Minus(n3, n4, NodeGen.generateNonDataBlockMetadata())
    )
    n3.setParentNode(minus)
    n4.setParentNode(minus)
    val round2 = new Round(minus, NodeGen.generateNonDataBlockMetadata())
    minus.setParentNode(round2)

    val expected = ReplaceFieldHint(StudentNode(minus), add).get
    StructuralReplacementHintGenerator(
      SolutionNode(add),
      StudentNode(minus)
    ) should contain only expected
  }

  it should "generate a hint to replace the child of an Add node" in {
    val n1  = new NumberLiteral(1.0)
    val n2  = new NumberLiteral(2.0)
    val add = new Add(n1, n2, NodeGen.generateNonDataBlockMetadata())
    n1.setParentNode(add)
    n2.setParentNode(add)

    val n3 = new NumberLiteral(1.0)
    val n4 = new NumberLiteral(3.0)
    val add2 =
      NodeGen.withActor(new Add(n3, n4, NodeGen.generateNonDataBlockMetadata()))
    n3.setParentNode(add2)
    n4.setParentNode(add2)

    val expected = ReplaceFieldHint(StudentNode(n4), n2).get
    StructuralReplacementHintGenerator(
      SolutionNode(add),
      StudentNode(add2)
    ) should contain only expected
  }

  it should "generate a hint to replace both children of an Add node" in {
    val n1 = new NumberLiteral(1.0)
    val n2 = new NumberLiteral(2.0)
    val add =
      NodeGen.withActor(new Add(n1, n2, NodeGen.generateNonDataBlockMetadata()))
    n1.setParentNode(add)
    n2.setParentNode(add)

    val n3 = new NumberLiteral(3.0)
    val n4 = new NumberLiteral(4.0)
    val add2 =
      NodeGen.withActor(new Add(n3, n4, NodeGen.generateNonDataBlockMetadata()))
    n3.setParentNode(add2)
    n4.setParentNode(add2)

    val expected = List(
      ReplaceFieldHint(StudentNode(n4), n2).get,
      ReplaceFieldHint(StudentNode(n3), n1).get
    )
    StructuralReplacementHintGenerator(
      SolutionNode(add),
      StudentNode(add2)
    ) should contain theSameElementsAs expected
  }

  it should "generate multiple hints at different levels" in {
    val n1 = new NumberLiteral(1.0)
    val n2 = new NumberLiteral(2.0)
    val add =
      NodeGen.withActor(new Add(n1, n2, NodeGen.generateNonDataBlockMetadata()))
    n1.setParentNode(add)
    n2.setParentNode(add)

    val n4 = new NumberLiteral(4.0)

    // (1.0 + 2.0) - (4.0)
    val minus = NodeGen.withActor(
      new Minus(add, n4, NodeGen.generateNonDataBlockMetadata())
    )
    add.setParentNode(minus)
    n4.setParentNode(minus)

    val n22  = new NumberLiteral(2.0)
    val n23  = new NumberLiteral(2.0)
    val add2 = new Add(n22, n23, NodeGen.generateNonDataBlockMetadata())
    n22.setParentNode(add2)
    n23.setParentNode(add2)

    // (2.0 + 2.0) - (3.0)
    val n3 = new NumberLiteral(3.0)
    val minus2 = NodeGen.withActor(
      new Minus(add2, n3, NodeGen.generateNonDataBlockMetadata())
    )
    add2.setParentNode(minus2)
    n3.setParentNode(minus2)

    val expected = List(
      ReplaceFieldHint(StudentNode(n22), n1).get,
      ReplaceFieldHint(StudentNode(n3), n4).get
    )
    StructuralReplacementHintGenerator(
      SolutionNode(minus),
      StudentNode(minus2)
    ) should contain theSameElementsAs expected
  }

  it should "generate no hints for NumFuncts if the function is the same" in {
    val funct  = new NumFunct(NumFunct.NumFunctType.COS.getFunction)
    val funct2 = new NumFunct(NumFunct.NumFunctType.COS.getFunction)

    StructuralReplacementHintGenerator(
      SolutionNode(funct),
      StudentNode(funct2)
    ) shouldBe empty
  }

  it should "generate a hint for NumFuncts if the function is NOT the same" in {
    val n1 = new NumberLiteral(1.0)
    val funct =
      NodeGen.withActor(new NumFunct(NumFunct.NumFunctType.COS.getFunction))
    val funct2 =
      NodeGen.withActor(new NumFunct(NumFunct.NumFunctType.SIN.getFunction))

    val nfo = new NumFunctOf(funct2, n1, NodeGen.generateNonDataBlockMetadata())
    funct2.setParentNode(nfo)

    val expected = List(ReplaceFieldHint(StudentNode(funct2), funct).get)
    StructuralReplacementHintGenerator(
      SolutionNode(funct),
      StudentNode(funct2)
    ) should contain theSameElementsAs expected
  }
}
