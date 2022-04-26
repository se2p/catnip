package de.uni_passau.fim.se2.catnip.postprocessing

import de.uni_passau.fim.se2.catnip.hint_generation.model.HintGenerationResult
import de.uni_passau.fim.se2.catnip.model.{ReplaceFieldHint, StudentNode}
import de.uni_passau.fim.se2.catnip.util.ASTNodeSimilarity
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode
import de.uni_passau.fim.se2.litterbox.ast.model.expression.BinaryExpression
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.{
  And,
  Equals,
  Or
}
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.{Add, Mult}

/** Removes hints from the hint list if they suggest switching the operands of a
  * commutative operation if the other operand already is of the desired format.
  */
object CommutativeOpHintReducer extends HintPostprocessor {
  override val name: String = "SwitchedOperandHintOptimiser"

  /** Applies some rules to change the generated hints into a new list of hints.
    *
    * @param hints
    *   the hint generation result the processing should be applied to.
    * @return
    *   an updated result with a new list of hints.
    */
  override def process(hints: HintGenerationResult): HintGenerationResult = {
    val (keep, candidates) = hints.hints.partitionMap {
      case r @ ReplaceFieldHint(parent, _, _) if isCommutativeOp(parent) =>
        Right(r)
      case h => Left(h)
    }

    val rem = candidates
      .groupBy(r => r.parent)
      .flatMap { case (parent, operatorHints) =>
        // remove the hint if the other operand is already the same
        operatorHints.filter { case ReplaceFieldHint(_, fieldName, value) =>
          val p = parent.node
            .asInstanceOf[BinaryExpression[? <: ASTNode, ? <: ASTNode]]

          fieldName match {
            case "operand1" => !value.structurallyEqual(p.getOperand2)
            case "operand2" => !value.structurallyEqual(p.getOperand1)
            case _          =>
              // can’t happen for a parent that is a binary expression
              true
          }
        }
      }

    hints.copy(hints = keep ++ rem)
  }

  private def isCommutativeOp(node: StudentNode): Boolean = {
    node.node match {
      case _: Or | _: And | _: Equals => true
      case _: Add | _: Mult           => true
      case _                          => false
    }
  }
}
