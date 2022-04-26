package algorithm.domain

import de.uni_passau.fim.se2.catnip.common.WhiskerResult

final case class SuccessfulTests(count: Int) extends AnyVal
final case class FailedTests(count: Int)     extends AnyVal

final case class TestResult(successful: SuccessfulTests, failed: FailedTests)

object TestResult {

  /** Converts a Whisker result into a more generic test result.
    *
    * Only failed tests and ones that errored will be considered as failed.
    * Skipped tests will not be considered to have failed.
    * @param whiskerResult
    *   some Whisker test run result.
    * @return
    *   a [[TestResult]] that has the same number of passed and failed tests as
    *   the Whisker result.
    */
  def apply(whiskerResult: WhiskerResult): TestResult = {
    val success = SuccessfulTests(whiskerResult.pass)
    val failed  = FailedTests(whiskerResult.fail + whiskerResult.error)
    TestResult(success, failed)
  }
}
