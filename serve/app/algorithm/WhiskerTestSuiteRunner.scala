package algorithm

import akka.ConfigurationException
import algorithm.domain.{FailedTests, TestResult, TestSuite}
import config.{KnownTasks, TaskId}
import controllers.model_converters.ProgramConverters.ProgramFromPath
import de.uni_passau.fim.se2.catnip.common.WhiskerRunner
import play.api.{Configuration, Logger}
import util.{copyFileToPermanentDirectory, pathLoader}

import java.nio.file.Path
import javax.inject.{Inject, Singleton}
import scala.util.{Failure, Success, Try}

/** Runs a Whisker test suite on a given program.
  * @param configuration
  *   of the application. Needed for the path to the Whisker files.
  * @param tasks
  *   stores the tasks and their associated hint generators. Used to add new
  *   solutions to the hint generator.
  */
@Singleton
class WhiskerTestSuiteRunner @Inject() (
    configuration: Configuration,
    tasks: KnownTasks
) extends TestSuite {
  private val log = Logger(this.getClass)

  private val runTestSuite =
    configuration.get[Boolean]("hintGenerator.runTestSuite")

  private val acceptNewSolutions = {
    val accept = configuration.get[Boolean]("hintGenerator.acceptNewSolutions")
    if (!runTestSuite && accept) {
      throw new ConfigurationException(
        "Cannot accept new solutions if 'runTestSuite' is 'false'!"
      )
    } else {
      accept
    }
  }

  private val receivedSolutionsDir = {
    if (acceptNewSolutions) {
      Some(configuration.get[Path]("hintGenerator.receivedSolutionsDirectory"))
    } else {
      None
    }
  }

  private val testSuiteRunner = {
    if (runTestSuite) {
      val whiskerJS = configuration.get[Path]("hintGenerator.whiskerJS")
      val testSuite = configuration.get[Path]("hintGenerator.whiskerTestFile")
      WhiskerRunner(whiskerJS, testSuite) match {
        case Right(runner) => Some(runner)
        case Left(err) =>
          throw new IllegalArgumentException(
            s"Cannot create Whisker Testsuite Runner: ${err.mkString(", ")}"
          )
      }
    } else {
      None
    }
  }

  /** Runs the Whisker test suite on the program.
    *
    * If no tests failed, also makes a permanent copy of the program file and
    * adds it to the list of solutions for the hint generating pipeline. If
    * tests failed, just returns the test result.
    * @param program
    *   a student program that should be tested.
    * @return
    *   the result of the test suite execution on `program`.
    */
  override def runTests(taskId: TaskId, program: Path): Try[TestResult] = {
    if (runTestSuite) {
      log.info(s"Starting testsuite execution for program $program…")
      val result = testSuiteRunner match {
        case Some(runner) => runner.run(program).map(TestResult(_))
        case None =>
          throw new ConfigurationException(
            "Whisker JS and test suite are not configured! Cannot run test suite!"
          )
      }

      result match {
        case Success(res) =>
          log.info(
            s"Successfully ran test suite for program $program with result $res."
          )
          addToSolutions(taskId, program, res)
        case Failure(err) =>
          log.warn(s"Running the test suite for program $program failed.", err)
      }

      result
    } else {
      Failure(
        new IllegalArgumentException(
          "Called WhiskerTestSuiteRunner.runTests even when 'runTestSuite' config is disabled!"
        )
      )
    }
  }

  /** If the result has no failed tests, copies the temporary file `program` to
    * a persistent location and adds the Scratch program to the list of
    * solutions in the `hintPipeline`.
    * @param program
    *   the student program under test.
    * @param result
    *   of the test execution.
    */
  private def addToSolutions(
      taskId: TaskId,
      program: Path,
      result: TestResult
  ): Unit = {
    if (acceptNewSolutions) {
      (result, receivedSolutionsDir) match {
        case (TestResult(_, FailedTests(0)), Some(recvDir)) =>
          val newProgram = copyFileToPermanentDirectory(program, recvDir)
          newProgram.program match {
            case Success(p) =>
              tasks.hintGenerator(taskId).foreach(_.addSolution(p))
            case Failure(err) =>
              log.warn(s"Cannot read student program $program from file.", err)
          }
        case (_, None) =>
          log.warn(
            "Cannot save correct student programs if no directory where they should be stored is defined!"
          )
        case _ => // do nothing
      }
    }
  }
}
