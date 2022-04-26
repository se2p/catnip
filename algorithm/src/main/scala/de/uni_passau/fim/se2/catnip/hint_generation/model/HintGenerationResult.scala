package de.uni_passau.fim.se2.catnip.hint_generation.model

import de.uni_passau.fim.se2.catnip.model.{
  Hint,
  SolutionProgram,
  StructuralHint,
  StudentProgram
}

/** Contains the result of a [[HintGenerator]].
  * @param studentProgram
  *   the student program the hints were generated for.
  * @param referenceProgram
  *   the solution the student’s program was compared against to generate the
  *   hints.
  * @param hints
  *   the actual hints that have been generated.
  */
final case class HintGenerationResult(
    studentProgram: StudentProgram,
    referenceProgram: SolutionProgram,
    hints: List[Hint]
) {

  /** Filters the list of `hints` to only [[StructuralHint]] s.
    *
    * @return
    *   only the `StructuralHints` contained in `hints`.
    */
  def structuralHints: List[StructuralHint] = {
    hints.collect { case s: StructuralHint => s }
  }

  /** Groups the hints by the actor their referenced nodes belong to.
    * @return
    *   a map of the identifier of an actor to all the hints that reference
    *   (transitive) children of it. Additionally, contains a list of hints that
    *   do not belong to any actor.
    */
  def sortedByActor: (Map[String, List[Hint]], List[Hint]) = {
    val (withActor, stage) =
      hints.foldLeft((Map.empty[String, List[Hint]], List.empty[Hint])) {
        case ((m, noActorList), hint) =>
          hint.actorId match {
            case Some(id) => // part of an actor => add hint to map
              val updatedMap = m.updatedWith(id) {
                case Some(value) => Some(hint :: value)
                case None        => Some(List(hint))
              }
              (updatedMap, noActorList)
            case None => // not part of an actor
              (m, hint :: noActorList)
          }
      }

    // the hints have been in a specific order before => restore it
    (
      withActor.map { case (key, values) => (key, values.reverse) },
      stage.reverse
    )
  }
}
