package de.uni_passau.fim.se2.catnip.normalisation

import de.uni_passau.fim.se2.catnip.util.NodeTypeHelpers
import de.uni_passau.fim.se2.litterbox.ast.model.{ASTNode, Program}
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.{
  SymbolTable,
  VariableInfo
}
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor

import scala.jdk.CollectionConverters.*

private class NormalisationVisitor(
    val originalVariableMap: Map[String, VariableInfo],
    val symbolTable: SymbolTable
) extends ScratchVisitor {

  /** The number of changes made while normalising the program.
    */
  var changeCount: Int = 0

  /** A mapping between old variable names and their new ones.
    */
  val variableNames: scala.collection.mutable.Map[String, String] =
    scala.collection.mutable.Map.empty[String, String]

  override def visitChildren(node: ASTNode): Unit = {
    val changed = node.getChildren.asScala.toList
      .flatMap(oldChild => {
        oldChild.accept(this)

        val newChild = NodeNormaliser(oldChild)
        newChild.setParentNode(node)

        if (newChild != oldChild) {
          Some((oldChild, newChild))
        } else {
          None
        }
      })

    changed.foreach { case (oldChild, newChild) =>
      NodeTypeHelpers.replaceChild(node, oldChild, newChild)
    }
  }
}

/** Normalises a program.
  *
  * Details on the actual normalisations implemented in [[NodeNormaliser]].
  */
object NormalisationVisitor {
  def apply(program: Program): Program = {
    val v = NormalisationVisitor.build(program.getSymbolTable)
    program.getActorDefinitionList.getDefinitions.forEach(_.accept(v))
    program
  }

  def applyWithVariableMap(program: Program): (Program, Map[String, String]) = {
    val v = NormalisationVisitor.build(program.getSymbolTable)
    program.getActorDefinitionList.getDefinitions.forEach(_.accept(v))

    (program, v.variableNames.toMap)
  }

  private def build(symbolTable: SymbolTable): NormalisationVisitor = {
    new NormalisationVisitor(mapVariables(symbolTable), symbolTable)
  }

  private def mapVariables(
      symbolTable: SymbolTable
  ): Map[String, VariableInfo] = {
    symbolTable.getVariables.asScala.values
      .map(variableInfo => variableInfo.getVariableName -> variableInfo)
      .foldLeft(Map.empty[String, VariableInfo]) { case (m, kv) => m + kv }
  }
}
