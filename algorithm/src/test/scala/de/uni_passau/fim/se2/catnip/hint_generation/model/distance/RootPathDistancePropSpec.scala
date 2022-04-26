package de.uni_passau.fim.se2.catnip.hint_generation.model.distance

import de.uni_passau.fim.se2.catnip.common.FileFinder
import de.uni_passau.fim.se2.catnip.common.defs.PropSpec
import de.uni_passau.fim.se2.catnip.model.NodeRootPath
import de.uni_passau.fim.se2.litterbox.ast.parser.Scratch3Parser
import de.uni_passau.fim.se2.catnip.util.Constants.EXAMPLES_FOLDER
import de.uni_passau.fim.se2.catnip.util.{ASTNodeExt, NodeListVisitor}
import org.scalatest.tags.Slow

@Slow
class RootPathDistancePropSpec extends PropSpec {
  property(
    "The Root Path Distance between identical nodes should always be zero"
  ) {
    val files =
      Table("path") ++ FileFinder.matchingSb3Files(
        EXAMPLES_FOLDER,
        "simple.*".r
      )
    files.length should be > 0

    val parser = new Scratch3Parser

    forAll(files) { file =>
      val program = parser.parseSB3File(file.toFile)
      val nodes   = NodeListVisitor(program)

      forAll(Table("node") ++ nodes) { node =>
        RootPathDistance.distance(node, node).distance shouldBe 0
      }
    }
  }

  property(
    "The Root Path Distance between nodes with the same path should be zero"
  ) {
    val files =
      Table("path") ++ FileFinder.matchingSb3Files(
        EXAMPLES_FOLDER,
        "simple_if.*".r
      )
    files.length should be > 0

    val parser = new Scratch3Parser
    forAll(files) { file =>
      val program = parser.parseSB3File(file.toFile)
      val nodes   = NodeListVisitor(program)

      for {
        nodeA <- nodes
        nodeB <- nodes
        if NodeRootPath(nodeA) == NodeRootPath(nodeB)
      } {
        withClue(
          s"RootPaths:\n${NodeRootPath(nodeA)}\n${NodeRootPath(nodeB)}\nDistance "
        ) {
          RootPathDistance.distance(nodeA, nodeB).distance shouldBe 0
          RootPathDistance.distance(nodeB, nodeA).distance shouldBe 0
        }
      }
    }
  }

  property(
    "The Root Path Distance between nodes with different path should be bigger than zero"
  ) {
    val files =
      Table("path") ++ FileFinder.matchingSb3Files(
        EXAMPLES_FOLDER,
        "simple_add.*".r
      )
    files.length should be > 0

    val parser = new Scratch3Parser
    forAll(files) { file =>
      val program = parser.parseSB3File(file.toFile)
      val nodes   = NodeListVisitor(program)

      for {
        nodeA <- nodes.slice(0, 200)
        nodeB <- nodes.reverse.slice(0, 200)
        if NodeRootPath(nodeA) != NodeRootPath(nodeB)
      } {
        withClue(
          s"RootPaths:\n${NodeRootPath(nodeA)}\n${NodeRootPath(nodeB)}\nDistance "
        ) {
          RootPathDistance.distance(nodeA, nodeB).distance should be > 0.0
          RootPathDistance.distance(nodeB, nodeA).distance should be > 0.0
        }
      }
    }
  }

  property(
    "In a program the root path distance of two containers should only be zero if their root paths are equal"
  ) {
    val files =
      Table("path") ++ FileFinder.matchingSb3Files(
        EXAMPLES_FOLDER,
        ".*".r
      )
    files.length should be > 0

    val parser = new Scratch3Parser
    forAll(files) { file =>
      val program1    = parser.parseSB3File(file.toFile)
      val containers1 = NodeListVisitor(program1, _.isContainer)

      val program2    = parser.parseSB3File(file.toFile)
      val containers2 = NodeListVisitor(program2, _.isContainer)

      val table =
        Table(("c1", "c2")) ++ (for {
          c1 <- containers1
          c2 <- containers2
        } yield (c1, c2))

      forAll(table) { (c1, c2) =>
        val p1 = NodeRootPath(c1)
        val p2 = NodeRootPath(c2)

        if (p1 == p2) {
          RootPathDistance.distance(c1, c2).distance shouldBe 0.0
          RootPathDistance.distance(c2, c1).distance shouldBe 0.0
        } else {
          RootPathDistance.distance(c1, c2).distance should be > 0.0
          RootPathDistance.distance(c2, c1).distance should be > 0.0
        }
      }
    }
  }
}
