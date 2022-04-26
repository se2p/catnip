package de.uni_passau.fim.se2.catnip.common

import cats.effect.IO
import cats.effect.kernel.Resource
import cats.effect.unsafe.implicits.global as catsGlobalEC
import cats.implicits.*
import org.slf4j.LoggerFactory

import java.nio.file.Path
import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.{Duration, MINUTES}
import scala.sys.process.{stringSeqToProcess, Process, ProcessLogger}
import scala.util.Try
import scala.util.matching.Regex

/** Contains the number of tests per category.
  * @param tests
  *   total number of tests.
  * @param pass
  *   successful tests.
  * @param fail
  *   unsuccessful tests.
  * @param error
  *   tests which resulted in an error.
  * @param skip
  *   not executed tests.
  */
final case class WhiskerResult(
    tests: Int,
    pass: Int,
    fail: Int,
    error: Int,
    skip: Int
)

/** Helper class to run a Whisker test suite in headless mode.
  * @param servant
  *   path to the `servant.js` file that is part of Whisker.
  * @param testFile
  *   the test suite to use when testing the programs.
  * @param accelerationFactor
  *   a factor by how much the execution of the Scratch program is accelerated.
  *   Has to be at least one, higher than ten is not recommended.
  * @param parallelExecutions
  *   the number of tabs that execute the tests in parallel. Has to be at least
  *   one.
  */
class WhiskerRunner private (
    servant: Path,
    testFile: Path,
    accelerationFactor: Int,
    parallelExecutions: Int
) {
  private val logger = LoggerFactory.getLogger(this.getClass)

  // …/whisker/servant/servant.js -> …/whisker/servant/
  private val whiskerDirectory = servant.getParent

  private val cmd = Seq(
    "bash",
    "-c",
    s"cd $whiskerDirectory && node $servant -d -a $accelerationFactor -p $parallelExecutions -s %program -t $testFile"
  )

  /** Try to run the Whisker test suite on `program`.
    *
    * Fails if `program` is not a `.sb3` file, or if Whisker itself returns with
    * non-zero exit code.
    * @param program
    *   some `*.sb3`-file that should be tested.
    * @return
    *   the output of the Whisker run.
    */
  def run(program: Path): Try[WhiskerResult] = {
    logger.info(s"Starting test suite for $program.")

    Try {
      require(
        program.toString.endsWith(".sb3"),
        "Program has to be a sb3-File!"
      )

      val output = ListBuffer.empty[String]
      val process = for {
        l <- processLogRes(output)
        p <- processRes(buildShellCommand(program), l)
      } yield p

      process
        .use { p =>
          IO.fromFuture(IO(Future(p.exitValue())))
        }
        .unsafeRunTimed(Duration(2, MINUTES))(catsGlobalEC) match {
        case Some(_) =>
          extractWhiskerResult(output.mkString("\n")) match {
            case Some(value) => value
            case None =>
              throw new UnsupportedOperationException(
                "Could not parse Whisker result!"
              )
          }
        case None =>
          throw new UnsupportedOperationException(
            "Execution of the test suite timed out after two minutes!"
          )
      }
    }
  }

  private def processLogRes(
      output: ListBuffer[String]
  ): Resource[IO, ProcessLogger] = {
    Resource.make {
      IO.blocking(new ProcessLogger {
        override def out(s: => String): Unit = {
          println(s)
          output.append(s)
        }

        override def err(s: => String): Unit = {
          Console.err.println(s)
          output.append(s)
        }

        override def buffer[T](f: => T): T = f
      })
    } { _ =>
      IO(())
    }
  }

  private def processRes(
      shellCommand: Seq[String],
      log: ProcessLogger
  ): Resource[IO, Process] = {
    Resource.make {
      IO.blocking(shellCommand.run(log))
    } { p =>
      IO.blocking(p.destroy())
    }
  }

  private def buildShellCommand(program: Path): Seq[String] = {
    cmd.map(_.replace("%program", program.toString))
  }

  def extractWhiskerResult(shellOutput: String): Option[WhiskerResult] = {
    def buildPattern(prefix: String): Regex = {
      val regex = s"#\\s{3}$prefix: (\\d+)"
      regex.r
    }
    def find(prefix: String): Option[Int] = {
      buildPattern(prefix).findFirstMatchIn(shellOutput).map(_.group(1).toInt)
    }

    for {
      tests <- find("tests")
      pass  <- find("pass")
      fail  <- find("fail")
      error <- find("error")
      skip  <- find("skip")
    } yield WhiskerResult(tests, pass, fail, error, skip)
  }
}

object WhiskerRunner {
  type ErrorMessage = String

  def apply(
      servant: Path,
      testFile: Path,
      accelerationFactor: Int = 4,
      parallelExecutions: Int = 2
  ): Either[List[ErrorMessage], WhiskerRunner] = {
    val validateServant = {
      if (servant.endsWith("servant.js")) servant.validNec
      else "The Whisker executable has to be called servant.js.".invalidNec
    }
    val validateTestFile = {
      if (testFile.toString.endsWith(".js")) testFile.validNec
      else "Test file has to be a JavaScript file.".invalidNec
    }
    val validateAcc = {
      if (accelerationFactor >= 1) accelerationFactor.validNec
      else "Acceleration factor has to be >= 1!".invalidNec
    }
    val validatePar = {
      if (parallelExecutions >= 1) parallelExecutions.validNec
      else "Number of parallel executions has to be >= 1!".invalidNec
    }

    (validateServant, validateTestFile, validateAcc, validatePar)
      .mapN(new WhiskerRunner(_, _, _, _))
      .toEither
      .leftMap(_.toList)
  }
}
