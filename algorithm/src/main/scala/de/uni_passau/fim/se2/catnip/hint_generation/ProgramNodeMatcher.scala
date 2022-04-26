package de.uni_passau.fim.se2.catnip.hint_generation

import de.uni_passau.fim.se2.catnip.hint_generation.model.{
  Matching,
  MatchingResultActor
}
import de.uni_passau.fim.se2.catnip.model.{
  SolutionNodeTyped,
  SolutionProgram,
  StudentNodeTyped,
  StudentProgram
}
import de.uni_passau.fim.se2.catnip.util.ActorDefinitionExt
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition

final case class ActorName(name: String) extends AnyVal

final case class ProgramNodeMatching(
    actorMatches: Matching[ActorDefinition],
    innerMatches: Map[ActorName, MatchingResultActor],
    costs: Double
)

/** Computes the matching of nodes between two programs.
  */
object ProgramNodeMatcher {

  /** Finds matches for nodes in `referenceProgram` to nodes in
    * `studentProgram`.
    * @param referenceProgram
    *   the program that should be used as reference for the matching.
    * @param studentProgram
    *   the student’s program that should be compared to `referenceProgram`.
    * @return
    *   a matching for the nodes in `referenceProgram` to nodes in
    *   `studentProgram`.
    */
  def matchNodes(
      referenceProgram: SolutionProgram,
      studentProgram: StudentProgram
  ): ProgramNodeMatching = {
    val actorMatching = matchActors(referenceProgram, studentProgram)
    val innerMatchings = actorMatching.matching.map {
      case (solutionActor, studentActor) =>
        val actorName = ActorName(solutionActor.node.name)
        actorName -> matchInnerNodes(solutionActor, studentActor)
    }
    val costs = innerMatchings.values.map(_.costs).sum

    ProgramNodeMatching(actorMatching, innerMatchings, costs)
  }

  /** Finds matches for actors by comparing their name.
    * @param referenceProgram
    *   the program that should be used as reference for the matching.
    * @param studentProgram
    *   the student’s program that should be compared to `referenceProgram`.
    * @return
    *   a map of actors in `referenceProgram` to actors in `studentProgram`.
    */
  private def matchActors(
      referenceProgram: SolutionProgram,
      studentProgram: StudentProgram
  ): Matching[ActorDefinition] = {
    val solutionActors = referenceProgram.program.actors
    val studentActors  = studentProgram.program.actors

    val matching = solutionActors
      .map { case (actorName, solutionActor) =>
        studentActors.get(actorName) match {
          case Some(studentActor) =>
            val sol  = SolutionNodeTyped(solutionActor)
            val stud = StudentNodeTyped(studentActor)
            Some(sol -> stud)
          case None => None
        }
      }
      .flatten
      .toMap

    model.Matching(matching)
  }

  /** Matches the scripts and containers between the two actors.
    * @param referenceActor
    *   the actor that should be used as reference for the matching.
    * @param studentActor
    *   the actor in the student’s program that should be compared to
    *   `referenceActor`.
    * @return
    *   a matching of scripts, containers, and children of containers together
    *   with a total cost of the matching.
    */
  private def matchInnerNodes(
      referenceActor: SolutionNodeTyped[ActorDefinition],
      studentActor: StudentNodeTyped[ActorDefinition]
  ): MatchingResultActor = {
    NodeMatching.findClosestNodes(
      referenceActor.asRegular,
      studentActor.asRegular
    )
  }
}
