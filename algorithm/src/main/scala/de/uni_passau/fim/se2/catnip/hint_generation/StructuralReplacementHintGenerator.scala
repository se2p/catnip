package de.uni_passau.fim.se2.catnip.hint_generation

import de.uni_passau.fim.se2.catnip.model.{
  ReplaceFieldHint,
  ReplaceHint,
  ReplaceStmtHint,
  SolutionNode,
  StudentNode
}
import de.uni_passau.fim.se2.litterbox.ast.model.{ASTLeaf, StmtList}

/** Generates hints purely based on the direct structural difference between
  * nodes.
  */
object StructuralReplacementHintGenerator {

  /** Compares the solutionNode and the studentNode structurally and recommends
    * a replacement hint where they differ.
    * @param solutionNode
    *   the target node as present in the solution program.
    * @param studentNode
    *   the current version of the student’s node.
    * @return
    *   a list of hints suggesting a replacement of nodes
    */
  def apply(
      solutionNode: SolutionNode,
      studentNode: StudentNode
  ): List[ReplaceHint] = {
    (solutionNode.node, studentNode.node) match {
      case (_: ASTLeaf, _: ASTLeaf) =>
        structuralHintLeaves(solutionNode, studentNode).toList
      // do not continue search when StmtLists are reached
      case (_: StmtList, _) | (_, _: StmtList) => List.empty
      // generate a hint here, not looking into children any further
      case (a, b) if a.getClass.getName != b.getClass.getName =>
        genHint(solutionNode, studentNode).toList
      // recursively walk through the tree, annotating the first found different
      // nodes on each path with a replacement hint
      case _ /* a.getClass == b.getClass */ =>
        solutionNode.children
          .lazyZip(studentNode.children)
          .flatMap { case (cA, cB) =>
            StructuralReplacementHintGenerator(cA, cB)
          }
          .toList
    }
  }

  /** Compares the two nodes by internal value (or just class if none available)
    * and suggests a replacing of the node or leaving it as is based on that.
    * @param solution
    *   the node as in the solution program.
    * @param student
    *   the node of the student program.
    * @return
    *   either a hint, if the two nodes differ; or none if they are the same.
    */
  private def structuralHintLeaves(
      solution: SolutionNode,
      student: StudentNode
  ): Option[ReplaceHint] = {
    if (!solution.structurallyEqual(student)) {
      genHint(solution, student)
    } else {
      None
    }
  }

  private def genHint(
      solution: SolutionNode,
      student: StudentNode
  ): Option[ReplaceHint] = {
    student.parentNode match {
      case Some(parent @ StudentNode(_: StmtList)) =>
        val idx = parent.node.getChildren.indexOf(student.node)
        ReplaceStmtHint(parent, student, solution.node, idx).toOption
      case _ => ReplaceFieldHint(student, solution.node)
    }
  }
}
