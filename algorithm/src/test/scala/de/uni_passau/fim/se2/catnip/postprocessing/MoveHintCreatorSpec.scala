package de.uni_passau.fim.se2.catnip.postprocessing

import de.uni_passau.fim.se2.catnip.common.FileFinder
import de.uni_passau.fim.se2.catnip.common.defs.UnitSpec
import de.uni_passau.fim.se2.catnip.hint_generation.model
import de.uni_passau.fim.se2.catnip.hint_generation.model.HintGenerationResult
import de.uni_passau.fim.se2.catnip.model.{
  DeletionHint,
  InsertInFieldHint,
  InsertionHint,
  MoveInFieldHint,
  MoveToPosHint,
  ReorderHint,
  ReplaceFieldHint,
  ReplaceStmtHint,
  ScratchProgram,
  SolutionProgram,
  StudentNode,
  StudentProgram
}
import de.uni_passau.fim.se2.catnip.util.{
  ASTNodeSimilarity,
  NodeGen,
  NodeListVisitor
}
import de.uni_passau.fim.se2.litterbox.ast.model.event.GreenFlag
import de.uni_passau.fim.se2.litterbox.ast.model.{Script, StmtList}
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.BiggerThan
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.{Add, Round}
import de.uni_passau.fim.se2.litterbox.ast.model.literals.{
  NumberLiteral,
  StringLiteral
}
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorsound.ChangeVolumeBy
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.WaitSeconds
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.{
  IfThenStmt,
  UntilStmt
}
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.Say
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.{
  PointInDirection,
  SetXTo,
  SetYTo
}

import scala.jdk.CollectionConverters.*

class MoveHintCreatorSpec extends UnitSpec {
  "The MoveHintCreator" should "not change the hint list if no combinations could be found" in {
    val program = ScratchProgram(
      FileFinder.loadProgramFromFile("example_programs/simple/simple_if_if.sb3")
    )
    val ifs = NodeListVisitor(program, _.isInstanceOf[IfThenStmt])
      .map(_.asInstanceOf[IfThenStmt])
      .map(StudentNode(_))
      .map(DeletionHint(_))

    val expected = HintGenerationResult(
      StudentProgram(program),
      SolutionProgram(program),
      ifs
    )
    val actual = MoveHintCreator.process(expected)

    actual.hints should contain theSameElementsAs ifs
  }

  it should "combine a delete and insert of the same ast leaf" in {
    val program = ScratchProgram(
      FileFinder.loadProgramFromFile(
        "example_programs/move_hint_test/move_hint_wait.sb3"
      )
    )
    val ifNode = NodeListVisitor(program, _.isInstanceOf[IfThenStmt])
      .map(_.asInstanceOf[IfThenStmt])
      .head
    val waits = NodeListVisitor(program, _.isInstanceOf[WaitSeconds])
      .map(_.asInstanceOf[WaitSeconds])

    val waitOther =
      waits.filter(_.getParentNode.getParentNode.isInstanceOf[Script]).head

    val del = DeletionHint(StudentNode(waitOther))
    val ins = InsertionHint(
      StudentNode(ifNode.getThenStmts),
      new WaitSeconds(
        new NumberLiteral(10),
        NodeGen.generateNonDataBlockMetadata()
      ),
      0
    )

    { // only one insert hint of the same type
      val hints = HintGenerationResult(
        StudentProgram(program),
        SolutionProgram(program),
        List(del, ins)
      )
      val expected =
        MoveToPosHint(
          StudentNode(ifNode.getThenStmts),
          StudentNode(waitOther),
          0
        )

      MoveHintCreator.process(hints).hints should contain only expected
    }

    { // two insert hints with fitting nodes => should only be removed once
      val hints = HintGenerationResult(
        StudentProgram(program),
        SolutionProgram(program),
        List(del, ins, ins)
      )
      val expected =
        MoveToPosHint(
          StudentNode(ifNode.getThenStmts),
          StudentNode(waitOther),
          0
        )

      MoveHintCreator
        .process(hints)
        .hints should contain theSameElementsAs List(expected, ins)
    }
  }

  it should "combine a delete and insert of the same complex node" in {
    val program = ScratchProgram(
      FileFinder.loadProgramFromFile(
        "example_programs/move_hint_test/move_hint_repeat_until.sb3"
      )
    )

    val ifNode = NodeListVisitor(program, _.isInstanceOf[IfThenStmt])
      .map(_.asInstanceOf[IfThenStmt])
      .head
    val repeatUntil = NodeListVisitor(program, _.isInstanceOf[UntilStmt])
      .map(_.asInstanceOf[UntilStmt])
      .head

    val del = DeletionHint(StudentNode(repeatUntil))
    val biggerThan = new BiggerThan(
      new Round(
        new NumberLiteral(23.4),
        NodeGen.generateNonDataBlockMetadata()
      ),
      new NumberLiteral(50.0),
      NodeGen.generateNonDataBlockMetadata()
    )
    val ins = InsertionHint(
      StudentNode(ifNode.getThenStmts),
      new UntilStmt(
        biggerThan,
        new StmtList(
          List(
            new ChangeVolumeBy(
              new NumberLiteral(-10),
              NodeGen.generateNonDataBlockMetadata()
            ).asInstanceOf[Stmt]
          ).asJava
        ),
        NodeGen.generateNonDataBlockMetadata()
      ),
      0
    )

    { // only one insert hint of the same type
      val hints = HintGenerationResult(
        StudentProgram(program),
        SolutionProgram(program),
        List(del, ins)
      )
      val expected =
        MoveToPosHint(
          StudentNode(ifNode.getThenStmts),
          StudentNode(repeatUntil),
          0
        )

      MoveHintCreator.process(hints).hints should contain only expected

      val postprocessor = new PostProcessingDriver
      postprocessor.add(MoveHintCreator)

      postprocessor.process(hints).hints should contain only expected
    }

    { // two insert hints with fitting nodes => should only be removed once
      val hints = HintGenerationResult(
        StudentProgram(program),
        SolutionProgram(program),
        List(del, ins, ins)
      )
      val expected =
        MoveToPosHint(
          StudentNode(ifNode.getThenStmts),
          StudentNode(repeatUntil),
          0
        )

      MoveHintCreator
        .process(hints)
        .hints should contain theSameElementsAs List(expected, ins)

      val postprocessor = new PostProcessingDriver
      postprocessor.add(MoveHintCreator)

      postprocessor.process(hints).hints should contain theSameElementsAs List(
        expected,
        ins
      )
    }
  }

  it should "combine a delete and insertInField hint" in {
    val program = ScratchProgram(
      FileFinder.loadProgramFromFile(
        "example_programs/move_hint_test/move_hint_if_condition.sb3"
      )
    )

    val ifNode = NodeListVisitor(program, _.isInstanceOf[IfThenStmt])
      .map(_.asInstanceOf[IfThenStmt])
      .head
    val condition = NodeListVisitor(program, _.isInstanceOf[BiggerThan])
      .map(_.asInstanceOf[BiggerThan])
      .head

    val ins = InsertInFieldHint(
      StudentNode(ifNode),
      new BiggerThan(
        new NumberLiteral(23.4),
        new NumberLiteral(50.0),
        NodeGen.generateNonDataBlockMetadata()
      ),
      "boolExpr"
    )
    val del = DeletionHint(StudentNode(condition))
    val unrelated = ReorderHint(
      StudentNode(ifNode),
      StudentNode(ifNode.getThenStmts.getStmts.get(1)),
      2
    )

    val hints = HintGenerationResult(
      StudentProgram(program),
      SolutionProgram(program),
      List(ins, del, unrelated)
    )
    val expected =
      MoveInFieldHint(StudentNode(ifNode), StudentNode(condition), "boolExpr")

    MoveHintCreator.process(hints).hints should contain theSameElementsAs List(
      expected,
      unrelated
    )
  }

  it should "combine a child of a deleted node and an insertInField hint" in {
    val program = ScratchProgram(
      FileFinder.loadProgramFromFile(
        "example_programs/move_hint_test/move_hint_repeat_until_if.sb3"
      )
    )

    val ifNode = NodeListVisitor(program, _.isInstanceOf[IfThenStmt])
      .map(_.asInstanceOf[IfThenStmt])
      .head
    val untilStmt = NodeListVisitor(program, _.isInstanceOf[UntilStmt])
      .map(_.asInstanceOf[UntilStmt])
      .head

    val condition = new BiggerThan(
      new NumberLiteral(100),
      new NumberLiteral(50),
      NodeGen.generateNonDataBlockMetadata()
    )
    val del = DeletionHint(StudentNode(untilStmt))

    val insertHint =
      InsertInFieldHint(StudentNode(ifNode), condition, "boolExpr")

    val hintGenResult = HintGenerationResult(
      StudentProgram(program),
      SolutionProgram(program),
      List(del, insertHint)
    )

    val actual = MoveHintCreator.process(hintGenResult).hints
    actual should have length 2
    actual should contain(del)

    val moveHintsActual = actual.collect { case m: MoveInFieldHint => m }
    moveHintsActual should have length 1
    val moveHint = moveHintsActual.head
    moveHint.fieldName shouldBe "boolExpr"
    moveHint.newParent shouldBe StudentNode(ifNode)
    moveHint.node.node.structurallyEqual(condition) shouldBe true
  }

  it should "combine a field to be replaced and an insert hint" in {
    // move replaced node to insert point
    // replace hint has to be kept

    val program = ScratchProgram(
      FileFinder.loadProgramFromFile(
        "example_programs/move_hint_test/move_hint_setx_sety.sb3"
      )
    )

    val setX =
      NodeListVisitor(program, _.isInstanceOf[SetXTo]).head.asInstanceOf[SetXTo]
    val setY =
      NodeListVisitor(program, _.isInstanceOf[SetYTo]).head.asInstanceOf[SetYTo]

    val newNode = {
      val n1  = new NumberLiteral(12.0)
      val n2  = new NumberLiteral(2.0)
      val add = new Add(n1, n2, NodeGen.generateNonDataBlockMetadata())
      n1.setParentNode(add)
      n2.setParentNode(add)
      add
    }
    val replaceFieldHint =
      ReplaceFieldHint(StudentNode(setX), "num", new NumberLiteral(1233))
    val insertionHint = InsertInFieldHint(StudentNode(setY), newNode, "num")

    val hintGenResult = HintGenerationResult(
      StudentProgram(program),
      SolutionProgram(program),
      List(replaceFieldHint, insertionHint)
    )

    val actual = MoveHintCreator.process(hintGenResult).hints
    actual should have length 2
    actual should contain(replaceFieldHint)

    val moveHintsActual = actual.collect { case m: MoveInFieldHint => m }
    moveHintsActual should have length 1
    moveHintsActual.head.newParent shouldBe StudentNode(setY)
    moveHintsActual.head.node.structurallyEqual(
      StudentNode(newNode)
    ) shouldBe true
    moveHintsActual.head.fieldName shouldBe "num"
  }

  it should "combine a statement to be replaced and an insert hint" in {
    // move replaced statement to insert points
    // replace hint has to be kept

    val program = ScratchProgram(
      FileFinder.loadProgramFromFile(
        "example_programs/move_hint_test/move_hint_sety_point.sb3"
      )
    )

    val greenFlag = NodeListVisitor(program, _.isInstanceOf[GreenFlag]).head
      .asInstanceOf[GreenFlag]
    val point = NodeListVisitor(program, _.isInstanceOf[PointInDirection]).head
      .asInstanceOf[PointInDirection]

    val insertionHint = {
      val p = new PointInDirection(
        new NumberLiteral(90),
        NodeGen.generateNonDataBlockMetadata()
      )
      InsertionHint(StudentNode(greenFlag), p, 2)
    }
    val newNode =
      new Say(new StringLiteral("asd"), NodeGen.generateNonDataBlockMetadata())
    val replaceStmtHint = ReplaceStmtHint(
      StudentNode(point.getParentNode),
      StudentNode(point),
      newNode,
      0
    ).get

    val hintGenResult = model.HintGenerationResult(
      StudentProgram(program),
      SolutionProgram(program),
      List(replaceStmtHint, insertionHint)
    )

    val actual = MoveHintCreator.process(hintGenResult).hints
    actual should have length 2
    actual should contain(replaceStmtHint)

    val moveHintsActual = actual.collect { case m: MoveToPosHint => m }
    moveHintsActual should have length 1
    val moveHint = moveHintsActual.head
    moveHint.newParent shouldBe StudentNode(greenFlag)
    moveHint.node shouldBe StudentNode(point)
    moveHint.position shouldBe 2
  }

  it should "combine a DeleteHint and the node inserted with a ReplaceFieldHint" in {
    // move deleted node to inserted place
    // replace hint no longer there

    val program = ScratchProgram(
      FileFinder.loadProgramFromFile(
        "example_programs/move_hint_test/move_hint_sety_point.sb3"
      )
    )

    val setX =
      NodeListVisitor(program, _.isInstanceOf[SetXTo]).head.asInstanceOf[SetXTo]
    val point = NodeListVisitor(program, _.isInstanceOf[PointInDirection]).head
      .asInstanceOf[PointInDirection]

    val delHint = DeletionHint(StudentNode(setX.getNum)) // delete 12+2
    val newNode = {
      val n1  = new NumberLiteral(12.0)
      val n2  = new NumberLiteral(2.0)
      val add = new Add(n1, n2, NodeGen.generateNonDataBlockMetadata())
      n1.setParentNode(add)
      n2.setParentNode(add)
      add
    }
    val replaceFieldHint =
      ReplaceFieldHint(StudentNode(point.getDirection), newNode).get

    val hintGenResult = HintGenerationResult(
      StudentProgram(program),
      SolutionProgram(program),
      List(delHint, replaceFieldHint)
    )

    val actual = MoveHintCreator.process(hintGenResult).hints
    actual should have length 1
    val moveHintsActual = actual.collect { case m: MoveInFieldHint => m }
    moveHintsActual should have length 1
    val moveHint = moveHintsActual.head
    moveHint.newParent shouldBe StudentNode(point)
    moveHint.node.structurallyEqual(StudentNode(newNode)) shouldBe true
    moveHint.fieldName shouldBe "direction"
  }

  it should "combine a DeleteHint and the statement inserted with a ReplaceStmtHint" in {
    // move deleted node to inserted place
    // delete hint, replace hint no longer there

    val program = ScratchProgram(
      FileFinder.loadProgramFromFile(
        "example_programs/move_hint_test/move_hint_sety_point.sb3"
      )
    )

    val setY =
      NodeListVisitor(program, _.isInstanceOf[SetYTo]).head.asInstanceOf[SetYTo]
    val point = NodeListVisitor(program, _.isInstanceOf[PointInDirection]).head
      .asInstanceOf[PointInDirection]

    val deletionHint = DeletionHint(StudentNode(setY))
    val newStmt =
      new SetYTo(new NumberLiteral(0), NodeGen.generateNonDataBlockMetadata())
    val replaceStmtHint = ReplaceStmtHint(
      StudentNode(point.getParentNode),
      StudentNode(point),
      newStmt,
      0
    ).get

    val hintGenResult = HintGenerationResult(
      StudentProgram(program),
      SolutionProgram(program),
      List(deletionHint, replaceStmtHint)
    )

    val actual = MoveHintCreator.process(hintGenResult).hints
    actual should have length 1
    val moveHintsActual = actual.collect { case m: MoveToPosHint => m }
    moveHintsActual should have length 1
    val moveHint = moveHintsActual.head
    moveHint.newParent shouldBe StudentNode(point.getParentNode)
    moveHint.node.structurallyEqual(StudentNode(newStmt)) shouldBe true
    moveHint.position shouldBe 0
  }
}
