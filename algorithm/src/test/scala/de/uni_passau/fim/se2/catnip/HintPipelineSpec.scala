package de.uni_passau.fim.se2.catnip

import de.uni_passau.fim.se2.catnip.common.FileFinder
import de.uni_passau.fim.se2.catnip.common.defs.UnitSpec
import de.uni_passau.fim.se2.catnip.hint_generation.StructuralHintGenerator
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
import org.apache.commons.lang3.reflect.FieldUtils

import scala.collection.mutable.ListBuffer

class HintPipelineSpec extends UnitSpec {
  "The HintPipeline" should "accept the structural hint generator" in {
    val sc = new StructuralHintGenerator(List.empty)
    val _  = new HintPipeline(sc)
  }

  it should "generate the same empty list of hints as the hint generator by itself for student program = solution" in {
    val program = FileFinder.loadProgramFromFile(
      "example_programs/num_expr/num_expr_normalise_test_control_and_operators.sb3"
    )
    val sc       = new StructuralHintGenerator(List(program))
    val pipeline = new HintPipeline(sc)

    val scHints = sc.generateHints(program)
    val plHints = pipeline.generateHints(program)

    plHints.hints should contain theSameElementsAs scHints.hints
    plHints.hints should be(empty)
  }

  "A Default Hint Pipeline" should "contain eight default postprocessors" in {
    val scp = HintPipeline.defaultHintPipeline

    val postProcessingDriver = FieldUtils
      .readField(scp, "postProcessor", true)
      .asInstanceOf[PostProcessingDriver]
    val postProcessors = FieldUtils
      .readField(postProcessingDriver, "processors", true)
      .asInstanceOf[ListBuffer[HintPostprocessor]]

    postProcessors should contain theSameElementsAs List(
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
