package de.uni_passau.fim.se2.catnip.hint_generation.model.distance

import de.uni_passau.fim.se2.catnip.common.FileFinder
import de.uni_passau.fim.se2.catnip.common.defs.PropSpec
import de.uni_passau.fim.se2.catnip.util.NodeListVisitor
import de.uni_passau.fim.se2.litterbox.ast.parser.Scratch3Parser
import de.uni_passau.fim.se2.catnip.util.Constants.EXAMPLES_FOLDER

class ChildDistancePropSpec extends PropSpec {
  property("The child distance between identical nodes should be zero") {
    val files =
      Table("path") ++ FileFinder.matchingSb3Files(EXAMPLES_FOLDER, ".*".r)
    files.length should be > 0

    val parser = new Scratch3Parser

    forAll(files) { file =>
      val program = parser.parseSB3File(file.toFile)
      val nodes   = NodeListVisitor(program, _.getParentNode != null)

      forAll(Table("node") ++ nodes) { node =>
        val d = new ChildDistance(node.getParentNode, node.getParentNode)
        d.distance(node, node).distance shouldBe 0
      }
    }
  }
}
