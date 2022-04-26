package de.uni_passau.fim.se2.catnip.hint_generation

import at.unisalzburg.dbresearch.apted.node.{Node, StringNodeData}
import de.uni_passau.fim.se2.catnip.model.{
  ScratchProgram,
  SolutionProgram,
  StudentProgram
}
import de.uni_passau.fim.se2.catnip.util.APTEDTreeDistance
import org.slf4j.LoggerFactory

object ClosestProgramFinder {
  private val logger = LoggerFactory.getLogger(this.getClass)

  /** Find the closest solution to the student’s program based on the root path
    * of containers and their children.
    *
    * Similar to the original SourceCheck distance calculation idea.
    * @param studentProgram
    *   the student’s program.
    * @return
    *   the best fitting solution, with the node map, and the map costs.
    */
  def findClosestSolution(
      solutionPrograms: Iterable[SolutionProgram],
      studentProgram: StudentProgram
  ): (SolutionProgram, ProgramNodeMatching) = {
    val (closestSolution, matching) = solutionPrograms
      .map(s => SolutionProgram(s.program))
      .map { solution =>
        (solution, ProgramNodeMatcher.matchNodes(solution, studentProgram))
      }
      .minBy { case (_, m) => m.costs }

    logger.info(
      s"Found closest solution ${closestSolution.program.name} to student program with distance ${matching.costs}."
    )

    (closestSolution, matching)
  }

  /** Find the closest solution to the student’s program based on the APTED tree
    * edit distance.
    * @param program
    *   the student’s program.
    * @return
    *   the closest solution.
    */
  def findClosestSolutionAPTED(
      solutionsAPTED: Map[ScratchProgram, Node[StringNodeData]],
      program: StudentProgram
  ): SolutionProgram = {
    val studentProgramTree = APTEDTreeDistance.toClassNameTree(program.program)
    val (closestSolution, costs) = solutionsAPTED
      .map { case (solutionProgram, solutionTree) =>
        (solutionProgram, APTEDTreeDistance(solutionTree, studentProgramTree))
      }
      .minBy(_._2)

    logger.info(
      s"Found closest solution ${closestSolution.name} to student program using the APTED algorithm with distance $costs."
    )

    SolutionProgram(closestSolution)
  }
}
