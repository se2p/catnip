package de.uni_passau.fim.se2.catnip.model

import de.uni_passau.fim.se2.catnip.common.FileFinder
import de.uni_passau.fim.se2.catnip.common.defs.PropSpec
import de.uni_passau.fim.se2.catnip.util.NodeListVisitor
import de.uni_passau.fim.se2.litterbox.ast.parser.Scratch3Parser

class NodeRootPathPropSpec extends PropSpec {
  val EXAMPLES_FOLDER = "example_programs"

  property(
    "The Root Path of identical nodes should always be equal"
  ) {
    val files =
      Table("path") ++ FileFinder.matchingSb3Files(EXAMPLES_FOLDER, ".*".r)
    files.length should be > 0

    val parser = new Scratch3Parser

    forAll(files) { file =>
      val program = parser.parseSB3File(file.toFile)
      val nodes   = NodeListVisitor(program)

      forAll(Table("node") ++ nodes) { node =>
        NodeRootPath(node) shouldEqual NodeRootPath(node)
      }
    }
  }
}
