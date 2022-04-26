package de.uni_passau.fim.se2.catnip.model

import de.uni_passau.fim.se2.catnip.util.{ASTNodeExt, ASTNodeSimilarity}
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode

import scala.jdk.CollectionConverters.*

sealed trait Nodes[A] /*extends ASTNode*/ {
  // extension of ASTNode intentionally disabled to make usage of underlying
  // node more explicit

  def node: ASTNode

  def children: Seq[Nodes[A]]

  /** Same as [[children]], but the list does not contain the metadata nodes.
    * @return
    *   the children of the node without metadata.
    */
  def filteredChildren: Seq[Nodes[A]]

  def parentNode: Option[Nodes[A]]

  /** Compares the inner [[Nodes.node nodes]] for structural equality by using
    * [[de.uni_passau.fim.se2.catnip.util.ASTNodeSimilarity.structurallyEqual[B<:de\.uni_passau\.fim\.se2\.litterbox\.ast\.model\.ASTNode](other:B):Boolean* the method in ASTNodeSimilarity]].
    *
    * @param other
    *   some other wrapped `ASTNode`.
    * @tparam B
    *   the concrete type of the other wrapper.
    * @return
    *   see documentation on
    *   [[de.uni_passau.fim.se2.catnip.util.ASTNodeSimilarity.structurallyEqual[B<:de\.uni_passau\.fim\.se2\.litterbox\.ast\.model\.ASTNode](other:B):Boolean* the method in ASTNodeSimilarity]]
    *   for a definition of structural equality.
    */
  def structurallyEqual[B](other: Nodes[B]): Boolean = {
    node.structurallyEqual(other.node)
  }

  /*
  override def accept(scratchVisitor: ScratchVisitor): Unit = {
    node.accept(scratchVisitor)
  }

  override def accept(cloneVisitor: CloneVisitor): ASTNode = {
    node.accept(cloneVisitor)
  }

  override def getChildren: util.List[_ <: ASTNode] = node.getChildren

  override def getMetadata: BlockMetadata = node.getMetadata

  override def getParentNode: ASTNode = node.getParentNode

  override def getUniqueName: String = node.getUniqueName

  override def setParentNode(astNode: ASTNode): Unit = {
    node.setParentNode(astNode)
  }
   */
}

final case class SolutionNode(node: ASTNode) extends Nodes[SolutionNode] {
  override def children: Seq[SolutionNode] = {
    node.getChildren.asScala.map(SolutionNode(_)).toList
  }

  override def filteredChildren: Seq[SolutionNode] = {
    node.filteredChildren.map(SolutionNode(_))
  }

  override def parentNode: Option[SolutionNode] = {
    Option(node.getParentNode).map(SolutionNode(_))
  }
}

final case class SolutionNodeTyped[A <: ASTNode](node: A) {
  def asRegular: SolutionNode = SolutionNode(node)
}

final case class StudentNodeTyped[A <: ASTNode](node: A) {
  def asRegular: StudentNode = StudentNode(node)
}

final case class StudentNode(node: ASTNode) extends Nodes[StudentNode] {
  override def children: Seq[StudentNode] = {
    node.getChildren.asScala.map(StudentNode(_)).toList
  }

  override def filteredChildren: Seq[StudentNode] = {
    node.filteredChildren.map(StudentNode(_))
  }

  override def parentNode: Option[StudentNode] = {
    Option(node.getParentNode).map(StudentNode(_))
  }
}
