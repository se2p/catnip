package de.uni_passau.fim.se2.catnip.postprocessing

import de.uni_passau.fim.se2.catnip.common.FileFinder
import de.uni_passau.fim.se2.catnip.common.defs.UnitSpec
import de.uni_passau.fim.se2.catnip.hint_generation.model.HintGenerationResult
import de.uni_passau.fim.se2.catnip.model.{
  ReorderHint,
  ScratchProgram,
  SolutionProgram,
  StudentNode,
  StudentProgram
}
import de.uni_passau.fim.se2.catnip.util.NodeListVisitor
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.NextBackdrop
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.WaitSeconds
import de.uni_passau.fim.se2.litterbox.ast.model.{ASTNode, StmtList}
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.{
  NextCostume,
  Say,
  SayForSecs,
  SwitchCostumeTo,
  ThinkForSecs
}
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.{
  GlideSecsTo,
  GlideSecsToXY,
  MoveSteps,
  PointTowards
}
import org.scalatest.prop.TableDrivenPropertyChecks

import scala.jdk.CollectionConverters.*

class SwappableBlocksHintReducerSpec
    extends UnitSpec
    with TableDrivenPropertyChecks {
  "The SwappableBlocksHintReducer" should "remove hints when the purple blocks are swapped in schifffahrt_init" in {
    val (program, stmtList) = getProgramAndStmts()
    val stmts               = stmtList.getStmts.asScala.map(StudentNode(_))

    val hints = List(
      ReorderHint(StudentNode(stmtList), stmts.head, 1),
      ReorderHint(StudentNode(stmtList), stmts(1), 0)
    )

    val input = HintGenerationResult(
      StudentProgram(program),
      SolutionProgram(program),
      hints
    )
    val reducedHints = SwappableBlocksHintReducer.process(input)

    reducedHints.hints shouldBe empty
  }

  it should "remove hints when the ChangeCostume and MoveToPos blocks are swapped in schifffahrt_init" in {
    val (program, stmtList) = getProgramAndStmts()
    val stmts               = stmtList.getStmts.asScala.map(StudentNode(_))

    val hints = List(
      ReorderHint(StudentNode(stmtList), stmts.head, 2),
      ReorderHint(StudentNode(stmtList), stmts(2), 0)
    )

    val input = HintGenerationResult(
      StudentProgram(program),
      SolutionProgram(program),
      hints
    )
    val reducedHints = SwappableBlocksHintReducer.process(input)

    reducedHints.hints shouldBe empty
  }

  it should "remove hints when a change backdrop and a change costume block are swapped" in {
    val (program, stmtList) =
      getProgramAndStmts("swappable/swappable_test_say_think_secs.sb3")
    val stmts = stmtList.getStmts.asScala.map(StudentNode(_))

    val changeCostume  = stmts.filter(_.node.isInstanceOf[NextCostume]).head
    val changeBackdrop = stmts.filter(_.node.isInstanceOf[NextBackdrop]).head
    val hints = List(
      ReorderHint(StudentNode(stmtList), changeCostume, 3),
      ReorderHint(StudentNode(stmtList), changeBackdrop, 2)
    )

    val input = HintGenerationResult(
      StudentProgram(program),
      SolutionProgram(program),
      hints
    )
    val reducedHints = SwappableBlocksHintReducer.process(input)

    reducedHints.hints shouldBe empty
  }

  it should "remove hints when changing position of all statements in schifffahrt_init" in {
    val (program, stmtList) = getProgramAndStmts()
    val stmts               = stmtList.getStmts.asScala.map(StudentNode(_))

    val permutations = (for {
      i <- 0 to 2
      j <- 0 to 2
    } yield (i, j)).permutations.map(_.slice(0, 3)).distinct

    forAll(Table("fromToPairs") ++ permutations) { fromToPairs =>
      val hints = fromToPairs
        .filter { case (a, b) => a != b }
        .map { case (a, b) => ReorderHint(StudentNode(stmtList), stmts(a), b) }
        .toList

      val input = HintGenerationResult(
        StudentProgram(program),
        SolutionProgram(program),
        hints
      )
      val reducedHints = SwappableBlocksHintReducer.process(input)

      reducedHints.hints shouldBe empty
    }
  }

  it should "remove the hints for special case: swapped movement blocks in schiffahrt_maussteuerung" in {
    def stmtListFinder(node: ASTNode) = {
      node.isInstanceOf[StmtList] && node.getParentNode.isInstanceOf[IfThenStmt]
    }

    val program = FileFinder.loadExampleProgramFromFile(
      "swappable/schifffahrt_maussteuerung.sb3"
    )
    val stmtList =
      NodeListVisitor(program, stmtListFinder).head.asInstanceOf[StmtList]
    val stmts = stmtList.getStmts.asScala.map(StudentNode(_))

    val hints = List(
      ReorderHint(StudentNode(stmtList), stmts.head, 1),
      ReorderHint(StudentNode(stmtList), stmts(1), 0)
    )

    val input = HintGenerationResult(
      StudentProgram(program),
      SolutionProgram(program),
      hints
    )
    val reducedHints = SwappableBlocksHintReducer.process(input)

    reducedHints.hints shouldBe empty
  }

  it should "not remove a swap hint if a ThinkForSecs block is moved" in {
    testUnchanged("swappable/swappable_test_say_think_secs.sb3")(1, 0)(
      _.node.isInstanceOf[PointTowards]
    )(_.node.isInstanceOf[ThinkForSecs])
  }

  it should "not remove a swap hint if a SayForSecs block is moved" in {
    testUnchanged("swappable/swappable_test_say_think_secs.sb3")(4, 3)(
      _.node.isInstanceOf[NextBackdrop]
    )(_.node.isInstanceOf[SayForSecs])
  }

  it should "not remove a swap hint if a SayForSecs block is in between" in {
    testUnchanged("swappable/swappable_test_say_think_secs.sb3")(2, 0)(
      _.node.isInstanceOf[PointTowards]
    )(_.node.isInstanceOf[NextCostume])
  }

  it should "not remove a swap hint if a ThinkForSecs block is in between" in {
    testUnchanged("swappable/swappable_test_say_think_secs.sb3")(5, 3)(
      _.node.isInstanceOf[NextBackdrop]
    )(_.node.isInstanceOf[MoveSteps])
  }

  it should "not remove a swap hint if a GlideTo block is in between" in {
    testUnchanged("swappable/swappable_test_glide.sb3")(2, 0)(
      _.node.isInstanceOf[SwitchCostumeTo]
    )(_.node.isInstanceOf[NextBackdrop])
  }

  it should "not remove a swap hint if a GlideTo block is moved" in {
    testUnchanged("swappable/swappable_test_glide.sb3")(0, 1)(
      _.node.isInstanceOf[GlideSecsTo]
    )(_.node.isInstanceOf[SwitchCostumeTo])
  }

  it should "not remove a swap hint if a GlideToXY block is in between" in {
    testUnchanged("swappable/swappable_test_glide.sb3")(8, 6)(
      _.node.isInstanceOf[MoveSteps]
    )(_.node.isInstanceOf[Say])
  }

  it should "not remove a swap hint if a GlideToXY block is moved" in {
    testUnchanged("swappable/swappable_test_glide.sb3")(8, 7)(
      _.node.isInstanceOf[GlideSecsToXY]
    )(_.node.isInstanceOf[Say])
  }

  it should "not remove a swap hint if a yellow block is in between" in {
    testUnchanged("swappable/swappable_test_glide.sb3")(4, 2)(
      _.node.isInstanceOf[NextBackdrop]
    )(_.node.isInstanceOf[PointTowards])
  }

  it should "not remove a swap hint if a yellow block is moved" in {
    testUnchanged("swappable/swappable_test_glide.sb3")(4, 3)(
      _.node.isInstanceOf[WaitSeconds]
    )(_.node.isInstanceOf[PointTowards])
  }

  it should "remove the hint if a NextCostume block is in between" in {
    testRemoved("swappable/swappable_test_glide.sb3")(6, 4)(
      _.node.isInstanceOf[PointTowards]
    )(_.node.isInstanceOf[MoveSteps])
  }

  private def testUnchanged(filename: String)(targetPosA: Int, targetPosB: Int)(
      nodeA: StudentNode => Boolean
  )(nodeB: StudentNode => Boolean): Unit = {
    testResult(filename)(targetPosA, targetPosB)(nodeA)(nodeB)(shouldBeEmpty =
      false
    )
  }

  private def testRemoved(filename: String)(targetPosA: Int, targetPosB: Int)(
      nodeA: StudentNode => Boolean
  )(nodeB: StudentNode => Boolean): Unit = {
    testResult(filename)(targetPosA, targetPosB)(nodeA)(nodeB)(shouldBeEmpty =
      true
    )
  }

  private def testResult(filename: String)(targetPosA: Int, targetPosB: Int)(
      nodeA: StudentNode => Boolean
  )(nodeB: StudentNode => Boolean)(shouldBeEmpty: Boolean): Unit = {
    val (program, stmtList) = getProgramAndStmts(filename)
    val stmts               = stmtList.getStmts.asScala.map(StudentNode(_))

    val foundNodeA = stmts.filter(nodeA).head
    val foundNodeB = stmts.filter(nodeB).head
    val hints = List(
      ReorderHint(StudentNode(stmtList), foundNodeA, targetPosA),
      ReorderHint(StudentNode(stmtList), foundNodeB, targetPosB)
    )

    val input = HintGenerationResult(
      StudentProgram(program),
      SolutionProgram(program),
      hints
    )
    val reducedHints = SwappableBlocksHintReducer.process(input)

    if (shouldBeEmpty) {
      reducedHints.hints shouldBe empty
    } else {
      reducedHints.hints should contain theSameElementsAs hints
    }
  }

  private def getProgramAndStmts(
      file: String = "swappable/schifffahrt_init.sb3"
  ): (ScratchProgram, StmtList) = {
    val program =
      FileFinder.loadExampleProgramFromFile(file)
    val stmtList = NodeListVisitor(program, _.isInstanceOf[StmtList]).head
      .asInstanceOf[StmtList]

    (ScratchProgram(program), stmtList)
  }
}
