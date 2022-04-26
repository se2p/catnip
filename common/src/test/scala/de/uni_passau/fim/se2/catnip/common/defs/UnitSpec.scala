package de.uni_passau.fim.se2.catnip.common.defs

import de.uni_passau.fim.se2.litterbox.ast.model.Program
import de.uni_passau.fim.se2.litterbox.ast.parser.Scratch3Parser
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import org.scalatest.{Inside, Inspectors}

import java.io.File

abstract class UnitSpec
    extends AnyFlatSpec
    with should.Matchers
    with Inside
    with Inspectors {
  def loadProgram(name: String): Program = {
    val s = new Scratch3Parser
    s.parseFile(
      new File(
        this.getClass
          .getResource("example_programs" + File.pathSeparator + name)
          .toURI
      )
    )
  }
}
