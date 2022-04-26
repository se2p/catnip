package de.uni_passau.fim.se2.catnip.hint_application

import de.uni_passau.fim.se2.catnip.common.FileFinder
import de.uni_passau.fim.se2.catnip.common.defs.UnitSpec
import de.uni_passau.fim.se2.catnip.model.{
  DeletionHint,
  DummyNode,
  InsertInFieldHint,
  InsertionHint,
  MissingActorHint,
  MoveInFieldHint,
  MoveToPosHint,
  ReorderHint,
  ReplaceFieldHint,
  ScratchProgram,
  StudentNode
}
import de.uni_passau.fim.se2.catnip.util.{ASTNodeExt, NodeGen, NodeListVisitor}
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.{
  Add,
  NumFunct,
  Round
}
import de.uni_passau.fim.se2.litterbox.ast.model.literals.{
  NumberLiteral,
  StringLiteral
}
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.{
  IfThenStmt,
  RepeatForeverStmt
}
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.Say
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.{
  ChangeXBy,
  ChangeYBy,
  IfOnEdgeBounce
}
import de.uni_passau.fim.se2.litterbox.ast.model.{ASTNode, Program, StmtList}

import scala.language.implicitConversions
import scala.jdk.CollectionConverters.*

class StructuralHintApplicatorSpec extends UnitSpec {
  implicit def programToScratchProgram(program: Program): ScratchProgram = {
    ScratchProgram(program)
  }

  "The Deletion Hint Application" should "delete a regular node" in {
    val program = FileFinder.loadProgramFromFile(
      "example_programs/hint_application_test/set_variable_expression.sb3"
    )

    val roundNode = NodeListVisitor(program, _.isInstanceOf[Round]).head

    val hint       = DeletionHint(StudentNode(roundNode))
    val newProgram = StructuralHintApplicator.apply(program, hint)

    NodeListVisitor(newProgram, _.isInstanceOf[Round]) should be(empty)
  }

  it should "delete a leaf" in {
    val program = FileFinder.loadProgramFromFile(
      "example_programs/hint_application_test/set_variable_expression.sb3"
    )

    val search = (node: ASTNode) =>
      node.isInstanceOf[NumberLiteral] && node
        .asInstanceOf[NumberLiteral]
        .getValue == 23.4

    val leaf = NodeListVisitor(program, search).head

    val hint = DeletionHint(StudentNode(leaf))

    {
      val newProgram = StructuralHintApplicator.apply(program, hint)
      NodeListVisitor(newProgram, search) should be(empty)
    }

    {
      val newProgram = StructuralHintApplicator.apply(program, List(hint))
      NodeListVisitor(newProgram, search) should be(empty)
    }
  }

  it should "delete only one leaf if multiple identical ones exist" in {
    val program = FileFinder.loadProgramFromFile(
      "example_programs/hint_application_test/identical_leaves.sb3"
    )

    val search = (node: ASTNode) =>
      node.isInstanceOf[NumberLiteral] && node
        .asInstanceOf[NumberLiteral]
        .getValue == 23.4

    val leaf = NodeListVisitor(program, search).head

    val hint       = DeletionHint(StudentNode(leaf))
    val newProgram = StructuralHintApplicator.apply(program, hint)

    NodeListVisitor(newProgram, search) should have length 1
  }

  it should "replace a StmtList by an empty list" in {
    val program =
      FileFinder.loadProgramFromFile("example_programs/simple/simple_if_if.sb3")
    val outerIf = NodeListVisitor(program, _.isInstanceOf[IfThenStmt]).head
      .asInstanceOf[IfThenStmt]
    outerIf.getThenStmts.getStmts should not be empty

    val hint = DeletionHint(StudentNode(outerIf.getThenStmts))

    val newProgram = StructuralHintApplicator(ScratchProgram(program), hint)
    val newIf = NodeListVisitor(newProgram, _.isInstanceOf[IfThenStmt]).head
      .asInstanceOf[IfThenStmt]
    newIf.getThenStmts.getStmts shouldBe empty
  }

  "The Insertion Hint Application" should "insert a node into a StmtList" in {
    val program = FileFinder.loadProgramFromFile(
      "example_programs/hint_application_test/set_variable_expression.sb3"
    )

    val parent = NodeListVisitor(program, _.isInstanceOf[StmtList]).head
      .asInstanceOf[StmtList]
    parent.getStmts should have size 2
    parent.getStmts.get(0) shouldBe a[IfThenStmt]

    val newNode =
      new Say(new StringLiteral("some"), NodeGen.generateNonDataBlockMetadata())
    val hint = InsertionHint(StudentNode(parent), newNode, 2)

    val newProgram = StructuralHintApplicator.apply(program, hint)

    NodeListVisitor(newProgram, _ == newNode) should contain only newNode

    val stmtLists = NodeListVisitor(newProgram, _.isInstanceOf[StmtList])
    val newParent = stmtLists.head.asInstanceOf[StmtList]

    newParent.getStmts.get(0) shouldBe a[IfThenStmt]
    newParent.getStmts should contain(newNode)
    newParent.getChildren should contain(newNode)
    newParent.getStmts.indexOf(newNode) should be(2)
    newParent.getStmts should have size 3
    newParent.getChildren should have size 3
    for (c <- newParent.getStmts.asScala) {
      c.getParentNode shouldBe newParent
    }
  }

  it should "leave other StmtLists untouched" in {
    val program = FileFinder.loadProgramFromFile(
      "example_programs/hint_application_test/stmt_list.sb3"
    )

    val parent = NodeListVisitor(program, _.isInstanceOf[StmtList]).head
      .asInstanceOf[StmtList]
    parent.getStmts should have size 4
    parent.getStmts.get(0) shouldBe a[IfThenStmt]

    val newNode =
      new Say(new StringLiteral("some"), NodeGen.generateNonDataBlockMetadata())
    val hint = InsertionHint(StudentNode(parent), newNode, 2)

    val newProgram = StructuralHintApplicator.apply(program, hint)

    NodeListVisitor(newProgram, _ == newNode) should contain only newNode

    val stmtLists = NodeListVisitor(newProgram, _.isInstanceOf[StmtList])
    stmtLists should have length 2

    val newParent = stmtLists.head.asInstanceOf[StmtList]
    newParent.getStmts.get(0) shouldBe a[IfThenStmt]
    newParent.getStmts should contain(newNode)
    newParent.getChildren should contain(newNode)
    newParent.getStmts.indexOf(newNode) should be(2)
    newParent.getStmts should have size 5
    newParent.getChildren should have size 5
    for (c <- newParent.getStmts.asScala) {
      c.getParentNode shouldBe newParent
    }

    val thenStmts = stmtLists.tail.head.asInstanceOf[StmtList]
    thenStmts.getParentNode shouldBe a[IfThenStmt]
    thenStmts.getStmts should have size 1
  }

  "The InsertInField Hint Application" should "replace an ASTLeaf" in {
    val program = FileFinder.loadProgramFromFile(
      "example_programs/hint_application_test/stmt_list.sb3"
    )

    val parent = NodeListVisitor(program, _.isInstanceOf[ChangeXBy]).head
      .asInstanceOf[ChangeXBy]
    parent.getNum should be(new NumberLiteral(10))

    val newNode = new Add(
      new NumberLiteral(1.0),
      new NumberLiteral(12.0),
      NodeGen.generateNonDataBlockMetadata()
    )
    val hint = InsertInFieldHint(StudentNode(parent), newNode, "num")

    val newProgram = StructuralHintApplicator.apply(program, hint)

    val newParent = NodeListVisitor(newProgram, _.isInstanceOf[ChangeXBy]).head
      .asInstanceOf[ChangeXBy]
    for (c <- newParent.getChildren.asScala) {
      c.getParentNode shouldBe newParent
    }
    newParent.getNum should be(newNode)
    newParent.getChildren should contain(newNode)
  }

  "The Reorder Hint Application" should "move a node to a new index in the StmtList" in {
    val program = FileFinder.loadProgramFromFile(
      "example_programs/hint_application_test/stmt_list.sb3"
    )

    val parent = NodeListVisitor(program, _.isInstanceOf[StmtList]).head
      .asInstanceOf[StmtList]
    parent.getStmts should have size 4

    val node = NodeListVisitor(program, _.isInstanceOf[IfOnEdgeBounce]).head
    parent.getStmts.indexOf(node) should be(1)

    val hint       = ReorderHint(StudentNode(parent), StudentNode(node), 2)
    val newProgram = StructuralHintApplicator.apply(program, hint)

    val newParent = NodeListVisitor(newProgram, _.isInstanceOf[StmtList]).head
      .asInstanceOf[StmtList]
    newParent.getStmts should have size 4
    newParent.getStmts.indexOf(node) should be(2)
    newParent.getChildren should contain(node)
  }

  it should "be able to move a node to the last position in the StmtList" in {
    val program = FileFinder.loadProgramFromFile(
      "example_programs/hint_application_test/stmt_list.sb3"
    )

    val parent = NodeListVisitor(program, _.isInstanceOf[StmtList]).head
      .asInstanceOf[StmtList]
    parent.getStmts should have size 4

    val node = NodeListVisitor(program, _.isInstanceOf[IfOnEdgeBounce]).head
    parent.getStmts.indexOf(node) should be(1)

    val hint       = ReorderHint(StudentNode(parent), StudentNode(node), 3)
    val newProgram = StructuralHintApplicator.apply(program, hint)

    val newParent = NodeListVisitor(newProgram, _.isInstanceOf[StmtList]).head
      .asInstanceOf[StmtList]
    newParent.getStmts should have size 4
    newParent.getStmts.indexOf(node) should be(3)
    newParent.getChildren should contain(node)
  }

  "The Replace Field Hint Application" should "replace a regular node" in {
    val program = FileFinder.loadProgramFromFile(
      "example_programs/hint_application_test/set_variable_expression.sb3"
    )

    val newNode   = new NumFunct(NumFunct.NumFunctType.ABS.getFunction)
    val roundNode = NodeListVisitor(program, _.isInstanceOf[Round]).head

    val hint = ReplaceFieldHint(StudentNode(roundNode), newNode).get

    {
      val newProgram = StructuralHintApplicator.apply(program, hint)

      NodeListVisitor(newProgram, _.isInstanceOf[Round]) should be(empty)
      NodeListVisitor(
        newProgram,
        _.isInstanceOf[NumFunct]
      ) should contain only newNode
    }

    {
      val newProgram = StructuralHintApplicator.apply(program, List(hint))

      NodeListVisitor(newProgram, _.isInstanceOf[Round]) should be(empty)
      NodeListVisitor(
        newProgram,
        _.isInstanceOf[NumFunct]
      ) should contain only newNode
    }
  }

  "The Move Hint Application" should "move a node from one StmtList to another" in {
    val program = FileFinder.loadProgramFromFile(
      "example_programs/hint_application_test/stmt_list.sb3"
    )

    val stmtLists = NodeListVisitor(program, _.isInstanceOf[StmtList])
    stmtLists should have length 2

    val oldParent = stmtLists.head.asInstanceOf[StmtList]
    oldParent.getStmts should have size 4
    val newParent = stmtLists.tail.head.asInstanceOf[StmtList]
    newParent.getParentNode shouldBe a[IfThenStmt]

    val node = NodeListVisitor(program, _.isInstanceOf[IfOnEdgeBounce]).head
    oldParent.getStmts.indexOf(node) should be(1)

    val hint       = MoveToPosHint(StudentNode(newParent), StudentNode(node), 1)
    val newProgram = StructuralHintApplicator.apply(program, hint)

    val updatedStmtLists = NodeListVisitor(newProgram, _.isInstanceOf[StmtList])
    updatedStmtLists should have length 2

    val oldParentUpdated = updatedStmtLists.head.asInstanceOf[StmtList]
    oldParentUpdated.getStmts should have size 3
    oldParentUpdated.getChildren should have size 3
    oldParentUpdated.getStmts should not contain node
    oldParentUpdated.getChildren should not contain node

    val newParentUpdated = updatedStmtLists.tail.head.asInstanceOf[StmtList]
    newParentUpdated.getStmts should have size 2
    newParentUpdated.getChildren should have size 2
    newParentUpdated.getStmts should contain(node)
    newParentUpdated.getChildren should contain(node)
    newParentUpdated.getStmts.indexOf(node) should be(1)
    newParentUpdated.getChildren.indexOf(node) should be(1)

    hint.node.node.getParentNode should be(newParentUpdated)
  }

  "The Move in Field Hint Application" should "move the node from one field to another" in {
    val program = FileFinder.loadProgramFromFile(
      "example_programs/hint_application_test/move_hint_fields.sb3"
    )

    val getChangeX = (p: Program) =>
      NodeListVisitor(p, _.isInstanceOf[ChangeXBy]).head.asInstanceOf[ChangeXBy]
    val getChangeY = (p: Program) =>
      NodeListVisitor(p, _.isInstanceOf[ChangeYBy]).head.asInstanceOf[ChangeYBy]

    val oldParent = getChangeX(program)
    val newParent = getChangeY(program)

    val hint = MoveInFieldHint(
      StudentNode(newParent),
      StudentNode(oldParent.getNum),
      "num"
    )
    val newProgram = StructuralHintApplicator(program, hint)

    val newChangeX = getChangeX(newProgram.program)
    val newChangeY = getChangeY(newProgram.program)

    newChangeX.getNum should be(new DummyNode)
    newChangeX.getChildren should not contain oldParent.getNum
    newChangeY.getNum.blockId shouldBe oldParent.getNum.blockId
    newChangeY.getChildren should contain(oldParent.getNum)

    val newNode = NodeListVisitor(newProgram, _.isInstanceOf[Round]).head
      .asInstanceOf[Round]
    newNode.getOperand1 should be(new NumberLiteral(20.4))
    newNode.getParentNode should be(newChangeY)
    newChangeY.getNum should be(newNode)
  }

  "The MissingActorHint Applicator" should "add a new actor to a program" in {
    val papageiProgram = FileFinder.loadProgramFromFile(
      "example_programs/multi_actor/papagei_solution.sb3"
    )
    val otherProgram = FileFinder.loadProgramFromFile(
      "example_programs/simple/simple_if_if.sb3"
    )

    papageiProgram.actors should have size 5

    val actorToAdd = otherProgram.program.actors("Sprite1")
    val hint = MissingActorHint(
      StudentNode(papageiProgram.getActorDefinitionList),
      actorToAdd
    ).get
    val newProgram =
      StructuralHintApplicator(ScratchProgram(papageiProgram), hint)

    newProgram.actors should have size 6
  }

  it should "add a new actor even if given some node in the program" in {
    val papageiProgram = FileFinder.loadProgramFromFile(
      "example_programs/multi_actor/papagei_solution.sb3"
    )
    val otherProgram = FileFinder.loadProgramFromFile(
      "example_programs/simple/simple_if_if.sb3"
    )

    papageiProgram.actors should have size 5

    val actorToAdd = otherProgram.program.actors("Sprite1")
    val papageiProgramNode =
      NodeListVisitor(papageiProgram, _.isInstanceOf[RepeatForeverStmt]).head
        .asInstanceOf[RepeatForeverStmt]
    papageiProgramNode.program should contain(papageiProgram)
    val hint = MissingActorHint(
      StudentNode(papageiProgram.getActorDefinitionList),
      actorToAdd
    ).get
    val newNode = StructuralHintApplicator(papageiProgramNode, hint)

    papageiProgram.actors should have size 6
    newNode shouldBe papageiProgramNode
    newNode.program should contain(papageiProgram)
  }

  it should "not crash when the given node does not belong to a program" in {
    val papageiProgram = FileFinder.loadProgramFromFile(
      "example_programs/multi_actor/papagei_solution.sb3"
    )
    val otherProgram = FileFinder.loadProgramFromFile(
      "example_programs/simple/simple_if_if.sb3"
    )

    papageiProgram.actors should have size 5

    val actorToAdd = otherProgram.program.actors("Sprite1")
    val papageiProgramNode =
      NodeListVisitor(papageiProgram, _.isInstanceOf[RepeatForeverStmt]).head
        .asInstanceOf[RepeatForeverStmt]

    papageiProgramNode.getParentNode.getParentNode.setParentNode(null)
    papageiProgramNode.program shouldBe empty

    val hint = MissingActorHint(
      StudentNode(papageiProgram.getActorDefinitionList),
      actorToAdd
    ).get
    val newNode = StructuralHintApplicator(papageiProgramNode, hint)

    papageiProgram.actors should have size 5
    newNode shouldBe papageiProgramNode
  }
}
