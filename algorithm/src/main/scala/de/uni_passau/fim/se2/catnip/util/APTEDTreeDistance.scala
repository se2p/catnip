package de.uni_passau.fim.se2.catnip.util

import at.unisalzburg.dbresearch.apted.costmodel.StringUnitCostModel
import at.unisalzburg.dbresearch.apted.distance.APTED
import at.unisalzburg.dbresearch.apted.node.{Node, StringNodeData}
import de.uni_passau.fim.se2.catnip.model.ScratchProgram
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.Metadata
import de.uni_passau.fim.se2.litterbox.ast.model.{ASTLeaf, ASTNode}

object APTEDTreeDistance {

  /** Calculates the tree edit distance using the APTED algorithm.
    * @param p1
    *   a program.
    * @param p2
    *   another program.
    * @return
    *   the edit distance between `p1` and `p2`.
    */
  def apply(p1: ASTNode, p2: ASTNode): Double = {
    val n1 = toClassNameTree(p1)
    val n2 = toClassNameTree(p2)
    apply(n1, n2)
  }

  /** Calculates the tree edit distance using the APTED algorithm.
    * @param n1
    *   a node.
    * @param n2
    *   another node.
    * @return
    *   the edit distance between `n1` and `n2`.
    */
  def apply(n1: Node[StringNodeData], n2: Node[StringNodeData]): Double = {
    val apted =
      new APTED[StringUnitCostModel, StringNodeData](new StringUnitCostModel)
    apted.computeEditDistance(n1, n2)
  }

  /** Maps a program into a tree of nodes usable by APTED.
    *
    * The top level node is `Program` with children `Scripts` and `Procedures`.
    * Those then have children as defined in `toClassNameTree(ASTNode)`.
    * @param program
    *   the program that should be converted.
    * @return
    *   a node in the format that can be used by APTED.
    */
  def toClassNameTree(program: ScratchProgram): Node[StringNodeData] = {
    val scripts = new Node(new StringNodeData("Scripts"))
    program.scripts.map(toClassNameTree(_)).foreach(scripts.addChild(_))

    val procedures = new Node(new StringNodeData("Procedures"))
    program.procedures.map(toClassNameTree(_)).foreach(procedures.addChild(_))

    val programTree = new Node(new StringNodeData("Program"))
    programTree.addChild(scripts)
    programTree.addChild(procedures)

    programTree
  }

  /** Maps each element of the tree to a node containing the class name as
    * string.
    *
    * All types of `Metadata` nodes will be converted to the string "Metadata"
    * to avoid unnecessary nodes that just contain nested metadata information.
    * @param node
    *   (part of) the program that should be converted.
    * @return
    *   a node in the format that can be used by APTED.
    */
  def toClassNameTree(node: ASTNode): Node[StringNodeData] = {
    node match {
      case _: Metadata => new Node(new StringNodeData("Metadata"))
      case n: ASTLeaf  => new Node(new StringNodeData(n.getClass.getSimpleName))
      case n: ASTNode =>
        val ret = new Node(new StringNodeData(n.getClass.getSimpleName))
        n.getChildren.forEach(c => ret.addChild(toClassNameTree(c)))
        ret
    }
  }
}
