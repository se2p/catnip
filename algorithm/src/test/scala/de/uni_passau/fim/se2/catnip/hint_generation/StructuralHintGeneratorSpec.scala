package de.uni_passau.fim.se2.catnip.hint_generation

import de.uni_passau.fim.se2.catnip.common.FileFinder
import de.uni_passau.fim.se2.catnip.common.defs.UnitSpec
import de.uni_passau.fim.se2.catnip.model.{
  DeletionHint,
  InsertionHint,
  MissingActorHint,
  ReplaceFieldHint,
  SolutionNode,
  StudentNode
}
import de.uni_passau.fim.se2.catnip.normalisation.NormalisationVisitor
import de.uni_passau.fim.se2.catnip.util.{
  ActorDefinitionExt,
  ASTNodeSimilarity,
  NodeListVisitor
}
import de.uni_passau.fim.se2.litterbox.ast.model.{Script, ScriptList}
import de.uni_passau.fim.se2.litterbox.ast.model.event.{
  AttributeAboveValue,
  GreenFlag,
  Never,
  ReceptionOfMessage
}
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral
import de.uni_passau.fim.se2.litterbox.ast.parser.Scratch3Parser
import org.scalatest.OptionValues

class StructuralHintGeneratorSpec extends UnitSpec with OptionValues {
  "The structural hint generator" should
    "not crash when constructed without solutions" in {
      new StructuralHintGenerator(List())
    }

  it should "normalise the solutions when entering them" in {
    val parser = new Scratch3Parser
    val f = FileFinder
      .matchingSb3Files(
        "example_programs",
        "num_expr_normalise_test_control_and_operators".r
      )
      .head
    val program = parser.parseSB3File(f.toFile)

    val s1 = new StructuralHintGenerator(List(program))
    s1.storedSolutions should have size 1

    val p2 = NormalisationVisitor(parser.parseSB3File(f.toFile))

    val diff = StructuralReplacementHintGenerator(
      SolutionNode(s1.storedSolutions.head.program),
      StudentNode(p2)
    )
    diff shouldBe empty
  }

  it should "whether using APTED or not yield the same result when having one solution" in {
    val solution = FileFinder.loadProgramFromFile(
      "example_programs/multi_actor/papagei_solution.sb3"
    )
    val studentProgram =
      FileFinder.loadProgramFromFile("example_programs/simple/simple_if_if.sb3")

    val s1 = new StructuralHintGenerator(List(solution))
    val s2 =
      new StructuralHintGenerator(List(solution), findMatchUsingApted = true)

    val result1 = s1.generateHints(studentProgram)
    val result2 = s2.generateHints(studentProgram)

    result1.hints should contain theSameElementsAs result2.hints
  }

  it should "generate a missing actor hint for the missing Baum actor" in {
    val solution = FileFinder.loadProgramFromFile(
      "example_programs/multi_actor/papagei_solution.sb3"
    )
    val studentProgram = FileFinder.loadProgramFromFile(
      "example_programs/multi_actor/papagei_missing_baum_actor.sb3"
    )

    val hintGen = new StructuralHintGenerator(List(solution))
    val hints   = hintGen.generateHints(studentProgram)

    val missingActorHints = hints.hints.collect { case h: MissingActorHint =>
      h
    }
    missingActorHints should have length 1
    missingActorHints.head.actor.name shouldBe "Baum"
  }

  it should "generate a hint to insert the correct script if one is missing" in {
    val solution = FileFinder.loadProgramFromFile(
      "example_programs/multi_actor/papagei_solution.sb3"
    )
    val studentProgram = FileFinder.loadProgramFromFile(
      "example_programs/multi_actor/papagei_missing_script.sb3"
    )

    val expectedScript = NodeListVisitor(
      solution.getActorDefinitionList.getDefinitions.get(1),
      _.isInstanceOf[Script]
    ).last.asInstanceOf[Script]
    expectedScript.getEvent shouldBe a[ReceptionOfMessage]

    val hintGen = new StructuralHintGenerator(List(solution))
    val hints   = hintGen.generateHints(studentProgram)
    hints.hints should have length 1

    val insertionHints = hints.hints.collect { case i: InsertionHint => i }
    insertionHints should have length 1

    val actualHint = insertionHints.head
    actualHint.actorId.value shouldBe "Papagei"
    actualHint.references.node shouldBe a[ScriptList]
    actualHint.node.structurallyEqual(expectedScript) shouldBe true
  }

  it should "generate a hint to insert the correct script if the other one is missing" in {
    val solution = FileFinder.loadProgramFromFile(
      "example_programs/multi_actor/papagei_solution.sb3"
    )
    val studentProgram = FileFinder.loadProgramFromFile(
      "example_programs/multi_actor/papagei_missing_script_02.sb3"
    )

    val expectedScript = NodeListVisitor(
      solution.getActorDefinitionList.getDefinitions.get(1),
      _.isInstanceOf[Script]
    ).head.asInstanceOf[Script]
    expectedScript.getEvent shouldBe a[GreenFlag]

    val hintGen = new StructuralHintGenerator(List(solution))
    val hints   = hintGen.generateHints(studentProgram)

    val insertionHints = hints.hints.collect {
      case i @ InsertionHint(_, _: Script, _) => i
    }
    insertionHints should have length 1

    val actualHint = insertionHints.head
    actualHint.actorId.value shouldBe "Papagei"
    actualHint.references.node shouldBe a[ScriptList]
    actualHint.node.structurallyEqual(expectedScript) shouldBe true
  }

  it should "generate a delete hint for a superfluous script" in {
    val solution = FileFinder.loadProgramFromFile(
      "example_programs/multi_actor/papagei_solution.sb3"
    )
    val studentProgram = FileFinder.loadProgramFromFile(
      "example_programs/multi_actor/papagei_additional_script.sb3"
    )

    val papageiActor = solution.getActorDefinitionList.getDefinitions.get(1)
    papageiActor.name shouldBe "Papagei"

    val hintGen = new StructuralHintGenerator(List(solution))
    val hints   = hintGen.generateHints(studentProgram)

    val deleteHints = hints.hints.collect {
      case d @ DeletionHint(StudentNode(_: Script)) => d
    }
    deleteHints should have length 1

    val actualHint = deleteHints.head
    actualHint.actorId.value shouldBe "Papagei"
    actualHint.node.node
      .asInstanceOf[Script]
      .getEvent shouldBe a[AttributeAboveValue]
  }

  it should "generate no delete hint for an unreachable superfluous script" in {
    val solution = FileFinder.loadProgramFromFile(
      "example_programs/multi_actor/papagei_solution.sb3"
    )
    val studentProgram = FileFinder.loadProgramFromFile(
      "example_programs/multi_actor/papagei_additional_script_never.sb3"
    )

    val hintGen = new StructuralHintGenerator(List())
    hintGen.addSolutions(solution)
    val hints = hintGen.generateHints(studentProgram)

    val deleteHints = hints.hints.collect {
      case d @ DeletionHint(StudentNode(_: Script)) => d
    }
    deleteHints shouldBe empty
  }

  it should "generate an ReplacementHint of the event from type Never to a proper one if only the script head is missing" in {
    val solution = FileFinder.loadProgramFromFile(
      "example_programs/multi_actor/papagei_solution.sb3"
    )
    val studentProgram = FileFinder.loadProgramFromFile(
      "example_programs/multi_actor/papagei_missing_script_03.sb3"
    )

    val hintGen = new StructuralHintGenerator(List(solution))
    val hints   = hintGen.generateHints(studentProgram)

    val replaceScriptsHints = hints.hints.collect {
      case r @ ReplaceFieldHint(_, "event", _) => r
    }
    replaceScriptsHints should have length 1

    val replaceEventHint = replaceScriptsHints.head
    val scriptToChange   = replaceEventHint.references.node.asInstanceOf[Script]
    scriptToChange.getEvent shouldBe a[Never]
    replaceEventHint.newNode
      .asInstanceOf[ReceptionOfMessage]
      .getMsg
      .getMessage
      .asInstanceOf[StringLiteral]
      .getText shouldBe "Milch entdeckt"
  }
}
