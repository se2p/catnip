package de.uni_passau.fim.se2.catnip.hint_generation

import de.uni_passau.fim.se2.catnip.hint_generation.model.distance.{
  ChildDistance,
  Distance,
  RootPathDistance,
  StructuralDistance
}
import de.uni_passau.fim.se2.catnip.hint_generation.model.{
  DistanceMatrixBuilder,
  Matching,
  MatchingContainers,
  MatchingResult,
  MatchingResultActor
}
import de.uni_passau.fim.se2.catnip.model.{
  DummyNode,
  SolutionNode,
  SolutionNodeTyped,
  StudentNode,
  StudentNodeTyped
}
import de.uni_passau.fim.se2.catnip.{model, util}
import de.uni_passau.fim.se2.catnip.util.{ASTNodeExt, NodeListVisitor}
import de.uni_passau.fim.se2.litterbox.ast.model.{ASTNode, Script, ScriptList}
import org.slf4j.LoggerFactory

import scala.jdk.CollectionConverters.*

/** Used to find the best matching of pairs between nodes in the solution
  * program and nodes in other programs.
  */
object NodeMatching {
  type ScriptMatchingResult =
    MatchingResult[SolutionNodeTyped[Script], StudentNodeTyped[Script]]

  private val logger = LoggerFactory.getLogger(this.getClass)

  /** Finds the best matching between nodes in the subtree of the `solutionNode`
    * and `studentNode`.
    *
    * Split into two steps:
    *
    *   - first map all container nodes to each other via their RootPath.
    *   - then for each matching found the children of the two nodes are matched
    *     to each other.
    *
    * @param solutionNode
    *   a node in the reference program.
    * @param studentNode
    *   a node in the program the student wants hints for.
    * @return
    *   a map (solution node -> (student node, childrenMap)) together with the
    *   total costs of the matching.
    */
  def findClosestNodes(
      solutionNode: SolutionNode,
      studentNode: StudentNode
  ): MatchingResultActor = {
    val (scriptsMap, containerMap, containerMapCost) = rootPathMatching(
      solutionNode,
      studentNode
    ) match {
      case (MatchingResult(sMap, sCosts), MatchingResult(itemMap, costs)) =>
        (sMap, itemMap, sCosts + costs)
    }

    logger.debug(
      s"Found ${containerMap.size} matches between containers in the trees of nodes $solutionNode and $studentNode with a cost of $containerMapCost."
    )

    val (finalMatching, totalCost) = containerMap.view
      .map { case (solutionContainer, studentContainer) =>
        // compute the matching for the children of the container pair
        matchChildNodes(solutionContainer, studentContainer) match {
          case MatchingResult(childrenMap, costs) =>
            (solutionContainer, studentContainer, childrenMap, costs)
        }
      }
      // map the student container to a pair of
      // - the student container
      // - the matching of their children
      // additionally, keep track of the total matching costs
      .foldLeft((List.empty[MatchingContainers], 0.0)) {
        case (
              (map, cost),
              (solutionContainer, studentContainer, childrenMap, cost2)
            ) =>
          val newContainerMap =
            MatchingContainers(solutionContainer, studentContainer, childrenMap)

          (newContainerMap :: map, cost + cost2)
      }

    logger.debug(
      s"Found ${finalMatching.size} matches between nodes in the trees of $solutionNode and $studentNode with a cost of ${containerMapCost + totalCost}."
    )

    MatchingResultActor(
      Matching(scriptsMap),
      finalMatching,
      containerMapCost + totalCost
    )
  }

  /** Map container nodes based on their root paths.
    * @param studentNode
    *   a node in the program the student submitted.
    * @return
    *   a matching of nodes from solution to student program with matching
    *   costs.
    */
  private def rootPathMatching(
      solutionNode: SolutionNode,
      studentNode: StudentNode
  ): (ScriptMatchingResult, MatchingResult[SolutionNode, StudentNode]) = {
    val scriptMatchingRes = findScriptsAndMatch(solutionNode, studentNode)
    val distances         = computeDistances(solutionNode, studentNode)
    val containerMatching = computeMatching(distances)

    (scriptMatchingRes, containerMatching)
  }

  /** Checks if both `solutionNode` and `studentNode` have got a script list. If
    * they have, finds a matching between those scripts.
    * @param solutionNode
    *   some node in the solution program.
    * @param studentNode
    *   some node in the student’s program.
    * @return
    *   a match between scripts in the tree of `solutionNode` and the ones in
    *   the tree of `studentNode` together with a matching cost.
    */
  private def findScriptsAndMatch(
      solutionNode: SolutionNode,
      studentNode: StudentNode
  ): ScriptMatchingResult = {
    (scriptList(solutionNode.node), scriptList(studentNode.node)) match {
      case (Some(scriptsSolution), Some(scriptsStudent)) =>
        val sol  = SolutionNodeTyped(scriptsSolution)
        val stud = StudentNodeTyped(scriptsStudent)
        val matching = computeScriptMatching(sol, stud).itemMap.collect {
          case (SolutionNode(a: Script), StudentNode(b: Script)) =>
            SolutionNodeTyped(a) -> StudentNodeTyped(b)
        }

        MatchingResult(matching, 0.0)
      case _ =>
        MatchingResult(Map.empty, 0.0)
    }
  }

  /** Computes a matching between the scripts stored in the given script lists.
    *
    * Compares them based on their trigger type.
    * @param solutionScripts
    *   the scripts as found in the solution program.
    * @param studentScripts
    *   the scripts as found in the student’s program.
    * @return
    *   a matching between the scripts in the solution and the ones in the
    *   student’s program.
    */
  private def computeScriptMatching(
      solutionScripts: SolutionNodeTyped[ScriptList],
      studentScripts: StudentNodeTyped[ScriptList]
  ): MatchingResult[SolutionNode, StudentNode] = {
    val distances = (for {
      sol  <- solutionScripts.node.getScriptList.asScala
      stud <- studentScripts.node.getScriptList.asScala
    } yield scriptDistance(sol, stud)).map { case Distance(sol, stud, d) =>
      Distance(model.SolutionNode(sol), StudentNode(stud), d)
    }
    computeMatching(distances)
  }

  /** Computes the distance between two scripts by looking their event and
    * direct children in the statement list.
    * @param a
    *   some Scratch script.
    * @param b
    *   another Scratch script.
    * @return
    *   the distance between the scripts `a` and `b`.
    */
  private def scriptDistance(a: Script, b: Script): Distance[Script, Script] = {
    val eventDistance = StructuralDistance.distanceCheckingLiteralValue(a, b)
    val childDistance =
      BlockSimilarity.distanceFromChildren(a.getStmtList, b.getStmtList)

    Distance(a, b, eventDistance.distance + childDistance)
  }

  /** Computes the distances between all container nodes in the subtrees of the
    * two given nodes.
    * @param solutionRoot
    *   the root of the subtree to look at in the solution program.
    * @param studentRoot
    *   the root of the subtree to look at in the student’s program.
    * @return
    *   the distances between pairs of containers in the solution/student
    *   program.
    */
  private def computeDistances(
      solutionRoot: SolutionNode,
      studentRoot: StudentNode
  ): Iterable[Distance[SolutionNode, StudentNode]] = {
    (for {
      sol  <- containerNodes(solutionRoot.node)
      stud <- containerNodes(studentRoot.node)
    } yield RootPathDistance.distance(sol, stud)).map {
      case Distance(from, to, d) =>
        Distance(SolutionNode(from), StudentNode(to), d)
    }
  }

  /** Maps children of `a` to children of `b` with least possible distance.
    * @param a
    *   a node that has children.
    * @param b
    *   another node with children.
    * @return
    *   a matching of children of a to children of b with total matching costs.
    */
  def matchChildNodes(
      a: SolutionNode,
      b: StudentNode
  ): MatchingResult[SolutionNode, StudentNode] = {
    val metric = new ChildDistance(a.node, b.node)

    val childrenA = a.node.filteredChildren
    val childrenB = b.node.filteredChildren

    val distances = (for {
      cA <- childrenA
      cB <- childrenB
    } yield metric.distance(cA, cB)).map { case Distance(from, to, d) =>
      Distance(model.SolutionNode(from), model.StudentNode(to), d)
    }

    computeMatching(distances)
  }

  /** Computes the optimal matching between the nodes based on their distances.
    *
    * Uses the Kuhn-Munkres-Algorithm to compute the matching.
    * @param distances
    *   a list of distances between nodes in the solution program and nodes in
    *   the student’s program.
    * @return
    *   an optimal matching between the given nodes together with a total cost
    *   of that matching.
    */
  private def computeMatching(
      distances: Iterable[Distance[SolutionNode, StudentNode]]
  ): MatchingResult[SolutionNode, StudentNode] = {
    val matrixBuilder = DistanceMatrixBuilder(
      distances,
      SolutionNode(new DummyNode),
      StudentNode(new DummyNode)
    )
    val matrix = matrixBuilder.build()
    matrix.computePairingsKuhnMunkres()
  }

  private def containerNodes(node: ASTNode): Iterable[ASTNode] = {
    util.NodeListVisitor(node, _.isContainer)
  }

  private def scriptList(node: ASTNode): Option[ScriptList] = {
    def isScriptList(n: ASTNode): Boolean = n match {
      case _: ScriptList => true
      case _             => false
    }
    NodeListVisitor(node, isScriptList).headOption.collect {
      case s: ScriptList => s
    }
  }
}
