/*
 * Copyright (C) 2019-2021 LitterBox contributors
 *
 * This file has been adapted from LitterBox version 1.5,
 * commit bfc58911d6674c874fd69bbcb239496101e66462.
 *
 * LitterBox is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * LitterBox is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LitterBox. If not, see <http://www.gnu.org/licenses/>.
 */
package controllers.model_converters

import de.uni_passau.fim.se2.catnip.util.ASTNodeExt
import de.uni_passau.fim.se2.litterbox.ast.model.`type`.{
  BooleanType,
  NumberType,
  StringType
}
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.WithExpr
import de.uni_passau.fim.se2.litterbox.ast.model.event.*
import de.uni_passau.fim.se2.litterbox.ast.model.expression.{
  Expression,
  SingularExpression
}
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.*
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.*
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.attributes.FixedAttribute
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.*
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.{Qualified, StrId}
import de.uni_passau.fim.se2.litterbox.ast.model.literals.{
  ColorLiteral,
  NumberLiteral,
  StringLiteral
}
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NonDataBlockMetadata
import de.uni_passau.fim.se2.litterbox.ast.model.position.{MousePos, RandomPos}
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.{
  ParameterDefinition,
  ProcedureDefinition,
  ProcedureDefinitionList
}
import de.uni_passau.fim.se2.litterbox.ast.model.statement.{
  CallStmt,
  ExpressionStmt
}
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.*
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorsound.*
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.*
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.{
  IfElseStmt,
  IfThenStmt,
  RepeatForeverStmt,
  RepeatTimesStmt,
  UntilStmt
}
import de.uni_passau.fim.se2.litterbox.ast.model.statement.list.{
  AddTo,
  DeleteAllOf,
  DeleteOf,
  InsertAt,
  ReplaceItem
}
import de.uni_passau.fim.se2.litterbox.ast.model.statement.pen.*
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.*
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.*
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.{
  DeleteClone,
  StopAll,
  StopThisScript
}
import de.uni_passau.fim.se2.litterbox.ast.model.timecomp.TimeComp
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.{
  AsTouchable,
  Edge,
  MousePointer,
  SpriteTouchable
}
import de.uni_passau.fim.se2.litterbox.ast.model.variable.{
  DataExpr,
  Parameter,
  ScratchList,
  Variable
}
import de.uni_passau.fim.se2.litterbox.ast.model.*
import de.uni_passau.fim.se2.litterbox.ast.opcodes.ProcedureOpcode
import de.uni_passau.fim.se2.litterbox.ast.visitor.PrintVisitor
import de.uni_passau.fim.se2.litterbox.jsonCreation.BlockJsonCreatorHelper

import scala.jdk.CollectionConverters.*

object CustomScratchBlocksVisitor {
  val ScratchBlocksStart = "[scratchblocks]"
  val ScratchBlocksEnd   = "[/scratchblocks]"

  def apply(node: ASTNode, depth: Int = 2): String = {
    /*
    ScratchBlocksStart ++ "\n" ++ withoutBeginEnd(
      node,
      depth
    ) ++ ScratchBlocksEnd ++ "\n"*/
    withoutBeginEnd(node, depth)
  }

  private def withoutBeginEnd(node: ASTNode, depth: Int): String = {
    val v = new CustomScratchBlocksVisitor(
      node.program,
      node.actorParent,
      depth
    )

    node.accept(v)
    v.getResult
  }
}

private class CustomScratchBlocksVisitor(
    program: Option[Program],
    var actorDefinition: Option[ActorDefinition],
    depth: Int
) extends PrintVisitor(null) {
  private var inScript    = true
  private var hasContent  = false
  private var lineWrapped = true

  implicit private val buffer: StringBuilder = new StringBuilder

  private def getResult: String = buffer.toString()

  private def visitChild(node: ASTNode, d: Int = depth)(implicit
      implBuffer: StringBuilder
  ): Unit = {
    val k = d - 1
    if (k > 0) {
      val res = CustomScratchBlocksVisitor.withoutBeginEnd(node, k)
      emitNoSpace(res)(implBuffer)
    } else {
      val emit = node match {
        case _: StringExpr => "[]"
        case _: BoolExpr   => "<>"
        case _: NumExpr    => "()"
        case _: StmtList   => "...\n"
        case _             => ""
      }
      emitNoSpace(emit)(implBuffer)
    }
  }

  override def visit(node: StmtList): Unit = {
    if (depth > 0) {
      node.getChildren.forEach(child => {
        val res = CustomScratchBlocksVisitor.withoutBeginEnd(child, depth - 1)
        emitNoSpace(res)
      })
    } else {
      if (!node.getStmts.isEmpty) {
        emitNoSpace("...")
        newLine()
      }
    }
  }

  override def visit(node: ActorDefinition): Unit = {
    actorDefinition = Some(node)
    super.visit(node)
    actorDefinition = None
  }

  override def visit(node: ProcedureDefinitionList): Unit = {
    node.getList.forEach(procedureDefinition => {
      if (hasContent) {
        newLine()
      }
      visitChild(procedureDefinition)
      hasContent = true
    })
  }

  override def visit(node: ScriptList): Unit = {
    node.getScriptList.forEach(script => {
      if (hasContent) {
        newLine()
      }
      visitChild(script)
      hasContent = true
    })
  }

  override def visit(node: Script): Unit = {
    inScript = true
    super.visit(node)
    inScript = false
  }

  /** Removes parameter definitions from a procedure name.
    * @param initial
    *   the initial name with parameter definitions.
    * @param parameters
    *   of the procedure.
    * @return
    *   the actual procedure name.
    */
  private def reduceProcedureName(
      initial: String,
      parameters: List[Either[ParameterDefinition, Expression]]
  ): String = {
    var procedureName = initial

    parameters.foreach(parameter => {
      val nextIndex = procedureName.indexOf('%')
      if (nextIndex >= 0) {
        procedureName = procedureName.substring(0, nextIndex)
          + (parameter match {
            case Left(p)  => getParameterName(p)
            case Right(p) => getParameterName(p)
          })
          + procedureName.substring(nextIndex + 2)
      }
    })

    procedureName
  }

  override def visit(node: ProcedureDefinition): Unit = {
    inScript = true
    emitNoSpace("define ")
    val actorName = actorDefinition.map(_.getIdent.getName)
    val procedureNameInit = program match {
      case Some(p) =>
        p.getProcedureMapping.getProcedures
          .get(actorName)
          .get(node.getIdent)
          .getName
      case None => "unknownProcedure"
    }
    val parameters =
      node.getParameterDefinitionList.getParameterDefinitions.asScala
        .map(Left(_))
        .toList

    emitNoSpace(reduceProcedureName(procedureNameInit, parameters))
    newLine()
    visit(node.getStmtList)
    inScript = false
  }

  /*
   * Event Blocks
   */

  override def visit(never: Never): Unit = {}

  override def visit(greenFlag: GreenFlag): Unit = {
    emitNoSpace("when green flag clicked")
    newLine()
  }

  override def visit(clicked: Clicked): Unit = {
    emitNoSpace("when this sprite clicked")
    newLine()
  }

  override def visit(keyPressed: KeyPressed): Unit = {
    emitNoSpace("when [")
    keyPressed.getKey.getKey match {
      case n: NumberLiteral =>
        emitNoSpace(BlockJsonCreatorHelper.getKeyValue(n.getValue.intValue()))
        emitNoSpace(" v] key pressed")
        newLine()
      case _ => // do nothing
    }
  }

  override def visit(node: StartedAsClone): Unit = {
    emitToken("when I start as a clone")
    newLine()
  }

  override def visit(node: ReceptionOfMessage): Unit = {
    emitNoSpace("when I receive [")
    node.getMsg.getMessage match {
      case msg: StringLiteral =>
        emitNoSpace(msg.getText)
        emitNoSpace(" v]")
        newLine()
      case _ => // do nothing
    }
  }

  override def visit(node: BackdropSwitchTo): Unit = {
    emitNoSpace("when backdrop switches to [")
    visitChild(node.getBackdrop)
    emitNoSpace(" v]")
    newLine()
  }

  override def visit(node: AttributeAboveValue): Unit = {
    emitNoSpace("when [")
    visitChild(node.getAttribute)
    emitNoSpace(" v] > ")
    visitChild(node.getValue)
    newLine()
  }

  override def visit(node: Broadcast): Unit = {
    emitNoSpace("broadcast ")
    visitChild(node.getMessage)
    newLine()
  }

  override def visit(node: BroadcastAndWait): Unit = {
    emitNoSpace("broadcast ")
    visitChild(node.getMessage)
    emitNoSpace(" and wait")
    newLine()
  }

  /*
   * Control Blocks
   */

  override def visit(node: WaitSeconds): Unit = {
    emitNoSpace("wait ")
    visitChild(node.getSeconds)
    emitNoSpace(" seconds")
    newLine()
  }

  override def visit(node: WaitUntil): Unit = {
    emitNoSpace("wait until ")
    visitChild(node.getUntil)
    newLine()
  }

  override def visit(node: StopAll): Unit = {
    emitNoSpace("stop [all v]")
    newLine()
  }

  override def visit(node: StopOtherScriptsInSprite): Unit = {
    emitToken("stop [other scripts in sprite v]")
    newLine()
  }

  override def visit(node: StopThisScript): Unit = {
    emitToken("stop [this script v]")
    newLine()
  }

  override def visit(node: CreateCloneOf): Unit = {
    emitNoSpace("create clone of ")
    visitChild(node.getStringExpr)
    newLine()
  }

  override def visit(node: DeleteClone): Unit = {
    emitToken("delete this clone")
    newLine()
  }

  private def visitInnerRepeatBlocks(stmtList: StmtList): Unit = {
    newLine()
    beginIndentation()
    visitChild(stmtList, 1)
    endIndentation()
    emitNoSpace("end")
    newLine()
  }

  override def visit(node: RepeatForeverStmt): Unit = {
    emitToken("forever")
    visitInnerRepeatBlocks(node.getStmtList)
  }

  override def visit(node: UntilStmt): Unit = {
    emitNoSpace("repeat until ")
    visitChild(node.getBoolExpr, 2)
    visitInnerRepeatBlocks(node.getStmtList)
  }

  override def visit(node: RepeatTimesStmt): Unit = {
    emitNoSpace("repeat ")
    visitChild(node.getTimes, 2)
    visitInnerRepeatBlocks(node.getStmtList)
  }

  override def visit(node: IfThenStmt): Unit = {
    emitNoSpace("if ")
    visitChild(node.getBoolExpr, 2)
    emitNoSpace(" then")
    newLine()
    beginIndentation()
    visitChild(node.getThenStmts, 1)
    endIndentation()
    emitNoSpace("end")
    newLine()
  }

  override def visit(node: IfElseStmt): Unit = {
    emitNoSpace("if ")
    visitChild(node.getBoolExpr, 2)
    emitNoSpace(" then")
    newLine()
    beginIndentation()
    visitChild(node.getStmtList, 1)
    endIndentation()
    emitNoSpace("else")
    newLine()
    beginIndentation()
    visitChild(node.getElseStmts, 1)
    endIndentation()
    emitNoSpace("end")
    newLine()
  }

  /*
   * Motion Blocks
   */

  override def visit(node: MoveSteps): Unit = {
    emitNoSpace("move ")
    visitChild(node.getSteps)
    emitNoSpace(" steps")
    newLine()
  }

  override def visit(node: TurnLeft): Unit = {
    emitNoSpace("turn left ")
    visitChild(node.getDegrees)
    emitNoSpace(" degrees")
    newLine()
  }

  override def visit(node: TurnRight): Unit = {
    emitNoSpace("turn right ")
    visitChild(node.getDegrees)
    emitNoSpace(" degrees")
    newLine()
  }

  override def visit(node: GoToPos): Unit = {
    emitNoSpace("go to ")
    visitChild(node.getPosition)
    newLine()
  }

  override def visit(node: GoToPosXY): Unit = {
    emitNoSpace("go to x: ")
    visitChild(node.getX)
    emitNoSpace(" y: ")
    visitChild(node.getY)
    newLine()
  }

  override def visit(node: GlideSecsTo): Unit = {
    emitNoSpace("glide ")
    visitChild(node.getSecs)
    emitNoSpace(" secs to ")
    visitChild(node.getPosition)
    newLine()
  }

  override def visit(node: GlideSecsToXY): Unit = {
    emitNoSpace("glide ")
    visitChild(node.getSecs)
    emitNoSpace(" secs to x: ")
    visitChild(node.getX)
    emitNoSpace(" y: ")
    visitChild(node.getY)
    newLine()
  }

  override def visit(node: PointInDirection): Unit = {
    emitNoSpace("point in direction ")
    visitChild(node.getDirection)
    newLine()
  }

  override def visit(node: PointTowards): Unit = {
    emitNoSpace("point towards ")
    visitChild(node.getPosition)
    newLine()
  }

  override def visit(node: ChangeXBy): Unit = {
    emitNoSpace("change x by ")
    visitChild(node.getNum)
    newLine()
  }

  override def visit(node: SetXTo): Unit = {
    emitNoSpace("set x to ")
    visitChild(node.getNum)
    newLine()
  }

  override def visit(node: ChangeYBy): Unit = {
    emitNoSpace("change y by ")
    visitChild(node.getNum)
    newLine()
  }

  override def visit(node: SetYTo): Unit = {
    emitNoSpace("set y to ")
    visitChild(node.getNum)
    newLine()
  }

  override def visit(node: IfOnEdgeBounce): Unit = {
    emitNoSpace("if on edge, bounce")
    newLine()
  }

  override def visit(node: SetRotationStyle): Unit = {
    emitNoSpace("set rotation style [")
    visitChild(node.getRotation)
    emitNoSpace(" v]")
    newLine()
  }

  /*
   * Looks Blocks
   */

  override def visit(node: SayForSecs): Unit = {
    emitNoSpace("say ")
    visitChild(node.getString)
    emitNoSpace(" for ")
    visitChild(node.getSecs)
    emitNoSpace(" seconds")
    newLine()
  }

  override def visit(node: Say): Unit = {
    emitNoSpace("say ")
    visitChild(node.getString)
    newLine()
  }

  override def visit(node: ThinkForSecs): Unit = {
    emitNoSpace("think ")
    visitChild(node.getThought)
    emitNoSpace(" for ")
    visitChild(node.getSecs)
    emitNoSpace(" seconds")
    newLine()
  }

  override def visit(node: Think): Unit = {
    emitNoSpace("think ")
    visitChild(node.getThought)
    newLine()
  }

  override def visit(node: SwitchCostumeTo): Unit = {
    emitNoSpace("switch costume to ")
    visitChild(node.getCostumeChoice)
    newLine()
  }

  override def visit(node: NextCostume): Unit = {
    emitNoSpace("next costume")
    newLine()
  }

  override def visit(node: SwitchBackdrop): Unit = {
    import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.*

    emitNoSpace("switch backdrop to ")
    node.getElementChoice match {
      case _: Next   => emitNoSpace("(next backdrop v)")
      case _: Prev   => emitNoSpace("(previous backdrop v)")
      case _: Random => emitNoSpace("(random backdrop v)")
      case _         => visitChild(node.getElementChoice)
    }
    newLine()
  }

  override def visit(node: NextBackdrop): Unit = {
    emitNoSpace("next backdrop")
    newLine()
  }

  override def visit(node: ChangeSizeBy): Unit = {
    emitNoSpace("change size by ")
    visitChild(node.getNum)
    newLine()
  }

  override def visit(node: SetSizeTo): Unit = {
    emitNoSpace("set size to ")
    visitChild(node.getPercent)
    emitNoSpace(" %")
    newLine()
  }

  override def visit(node: ChangeGraphicEffectBy): Unit = {
    emitNoSpace("change [")
    visitChild(node.getEffect)
    emitNoSpace(" v] effect by ")
    visitChild(node.getValue)
    newLine()
  }

  override def visit(node: SetGraphicEffectTo): Unit = {
    emitNoSpace("set [")
    visitChild(node.getEffect)
    emitNoSpace(" v] effect to ")
    visitChild(node.getValue)
    newLine()
  }

  override def visit(node: ClearGraphicEffects): Unit = {
    emitNoSpace("clear graphic effects")
    newLine()
  }

  override def visit(node: Show): Unit = {
    emitNoSpace("show")
    newLine()
  }

  override def visit(node: Hide): Unit = {
    emitNoSpace("hide")
    newLine()
  }

  override def visit(node: GoToLayer): Unit = {
    emitNoSpace("go to [")
    visitChild(node.getLayerChoice)
    emitNoSpace(" v] layer")
    newLine()
  }

  override def visit(node: ChangeLayerBy): Unit = {
    emitNoSpace("go [")
    visitChild(node.getForwardBackwardChoice)
    emitNoSpace(" v] ")
    visitChild(node.getNum)
    emitNoSpace(" layers")
    newLine()
  }

  /*
   * Sound Blocks
   */

  override def visit(node: PlaySoundUntilDone): Unit = {
    emitNoSpace("play sound ")
    visitChild(node.getElementChoice)
    emitNoSpace(" until done")
    newLine()
  }

  override def visit(node: StartSound): Unit = {
    emitNoSpace("start sound ")
    visitChild(node.getElementChoice)
    newLine()
  }

  override def visit(node: StopAllSounds): Unit = {
    emitNoSpace("stop all sounds")
    newLine()
  }

  override def visit(node: ChangeSoundEffectBy): Unit = {
    emitNoSpace("change [")
    visitChild(node.getEffect)
    emitNoSpace(" v] effect by ")
    visitChild(node.getValue)
    newLine()
  }

  override def visit(node: SetSoundEffectTo): Unit = {
    emitNoSpace("set [")
    visitChild(node.getEffect)
    emitNoSpace(" v] effect to ")
    visitChild(node.getValue)
    newLine()
  }

  override def visit(node: ClearSoundEffects): Unit = {
    emitNoSpace("clear sound effects")
    newLine()
  }

  override def visit(node: ChangeVolumeBy): Unit = {
    emitNoSpace("change volume by ")
    visitChild(node.getVolumeValue)
    newLine()
  }

  override def visit(node: SetVolumeTo): Unit = {
    emitNoSpace("set volume to ")
    visitChild(node.getVolumeValue)
    emitNoSpace(" %")
    newLine()
  }

  /*
   * Sensing Blocks
   */

  override def visit(node: AskAndWait): Unit = {
    emitNoSpace("ask ")
    visitChild(node.getQuestion)
    emitNoSpace(" and wait")
    newLine()
  }

  override def visit(node: SetDragMode): Unit = {
    emitNoSpace("set drag mode [")
    visitChild(node.getDrag)
    emitNoSpace(" v]")
    newLine()
  }

  override def visit(node: ResetTimer): Unit = {
    emitNoSpace("reset timer")
    newLine()
  }

  /*
   * Pen Blocks
   */

  override def visit(node: PenClearStmt): Unit = {
    emitNoSpace("erase all")
    newLine()
  }

  override def visit(node: PenStampStmt): Unit = {
    emitNoSpace("stamp")
    newLine()
  }

  override def visit(node: PenDownStmt): Unit = {
    emitNoSpace("pen down")
    newLine()
  }

  override def visit(node: PenUpStmt): Unit = {
    emitNoSpace("pen up")
    newLine()
  }

  override def visit(node: SetPenColorToColorStmt): Unit = {
    emitNoSpace("set pen color to ")
    visitChild(node.getColorExpr, Int.MaxValue)
    newLine()
  }

  private def visitPenParam(param: StringExpr): Unit = {
    param match {
      case s: StringLiteral => emitNoSpace(s"(${s.getText} v)")
      case p                => visitChild(p)
    }
  }

  override def visit(node: ChangePenColorParamBy): Unit = {
    emitNoSpace("change pen ")
    visitPenParam(node.getParam)
    emitNoSpace(" by ")
    visitChild(node.getValue)
    newLine()
  }

  override def visit(node: SetPenColorParamTo): Unit = {
    emitNoSpace("set pen ")
    visitPenParam(node.getParam)
    emitNoSpace(" to ")
    visitChild(node.getValue)
    newLine()
  }

  override def visit(node: ChangePenSizeBy): Unit = {
    emitNoSpace("change pen size by ")
    visitChild(node.getValue)
    newLine()
  }

  override def visit(node: SetPenSizeTo): Unit = {
    emitNoSpace("set pen size to ")
    visitChild(node.getValue)
    newLine()
  }

  /*
   * Variables Blocks
   */

  override def visit(node: SetVariableTo): Unit = {
    if (inScript) {
      emitNoSpace("set [")
      visitChild(node.getIdentifier, Int.MaxValue)
      emitNoSpace(" v] to ")
      if (isQualified(node.getExpr)) {
        emitNoSpace("(")
        visitChild(node.getExpr, Int.MaxValue)
        emitNoSpace(")")
      } else {
        visitChild(node.getExpr)
      }
      newLine()
    }
  }

  override def visit(node: ChangeVariableBy): Unit = {
    emitNoSpace("change [")
    visitChild(node.getIdentifier, Int.MaxValue)
    emitNoSpace(" v] by ")

    def isOp1Qualified(expr: AsNumber): Boolean = isQualified(expr.getOperand1)

    node.getExpr match {
      case expr: AsNumber if isOp1Qualified(expr) =>
        visitChild(expr.getOperand1, Int.MaxValue)
      case expr => visitChild(expr)
    }
    newLine()
  }

  override def visit(node: ShowVariable): Unit = {
    emitNoSpace("show variable [")
    visitChild(node.getIdentifier, Int.MaxValue)
    emitNoSpace(" v")
    emitNoSpace("]")
    newLine()
  }

  override def visit(node: HideVariable): Unit = {
    emitNoSpace("hide variable [")
    visitChild(node.getIdentifier, Int.MaxValue)
    emitNoSpace(" v")
    emitNoSpace("]")
    newLine()
  }

  override def visit(node: AddTo): Unit = {
    emitNoSpace("add ")
    visitChild(node.getString)
    emitNoSpace(" to [")
    visitChild(node.getIdentifier, Int.MaxValue)
    emitNoSpace(" v")
    emitNoSpace("]")
    newLine()
  }

  override def visit(node: DeleteOf): Unit = {
    emitNoSpace("delete ")
    visitChild(node.getNum)
    emitNoSpace(" of [")
    visitChild(node.getIdentifier, Int.MaxValue)
    emitNoSpace(" v")
    emitNoSpace("]")
    newLine()
  }

  override def visit(node: DeleteAllOf): Unit = {
    emitNoSpace("delete all of [")
    visitChild(node.getIdentifier, Int.MaxValue)
    emitNoSpace(" v")
    emitNoSpace("]")
    newLine()
  }

  override def visit(node: InsertAt): Unit = {
    emitNoSpace("insert ")
    visitChild(node.getString)
    emitNoSpace(" at ")
    visitChild(node.getIndex)
    emitNoSpace(" of [")
    visitChild(node.getIdentifier, Int.MaxValue)
    emitNoSpace(" v")
    emitNoSpace("]")
    newLine()
  }

  override def visit(node: ReplaceItem): Unit = {
    emitNoSpace("replace item ")
    visitChild(node.getIndex)
    emitNoSpace(" of [")
    visitChild(node.getIdentifier, Int.MaxValue)
    emitNoSpace(" v] with ")
    visitChild(node.getString)
    newLine()
  }

  override def visit(node: ShowList): Unit = {
    emitNoSpace("show list [")
    visitChild(node.getIdentifier, Int.MaxValue)
    emitNoSpace(" v")
    emitNoSpace("]")
    newLine()
  }

  override def visit(node: HideList): Unit = {
    emitNoSpace("hide list [")
    visitChild(node.getIdentifier, Int.MaxValue)
    emitNoSpace(" v")
    emitNoSpace("]")
    newLine()
  }

  override def visit(node: ItemOfVariable): Unit = {
    emitNoSpace("(item ")
    visitChild(node.getNum)
    emitNoSpace(" of [")
    visitChild(node.getIdentifier, Int.MaxValue)
    emitNoSpace(" v]")
    emitNoSpace(")")
  }

  override def visit(node: IndexOf): Unit = {
    emitNoSpace("(item # of ")
    visitChild(node.getExpr)
    emitNoSpace(" in [")
    visitChild(node.getIdentifier, Int.MaxValue)
    emitNoSpace(" v]")
    emitNoSpace(")")
  }

  override def visit(node: LengthOfVar): Unit = {
    emitNoSpace("(length of [")
    visitChild(node.getIdentifier, Int.MaxValue)
    emitNoSpace(" v]")
    emitNoSpace(")")
  }

  override def visit(node: ListContains): Unit = {
    emitNoSpace("<[")
    visitChild(node.getIdentifier, Int.MaxValue)
    emitNoSpace(" v] contains ")
    visitChild(node.getElement)
    emitNoSpace(" ?")
    emitNoSpace(">")
  }

  override def visit(node: NumberLiteral): Unit = {
    emitNoSpace("(")
    if (node.getValue.isValidInt) {
      emitNoSpace(s"${node.getValue.intValue}")
    } else {
      emitNoSpace(s"${node.getValue}")
    }
    emitNoSpace(")")
  }

  override def visit(node: AttributeOf): Unit = {
    emitNoSpace("([")
    visitChild(node.getAttribute)
    emitNoSpace(" v] of ")
    visitChild(node.getElementChoice)
    emitNoSpace("?)")
  }

  override def visit(node: WithExpr): Unit = {
    node.getExpression match {
      case s: StrId =>
        emitNoSpace("(")
        if ("_stage_" == s.getName) {
          emitNoSpace("Stage")
        } else {
          visitChild(s, Int.MaxValue)
        }
        emitNoSpace(" v)")
      case q: Qualified =>
        emitNoSpace("(")
        visitChild(q, Int.MaxValue)
        emitNoSpace(")")
      case expr => visitChild(expr)
    }
  }

  override def visit(node: FixedAttribute): Unit = {
    emitNoSpace(node.getTypeName)
  }

  override def visit(node: Volume): Unit = {
    emitNoSpace("(volume")
    emitNoSpace(")")
  }

  override def visit(node: Timer): Unit = {
    emitNoSpace("(timer")
    emitNoSpace(")")
  }

  override def visit(node: Answer): Unit = {
    emitNoSpace("(answer")
    emitNoSpace(")")
  }

  override def visit(node: MousePos): Unit = {
    emitNoSpace("(mouse-pointer v)")
  }

  override def visit(node: RandomPos): Unit = {
    emitNoSpace("(random position v)")
  }

  override def visit(node: RotationStyle): Unit = {
    emitNoSpace(node.getTypeName)
  }

  override def visit(node: ExpressionStmt): Unit = {
    val dataExpr: Option[DataExpr] = node.getExpression match {
      case q: Qualified => Some(q.getSecond)
      case _            => None
    }

    dataExpr match {
      case Some(_: Variable) | Some(_: ScratchList) => emitNoSpace("(")
      case _                                        => // do nothing
    }
    visitChild(node.getExpression)
    dataExpr match {
      case Some(_: Variable)    => emitNoSpace(")")
      case Some(_: ScratchList) => emitNoSpace(" :: list)")
      case _                    => // do nothing
    }
  }

  override def visit(node: ScratchList): Unit = {
    if (inScript) {
      visitChild(node.getName)
    }
  }

  override def visit(node: Variable): Unit = {
    if (inScript) {
      visitChild(node.getName, Int.MaxValue)
    }
  }

  override def visit(node: Backdrop): Unit = {
    emitNoSpace("(backdrop [")
    visitChild(node.getType)
    emitNoSpace(" v])")
  }

  override def visit(node: NameNum): Unit = {
    emitNoSpace(node.getTypeName)
  }

  override def visit(stringLiteral: StringLiteral): Unit = {
    if (inScript) {
      emitNoSpace("[")
      emitNoSpace(stringLiteral.getText)
      emitNoSpace("]")
    }
  }

  override def visit(colorLiteral: ColorLiteral): Unit = {
    emitNoSpace("[#")
    emitNoSpace(String.format("%02x", colorLiteral.getRed))
    emitNoSpace(String.format("%02x", colorLiteral.getGreen))
    emitNoSpace(String.format("%02x", colorLiteral.getBlue))
    emitNoSpace("]")
  }

  override def visit(node: GraphicEffect): Unit = {
    emitNoSpace(node.getTypeName)
  }

  override def visit(node: SoundEffect): Unit = {
    emitNoSpace(node.getTypeName)
  }

  override def visit(node: LayerChoice): Unit = {
    emitNoSpace(node.getTypeName)
  }

  override def visit(node: DragMode): Unit = {
    emitNoSpace(node.getTypeName)
  }

  override def visit(node: ForwardBackwardChoice): Unit = {
    emitNoSpace(node.getTypeName)
  }

  override def visit(node: Message): Unit = {
    node.getMessage match {
      case msg: StringLiteral => emitNoSpace(s"(${msg.getText} v)")
      case msg                => visitChild(msg)
    }
  }

  override def visit(node: EventAttribute): Unit = {
    emitNoSpace(node.getTypeName)
  }

  override def visit(node: Qualified): Unit = {
    visitChild(node.getSecond)
  }

  override def visit(node: PositionX): Unit = {
    emitNoSpace("(x position)")
  }

  override def visit(node: PositionY): Unit = {
    emitNoSpace("(y position)")
  }

  override def visit(node: Direction): Unit = {
    emitNoSpace("(direction)")
  }

  override def visit(node: Size): Unit = {
    emitNoSpace("(size)")
  }

  override def visit(node: MouseX): Unit = {
    emitNoSpace("(mouse x)")
  }

  override def visit(node: MouseY): Unit = {
    emitNoSpace("(mouse y)")
  }

  override def visit(node: DaysSince2000): Unit = {
    emitNoSpace("(days since 2000)")
  }

  override def visit(node: Username): Unit = {
    emitNoSpace("(username)")
  }

  override def visit(node: Loudness): Unit = {
    emitNoSpace("(loudness)")
  }

  override def visit(node: DistanceTo): Unit = {
    emitNoSpace("(distance to ")
    visitChild(node.getPosition)
    emitNoSpace(")")
  }

  override def visit(node: Current): Unit = {
    emitNoSpace("(current (")
    visitChild(node.getTimeComp)
    emitNoSpace(" v))")
  }

  override def visit(node: TimeComp): Unit = {
    emitNoSpace(node.getTypeName)
  }

  override def visit(node: Costume): Unit = {
    emitNoSpace("(costume [")
    visitChild(node.getType)
    emitNoSpace(" v])")
  }

  private def visitAsOtherExpr(expr: Expression): Unit = {
    if (isQualified(expr)) {
      emitNoSpace("(")
      visitChild(expr, Int.MaxValue)
      emitNoSpace(")")
    } else {
      visitChild(expr)
    }
  }

  override def visit(node: AsNumber): Unit = {
    visitAsOtherExpr(node.getOperand1)
  }

  override def visit(node: AsTouchable): Unit = {
    visitAsOtherExpr(node.getOperand1)
  }

  override def visit(node: AsBool): Unit = {
    visitAsOtherExpr(node.getOperand1)
  }

  override def visit(node: AsString): Unit = {
    node.getOperand1 match {
      case _: SingularExpression | _: BoolExpr | _: NumExpr | _: Parameter =>
        visitChild(node.getOperand1)
      case _: Qualified =>
        emitNoSpace("(")
        visitChild(node.getOperand1, Int.MaxValue)
        emitNoSpace(")")
      case op: StrId =>
        emitNoSpace("(")
        if ("_myself_" == op.getName) {
          emitNoSpace("myself")
        } else {
          emitNoSpace(op.getName)
        }
        emitNoSpace(" v)")
      case op =>
        emitNoSpace("(")
        visitChild(op)
        emitNoSpace(")")
    }
  }

  override def visit(node: Add): Unit = {
    emitNoSpace("(")
    visitChild(node.getOperand1)
    emitNoSpace("+")
    visitChild(node.getOperand2)
    emitNoSpace(")")
  }

  override def visit(node: Minus): Unit = {
    emitNoSpace("(")
    visitChild(node.getOperand1)
    emitNoSpace("-")
    visitChild(node.getOperand2)
    emitNoSpace(")")
  }

  override def visit(node: Mult): Unit = {
    emitNoSpace("(")
    visitChild(node.getOperand1)
    emitNoSpace("*")
    visitChild(node.getOperand2)
    emitNoSpace(")")
  }

  override def visit(node: Div): Unit = {
    emitNoSpace("(")
    visitChild(node.getOperand1)
    emitNoSpace("/")
    visitChild(node.getOperand2)
    emitNoSpace(")")
  }

  override def visit(node: Round): Unit = {
    emitNoSpace("(round ")
    visitChild(node.getOperand1)
    emitNoSpace(")")
  }

  override def visit(node: PickRandom): Unit = {
    emitNoSpace("(pick random ")
    visitChild(node.getOperand1)
    emitNoSpace(" to ")
    visitChild(node.getOperand2)
    emitNoSpace(")")
  }

  override def visit(node: Mod): Unit = {
    emitNoSpace("(")
    visitChild(node.getOperand1)
    emitNoSpace(" mod ")
    visitChild(node.getOperand2)
    emitNoSpace(")")
  }

  override def visit(node: NumFunctOf): Unit = {
    emitNoSpace("([")
    visitChild(node.getOperand1)
    emitNoSpace(" v] of ")
    visitChild(node.getOperand2)
    emitNoSpace(")")
  }

  override def visit(node: NumFunct): Unit = {
    emitNoSpace(node.getTypeName)
  }

  override def visit(node: BiggerThan): Unit = {
    emitNoSpace("<")
    visitChild(node.getOperand1)
    emitNoSpace(" > ")
    visitChild(node.getOperand2)
    emitNoSpace(">")
  }

  override def visit(node: LessThan): Unit = {
    emitNoSpace("<")
    visitChild(node.getOperand1)
    emitNoSpace(" < ")
    visitChild(node.getOperand2)
    emitNoSpace(">")
  }

  override def visit(node: Equals): Unit = {
    emitNoSpace("<")
    visitChild(node.getOperand1)
    emitNoSpace(" = ")
    visitChild(node.getOperand2)
    emitNoSpace(">")
  }

  override def visit(node: Not): Unit = {
    emitNoSpace("<not ")
    visitChild(node.getOperand1)
    emitNoSpace(">")
  }

  override def visit(node: And): Unit = {
    emitNoSpace("<")
    visitChild(node.getOperand1)
    emitNoSpace(" and ")
    visitChild(node.getOperand2)
    emitNoSpace(">")
  }

  override def visit(node: Or): Unit = {
    emitNoSpace("<")
    visitChild(node.getOperand1)
    emitNoSpace(" or ")
    visitChild(node.getOperand2)
    emitNoSpace(">")
  }

  override def visit(node: StringContains): Unit = {
    emitNoSpace("<")
    visitChild(node.getContaining)
    emitNoSpace(" contains ")
    visitChild(node.getContained)
    emitNoSpace("?>")
  }

  override def visit(node: Touching): Unit = {
    emitNoSpace("<touching ")
    visitChild(node.getTouchable, Int.MaxValue)
    emitNoSpace(" ?>")
  }

  override def visit(node: Edge): Unit = {
    emitNoSpace("(edge v)")
  }

  override def visit(node: MousePointer): Unit = {
    emitNoSpace("(mouse-pointer v)")
  }

  override def visit(node: SpriteTouchingColor): Unit = {
    emitNoSpace("<touching color ")
    visitChild(node.getColor, Int.MaxValue)
    emitNoSpace(" ?>")
  }

  override def visit(node: SpriteTouchable): Unit = {
    emitNoSpace("(")
    node.getStringExpr match {
      case l: StringLiteral => emitNoSpace(l.getText)
      case _                => // do nothing
    }
    emitNoSpace(" v)")
  }

  override def visit(node: ColorTouchingColor): Unit = {
    emitNoSpace("<color ")
    visitChild(node.getOperand1, Int.MaxValue)
    emitNoSpace(" is touching ")
    visitChild(node.getOperand2, Int.MaxValue)
    emitNoSpace(" ?>")
  }

  override def visit(node: IsKeyPressed): Unit = {
    emitNoSpace("<key ")
    visitChild(node.getKey, Int.MaxValue)
    emitNoSpace(" pressed?")
    emitNoSpace(">")
  }

  override def visit(node: Key): Unit = {
    node.getKey match {
      case n: NumberLiteral =>
        emitNoSpace("(")
        emitNoSpace(BlockJsonCreatorHelper.getKeyValue(n.getValue.intValue))
        emitNoSpace(" v)")
      case key => visitChild(key)
    }
  }

  override def visit(node: IsMouseDown): Unit = {
    emitNoSpace("<mouse down?")
    emitNoSpace(">")
  }

  override def visit(node: UnspecifiedBoolExpr): Unit = {
    emitNoSpace("<>")
  }

  override def visit(node: UnspecifiedStringExpr): Unit = {
    emitNoSpace("[]")
  }

  override def visit(node: UnspecifiedNumExpr): Unit = {
    emitNoSpace("()")
  }

  override def visit(node: Join): Unit = {
    emitNoSpace("(join ")
    visitChild(node.getOperand1)
    visitChild(node.getOperand2)
    emitNoSpace(")")
  }

  override def visit(node: LetterOf): Unit = {
    emitNoSpace("(letter ")
    visitChild(node.getNum)
    emitNoSpace(" of ")
    visitChild(node.getStringExpr)
    emitNoSpace(")")
  }

  override def visit(node: LengthOfString): Unit = {
    emitNoSpace("(length of ")
    visitChild(node.getStringExpr)
    emitNoSpace(")")
  }

  override def visit(node: Parameter): Unit = {
    node.getMetadata match {
      case metadata: NonDataBlockMetadata =>
        val isBool =
          metadata.getOpcode == ProcedureOpcode.argument_reporter_boolean.name()

        if (isBool) { emitNoSpace("<") }
        else { emitNoSpace("(") }
        visitChildren(node)
        if (isBool) { emitNoSpace(">") }
        else { emitNoSpace(")") }

      case _ => // do nothing
    }
  }

  override def visit(node: StrId): Unit = {
    if (inScript) {
      emitNoSpace(node.getName)
    }
  }

  override def visit(node: CallStmt): Unit = {
    val procedureNameInit = node.getIdent.getName
    val parameters =
      node.getExpressions.getExpressions.asScala.map(Right(_)).toList

    emitNoSpace(reduceProcedureName(procedureNameInit, parameters))
    newLine()
  }

  /*
   * Helper Functions
   */

  private def isQualified(n: ASTNode): Boolean = {
    n match {
      case _: Qualified => true
      case _            => false
    }
  }

  private def newLine()(implicit implBuffer: StringBuilder): Unit = {
    emitNoSpace(System.lineSeparator())(implBuffer)
    lineWrapped = true
  }

  private def emitNoSpace(
      s: String
  )(implicit implBuffer: StringBuilder): Unit = {
    implBuffer.append(s)
    lineWrapped = false
  }

  private def emitToken(s: String)(implicit implBuffer: StringBuilder): Unit = {
    emitNoSpace(s)(implBuffer)
    emitNoSpace(" ")(implBuffer)
  }

  private def getParameterName(node: ParameterDefinition) = {
    val buf = new StringBuilder

    node.getType match {
      case _: BooleanType =>
        emitNoSpace("<")(buf)
        visitChild(node.getIdent)(buf)
        emitNoSpace(">")(buf)
      case _: NumberType =>
        emitNoSpace("(")(buf)
        visitChild(node.getIdent)(buf)
        emitNoSpace(")")(buf)
      case _: StringType =>
        emitNoSpace("[")(buf)
        visitChild(node.getIdent)(buf)
        emitNoSpace("]")(buf)
      case _ => // no other types exist, Java code cannot be checked exhaustively
    }

    buf.toString()
  }

  private def getParameterName(node: Expression) = {
    val buf = new StringBuilder
    visitChild(node)(buf)
    buf.toString()
  }
}
