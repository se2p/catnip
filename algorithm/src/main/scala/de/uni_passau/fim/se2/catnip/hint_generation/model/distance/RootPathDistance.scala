package de.uni_passau.fim.se2.catnip.hint_generation.model.distance

import de.uni_passau.fim.se2.catnip.hint_generation.BlockSimilarity
import de.uni_passau.fim.se2.catnip.model.{
  NodeRootPath,
  RootPathElement,
  RootPathElementField,
  RootPathElementIndex,
  RootPathRoot
}
import de.uni_passau.fim.se2.catnip.util.ASTNodeSimilarity
import de.uni_passau.fim.se2.litterbox.ast.model.{ASTNode, Script}

object RootPathDistance extends NodeDistanceMetric {

  /** Defines by how much the distance should increase when the two nodes belong
    * to different actors.
    */
  private val penaltyDifferentActor = 2

  /** Defines by how much the distance should increase when two compared nodes
    * are stored in different fields or at different indices of their respective
    * parent.
    */
  private val penaltyDifferentChildPos = 0.2

  /** Calculates a distance between `from` and `to`.
    *
    * @param from
    *   a T.
    * @param to
    *   another T.
    * @return
    *   a distance between `from` and `to` in range <math
    *   xmlns="http://www.w3.org/1998/Math/MathML"
    *   display="inline"><mo>[</mo><mn>0.0</mn><mo>,</mo><mi
    *   mathvariant="normal">∞</mi><mo>)</mo></math>.
    */
  override def distance(
      from: ASTNode,
      to: ASTNode
  ): Distance[ASTNode, ASTNode] = {
    val fromPath = NodeRootPath(from)
    val toPath   = NodeRootPath(to)

    val baseDistance = if (fromPath == toPath) {
      0
    } else {
      val directDifference = similarityIndex(fromPath, toPath)
      val bestDifference =
        if (fromPath.lengthCompare(2) > 0 && toPath.lengthCompare(2) > 0) {
          val offsetPlusOne  = similarityIndex(fromPath, toPath.drop(1))
          val offsetMinusOne = similarityIndex(fromPath.drop(1), toPath)
          math.min(math.min(directDifference, offsetPlusOne), offsetMinusOne)
        } else {
          directDifference
        }

      val actorPenalty =
        if (from.belongsToSameActor(to)) 0 else penaltyDifferentActor

      bestDifference + actorPenalty +
        // add penalty for different nesting depths
        math.abs(fromPath.length - toPath.length)
    }

    Distance(from, to, baseDistance)
  }

  /** Compares the elements pairwise and checks each pair for similarity.
    * @param as
    *   some sequence of root path elements.
    * @param bs
    *   another sequence of root path elements.
    * @return
    *   a value describing the similarity between the two sequences. Higher
    *   value is worse, zero is best.
    */
  private def similarityIndex(
      as: Iterable[RootPathElement],
      bs: Iterable[RootPathElement]
  ): Double = {
    def nodeSimilarity(a: ASTNode, b: ASTNode): Double = {
      StructuralDistance.distanceCheckingLiteralValue(a, b).distance
    }

    def compare(n1: ASTNode, n2: ASTNode, addPenalty: Boolean): Double = {
      val sim = (n1, n2) match {
        case (s1: Script, s2: Script) =>
          val childDistance =
            BlockSimilarity.distanceFromChildren(s1.getStmtList, s2.getStmtList)
          nodeSimilarity(s1.getEvent, s2.getEvent) + childDistance
        case _ => nodeSimilarity(n1, n2)
      }

      if (addPenalty || sim == 0) {
        sim
      } else {
        (sim + penaltyDifferentChildPos) / (1 + penaltyDifferentChildPos)
      }
    }

    as.lazyZip(bs)
      .map {
        case (RootPathRoot(_), RootPathRoot(_)) => 0
        case (RootPathElementField(n1, f1), RootPathElementField(n2, f2)) =>
          compare(n1, n2, f1 == f2)
        case (RootPathElementIndex(n1, i1), RootPathElementIndex(n2, i2)) =>
          compare(n1, n2, i1 == i2)
        case _ => 1
      }
      .sum
  }
}
