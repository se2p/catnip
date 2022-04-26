package de.uni_passau.fim.se2.catnip.hint_generation.model

import de.uni_passau.fim.se2.catnip.model.{
  SolutionNode,
  SolutionNodeTyped,
  StudentNode,
  StudentNodeTyped
}
import de.uni_passau.fim.se2.litterbox.ast.model.{ASTNode, Script}

final case class Matching[A <: ASTNode](
    matching: Map[SolutionNodeTyped[A], StudentNodeTyped[A]]
) extends AnyVal

/** Contains the result when calculating the matching between items including
  * the total cost for the given matching.
  * @param itemMap
  *   the actual matching of items.
  * @param costs
  *   the total sum of distances between items in the pairs in `itemMap`.
  * @tparam A
  *   the concrete type of the source items of the matching.
  * @tparam B
  *   the concrete type of the target items of the matching.
  */
final case class MatchingResult[A, B](itemMap: Map[A, B], costs: Double)

/** Describes that matching from a container in the solution program to a
  * container in the student’s program together with the matching of their
  * respective children.
  * @param solutionContainer
  *   a container in the solution program.
  * @param studentContainer
  *   a container in the student’s program.
  * @param childrenMap
  *   a map of children of `solutionContainer` to children of `studentNode`.
  */
final case class MatchingContainers(
    solutionContainer: SolutionNode,
    studentContainer: StudentNode,
    childrenMap: Map[SolutionNode, StudentNode]
)

/** Contains the result when calculating the matching between programs based on
  * containers and their children.
  * @param scriptsMap
  *   maps scripts in the solution program to corresponding scripts in the
  *   student’s program.
  * @param containerMap
  *   a list of all containers in the solution program for which a partner in
  *   the student’s program could be found.
  * @param costs
  *   the total costs of the matching.
  */
final case class MatchingResultActor(
    scriptsMap: Matching[Script],
    containerMap: List[MatchingContainers],
    costs: Double
)
