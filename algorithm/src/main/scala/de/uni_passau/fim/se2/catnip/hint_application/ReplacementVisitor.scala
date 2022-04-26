package de.uni_passau.fim.se2.catnip.hint_application

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode

object ReplacementVisitor {
  def apply[A <: ASTNode](
      rootNode: A,
      parent: ASTNode,
      fieldName: String,
      newNode: ASTNode
  ): A = {
    val v = new InsertionInFieldVisitor(parent, newNode, fieldName)
    rootNode.accept(v)
    rootNode
  }
}
