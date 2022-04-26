package de.uni_passau.fim.se2.catnip.model

import de.uni_passau.fim.se2.catnip.common.FileFinder
import de.uni_passau.fim.se2.catnip.common.defs.UnitSpec
import de.uni_passau.fim.se2.catnip.hint_generation.StructuralReplacementHintGenerator
import de.uni_passau.fim.se2.catnip.util.NodeListVisitor
import de.uni_passau.fim.se2.litterbox.ast.visitor.CloneVisitor

class ScratchProgramSpec extends UnitSpec {
  "The ScratchProgram" should "clone the program with a CloneVisitor" in {
    val p = FileFinder.loadProgramFromFile(
      "example_programs/simple/simple_add_mult.sb3"
    )
    val sp = ScratchProgram(p)
    val p2 = sp.accept(new CloneVisitor)

    StructuralReplacementHintGenerator(
      SolutionNode(p),
      StudentNode(p2)
    ) should be(empty)
  }

  it should "should clone the program when wrapped in a student program" in {
    val p = FileFinder.loadProgramFromFile(
      "example_programs/simple/simple_add_mult.sb3"
    )
    val sp = StudentProgram(ScratchProgram(p))
    val p2 = sp.accept(new CloneVisitor)

    StructuralReplacementHintGenerator(
      SolutionNode(p),
      StudentNode(p2.program.program)
    ) should be(empty)
  }

  it should "should clone the program when wrapped in a solution program" in {
    val p = FileFinder.loadProgramFromFile(
      "example_programs/simple/simple_add_mult.sb3"
    )
    val sp = SolutionProgram(ScratchProgram(p))
    val p2 = sp.accept(new CloneVisitor)

    StructuralReplacementHintGenerator(
      SolutionNode(p),
      StudentNode(p2.program.program)
    ) should be(empty)
  }

  it should "accept a ScratchVisitor" in {
    val p = FileFinder.loadProgramFromFile(
      "example_programs/simple/simple_add_mult.sb3"
    )
    val sp       = ScratchProgram(p)
    val solProg  = SolutionProgram(sp)
    val studProg = StudentProgram(sp)

    val nodes1 = NodeListVisitor(sp)
    val nodes2 = NodeListVisitor(solProg.program)
    val nodes3 = NodeListVisitor(studProg.program)

    nodes1 should contain theSameElementsAs nodes2
    nodes1 should contain theSameElementsAs nodes3
    nodes2 should contain theSameElementsAs nodes3
  }

  it should "filter unreachable scripts" in {
    val p = FileFinder.loadProgramFromFile(
      "example_programs/simple/simple_detached.sb3"
    )
    val sp = ScratchProgram(p)

    sp.reachableScripts should have length 1
  }
}
