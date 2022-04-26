package de.uni_passau.fim.se2.catnip

import de.uni_passau.fim.se2.catnip.hint_generation.StructuralHintGenerator
import de.uni_passau.fim.se2.catnip.hint_generation.model.{
  HintGenerationResult,
  HintGenerator
}
import de.uni_passau.fim.se2.catnip.model.Hint
import de.uni_passau.fim.se2.catnip.postprocessing.{
  CommutativeOpHintReducer,
  DeleteHintReducer,
  HintDeduplicator,
  HintPostprocessor,
  HintSorter,
  MoveHintCreator,
  PostProcessingDriver,
  StatementListReorderReducer,
  SwappableBlocksHintReducer,
  UnreachableHintRemover
}
import de.uni_passau.fim.se2.litterbox.ast.model.Program
import org.slf4j.LoggerFactory

/** Helper class that manages the complete process from student program to
  * processed hints.
  * @param hintGenerator
  *   the algorithm to use for hint generation.
  * @tparam G
  *   the concrete type of the hint generation algorithm.
  */
class HintPipeline[G <: HintGenerator](private val hintGenerator: G) {
  private val logger = LoggerFactory.getLogger(this.getClass)

  private val postProcessor = new PostProcessingDriver

  /** Feeds the program into the hint generator and post-processes those hints.
    * @param program
    *   the student program to generate hints for.
    * @return
    *   a list of post-processed hints for that
    */
  def generateHints(program: Program): HintGenerationResult = {
    val hintGenerationResult = hintGenerator.generateHints(program)
    val processedHints       = postProcessor.process(hintGenerationResult)

    logHintCounts(hintGenerationResult, processedHints)
    logHintTypes(processedHints)

    processedHints
  }

  private def logHintCounts(
      beforePostProcessing: HintGenerationResult,
      afterPostProcessing: HintGenerationResult
  ): Unit = {
    def hintsPerActor(hints: List[Hint]): Map[String, Int] = {
      hints
        .flatMap(hint => hint.actorId)
        .groupMapReduce(identity)(_ => 1)(_ + _)
    }

    def printStats(
        isBefore: Boolean,
        actors: List[String],
        stats: Map[String, Int]
    ): Unit = {
      val total      = stats.values.sum
      val hintCounts = actors.map(stats.get).map(_.getOrElse(0))
      logger.info(s"""Hint count ${
                      if (isBefore) "before"
                      else "after"
                    } postprocessing:
                     |Total;${actors.mkString(";")}
                     |$total;${hintCounts.mkString(";")}""".stripMargin)
    }

    this.synchronized {
      val before = hintsPerActor(beforePostProcessing.hints)
      val after  = hintsPerActor(afterPostProcessing.hints)
      val actors = List("Apple", "Bananas", "Bowl", "Stage").sorted

      printStats(isBefore = true, actors, before)
      printStats(isBefore = false, actors, after)
    }
  }

  private def logHintTypes(result: HintGenerationResult): Unit = {
    this.synchronized {
      logger.info("Generated hints by type:")
      val g = result.hints.groupBy(_.getClass.getSimpleName)
      for {
        hintType <- g.keySet.toList.sorted
      } {
        logger.info(f"$hintType%-25s${g(hintType).size}")
      }
    }
  }

  /** Adds new reference solution(s) to the hint generator to be used in future
    * hint generation queries.
    * @param program
    *   reference solution(s) that student programs can be compared to.
    */
  def addSolutions(program: Program*): Unit = {
    hintGenerator.addSolutions(program*)
  }

  /** Adds a new post-processing step.
    * @param p
    *   the postprocessor to add.
    */
  def addPostProcessor(p: HintPostprocessor): Unit = {
    postProcessor.add(p)
  }

  /** Adds one or more post-processors to the pipeline.
    * @param p
    *   the post-processor(s) to add.
    * @return
    *   `this`, but with the added post-processors.
    */
  def withPostProcessors(p: HintPostprocessor*): HintPipeline[G] = {
    p.foreach(this.addPostProcessor)
    this
  }
}

/** Factory for some example configurations of [[HintPipeline]] s.
  */
object HintPipeline {

  /** Creates a default hint pipeline.
    *
    * Already includes the standard post-processors:
    *
    *   - [[HintDeduplicator]]
    *   - [[CommutativeOpHintReducer]]
    *   - [[MoveHintCreator]]
    *   - [[UnreachableHintRemover]]
    *   - [[DeleteHintReducer]]
    *   - [[StatementListReorderReducer]]
    *   - [[SwappableBlocksHintReducer]]
    *   - [[HintSorter]]
    *
    * @return
    *   a (apart from missing reference solutions) ready-to-use hint pipeline as
    *   described above.
    */
  def defaultHintPipeline: HintPipeline[StructuralHintGenerator] = {
    val sc = new StructuralHintGenerator(List.empty)
    new HintPipeline[StructuralHintGenerator](sc)
      .withPostProcessors(
        HintDeduplicator,
        CommutativeOpHintReducer,
        MoveHintCreator,
        UnreachableHintRemover,
        DeleteHintReducer,
        StatementListReorderReducer,
        SwappableBlocksHintReducer,
        HintSorter
      )
  }
}
