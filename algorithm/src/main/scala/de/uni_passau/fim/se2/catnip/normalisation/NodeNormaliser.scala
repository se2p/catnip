package de.uni_passau.fim.se2.catnip.normalisation

import de.uni_passau.fim.se2.catnip.model.BlockId
import de.uni_passau.fim.se2.catnip.util.ASTNodeExt
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode
import de.uni_passau.fim.se2.litterbox.ast.model.expression.BinaryExpression
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.*
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.*
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Qualified
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astLists.{
  FieldsMetadataList,
  InputMetadataList
}
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.{
  NoMutationMetadata,
  NonDataBlockMetadata
}
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt
import de.uni_passau.fim.se2.litterbox.ast.opcodes.BoolExprOpcode

import scala.jdk.CollectionConverters.*

/** Converts certain nodes into their normalised variant.
  */
object NodeNormaliser {

  /** Delegates the call to the correct function.
    *
    * Does nothing and returns `node`, if no normalisation applicator could be
    * found for this type.
    * @param node
    *   that should be normalised.
    * @return
    *   either a normalised version of `node` if a normaliser exists for its
    *   type, or `node` itself.
    */
  @inline
  def apply(node: ASTNode): ASTNode = {
    node match {
      case a: Add        => apply(a)
      case a: And        => apply(a)
      case b: BiggerThan => apply(b)
      case d: Div        => apply(d)
      case i: IfThenStmt => apply(i)
      case m: Minus      => apply(m)
      case m: Mod        => apply(m)
      case m: Mult       => apply(m)
      case o: Or         => apply(o)
      case r: Round      => apply(r)
      case _             => node
    }
  }

  /** Bigger than comparisons should be turned into less than ones.
    *
    * Example: `x > 50` will be turned into the equivalent `50 < x`.
    *
    * @param node
    *   the node to normalise.
    * @return
    *   the converted node.
    */
  def apply(node: BiggerThan): LessThan = {
    val oldMetadata = (node.getMetadata: @unchecked) match {
      case n: NonDataBlockMetadata => n
    }

    val metadataNew = new NonDataBlockMetadata(
      oldMetadata.getCommentId,
      oldMetadata.getBlockId,
      BoolExprOpcode.operator_lt.toString,
      oldMetadata.getNext,
      oldMetadata.getParent,
      oldMetadata.getInputMetadata,
      oldMetadata.getFields,
      oldMetadata.isTopLevel,
      oldMetadata.isShadow,
      oldMetadata.getMutation
    )

    val newLt = new LessThan(node.getOperand2, node.getOperand1, metadataNew)
    newLt.getOperand1.setParentNode(newLt)
    newLt.getOperand2.setParentNode(newLt)
    metadataNew.setParentNode(newLt)
    newLt.setParentNode(node.getParentNode)

    newLt
  }

  /** Normalises an addition.
    *
    * Applied rules:
    *
    *   - Constant + Constant: smaller one to the left
    *   - Constant + Variable: variable to the left
    *   - Constant + Anything Else: constant to the left
    *
    * @param node
    *   the node to normalise.
    * @return
    *   the converted node.
    */
  def apply(node: Add): NumExpr = {
    constEval(node)(_ + _) match {
      case Right(l: NumberLiteral) => l
      case Left(n: Add) =>
        val (newLeft, newRight) =
          sortBinaryNumExpressionOperators(n.getOperand1, n.getOperand2)
        val newAdd = new Add(newLeft, newRight, node.getMetadata)
        newLeft.setParentNode(newAdd)
        newRight.setParentNode(newAdd)
        node.getMetadata.setParentNode(newAdd)

        newAdd
    }
  }

  /** Normalises a multiplication.
    *
    * Applied rules: see `apply(Add)`.
    *
    * @param node
    *   the node to normalise.
    * @return
    *   the converted node.
    */
  def apply(node: Mult): NumExpr = {
    constEval(node)(_ * _) match {
      case Right(l: NumberLiteral) => l
      case Left(n: Mult) =>
        val (newLeft, newRight) =
          sortBinaryNumExpressionOperators(n.getOperand1, n.getOperand2)
        val newMult = new Mult(newLeft, newRight, node.getMetadata)
        newLeft.setParentNode(newLeft)
        newRight.setParentNode(newRight)
        node.getMetadata.setParentNode(newMult)

        newMult
    }
  }

  /** Try to evaluate the expression.
    * @param node
    *   the expression to possibly evaluate.
    * @return
    *   a single constant if both operands were constants, `node` otherwise.
    */
  def apply(node: Minus): NumExpr = {
    constEval(node)(_ - _) match {
      case Left(n)  => n
      case Right(n) => n
    }
  }

  /** Try to evaluate the expression.
    * @param node
    *   the expression to possibly evaluate.
    * @return
    *   a single constant if both operands were constants, `node` otherwise.
    */
  def apply(node: Div): NumExpr = {
    node.getOperand2 match {
      case n: NumberLiteral
          if BigDecimal.decimal(n.getValue) == BigDecimal(0) =>
        node
      case _ =>
        constEval(node)(_ / _) match {
          case Left(n)  => n
          case Right(n) => n
        }
    }
  }

  /** Try to evaluate the expression.
    * @param node
    *   the expression to possibly evaluate.
    * @return
    *   a single constant if both operands were constants, `node` otherwise.
    */
  def apply(node: Mod): NumExpr = {
    node.getOperand2 match {
      case n: NumberLiteral
          if BigDecimal.decimal(n.getValue) == BigDecimal(0) =>
        node
      case _ =>
        constEval(node)(_ % _) match {
          case Left(n)  => n
          case Right(n) => n
        }
    }
  }

  /** Try to evaluate the expression.
    * @param node
    *   the expression to possibly evaluate.
    * @return
    *   a single constant if the operand was a constant, `node` otherwise.
    */
  def apply(node: Round): NumExpr = {
    node.getOperand1 match {
      case n: NumberLiteral => new NumberLiteral(n.getValue.round.toDouble)
      case _                => node
    }
  }

  /** Try to evaluate a binary expression having two constant operands.
    * @param node
    *   the binary numerical expression.
    * @param operation
    *   the operation to apply to the operands, if both are constant.
    * @tparam T
    *   the concrete subtype of the binary expression, e.g. Add, Mult
    * @return
    *   a NumberLiteral if it could be evaluated, or the unchanged `node`.
    */
  private def constEval[T <: BinaryExpression[NumExpr, NumExpr] & NumExpr](
      node: T
  )(operation: (Double, Double) => Double): Either[T, NumberLiteral] = {
    (node.getOperand1, node.getOperand2) match {
      case (left: NumberLiteral, right: NumberLiteral) =>
        Right(new NumberLiteral(operation(left.getValue, right.getValue)))
      case (_, _) => Left(node)
    }
  }

  /** Apply operand sorting according to the rules specified at `apply(Add)`.
    *
    * @param a
    *   the first operand.
    * @param b
    *   the other operand.
    * @return
    *   a tuple with operands in order (left, right).
    */
  private def sortBinaryNumExpressionOperators(
      a: NumExpr,
      b: NumExpr
  ): (NumExpr, NumExpr) = {
    def isQualified(op: ASTNode) = op match {
      case _: Qualified => true
      case _            => false
    }

    (a, b) match {
      case (a: NumberLiteral, b: AsNumber) if isQualified(b.getOperand1) =>
        // case: number + variable => variable + number
        (b, a)
      case (a: AsNumber, b) if isQualified(a.getOperand1) =>
        // case: variable + _ => variable + _
        (a, b)
      case (a: NumExpr, b: NumberLiteral) =>
        // case: _ + constant => constant + _
        (b, a)
      case (a, b) => (a, b)
    }
  }

  /** Normalises a logical and operation.
    *
    * Applied rules:
    *
    *   - And and Or as operands: And to the left.
    *   - And or Or as one operand, another expression to the other side: And/Or
    *     to the left.
    *
    * @param node
    *   the node to normalise.
    * @return
    *   the converted node.
    */
  def apply(node: And): And = {
    val (newLeft, newRight) =
      sortBinaryBoolExprOperators(node.getOperand1, node.getOperand2)
    val newAnd = new And(newLeft, newRight, node.getMetadata)
    newLeft.setParentNode(newAnd)
    newRight.setParentNode(newAnd)
    node.getMetadata.setParentNode(newAnd)

    newAnd
  }

  /** Normalises a logical or operation.
    *
    * Applied rules:
    *
    *   - And and Or as operands: And to the left.
    *   - And or Or as one operand, another expression to the other side: And/Or
    *     to the left.
    *
    * @param node
    *   the node to normalise.
    * @return
    *   the converted node.
    */
  def apply(node: Or): Or = {
    val (newLeft, newRight) =
      sortBinaryBoolExprOperators(node.getOperand1, node.getOperand2)
    val newOr = new Or(newLeft, newRight, node.getMetadata)
    newLeft.setParentNode(newOr)
    newRight.setParentNode(newOr)
    node.getMetadata.setParentNode(newOr)

    newOr
  }

  /** Apply operand sorting according to the rules specified at `apply(And)`.
    *
    * @param a
    *   the first operand.
    * @param b
    *   the second operand.
    * @return
    *   a tuple with operands in order (left, right).
    */
  private def sortBinaryBoolExprOperators(
      a: BoolExpr,
      b: BoolExpr
  ): (BoolExpr, BoolExpr) = {
    (a, b) match {
      case (a: Or, b: And) => (b, a)
      case (a: And, b)     => (a, b)
      case (a: Or, b)      => (a, b)
      case (a, b: And)     => (b, a)
      case (a, b: Or)      => (b, a)
      case (a, b)          => (a, b)
    }
  }

  /** Normalises an IfThenStmt.
    *
    * Applied rule:
    * ```scala
    * if (a) {
    *   if (b) {
    *     …
    *   }
    * }
    * ```
    * will be transformed into
    * ```scala
    * if (a && b) {
    *   …
    * }
    * ```
    *
    * @param node
    *   the node to normalise.
    * @return
    *   the converted node.
    */
  def apply(node: IfThenStmt): IfThenStmt = {
    val thenStmts = node.getThenStmts.getStmts.asScala.toList

    thenStmts match {
      case (innerIf: IfThenStmt) :: Nil =>
        val newThenStmts = innerIf.getThenStmts

        val left        = node.getBoolExpr
        val right       = innerIf.getBoolExpr
        val newBoolExpr = NodeNormaliser(createConjunction(node, left, right))

        val newNode =
          new IfThenStmt(newBoolExpr, newThenStmts, node.getMetadata)
        newNode.setParentNode(node.getParentNode)
        newBoolExpr.setParentNode(newNode)
        newThenStmts.setParentNode(newNode)
        newThenStmts.getStmts.forEach(_.setParentNode(newThenStmts))
        node.getMetadata.setParentNode(newNode)

        newNode
      case _ => node
    }
  }

  /** Creates a new block left && right.
    *
    * @param parent
    *   the parent this block will be added to later on.
    * @param left
    *   the left operand of the conjunction.
    * @param right
    *   the right operand of the conjunction.
    * @return
    *   a new And block with random block id and operands left and right.
    */
  private def createConjunction(
      parent: ASTNode,
      left: BoolExpr,
      right: BoolExpr
  ): And = {
    val andMetadata = generateNonDataBlockMetadata(
      BlockId.random().toString,
      BoolExprOpcode.operator_and.toString,
      parent.blockId.id
    )
    val newAnd = new And(left, right, andMetadata)
    left.setParentNode(newAnd)
    right.setParentNode(newAnd)
    andMetadata.setParentNode(newAnd)

    newAnd
  }

  private def generateNonDataBlockMetadata(
      blockId: String,
      opcode: String,
      parentId: String
  ): NonDataBlockMetadata = {
    new NonDataBlockMetadata(
      "",
      blockId,
      opcode,
      "",
      parentId,
      new InputMetadataList(java.util.List.of()),
      new FieldsMetadataList(java.util.List.of()),
      false,
      false,
      new NoMutationMetadata()
    )
  }
}
