package controllers.model_converters

import de.uni_passau.fim.se2.litterbox.ast.model.{AbstractNode, ASTNode}

final case class ScratchBlocksFigure(value: String) extends AnyVal

object ScratchBlocksBuilder {
  def apply(node: ASTNode, depth: Int = 2): ScratchBlocksFigure = {
    val d = node match {
      case _: AbstractNode => depth + 1
      case _               => depth
    }
    val res = CustomScratchBlocksVisitor.apply(node, d)
    ScratchBlocksFigure(res)
  }
}
