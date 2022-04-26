package de.uni_passau.fim.se2.catnip.hint_generation

import at.unisalzburg.dbresearch.apted.node.{Node, StringNodeData}
import de.uni_passau.fim.se2.catnip.hint_generation.model.{
  HintGenerationResult,
  HintGenerator,
  Matching,
  MatchingContainers,
  MatchingResultActor
}
import de.uni_passau.fim.se2.catnip.model.{
  BlockId,
  DeletionHint,
  InsertionHint,
  MissingActorHint,
  NormalisedScratchProgram,
  ReorderHint,
  ReplaceFieldHint,
  ReplaceStmtHint,
  ScratchProgram,
  SolutionNode,
  SolutionNodeTyped,
  SolutionProgram,
  StructuralHint,
  StudentNode,
  StudentNodeTyped,
  StudentProgram
}
import de.uni_passau.fim.se2.catnip.{model, util}
import de.uni_passau.fim.se2.catnip.util.{
  removeTransitiveChildren,
  ActorDefinitionExt,
  APTEDTreeDistance,
  ASTNodeExt,
  ASTNodeExtTyped,
  NodeListVisitor
}
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never
import de.uni_passau.fim.se2.litterbox.ast.model.{
  ActorDefinition,
  ASTNode,
  Program,
  Script,
  ScriptList
}
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NonDataBlockMetadata
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.{
  IfElseStmt,
  IfThenStmt
}
import de.uni_passau.fim.se2.litterbox.ast.opcodes.ControlStmtOpcode
import org.slf4j.LoggerFactory

import scala.collection.mutable
import scala.jdk.CollectionConverters.*
import scala.util.{Failure, Success}

/** The actual hint generation algorithm.
  *
  * @param referenceSolutions
  *   the solution programs the student program should be compared to.
  * @param findMatchUsingApted
  *   true, if the [[APTEDTreeDistance]] should be used to find the closest
  *   solution to the student program.
  */
class StructuralHintGenerator(
    referenceSolutions: List[Program],
    findMatchUsingApted: Boolean = false
) extends HintGenerator {
  private val logger = LoggerFactory.getLogger(this.getClass)

  private val solutionPrograms = {
    val normalisedSolutions =
      referenceSolutions.map(NormalisedScratchProgram(_))
    mutable.ListBuffer.from(normalisedSolutions)
  }

  /** The APTED algorithm needs a different format for its calculations.
    *
    * Cache the converted programs to avoid doing the same transformation
    * multiple times.
    */
  private val solutionsForAPTED
      : mutable.Map[ScratchProgram, Node[StringNodeData]] = {
    mutable.Map.from(
      solutionPrograms.map(p => p -> APTEDTreeDistance.toClassNameTree(p))
    )
  }

  def storedSolutions: List[NormalisedScratchProgram] = solutionPrograms.toList

  /** Adds example solution(s) into the pool of solutions the student’s program
    * is compared to.
    *
    * Normalises each solution before it is used for any further operations.
    * @param solutions
    *   some example solution(s).
    */
  override def addSolutions(solutions: Program*): Unit = {
    solutions.foreach { s =>
      val sNorm = NormalisedScratchProgram(s)
      solutionPrograms += sNorm
      solutionsForAPTED += (sNorm -> APTEDTreeDistance.toClassNameTree(sNorm))
    }
  }

  /** Generate the actual hints for a student’s program.
    * @param program
    *   for which the student requests hints.
    * @return
    *   structural hints for `program`.
    */
  override def generateHints(program: Program): HintGenerationResult = {
    val studentProgram = StudentProgram(NormalisedScratchProgram(program))

    val (closestSolution, matching) = if (findMatchUsingApted) {
      val s = ClosestProgramFinder.findClosestSolutionAPTED(
        solutionsForAPTED.toMap,
        studentProgram
      )
      val m = ProgramNodeMatcher.matchNodes(s, studentProgram)
      (s, m)
    } else {
      val solutions = solutionPrograms.map(s => SolutionProgram(s.program))
      ClosestProgramFinder.findClosestSolution(solutions, studentProgram)
    }

    val missingActorHints = generateHintsMissingActors(
      closestSolution,
      studentProgram,
      matching.actorMatches
    )
    val structuralHints = generateHintsPerActor(matching)

    HintGenerationResult(
      studentProgram,
      closestSolution,
      missingActorHints ++ structuralHints
    )
  }

  /** For each actor that is present in the reference program but not in the
    * student program, generate a hint that recommends adding a new actor.
    * @param referenceProgram
    *   the closest solution to the student program.
    * @param studentProgram
    *   the current student program.
    * @param actorMatches
    *   the already computed matching between actors of `referenceProgram` and
    *   `studentProgram`.
    * @return
    *   a hint for each actor that is missing in the student’s program.
    */
  private def generateHintsMissingActors(
      referenceProgram: SolutionProgram,
      studentProgram: StudentProgram,
      actorMatches: Matching[ActorDefinition]
  ): List[MissingActorHint] = {
    val solutionActors = referenceProgram.program.actors
    val matchedActors  = actorMatches.matching.keySet.map(_.node.name)
    val studentActorDefinitions = StudentNode(
      studentProgram.program.program.getActorDefinitionList
    )

    (solutionActors.keySet diff matchedActors)
      .map { actorName =>
        val actor = solutionActors(actorName)
        MissingActorHint(studentActorDefinitions, actor)
      }
      .flatMap {
        case Success(hint) => Some(hint)
        case Failure(err) =>
          logger.warn(s"Could not create MissingActorHint.", err)
          None
      }
      .toList
  }

  /** Generates structural hints per actor for each actor in the
    * `solutionProgram` that has a partner in the `studentProgram`.
    * @param nodeMatching
    *   a matching between actors and their nodes.
    * @return
    *   all structural hints generated for the actors.
    */
  private def generateHintsPerActor(
      nodeMatching: ProgramNodeMatching
  ): List[StructuralHint] = {
    nodeMatching.actorMatches.matching.flatMap {
      case (solutionActor, studentActor) =>
        val actorName = solutionActor.node.name
        val innerMap  = nodeMatching.innerMatches(ActorName(actorName))
        generateHintsSingleActor(solutionActor, studentActor, innerMap)
    }.toList
  }

  /** Generates structural hints for a single pair of matching solution and
    * student actors.
    * @param solutionActor
    *   some actor of the solution program.
    * @param studentActor
    *   the corresponding actor to `solutionActor` in the student’s program.
    * @return
    */
  private def generateHintsSingleActor(
      solutionActor: SolutionNodeTyped[ActorDefinition],
      studentActor: StudentNodeTyped[ActorDefinition],
      nodeMap: MatchingResultActor
  ): Iterable[StructuralHint] = {
    val scriptHints =
      generateScriptHints(solutionActor, studentActor, nodeMap.scriptsMap)

    val containerMap = nodeMap.containerMap.map {
      case MatchingContainers(sol, stud, _) => sol -> stud
    }.toMap
    val containerHints =
      generateContainerHints(solutionActor, studentActor, containerMap)

    val childrenHints = nodeMap.containerMap.flatMap {
      case MatchingContainers(
            solutionContainer,
            studentContainer,
            childrenMap
          ) =>
        generateChildrenHints(solutionContainer, studentContainer, childrenMap)
    }

    scriptHints ++ containerHints ++ childrenHints
  }

  /** Generates hints for the scripts in the actor based on the matching.
    *
    * Scripts in the solution which have no partner in the student’s program
    * result in an [[InsertionHint InsertionHint]].
    *
    * Scripts in the student’s program which have no partner in the solution
    * result in a [[DeletionHint DeletionHint]]. However, if the script event
    * type is `Never`, then the script is ignored, as its presence does not
    * change the execution of the program.
    *
    * @param solutionActor
    *   some actor in the solution program.
    * @param studentActor
    *   the matched partner to `solutionActor` in the student’s program.
    * @param scriptsMap
    *   the computed matching of scripts in `solutionActor` to scripts in
    *   `studentActor`.
    * @return
    *   a list of [[InsertionHint InsertionHints]] and
    *   [[DeletionHint DeletionHints]] as described above.
    */
  private def generateScriptHints(
      solutionActor: SolutionNodeTyped[ActorDefinition],
      studentActor: StudentNodeTyped[ActorDefinition],
      scriptsMap: Matching[Script]
  ): List[StructuralHint] = {
    val solutionScripts = solutionActor.node.getScripts.getScriptList.asScala
    val studentScripts  = studentActor.node.getScripts.getScriptList.asScala

    val matchedButDifferentEventType = scriptsMap.matching
      .filter { case (sol, stud) =>
        val a = sol.node.getEvent.getClass
        val b = stud.node.getEvent.getClass
        a != b
      }
      .map { case (sol, stud) =>
        ReplaceFieldHint(stud.asRegular, "event", sol.node.getEvent)
      }

    val (unmatchedSolutionScripts, unmatchedStudentScripts) = missingInMap(
      solutionScripts,
      studentScripts,
      scriptsMap.matching.map { case (k, v) => k.node -> v.node }
    )
    val insertMissingScripts = unmatchedSolutionScripts.map { script =>
      InsertionHint(StudentNode(studentActor.node.getScripts), script, 0)
    }
    val deleteSuperfluousScripts = unmatchedStudentScripts
      .filter {
        _.getEvent match {
          case _: Never => false
          case _        => true
        }
      }
      .map(s => DeletionHint(StudentNode(s)))

    (matchedButDifferentEventType ++ insertMissingScripts ++ deleteSuperfluousScripts).toList
  }

  /** Generate hints for containers for which no match could be found.
    * @param solutionActor
    *   the solution root node of a sub-AST.
    * @param studentActor
    *   the student root node of a sub-AST.
    * @param containerMap
    *   the node map calculated in a previous step.
    * @return
    *   hints for container nodes found in the subtrees of the programs.
    */
  private def generateContainerHints(
      solutionActor: SolutionNodeTyped[ActorDefinition],
      studentActor: StudentNodeTyped[ActorDefinition],
      containerMap: Map[SolutionNode, StudentNode]
  ): List[StructuralHint] = {
    // unmatched solution containers can be ignored, they are children of other
    // containers and therefore hints are already generated when matching
    // children of statement lists
    val (_, unmatchedInStudentTree) = unmatchedContainers(
      solutionActor,
      studentActor,
      containerMap
    )

    // only consider the top-most containers for insertion/deletion
    val reducedStudentContainers = removeTransitiveChildren(
      unmatchedInStudentTree
    )

    // keep script hints until this point to allow for removal of transitive hints
    hintsUnmatchedStudentContainers(reducedStudentContainers).filter {
      _.references.node match {
        case _: ScriptList => false
        case _             => true
      }
    }
  }

  /** Generates hints for container nodes in the student’s program for which no
    * partner in the reference solution could be found.
    * @param nodes
    *   containers in the student’s program without a partner.
    * @return
    *   one hint for each node in `nodes`.
    */
  private def hintsUnmatchedStudentContainers(
      nodes: List[StudentNode]
  ): List[StructuralHint] = {
    def buildIfStmt(i: IfElseStmt): IfThenStmt = {
      val boolExpr  = i.getBoolExpr.cloned
      val thenStmts = i.getStmtList.cloned
      val origMeta = (i.getMetadata: @unchecked) match {
        case n: NonDataBlockMetadata => n
      }
      val meta = new NonDataBlockMetadata(
        null,
        BlockId.random().toString,
        ControlStmtOpcode.control_if.toString,
        origMeta.getNext,
        origMeta.getParent,
        origMeta.getInputMetadata,
        origMeta.getFields,
        origMeta.isShadow,
        origMeta.isShadow,
        origMeta.getMutation
      )
      val ifStmt = new IfThenStmt(boolExpr, thenStmts, meta)
      boolExpr.setParentNode(ifStmt)
      thenStmts.setParentNode(ifStmt)
      meta.setParentNode(ifStmt)

      ifStmt
    }

    nodes
      .map(node => (node, node.parentNode))
      .collect { case (StudentNode(node), Some(parent)) =>
        parent match {
          // `else`-Block has no match => suggest if-stmt instead
          case StudentNode(i: IfElseStmt) if i.getElseStmts == node =>
            val stmtList = StudentNode(i.getParentNode)
            val oldNode  = StudentNode(i)
            val ifStmt   = buildIfStmt(i)
            val index    = i.getStmtList.getStmts.indexOf(node)

            ReplaceStmtHint(stmtList, oldNode, ifStmt, index).toOption
          // otherwise suggest deleting the parent node
          case n => Some(model.DeletionHint(n))
        }
      }
      .flatten
  }

  /** Generates hints for the children in the map and unmatched children.
    * @param solutionContainer
    *   the parent container of the children in the solution program.
    * @param studentContainer
    *   the parent container of the children in the student’s program.
    * @param childMap
    *   the matching of the children as computed in a previous step.
    * @return
    *   hints for the nodes that are children of the `studentContainer`,
    *   including hints about missing ones.
    */
  private def generateChildrenHints(
      solutionContainer: SolutionNode,
      studentContainer: StudentNode,
      childMap: Map[SolutionNode, StudentNode]
  ): List[StructuralHint] = {
    val unmatchedHints = generateChildrenHintsUnmatched(
      solutionContainer,
      studentContainer,
      childMap
    )
    val matchedHints = generateChildrenHintsMatched(
      solutionContainer,
      studentContainer,
      childMap
    )

    unmatchedHints ++ matchedHints
  }

  /** Generates hints for the children of the two containers for which no
    * partner could be found.
    * @param solutionContainer
    *   the parent container of the children in the solution program.
    * @param studentContainer
    *   the parent container of the children in the student’s program.
    * @param childMatches
    *   the matching of the children as computed in a previous step.
    * @return
    *   hints about missing/superfluous children of the `studentContainer`.
    */
  private def generateChildrenHintsUnmatched(
      solutionContainer: SolutionNode,
      studentContainer: StudentNode,
      childMatches: Map[SolutionNode, StudentNode]
  ): List[StructuralHint] = {
    val (unmatchedInSolution, unmatchedInStudentProgram) =
      unmatchedChildren(solutionContainer, studentContainer, childMatches)

    // in solution without partner: insertion hint at index of solutionNode
    val addHints = unmatchedInSolution.map { node =>
      val index = solutionContainer.children.indexOf(node)
      InsertionHint(studentContainer, node.node, index)
    }
    // in student program without partner: delete
    val deleteHints =
      unmatchedInStudentProgram.map(node => model.DeletionHint(node))

    addHints ++ deleteHints
  }

  /** Generates hints on how the children of `studentContainer` with a partner
    * in `solutionContainer` should be changed.
    * @param solutionContainer
    *   the parent container of the children in the solution program.
    * @param studentContainer
    *   the parent container of the children in the student’s program.
    * @param childMap
    *   the matching of the children as computed in a previous step.
    * @return
    *   hints how the children of `studentContainer` with a partner should be
    *   changed.
    */
  private def generateChildrenHintsMatched(
      solutionContainer: SolutionNode,
      studentContainer: StudentNode,
      childMap: Map[SolutionNode, StudentNode]
  ): Iterable[StructuralHint] = {
    childMap.flatMap { case (solutionNode, studentNode) =>
      StructuralReplacementHintGenerator(solutionNode, studentNode) match {
        case Nil =>
          // the nodes are structurally equal
          // => check if they are at the same position, too
          generateReorderHints(
            solutionContainer,
            solutionNode,
            studentContainer,
            studentNode
          )
        case structuralHints => structuralHints
      }
    }
  }

  /** Generates a [[ReorderHint]] if the index of `solutionNode` and
    * `studentNode` in their respective containers is different.
    *
    * @param solutionContainer
    *   of which `solutionNode` is a child.
    * @param solutionNode
    *   a child of `solutionContainer`.
    * @param studentContainer
    *   of which `studentNode` is a child.
    * @param studentNode
    *   a child of `studentContainer`.
    * @return
    *   a `ReorderHint` only if the index of `solutionNode` and `studentNode`
    *   inside their parents is different.
    */
  private def generateReorderHints(
      solutionContainer: SolutionNode,
      solutionNode: SolutionNode,
      studentContainer: StudentNode,
      studentNode: StudentNode
  ): Option[ReorderHint] = {
    val solutionIndex = solutionContainer.children.indexOf(solutionNode)
    val studentIndex  = studentContainer.children.indexOf(studentNode)

    if (studentIndex != solutionIndex) {
      Some(ReorderHint(studentContainer, studentNode, solutionIndex))
    } else {
      None
    }
  }

  /** Find a list of container nodes for which no partner could be found.
    * @param solutionActor
    *   the solution node closest to the student’s one.
    * @param studentActor
    *   a node in the student’s program.
    * @param matchedContainers
    *   the node matching as computed in a previous step.
    * @return
    *   two lists: (all containers in the tree of the solution node without
    *   partner, all containers in the tree of the student node without
    *   partner).
    */
  private def unmatchedContainers(
      solutionActor: SolutionNodeTyped[ActorDefinition],
      studentActor: StudentNodeTyped[ActorDefinition],
      matchedContainers: Map[SolutionNode, StudentNode]
  ): (List[SolutionNode], List[StudentNode]) = {
    def isParentScript(n: ASTNode): Boolean = n.parentNode match {
      case Some(_: Script) => true
      case _               => false
    }

    val containersSolution = NodeListVisitor(solutionActor.node, _.isContainer)
      .filterNot(isParentScript)
      .map(SolutionNode(_))
    val containersStudent = util
      .NodeListVisitor(studentActor.node, _.isContainer)
      .filterNot(isParentScript)
      .map(StudentNode(_))

    missingInMap(containersSolution, containersStudent, matchedContainers)
  }

  /** Find a list of container nodes for which no partner could be found.
    * @param solutionContainer
    *   the parent of the nodes in the keys of `childMap`.
    * @param studentContainer
    *   the parent of the nodes in the values of `childMap`.
    * @param childMatches
    *   the node matching as computed in a previous step.
    * @return
    *   two lists: (all children of `solutionContainer` without partner, all
    *   children of `studentContainer` without partner).
    */
  private def unmatchedChildren(
      solutionContainer: SolutionNode,
      studentContainer: StudentNode,
      childMatches: Map[SolutionNode, StudentNode]
  ): (List[SolutionNode], List[StudentNode]) = {
    val solutionContainerChildren = solutionContainer.filteredChildren
    val studentContainerChildren  = studentContainer.filteredChildren

    missingInMap(
      solutionContainerChildren,
      studentContainerChildren,
      childMatches
    )
  }

  /** Finds elements of `completeKeys`/`completeValues` that are not stored in
    * the `map`.
    * @param completeKeys
    *   all possible keys the `map` could contain.
    * @param completeValues
    *   all possible values the `map` could contain.
    * @param map
    *   a map with keys in `completeKeys` and values in `completeValues`.
    * @tparam A
    *   type of the keys of the map.
    * @tparam B
    *   type of the values of the map.
    * @return
    *   two lists: (`completeKeys - map.keys`, `completeValues - map.values`).
    */
  private def missingInMap[A, B](
      completeKeys: Iterable[A],
      completeValues: Iterable[B],
      map: Map[A, B]
  ): (List[A], List[B]) = {
    (
      completeKeys.filter(c => !map.keys.exists(_ == c)).toList,
      completeValues.filter(c => !map.values.exists(_ == c)).toList
    )
  }
}
