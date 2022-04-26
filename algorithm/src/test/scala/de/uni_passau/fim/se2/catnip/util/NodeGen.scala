package de.uni_passau.fim.se2.catnip.util

import de.uni_passau.fim.se2.catnip.model.BlockId
import de.uni_passau.fim.se2.litterbox.ast.model.event.GreenFlag
import de.uni_passau.fim.se2.litterbox.ast.model.{
  ActorDefinition,
  ActorType,
  ASTNode,
  Script,
  ScriptList,
  SetStmtList,
  StmtList
}
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.AsNumber
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.{Qualified, StrId}
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.{
  BroadcastMetadata,
  CommentMetadata,
  ListMetadata,
  VariableMetadata
}
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.actor.SpriteMetadata
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astLists.{
  BroadcastMetadataList,
  CommentMetadataList,
  FieldsMetadataList,
  ImageMetadataList,
  InputMetadataList,
  ListMetadataList,
  SoundMetadataList,
  VariableMetadataList
}
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.{
  NoMutationMetadata,
  NonDataBlockMetadata
}
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.resources.{
  ImageMetadata,
  SoundMetadata
}
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.{
  ProcedureDefinition,
  ProcedureDefinitionList
}
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.SetStmt
import de.uni_passau.fim.se2.litterbox.ast.model.statement.declaration.{
  DeclarationStmt,
  DeclarationStmtList
}
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.RotationStyle
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Variable

import java.util
import scala.jdk.CollectionConverters.*

object NodeGen {
  def generateNumberVariable(identifier: String): AsNumber = {
    new AsNumber(generateVariable(identifier))
  }

  def generateVariable(identifier: String): Qualified = {
    new Qualified(new StrId(identifier), new Variable(new StrId(identifier)))
  }

  def generateNonDataBlockMetadata(
      blockId: String = BlockId.random().toString,
      opcode: String = "add",
      parentId: String = "123"
  ): NonDataBlockMetadata = {
    new NonDataBlockMetadata(
      "asd",
      blockId,
      opcode,
      "111",
      parentId,
      new InputMetadataList(java.util.List.of()),
      new FieldsMetadataList(java.util.List.of()),
      false,
      false,
      new NoMutationMetadata
    )
  }

  def withActor[T <: ASTNode](node: T): T = {
    val stmtList = new StmtList(List().asJava)
    node.setParentNode(stmtList)

    val script = new Script(
      new GreenFlag(NodeGen.generateNonDataBlockMetadata()),
      stmtList
    )
    stmtList.setParentNode(script)

    val scriptList = new ScriptList(List(script).asJava)
    script.setParentNode(scriptList)

    val actor = new ActorDefinition(
      ActorType.getSprite,
      new StrId("asdasd"),
      new DeclarationStmtList(new util.ArrayList[DeclarationStmt]()),
      new SetStmtList(new util.ArrayList[SetStmt]()),
      new ProcedureDefinitionList(new util.ArrayList[ProcedureDefinition]()),
      scriptList,
      new SpriteMetadata(
        new CommentMetadataList(new util.ArrayList[CommentMetadata]()),
        new VariableMetadataList(new util.ArrayList[VariableMetadata]()),
        new ListMetadataList(new util.ArrayList[ListMetadata]()),
        new BroadcastMetadataList(new util.ArrayList[BroadcastMetadata]()),
        0,
        new ImageMetadataList(new util.ArrayList[ImageMetadata]()),
        new SoundMetadataList(new util.ArrayList[SoundMetadata]()),
        0.1,
        0,
        true,
        0,
        0,
        1.0,
        23,
        true,
        RotationStyle.RotationStyleType.all_around.toString
      )
    )
    scriptList.setParentNode(actor)

    node
  }
}
