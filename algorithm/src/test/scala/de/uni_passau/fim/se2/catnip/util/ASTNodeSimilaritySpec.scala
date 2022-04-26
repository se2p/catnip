package de.uni_passau.fim.se2.catnip.util

import de.uni_passau.fim.se2.catnip.common.defs.UnitSpec
import de.uni_passau.fim.se2.catnip.model.BlockId
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.Next
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.{
  And,
  BiggerThan,
  LessThan,
  Or
}
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.{
  Add,
  Div,
  LengthOfString,
  LengthOfVar,
  Minus,
  Mod,
  MouseX,
  MouseY,
  Mult,
  PositionX,
  PositionY
}
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.Answer
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId
import de.uni_passau.fim.se2.litterbox.ast.model.literals.{
  BoolLiteral,
  NumberLiteral,
  StringLiteral
}
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NonDataBlockMetadata
import de.uni_passau.fim.se2.litterbox.ast.model.position.RandomPos
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.{
  SwitchBackdrop,
  SwitchBackdropAndWait
}
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.{
  ChangeVariableBy,
  SetVariableTo
}
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.{
  IfElseStmt,
  IfThenStmt,
  RepeatForeverStmt,
  RepeatTimesStmt,
  UntilStmt
}
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.{
  Say,
  SayForSecs,
  Think,
  ThinkForSecs
}
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.{
  ChangeXBy,
  ChangeYBy,
  GlideSecsTo,
  GlideSecsToXY,
  GoToPos,
  GoToPosXY,
  SetXTo,
  SetYTo,
  TurnLeft,
  TurnRight
}

import scala.jdk.CollectionConverters.*

class ASTNodeSimilaritySpec extends UnitSpec {
  "The ASTNode Similarity" should "mark nodes of the same type as similar and very similar" in {
    val m1 = NodeGen.generateNonDataBlockMetadata()
    val m2 = NodeGen.generateNonDataBlockMetadata(opcode = "sub")

    m1.similar(m2) should be(true)
    m1.verySimilar(m2) should be(true)
  }

  it should
    "mark nodes of completely different types as not (very) similar" in {
      val n1 = NodeGen.generateNonDataBlockMetadata()
      val n2 = new NumberLiteral(12.0)

      n1.similar(n2) should be(false)
      n1.verySimilar(n2) should be(false)
    }

  it should "mark variables with same starting letter as very similar" in {
    val n1 = new StrId("n123")
    val n2 = new StrId("n222")

    n1.verySimilar(n2) should be(true)
    n2.verySimilar(n1) should be(true)
  }

  it should
    "mark variables with different starting letter as not very similar" in {
      val n1 = new StrId("n123")
      val n2 = new StrId("l123")

      n1.verySimilar(n2) should be(false)
      n2.verySimilar(n1) should be(false)
    }

  it should "mark IfThen and IfElse as (very) similar" in {
    val thenStmts = new StmtList(List[Stmt]().asJava)
    val meta      = NodeGen.generateNonDataBlockMetadata(opcode = "if")
    val n1        = new IfThenStmt(new BoolLiteral(true), thenStmts, meta)
    val n2 = new IfElseStmt(new BoolLiteral(false), thenStmts, thenStmts, meta)

    n1.verySimilar(n2) should be(true)
    n2.verySimilar(n1) should be(true)
    n1.similar(n2) should be(true)
    n2.similar(n1) should be(true)
  }

  it should "mark add and minus as very similar" in {
    val add =
      new Add(new NumberLiteral(1.0), new NumberLiteral(2.0), meta("add"))
    val minus =
      new Minus(new NumberLiteral(3.0), new NumberLiteral(4.0), meta("minus"))

    add.verySimilar(minus) should be(true)
    minus.verySimilar(add) should be(true)
  }

  it should "mark multiplication and division as very similar" in {
    val mult =
      new Mult(new NumberLiteral(1.0), new NumberLiteral(2.0), meta("mult"))
    val div =
      new Div(new NumberLiteral(3.0), new NumberLiteral(4.0), meta("div"))

    mult.verySimilar(div) should be(true)
    div.verySimilar(mult) should be(true)
  }

  it should "mark division and modulo as very similar" in {
    val div =
      new Div(new NumberLiteral(1.0), new NumberLiteral(2.0), meta("div"))
    val mod =
      new Mod(new NumberLiteral(3.0), new NumberLiteral(4.0), meta("mod"))

    mod.verySimilar(div) should be(true)
    div.verySimilar(mod) should be(true)
  }

  it should "mark LengthOfString and LengthOfVar as very similar" in {
    val los = new LengthOfString(new StringLiteral("something"), meta("los"))
    val lov = new LengthOfVar(new StrId("x"), meta("lov"))

    los.verySimilar(lov) should be(true)
    lov.verySimilar(los) should be(true)
  }

  it should "mark MouseX and MouseY as very similar" in {
    val mx = new MouseX(meta("mx"))
    val my = new MouseY(meta("my"))

    mx.verySimilar(my) should be(true)
    my.verySimilar(mx) should be(true)
  }

  it should "mark PositionX and PositionY as very similar" in {
    val px = new PositionX(meta("px"))
    val py = new PositionY(meta("py"))

    px.verySimilar(py) should be(true)
    py.verySimilar(px) should be(true)
  }

  it should "mark BiggerThan and LessThan as very similar" in {
    val bt =
      new BiggerThan(new NumberLiteral(1.0), new NumberLiteral(2.0), meta("bt"))
    val lt =
      new LessThan(new NumberLiteral(3.0), new NumberLiteral(4.0), meta("lt"))

    bt.verySimilar(lt) should be(true)
    lt.verySimilar(bt) should be(true)
  }

  it should "mark SwitchBackdrop and SwitchBackdropAndWait as very similar" in {
    val sw  = new SwitchBackdrop(new Next(meta("n")), meta("sw"))
    val sww = new SwitchBackdropAndWait(new Next(meta("n")), meta("sww"))

    sw.verySimilar(sww) should be(true)
    sww.verySimilar(sw) should be(true)
  }

  it should "mark Say and SayForSecs as very similar" in {
    val s = new Say(new StringLiteral("some"), meta("say"))
    val ss = new SayForSecs(
      new StringLiteral("sfs"),
      new NumberLiteral(1.0),
      meta("sfs")
    )

    s.verySimilar(ss) should be(true)
    ss.verySimilar(s) should be(true)
  }

  it should "mark Think and ThinkForSecs as very similar" in {
    val t = new Think(new StringLiteral("some"), meta("think"))
    val tfs = new ThinkForSecs(
      new StringLiteral("sfs"),
      new NumberLiteral(1.0),
      meta("tfs")
    )

    t.verySimilar(tfs) should be(true)
    tfs.verySimilar(t) should be(true)
  }

  it should "mark ChangeXBy and ChangeYBy as very similar" in {
    val cx = new ChangeXBy(new NumberLiteral(1.0), meta("cx"))
    val cy = new ChangeYBy(new NumberLiteral(2.0), meta("cy"))

    cx.verySimilar(cy) should be(true)
    cy.verySimilar(cx) should be(true)
  }

  it should "mark SetXTo and SetYTo as very similar" in {
    val sx = new SetXTo(new NumberLiteral(1.0), meta("sx"))
    val sy = new SetYTo(new NumberLiteral(2.0), meta("sy"))

    sx.verySimilar(sy) should be(true)
    sy.verySimilar(sx) should be(true)
  }

  it should "mark GlideSecsTo and GlideSecsToXY as very similar" in {
    val n1 = new GlideSecsTo(
      new NumberLiteral(12.0),
      new RandomPos(meta("randpos")),
      meta("glide")
    )
    val n2 = new GlideSecsToXY(
      new NumberLiteral(2.0),
      new NumberLiteral(120.0),
      new NumberLiteral(123),
      meta("glideXY")
    )

    n1.verySimilar(n2) should be(true)
    n2.verySimilar(n1) should be(true)
  }

  it should "mark GoToPos and GoToPosXY as very similar" in {
    val n1 = new GoToPos(new RandomPos(meta("randpos")), meta("goto"))
    val n2 = new GoToPosXY(
      new NumberLiteral(120.0),
      new NumberLiteral(123),
      meta("gotoXY")
    )

    n1.verySimilar(n2) should be(true)
    n2.verySimilar(n1) should be(true)
  }

  it should "mark TurnLeft and TurnRight as very similar" in {
    val n1 = new TurnLeft(new NumberLiteral(12.0), meta("turnL"))
    val n2 = new TurnRight(new NumberLiteral(11.0), meta("turnR"))

    n1.verySimilar(n2) should be(true)
    n2.verySimilar(n1) should be(true)
  }

  it should "mark And and Or as similar" in {
    val and =
      new And(new BoolLiteral(true), new BoolLiteral(false), meta("and"))
    val or = new Or(new BoolLiteral(false), new BoolLiteral(true), meta("or"))

    and.similar(or) should be(true)
    or.similar(and) should be(true)
    and.verySimilar(or) should be(false)
    or.verySimilar(and) should be(false)
  }

  it should "mark number expressions as similar" in {
    val n1 = new NumberLiteral(1.2)
    val n2 =
      new Add(new NumberLiteral(1.4), new NumberLiteral(56.0), meta("add"))

    n1.similar(n2) should be(true)
    n2.similar(n1) should be(true)
    n1.verySimilar(n2) should be(false)
    n2.verySimilar(n1) should be(false)
  }

  it should "mark string expressions as similar" in {
    val n1 = new StringLiteral("text")
    val n2 = new Answer(meta("answer"))

    n1.similar(n2) should be(true)
    n2.similar(n1) should be(true)
    n1.verySimilar(n2) should be(false)
    n2.verySimilar(n1) should be(false)
  }

  it should "mark Say(ForSecs) and Think(ForSecs) as similar" in {
    val n1 = new Say(new StringLiteral("some"), meta("say"))
    val n2 = new Think(new StringLiteral("some"), meta("think"))
    val n3 = new SayForSecs(
      new StringLiteral("saysec"),
      new NumberLiteral(10.0),
      meta("sfs")
    )
    val n4 = new ThinkForSecs(
      new StringLiteral("some"),
      new NumberLiteral(12.0),
      meta("tfs")
    )

    n1.similar(n2) should be(true)
    n2.similar(n1) should be(true)
    n1.similar(n4) should be(true)
    n4.similar(n1) should be(true)
    n3.similar(n2) should be(true)
    n2.similar(n3) should be(true)

    n1.verySimilar(n2) should be(false)
    n2.verySimilar(n1) should be(false)
    n1.verySimilar(n4) should be(false)
    n4.verySimilar(n1) should be(false)
    n3.verySimilar(n2) should be(false)
    n2.verySimilar(n3) should be(false)
  }

  it should "mark ChangeVariableBy and SetVariableTo as similar" in {
    val n1 = new ChangeVariableBy(
      new StrId("x"),
      new NumberLiteral(12.0),
      meta("change")
    )
    val n2 =
      new SetVariableTo(new StrId("y"), new NumberLiteral(6.0), meta("set"))

    n1.similar(n2) should be(true)
    n2.similar(n1) should be(true)
    n1.verySimilar(n2) should be(false)
    n2.verySimilar(n1) should be(false)
  }

  it should "mark RepeatTimes and Until as similar" in {
    val n1 = new RepeatTimesStmt(
      new NumberLiteral(3.0),
      new StmtList(List().asJava),
      meta("repeat")
    )
    val n2 = new UntilStmt(
      new BoolLiteral(false),
      new StmtList(List().asJava),
      meta("until")
    )

    n1.similar(n2) should be(true)
    n2.similar(n1) should be(true)
    n1.verySimilar(n2) should be(false)
    n2.verySimilar(n1) should be(false)
  }

  it should "mark RepeatForever and Until as similar" in {
    val n1 = new RepeatForeverStmt(new StmtList(List().asJava), meta("repeat"))
    val n2 = new UntilStmt(
      new BoolLiteral(false),
      new StmtList(List().asJava),
      meta("until")
    )

    n1.similar(n2) should be(true)
    n2.similar(n1) should be(true)
    n1.verySimilar(n2) should be(false)
    n2.verySimilar(n1) should be(false)
  }

  private def meta(opcode: String): NonDataBlockMetadata = {
    NodeGen.generateNonDataBlockMetadata(BlockId.random().toString, opcode)
  }
}
