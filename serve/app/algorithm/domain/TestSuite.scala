package algorithm.domain

import config.TaskId

import java.nio.file.Path
import scala.util.Try

trait TestSuite {
  def runTests(taskId: TaskId, program: Path): Try[TestResult]
}
