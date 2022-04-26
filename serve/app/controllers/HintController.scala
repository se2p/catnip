package controllers

import akka.actor.ActorSystem
import algorithm.WhiskerTestSuiteRunner
import algorithm.domain.{FailedTests, TestResult}
import config.{KnownTasks, TaskId}
import controllers.model_converters.ProgramConverters.ProgramFromPath
import controllers.model_converters.{HintGenerationResultConverter, Hints}
import de.uni_passau.fim.se2.catnip.common.{formatDuration, timed}
import de.uni_passau.fim.se2.catnip.hint_generation.model.HintGenerationResult
import play.api.i18n.{I18nSupport, Lang, Langs}
import play.api.libs.Files
import play.api.libs.json.{Json, JsValue, Writes}
import play.api.mvc.{AbstractController, Action, ControllerComponents, Result}
import play.api.{Configuration, Logger}
import util.pathLoader

import java.nio.file.{Path, Paths}
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

@Singleton
class HintController @Inject() (
    configuration: Configuration,
    components: ControllerComponents,
    akkaSystem: ActorSystem,
    langs: Langs,
    knownTasks: KnownTasks,
    testSuiteRunner: WhiskerTestSuiteRunner,
    hintGenerationResultConverter: HintGenerationResultConverter
)(implicit ec: ExecutionContext)
    extends AbstractController(components)
    with I18nSupport {

  private val log = Logger(this.getClass)

  implicit private val lang: Lang =
    langs.availables.headOption.getOrElse(Lang.defaultLang)

  private val runTestSuite =
    configuration.get[Boolean]("hintGenerator.runTestSuite")

  private val receivedProgramsStoreDir =
    configuration.get[Option[Path]]("hintGenerator.receivedProgramsStoreDir")

  private val hintGenEC: ExecutionContext =
    akkaSystem.dispatchers.lookup("hintGen-context")
  private val testSuiteEC: ExecutionContext =
    akkaSystem.dispatchers.lookup("testSuite-context")

  case class HintGenResponse(
      testsSuccessful: Boolean,
      testsSuccessfulMessage: Option[String],
      hints: Option[Hints]
  )

  object HintGenResponse {
    implicit val jsonWrites: Writes[HintGenResponse] =
      new Writes[HintGenResponse] {
        import hintGenerationResultConverter.hintsWrites

        override def writes(r: HintGenResponse): JsValue = {
          Json.obj(
            "testsSuccessful"        -> r.testsSuccessful,
            "testsSuccessfulMessage" -> r.testsSuccessfulMessage,
            "hints"                  -> Json.toJson(r.hints)
          )
        }
      }

    def apply(
        testsSuccess: Boolean,
        successMessage: String
    ): HintGenResponse = {
      new HintGenResponse(testsSuccess, Some(successMessage), None)
    }

    def apply(hints: HintGenerationResult): HintGenResponse = {
      val res = hintGenerationResultConverter.buildHintsResponse(hints)
      new HintGenResponse(false, None, Some(res))
    }
  }

  /** Gets a Scratch 3 program in sb3-format as body.
    * @return
    *   a list of JSON-encoded hints, or just a message that all unit tests have
    *   passed.
    */
  def requestHints(taskId: String): Action[Files.TemporaryFile] = {
    implicit val hintsToJsonConverter: Writes[HintGenerationResult] =
      hintGenerationResultConverter.hintGenerationResultWrites

    Action(parse.temporaryFile).async { request =>
      val task        = TaskId(taskId)
      val programPath = sb3FromTempFile(request.body)

      for {
        _                <- Future(saveReceivedProgram(task, programPath))
        testSuiteSuccess <- testSuiteResult(task, programPath)
        hintGenResult    <- hintGenerationResult(task, programPath)
      } yield {
        if (testSuiteSuccess) {
          log.info(
            s"All tests successful. No hint generation for program $programPath."
          )
          noHintGenerationNeeded()
        } else {
          sendHintGenResult(hintGenResult)
        }
      }
    }
  }

  private def testSuiteResult(
      taskId: TaskId,
      programPath: Path
  ): Future[Boolean] = {
    if (runTestSuite) {
      Future {
        val (testResult, time) = timed { testsSuccessful(taskId, programPath) }
        log.info(s"Ran tests for $programPath in ${formatDuration(time)}.")
        testResult
      }(testSuiteEC)
    } else {
      Future(false)
    }
  }

  private def hintGenerationResult(
      taskId: TaskId,
      programPath: Path
  ): Future[Try[HintGenerationResult]] = {
    Future {
      val (hintResult, time) = timed {
        hintGeneration(taskId, programPath)
      }
      log.info(
        s"Generated hints for $programPath in ${formatDuration(time)}."
      )
      hintResult
    }(hintGenEC)
  }

  private def sendHintGenResult(
      hintGenResult: Try[HintGenerationResult]
  ): Result = {
    hintGenResult.map(HintGenResponse(_)) match {
      case Success(hints) => Ok(Json.toJson(hints))
      case Failure(err) =>
        log.error("Hint generation failed!", err)
        InternalServerError(
          "Received a damaged Scratch 3 program. Cannot generate hints!"
        )
    }
  }

  private def noHintGenerationNeeded(): Result = {
    val response = HintGenResponse(
      testsSuccess = true,
      messagesApi("response.programAlreadyCorrect")
    )
    Ok(Json.toJson(response))
  }

  private def hintGeneration(
      taskId: TaskId,
      programPath: Path
  ): Try[HintGenerationResult] = {
    programPath.program match {
      case Failure(err) =>
        log.error(s"Invalid Scratch 3 program: $programPath", err)
        Failure(err)
      case Success(program) =>
        log.info(s"Generating hints for program $programPath.")
        knownTasks
          .hintGenerator(taskId)
          .map(_.generateHints(program))
          .map(Success(_))
          .getOrElse(
            Failure(
              new NoSuchElementException(
                s"Task with id $taskId has no hint generator!"
              )
            )
          )
    }
  }

  private def testsSuccessful(taskId: TaskId, program: Path): Boolean = {
    testSuiteRunner.runTests(taskId, program) match {
      case Success(TestResult(_, FailedTests(0))) =>
        log.info(s"Program $program passed all tests.")
        true
      case Success(testResult) =>
        log.info(s"Program $program did not pass all tests ($testResult).")
        false
      case Failure(_) =>
        false
    }
  }

  private def sb3FromTempFile(t: Files.TemporaryFile): Path = {
    val newPath = Paths.get(s"${t.path.toString}.sb3")
    t.moveTo(newPath, replace = true)
  }

  private def saveReceivedProgram(taskId: TaskId, t: Path): Unit = {
    import java.nio.file.Files

    receivedProgramsStoreDir match {
      case Some(dir) =>
        val newFolder = dir.resolve(taskId.value)
        if (!Files.exists(newFolder)) {
          Files.createDirectories(newFolder)
        }
        val newFileName = newFolder.resolve(t.getFileName)
        Files.copy(t, newFileName)
      case None => // do nothing
    }
  }
}
