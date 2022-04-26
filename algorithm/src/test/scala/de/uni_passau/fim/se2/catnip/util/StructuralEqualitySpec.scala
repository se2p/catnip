package de.uni_passau.fim.se2.catnip.util

import de.uni_passau.fim.se2.catnip.common.defs.UnitSpec
import de.uni_passau.fim.se2.litterbox.ast.model.event.EventAttribute
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.{
  Loudness,
  NumFunct
}
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.NameNum
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.attributes.FixedAttribute
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId
import de.uni_passau.fim.se2.litterbox.ast.model.literals.{
  BoolLiteral,
  ColorLiteral,
  NumberLiteral,
  StringLiteral
}
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.GraphicEffect
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorsound.SoundEffect
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.SetVariableTo
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.{
  ForwardBackwardChoice,
  LayerChoice
}
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.{
  DragMode,
  RotationStyle
}
import de.uni_passau.fim.se2.litterbox.ast.model.timecomp.TimeComp
import de.uni_passau.fim.se2.litterbox.ast.model.{ActorType, ASTNode, StmtList}

import scala.jdk.CollectionConverters.*

class StructuralEqualitySpec extends UnitSpec {

  "The structural equality check on leaves" should
    "not consider nodes of different types equal" in {
      val n1 = new NumberLiteral(12.0)
      val n2 = new BoolLiteral(true)
      n1.structurallyEqual(n2) should be(false)

      val n3 = NodeGen.generateNumberVariable("x")
      n1.structurallyEqual(n3) should be(false)
    }

  it should
    "consider two boolean literals as equal if their value is equal" in {
      val n1 = new BoolLiteral(true)
      val n2 = new BoolLiteral(true)
      val n3 = new BoolLiteral(false)

      n1.structurallyEqual(n2) should be(true)
      n1.structurallyEqual(n3) should be(false)
    }

  it should
    "consider two number literals as equal if their values are equal" in {
      val n1 = new NumberLiteral(10.0)
      val n2 = new NumberLiteral(10.0)
      val n3 = new NumberLiteral(5.0)

      n1.structurallyEqual(n2) should be(true)
      n1.structurallyEqual(n3) should be(false)
    }

  it should "consider two string literals as equal if their text is equal" in {
    val n1 = new StringLiteral("some")
    val n2 = new StringLiteral("some")
    val n3 = new StringLiteral("else")

    n1.structurallyEqual(n2) should be(true)
    n1.structurallyEqual(n3) should be(false)
  }

  it should
    "consider two colour literals as equal if all three parts of RGB are equal" in {
      val n1 = new ColorLiteral(1, 1, 1)
      val n2 = new ColorLiteral(1, 1, 1)
      val n3 = new ColorLiteral(1, 1, 0)
      val n4 = new ColorLiteral(1, 0, 1)
      val n5 = new ColorLiteral(0, 1, 1)

      n1.structurallyEqual(n2) should be(true)
      n1.structurallyEqual(n3) should be(false)
      n1.structurallyEqual(n4) should be(false)
      n1.structurallyEqual(n5) should be(false)
      n3.structurallyEqual(n4) should be(false)
      n3.structurallyEqual(n5) should be(false)
      n4.structurallyEqual(n5) should be(false)
    }

  it should
    "consider types with no special equal method equal if they have the same class" in {
      val n1 = new Loudness(NodeGen.generateNonDataBlockMetadata("Loudness"))
      val n2 = new Loudness(NodeGen.generateNonDataBlockMetadata("Loudness"))

      n1.structurallyEqual(n2)
    }

  it should
    "consider the types that have a proper equal method equal according to that" in {
      def checkEq(n1: ASTNode, n2: ASTNode, n3: ASTNode): Unit = {
        withClue((n1, n2)) {
          n1.structurallyEqual(n2) should be(n1 == n2)
        }
        withClue((n2, n3)) {
          n2.structurallyEqual(n3) should be(n2 == n3)
        }
      }

      {
        val n1 = ActorType.getStage
        val n2 = ActorType.getStage
        val n3 = ActorType.getSprite
        checkEq(n1, n2, n3)
      }

      {
        val n1 =
          new EventAttribute(EventAttribute.EventAttributeType.TIMER.getType)
        val n2 =
          new EventAttribute(EventAttribute.EventAttributeType.TIMER.getType)
        val n3 =
          new EventAttribute(EventAttribute.EventAttributeType.LOUDNESS.getType)
        checkEq(n1, n2, n3)
      }

      {
        val n1 = new NumFunct(NumFunct.NumFunctType.SIN.getFunction)
        val n2 = new NumFunct(NumFunct.NumFunctType.SIN.getFunction)
        val n3 = new NumFunct(NumFunct.NumFunctType.ABS.getFunction)
        checkEq(n1, n2, n3)
      }

      {
        val n1 = new NameNum(NameNum.NameNumType.NAME.getType)
        val n2 = new NameNum(NameNum.NameNumType.NAME.getType)
        val n3 = new NameNum(NameNum.NameNumType.NUMBER.getType)
        checkEq(n1, n2, n3)
      }

      {
        val n1 =
          new FixedAttribute(FixedAttribute.FixedAttributeType.SIZE.getType)
        val n2 =
          new FixedAttribute(FixedAttribute.FixedAttributeType.SIZE.getType)
        val n3 =
          new FixedAttribute(FixedAttribute.FixedAttributeType.VOLUME.getType)
        checkEq(n1, n2, n3)
      }

      {
        val n1 =
          new GraphicEffect(GraphicEffect.GraphicEffectType.GHOST.getToken)
        val n2 =
          new GraphicEffect(GraphicEffect.GraphicEffectType.GHOST.getToken)
        val n3 =
          new GraphicEffect(GraphicEffect.GraphicEffectType.WHIRL.getToken)
        checkEq(n1, n2, n3)
      }

      {
        val n1 = new SoundEffect(SoundEffect.SoundEffectType.PAN.getToken)
        val n2 = new SoundEffect(SoundEffect.SoundEffectType.PAN.getToken)
        val n3 = new SoundEffect(SoundEffect.SoundEffectType.PITCH.getToken)
        checkEq(n1, n2, n3)
      }

      {
        val n1 = new ForwardBackwardChoice(
          ForwardBackwardChoice.ForwardBackwardChoiceType.FORWARD.getType
        )
        val n2 = new ForwardBackwardChoice(
          ForwardBackwardChoice.ForwardBackwardChoiceType.FORWARD.getType
        )
        val n3 = new ForwardBackwardChoice(
          ForwardBackwardChoice.ForwardBackwardChoiceType.BACKWARD.getType
        )
        checkEq(n1, n2, n3)
      }

      {
        val n1 = new LayerChoice(LayerChoice.LayerChoiceType.BACK.getType)
        val n2 = new LayerChoice(LayerChoice.LayerChoiceType.BACK.getType)
        val n3 = new LayerChoice(LayerChoice.LayerChoiceType.FRONT.getType)
        checkEq(n1, n2, n3)
      }

      {
        val n1 = new DragMode(DragMode.DragModeType.draggable.getToken)
        val n2 = new DragMode(DragMode.DragModeType.draggable.getToken)
        val n3 = new DragMode(DragMode.DragModeType.not_draggable.getToken)
        checkEq(n1, n2, n3)
      }

      {
        val n1 = new RotationStyle(
          RotationStyle.RotationStyleType.dont_rotate.getToken
        )
        val n2 = new RotationStyle(
          RotationStyle.RotationStyleType.dont_rotate.getToken
        )
        val n3 =
          new RotationStyle(RotationStyle.RotationStyleType.left_right.getToken)
        checkEq(n1, n2, n3)
      }

      {
        val n1 = new TimeComp(TimeComp.TimeCompType.DATE.getLabel)
        val n2 = new TimeComp(TimeComp.TimeCompType.DATE.getLabel)
        val n3 = new TimeComp(TimeComp.TimeCompType.SECOND.getLabel)
        checkEq(n1, n2, n3)
      }
    }

  it should
    "not consider two nodes equal if they have different amount of children" in {
      val s1 = new SetVariableTo(
        new StrId("x"),
        new NumberLiteral(1.0),
        NodeGen.generateNonDataBlockMetadata("set")
      )
      val n1 = new StmtList(List[Stmt](s1, s1, s1).asJava)
      val n2 = new StmtList(List[Stmt](s1, s1).asJava)

      n1.structurallyEqual(n2) should be(false)
    }

  it should "consider two nodes equal if they have the same children" in {
    val s1 = new SetVariableTo(
      new StrId("x"),
      new NumberLiteral(1.0),
      NodeGen.generateNonDataBlockMetadata("set")
    )
    val n1 = new StmtList(List[Stmt](s1, s1, s1).asJava)
    val n2 = new StmtList(List[Stmt](s1, s1, s1).asJava)

    n1.structurallyEqual(n2) should be(true)
  }
}
