package de.uni_passau.fim.se2.catnip.common

import de.uni_passau.fim.se2.catnip.common.defs.UnitSpec
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.Round
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.astLists.{
  FieldsMetadataList,
  InputMetadataList
}
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.{
  NoMutationMetadata,
  NonDataBlockMetadata
}

import java.util.Optional
import scala.jdk.CollectionConverters.*

class FieldSetterSpec extends UnitSpec {
  val meta = new NonDataBlockMetadata(
    "",
    "",
    "",
    "",
    "",
    new InputMetadataList(List().asJava),
    new FieldsMetadataList(List().asJava),
    false,
    false,
    new NoMutationMetadata
  )

  "The Field Setter" should "not crash when the field could not be found" in {
    val o = Optional.of(Integer.valueOf(12))

    FieldSetter.setField(o, "nonExisting", Integer.valueOf(200))
  }

  it should "get the field value given its field name" in {
    val n = new NumberLiteral(12)
    val r = new Round(n, meta)

    FieldSetter.getFieldValue(r, "operand1") shouldBe Some(n)
  }

  it should "get the name of the field given its current value" in {
    val n = new NumberLiteral(12)
    val r = new Round(n, meta)

    FieldSetter.getFieldNameWithValue(r, n) shouldBe Some("operand1")
  }

  it should "replace the field given its current value" in {
    val n  = new NumberLiteral(12)
    val n2 = new NumberLiteral(20)
    val r  = new Round(n, meta)

    FieldSetter.replaceValueInField(r, n, n2)
    r.getOperand1 shouldBe n2
  }

  it should "not crash if no field with the given value exists and it could therefore not be replaced" in {
    val n  = new NumberLiteral(12)
    val n2 = new NumberLiteral(20)
    val n3 = new NumberLiteral(123)
    val r  = new Round(n, meta)

    FieldSetter.replaceValueInField(r, n2, n3)
    r.getOperand1 shouldBe n
  }
}
