package de.uni_passau.fim.se2.catnip.hint_generation.model

import de.uni_passau.fim.se2.catnip.common.FileFinder
import de.uni_passau.fim.se2.catnip.common.defs.UnitSpec
import de.uni_passau.fim.se2.catnip.model.{
  DeletionHint,
  SolutionProgram,
  StudentNode,
  StudentProgram
}
import de.uni_passau.fim.se2.catnip.util.NodeListVisitor
import de.uni_passau.fim.se2.litterbox.ast.model.{
  ActorDefinition,
  ActorDefinitionList,
  ASTNode
}
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.{
  ChangeXBy,
  ChangeYBy,
  SetXTo,
  SetYTo
}

class HintGenerationResultSpec extends UnitSpec {
  "The HintGenResult" should "be sortable by actor id" in {
    val p = FileFinder.loadProgramFromFile(
      "example_programs/multi_actor/multi_actor_test01.sb3"
    )

    def actorByName(name: String)(node: ASTNode) = {
      node match {
        case a: ActorDefinition => a.getIdent.getName == name
        case _                  => false
      }
    }

    // actor 1
    val changeX = NodeListVisitor(p, _.isInstanceOf[ChangeXBy]).head
    val setX    = NodeListVisitor(p, _.isInstanceOf[SetXTo]).head
    val actor1 = NodeListVisitor(p, actorByName("Figur1")(_)).head
      .asInstanceOf[ActorDefinition]

    // actor 2
    val changeY = NodeListVisitor(p, _.isInstanceOf[ChangeYBy]).head
    val setY    = NodeListVisitor(p, _.isInstanceOf[SetYTo]).head
    val actor2 = NodeListVisitor(p, actorByName("Bear")(_)).head
      .asInstanceOf[ActorDefinition]

    val adl     = NodeListVisitor(p, _.isInstanceOf[ActorDefinitionList]).head
    val adlHint = DeletionHint(StudentNode(adl))

    val hints = List(
      DeletionHint(StudentNode(changeX)),
      DeletionHint(StudentNode(setY)),
      DeletionHint(StudentNode(setX)),
      DeletionHint(StudentNode(changeY)),
      adlHint
    )
    val res = HintGenerationResult(StudentProgram(p), SolutionProgram(p), hints)
    res.hints should contain theSameElementsAs res.structuralHints

    val expected = Map(
      actor1.getIdent.getName -> List(
        DeletionHint(StudentNode(setX)),
        DeletionHint(StudentNode(changeX))
      ),
      actor2.getIdent.getName -> List(
        DeletionHint(StudentNode(setY)),
        DeletionHint(StudentNode(changeY))
      )
    )
    val actual = res.sortedByActor

    forAll(expected) { case (k, v) =>
      actual._2 should contain only adlHint
      actual._1 should contain key k
      actual._1(k) should contain theSameElementsAs v
    }
  }
}
