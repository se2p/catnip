package algorithm

import controllers.model_converters.ProgramConverters.ProgramFromPath
import de.uni_passau.fim.se2.catnip.HintPipeline
import de.uni_passau.fim.se2.catnip.hint_generation.model.HintGenerationResult
import de.uni_passau.fim.se2.litterbox.ast.model.Program
import play.api.Logger

import java.nio.file.{Files, FileVisitOption, Path}
import scala.jdk.CollectionConverters.*
import scala.util.Success

/** Wrapper for the actual hint generation process.
  */
class HintPipelineWrapper(solutionsDirectory: Path) {
  private val log = Logger(this.getClass)

  private val hintGenerator = HintPipeline.defaultHintPipeline

  Files
    .walk(solutionsDirectory, FileVisitOption.FOLLOW_LINKS)
    .iterator()
    .asScala
    .collect { case p: Path if p.toString.endsWith(".sb3") => p.program }
    .collect { case Success(program) => program }
    .foreach(addSolution)

  def generateHints(program: Program): HintGenerationResult = {
    hintGenerator.generateHints(program)
  }

  def addSolutions(ss: List[Program]): Unit = {
    ss.foreach(addSolution)
  }

  def addSolution(solution: Program): Unit = {
    // mutable collection is potentially modified from multiple threads at the
    // same time
    this.synchronized {
      hintGenerator.addSolutions(solution)
      log.info(
        s"Added solution ${solution.getIdent.getName} to hint generator!"
      )
    }
  }
}
