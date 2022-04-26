package de.uni_passau.fim.se2.catnip.common

import de.uni_passau.fim.se2.catnip.common.defs.UnitSpec
import org.scalatest.{EitherValues, TryValues}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

import java.nio.file.Path

class WhiskerRunnerSpec
    extends UnitSpec
    with ScalaCheckPropertyChecks
    with TryValues
    with EitherValues {
  "The Whisker Runner result parser" should "be able to extract the test failures from shell output" in {
    val expected = WhiskerResult(1, 1, 0, 0, 0)
    val parsed   = runner.extractWhiskerResult(validShellOutput(expected))
    parsed shouldBe Some(expected)
  }

  it should "accept randomly generated valid shell outputs" in {
    val c = (_: Int) != Integer.MIN_VALUE

    forAll { (tests: Int, pass: Int, fail: Int, error: Int, skip: Int) =>
      whenever(c(tests) && c(pass) && c(fail) && c(error) && c(skip)) {
        val expected =
          WhiskerResult(tests.abs, pass.abs, fail.abs, error.abs, skip.abs)
        val parsed = runner.extractWhiskerResult(validShellOutput(expected))
        parsed shouldBe Some(expected)
      }
    }
  }

  it should "not parse a shell output with negative total test number" in {
    val expected = WhiskerResult(-1, 1, 0, 0, 0)
    val parsed   = runner.extractWhiskerResult(validShellOutput(expected))
    parsed shouldBe None
  }

  it should "not parse a shell output with negative skipped test number" in {
    val expected = WhiskerResult(2, 1, 0, 0, -1)
    val parsed   = runner.extractWhiskerResult(validShellOutput(expected))
    parsed shouldBe None
  }

  "The Whisker Runner" should "only accept a path ending in servant.js as servant" in {
    val runner =
      WhiskerRunner(Path.of("some", "path", "rand.js"), Path.of("tests.js"))
    runner.left.value should contain only "The Whisker executable has to be called servant.js."
  }

  it should "only accept JavaScript files a test files" in {
    val runner = WhiskerRunner(
      Path.of("some", "path", "servant.js"),
      Path.of("tests.ts")
    )
    runner.left.value should contain only "Test file has to be a JavaScript file."
  }

  it should "only accept sb3 program files" in {
    val w   = runner
    val res = w.run(Path.of("some", "path", "not.sb3.exe"))
    res.failure.exception should have message "requirement failed: Program has to be a sb3-File!"
  }

  it should "only accept acceleration factors >= 1" in {
    val runner =
      WhiskerRunner(Path.of("some", "servant.js"), Path.of("test.js"), 0)
    runner.left.value should contain only "Acceleration factor has to be >= 1!"
  }

  it should "only accept parallelization factors >= 1" in {
    val runner =
      WhiskerRunner(Path.of("some", "servant.js"), Path.of("test.js"), 1, 0)
    runner.left.value should contain only "Number of parallel executions has to be >= 1!"
  }

  it should "validate multiple parameters at the same time" in {
    val runner =
      WhiskerRunner(Path.of("some", "servantx.js"), Path.of("test.ts"), 0, 0)
    runner.left.value should contain theSameElementsAs List(
      "The Whisker executable has to be called servant.js.",
      "Test file has to be a JavaScript file.",
      "Acceleration factor has to be >= 1!",
      "Number of parallel executions has to be >= 1!"
    )
  }

  it should "fail in the test environment as no actual Whisker is present" in {
    val w   = runner
    val res = w.run(Path.of("valid", "some.sb3"))
    res.isFailure shouldBe true
  }

  def runner: WhiskerRunner = {
    WhiskerRunner(Path.of("servant.js"), Path.of("tests.js")).toOption.get
  }

  def validShellOutput(res: WhiskerResult): String = {
    s"""|ok 1 - Generated Test
        |# summary:
        |#   tests: ${res.tests}
        |#   pass: ${res.pass}
        |#   fail: ${res.fail}
        |#   error: ${res.error}
        |#   skip: ${res.skip}
        |# coverage:""".stripMargin
  }
}
