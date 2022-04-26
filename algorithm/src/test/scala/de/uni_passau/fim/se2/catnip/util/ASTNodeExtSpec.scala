package de.uni_passau.fim.se2.catnip.util

import de.uni_passau.fim.se2.catnip.common.FileFinder
import de.uni_passau.fim.se2.catnip.common.defs.UnitSpec
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.Add
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.VariableMetadata
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt
import org.scalatest.OptionValues

import scala.jdk.CollectionConverters.*

class ASTNodeExtSpec extends UnitSpec with OptionValues {
  "The Extension Methods on ASTNode" should "recognise metadata as such" in {
    val m1 = NodeGen.generateNonDataBlockMetadata()
    val m2 = new VariableMetadata("var", "va3", "1223")

    m1.isMetadata should be(true)
    m2.isMetadata should be(true)
    new NumberLiteral(12.0).isMetadata should be(false)
  }

  it should "get the blockId of an Addition" in {
    val id = "ABCDEFGHIJKLOSBRJKUD"
    val n1 = new NumberLiteral(1.0)
    val n2 = new NumberLiteral(2.0)
    val add =
      new Add(n1, n2, NodeGen.generateNonDataBlockMetadata(blockId = id))

    add.blockId.id should be(id)
  }

  it should "mark containers as such" in {
    val l = new StmtList(List[Stmt]().asJava)
    l.isContainer should be(true)

    val m1 = NodeGen.generateNonDataBlockMetadata()
    m1.isContainer should be(false)

    new NumberLiteral(1.0).isContainer should be(false)
  }

  it should "find the program of a node" in {
    val program =
      FileFinder.loadProgramFromFile("example_programs/simple/simple_if_if.sb3")
    val innerIf = NodeListVisitor(program, _.isInstanceOf[IfThenStmt]).last
      .asInstanceOf[IfThenStmt]

    innerIf.program.value shouldBe program
    program.program.value shouldBe program
  }

  it should "return none if the node does not belong to a program" in {
    val program =
      FileFinder.loadProgramFromFile("example_programs/simple/simple_if_if.sb3")
    val ifs = NodeListVisitor(program, _.isInstanceOf[IfThenStmt])
      .map(_.asInstanceOf[IfThenStmt])

    val (outerIf, innerIf) = (ifs.head, ifs.last)
    outerIf.setParentNode(null)

    innerIf.program shouldBe None
    outerIf.program shouldBe None
  }
}
