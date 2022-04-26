package de.uni_passau.fim.se2.catnip.model

import de.uni_passau.fim.se2.catnip.common.FileFinder
import de.uni_passau.fim.se2.catnip.common.defs.UnitSpec
import de.uni_passau.fim.se2.litterbox.ast.model.literals.{
  NumberLiteral,
  StringLiteral
}
import org.scalatest.TryValues

class StructuralHintSpec extends UnitSpec with TryValues {
  "The MissingActorHint creation" should "fail if an actor with the same name already exists" in {
    val papageiProgram = FileFinder.loadProgramFromFile(
      "example_programs/multi_actor/papagei_solution.sb3"
    )

    val actorToAdd = ScratchProgram(papageiProgram).actors("Milch")

    val hint = MissingActorHint(
      StudentNode(papageiProgram.getActorDefinitionList),
      actorToAdd
    )
    hint.failure.exception should have message "Cannot create a MissingActorHint if an actor with the same name 'Milch' already exists!"
  }

  it should "fail if the studentNode is not an ActorDefinitionList" in {
    val papageiProgram = FileFinder.loadProgramFromFile(
      "example_programs/multi_actor/papagei_solution.sb3"
    )
    val otherProgram = FileFinder.loadProgramFromFile(
      "example_programs/simple/simple_if_if.sb3"
    )

    val actorToAdd = ScratchProgram(otherProgram).actors("Sprite1")
    val hint = MissingActorHint(
      StudentNode(papageiProgram.getActorDefinitionList.getDefinitions.get(0)),
      actorToAdd
    )
    hint.failure.exception should have message "Cannot create a MissingActorHint if the actorList is not of type ActorDefinitionList!"
  }

  "The ReplaceStmtHint creation" should "fail if the parent is not a StmtList" in {
    val hint = ReplaceStmtHint(
      StudentNode(new NumberLiteral(2.0)),
      StudentNode(new NumberLiteral(390)),
      new StringLiteral("some"),
      2
    )
    hint.failure.exception should have message "Cannot create a replacement hint for a child if the parent is not a StmtList!"
  }
}
