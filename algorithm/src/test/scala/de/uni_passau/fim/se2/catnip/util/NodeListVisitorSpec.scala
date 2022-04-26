package de.uni_passau.fim.se2.catnip.util

import de.uni_passau.fim.se2.catnip.common.FileFinder
import de.uni_passau.fim.se2.catnip.common.defs.UnitSpec
import de.uni_passau.fim.se2.catnip.model.ScratchProgram
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.Add
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition
import de.uni_passau.fim.se2.litterbox.ast.model.{ASTNode, Script}

import scala.jdk.CollectionConverters.*

class NodeListVisitorSpec extends UnitSpec {
  "The Node List Visitor" should "add a single leaf node to the list" in {
    val n1 = new NumberLiteral(1.0)

    NodeListVisitor(n1) should contain only n1
  }

  it should "filter metadata nodes when an appropriate filter is used" in {
    val n1 = new NumberLiteral(1.0)
    val n2 = new NumberLiteral(2.0)
    val a  = new Add(n1, n2, NodeGen.generateNonDataBlockMetadata())

    NodeListVisitor(a, !_.isMetadata) should contain theSameElementsAs Seq(
      n1,
      n2,
      a
    )
  }

  it should "return a list of all nodes when no filter is used" in {
    val n1 = new NumberLiteral(1.0)
    val n2 = new NumberLiteral(2.0)
    val a  = new Add(n1, n2, NodeGen.generateNonDataBlockMetadata())

    NodeListVisitor(a) should have length 7
  }

  it should "return all nodes of a ScratchProgram" in {
    val p =
      FileFinder.loadProgramFromFile("example_programs/simple/simple_if.sb3")
    val p2 = ScratchProgram(p)

    val actors = p.getActorDefinitionList.getDefinitions.asScala
    val expected = actors.flatMap(
      _.getScripts.getScriptList.asScala.flatMap(NodeListVisitor(_))
    ) ++ actors.flatMap(
      _.getProcedureDefinitionList.getList.asScala.flatMap(NodeListVisitor(_))
    )

    NodeListVisitor(p2) should contain theSameElementsAs expected
  }

  it should "return all scripts for a ScratchProgram" in {
    val p =
      FileFinder.loadProgramFromFile("example_programs/simple/simple_if.sb3")
    val p2 = ScratchProgram(p)

    val filter = (n: ASTNode) => n.isInstanceOf[Script]

    NodeListVisitor(p, filter) should contain theSameElementsAs NodeListVisitor(
      p2,
      filter
    )
  }

  it should "return all procedures for a ScratchProgram" in {
    val p = FileFinder.loadProgramFromFile(
      "example_programs/simple/simple_procedure.sb3"
    )
    val p2 = ScratchProgram(p)

    val filter = (n: ASTNode) => n.isInstanceOf[ProcedureDefinition]

    NodeListVisitor(p, filter) should contain theSameElementsAs NodeListVisitor(
      p2,
      filter
    )
  }
}
