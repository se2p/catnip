package de.uni_passau.fim.se2.catnip.hint_generation

import de.uni_passau.fim.se2.catnip.common.FileFinder
import de.uni_passau.fim.se2.catnip.common.defs.PropSpec
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode
import de.uni_passau.fim.se2.litterbox.ast.parser.Scratch3Parser
import NodeMatching.ScriptMatchingResult
import de.uni_passau.fim.se2.catnip.hint_generation.model.{
  MatchingContainers,
  MatchingResult
}
import de.uni_passau.fim.se2.catnip.model.{
  NodeRootPath,
  SolutionNode,
  StudentNode
}
import de.uni_passau.fim.se2.catnip.util.Constants.EXAMPLES_FOLDER
import de.uni_passau.fim.se2.catnip.util.{ASTNodeExtTyped, NodeListVisitor}
import org.scalatest.PrivateMethodTester

class NodeMatchingPropSpec extends PropSpec with PrivateMethodTester {
  type RootPathMatchingResult =
    (ScriptMatchingResult, MatchingResult[SolutionNode, StudentNode])
  private val rootPathMatching =
    PrivateMethod[RootPathMatchingResult](Symbol("rootPathMatching"))

  property(
    "If solution and student program are identical the container root-path matching should have distance zero"
  ) {
    val files = Table("path") ++ FileFinder.matchingSb3Files(
      EXAMPLES_FOLDER,
      ".*".r
    )
    files.length should be > 0

    val parser = new Scratch3Parser

    forAll(files) { file =>
      val program = parser.parseSB3File(file.toFile)
      val r = NodeMatching.invokePrivate(
        rootPathMatching(SolutionNode(program), StudentNode(program))
      )
      r._1.costs shouldBe 0
      r._2.costs shouldBe 0
    }
  }

  property(
    "For identical nodes the matching of child nodes should have distance zero"
  ) {
    val files = Table("path") ++ FileFinder.matchingSb3Files(
      EXAMPLES_FOLDER,
      ".*".r
    )
    files.length should be > 0

    val parser = new Scratch3Parser

    forAll(files) { file =>
      val program = parser.parseSB3File(file.toFile)
      val nodes   = NodeListVisitor(program)
      val nodes2  = NodeListVisitor(program.cloned)

      val table = Table(("n1", "n2")) ++ nodes.zip(nodes2)
      forAll(table) { (n1: ASTNode, n2: ASTNode) =>
        val res =
          NodeMatching.matchChildNodes(SolutionNode(n1), StudentNode(n2))
        res.costs shouldBe 0
      }
    }
  }

  property(
    "For nodes with a different number of children the matching of child nodes should have distance > zero"
  ) {
    val files = Table("path") ++ FileFinder.matchingSb3Files(
      EXAMPLES_FOLDER,
      ".*".r
    )
    files.length should be > 0

    val parser = new Scratch3Parser

    forAll(files) { file =>
      val program = parser.parseSB3File(file.toFile)
      val nodes   = NodeListVisitor(program)
      val nodes2  = NodeListVisitor(program.cloned)

      val nodePairs = nodes.zip(nodes2).filterNot { case (n1, n2) =>
        n1.getChildren.size() == n2.getChildren.size()
      }
      forAll(Table(("n1", "n2")) ++ nodePairs) { (n1: ASTNode, n2: ASTNode) =>
        val res =
          NodeMatching.matchChildNodes(SolutionNode(n1), StudentNode(n2))
        res.costs should be > 0.0
      }
    }
  }

  property(
    "If solution and student program are the same, then the matching costs should be zero"
  ) {
    val files = Table("path") ++ FileFinder.matchingSb3Files(
      EXAMPLES_FOLDER,
      ".*".r
    )
    files.length should be > 0

    val parser = new Scratch3Parser

    forAll(files) { file =>
      val program  = parser.parseSB3File(file.toFile)
      val program2 = program.cloned

      val actual = NodeMatching.findClosestNodes(
        SolutionNode(program),
        StudentNode(program2)
      )
      // actual.costs shouldBe 0

      actual.containerMap.foreach {
        case MatchingContainers(solutionContainer, studentContainer, _) =>
          val p1 = NodeRootPath(solutionContainer)
          val p2 = NodeRootPath(studentContainer)

          withClue(s"\n$p1\n$p2\n") {
            p1 shouldEqual p2
          }
      }
    }
  }
}
