package controllers.model_converters

import de.uni_passau.fim.se2.catnip.common.FileFinder
import de.uni_passau.fim.se2.catnip.util.{NodeGen, NodeListVisitor}
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.And
import de.uni_passau.fim.se2.litterbox.ast.model.literals.{
  BoolLiteral,
  NumberLiteral
}
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.{
  ChangeVariableBy,
  SetVariableTo
}
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.{
  IfElseStmt,
  IfThenStmt,
  RepeatForeverStmt
}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import org.scalatest.{Inside, Inspectors, OptionValues}

import scala.jdk.CollectionConverters.*

class ScratchBlocksBuilderSpec
    extends AnyFlatSpec
    with should.Matchers
    with OptionValues
    with Inside
    with Inspectors {
  "The Scratch Blocks Builder" should "print first two levels of the if-stmt condition" in {
    val p = FileFinder.loadProgramFromFile(
      "example_programs/scratchblocks/scratchblocks_if.sb3"
    )
    val ifThenStmt = NodeListVisitor(p, _.isInstanceOf[IfThenStmt]).head
      .asInstanceOf[IfThenStmt]

    val expected =
      """if <() > ()> then
        |...
        |end
        |""".stripMargin

    val sb = ScratchBlocksBuilder(ifThenStmt)
    sb.value shouldBe expected
  }

  it should "show the full variable in a setVariableTo stmt" in {
    val s = new SetVariableTo(
      NodeGen.generateVariable("x"),
      new NumberLiteral(2),
      NodeGen.generateNonDataBlockMetadata()
    )
    val sb = ScratchBlocksBuilder(s)
    sb.value shouldBe "set [x v] to (2)\n"
  }

  it should "show the full variable in a changeVariableBy stmt" in {
    val c = new ChangeVariableBy(
      NodeGen.generateVariable("y"),
      new NumberLiteral(-2),
      NodeGen.generateNonDataBlockMetadata()
    )
    val sb = ScratchBlocksBuilder(c)
    sb.value shouldBe "change [y v] by (-2)\n"
  }

  it should "shorten the stmtList for a repeatForever stmt" in {
    val s = new SetVariableTo(
      NodeGen.generateVariable("x"),
      new NumberLiteral(2),
      NodeGen.generateNonDataBlockMetadata()
    )
    val c: Stmt = new ChangeVariableBy(
      NodeGen.generateVariable("y"),
      new NumberLiteral(-2),
      NodeGen.generateNonDataBlockMetadata()
    )
    val r = new RepeatForeverStmt(
      new StmtList(List(c, s).asJava),
      NodeGen.generateNonDataBlockMetadata()
    )

    val expected =
      """forever 
        |...
        |end
        |""".stripMargin

    val sb = ScratchBlocksBuilder(r, Int.MaxValue)
    sb.value shouldBe expected
  }

  it should "shorten both stmtLists for an IfElse stmt" in {
    val s: Stmt = new SetVariableTo(
      NodeGen.generateVariable("x"),
      new NumberLiteral(2),
      NodeGen.generateNonDataBlockMetadata()
    )
    val c: Stmt = new ChangeVariableBy(
      NodeGen.generateVariable("y"),
      new NumberLiteral(-2),
      NodeGen.generateNonDataBlockMetadata()
    )
    val cond = new And(
      new BoolLiteral(true),
      new BoolLiteral(false),
      NodeGen.generateNonDataBlockMetadata()
    )

    val ifElseStmt = new IfElseStmt(
      cond,
      new StmtList(List(s).asJava),
      new StmtList(List(c).asJava),
      NodeGen.generateNonDataBlockMetadata()
    )

    val expected =
      """if <<> and <>> then
        |...
        |else
        |...
        |end
        |""".stripMargin

    val sb = ScratchBlocksBuilder(ifElseStmt, Int.MaxValue)
    sb.value shouldBe expected
  }
}
