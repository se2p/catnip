package config

import algorithm.HintPipelineWrapper
import play.api.{Configuration, Logger}
import util.pathLoader

import java.nio.file.{Files, Path}
import javax.inject.{Inject, Singleton}
import scala.jdk.OptionConverters.*

trait KnownTaskService {
  val tasks: Map[TaskId, Task]

  def task(taskId: TaskId): Option[Task]

  def taskStartupProgram(taskId: TaskId): Option[Path]

  def taskSolutionsPath(taskId: TaskId): Option[Path]

  def hintGenerator(taskId: TaskId): Option[HintPipelineWrapper]
}

case class TaskId(value: String) extends AnyVal

/** A Scratch programming exercise
  *
  * @param id
  *   of the exercise.
  * @param folder
  *   where all files for this task are stored. Usually contains:
  *   - A `sb3`-file that should be used as starting point for students.
  *   - A folder `solutions`: example solutions to use when generating hints.
  */
case class Task(id: TaskId, folder: Path)

@Singleton
class KnownTasks @Inject() (configuration: Configuration)
    extends KnownTaskService {
  private val log = Logger(this.getClass)

  private val SOLUTION_DIR: Path = Path.of("solutions")

  private val basePath: Path = configuration.get("hintGenerator.tasksBasePath")

  override val tasks: Map[TaskId, Task] = {
    configuration
      .get[Seq[String]]("hintGenerator.tasks")
      .map(id => {
        val task = Task(TaskId(id), basePath.resolve(id))
        val solutionCount = Files
          .walk(task.folder.resolve(SOLUTION_DIR))
          .filter(isSb3File)
          .count()

        log.info(
          s"Loaded task with id ${task.id} and data folder ${task.folder} ($solutionCount solutions)."
        )

        task
      })
      .map(task => task.id -> task)
      .toMap
  }

  private val hintGenerators: Map[TaskId, HintPipelineWrapper] = {
    tasks
      .flatMap { case (taskId, _) =>
        taskSolutionsPath(taskId).map((taskId, _))
      }
      .map { case (taskId, solutionsPath) =>
        (taskId, new HintPipelineWrapper(solutionsPath))
      }
  }

  override def task(taskId: TaskId): Option[Task] = tasks.get(taskId)

  override def taskStartupProgram(taskId: TaskId): Option[Path] = {
    tasks.get(taskId).flatMap { task =>
      Files
        .walk(task.folder, 1)
        .filter(isSb3File)
        .findFirst()
        .toScala
    }
  }

  override def taskSolutionsPath(taskId: TaskId): Option[Path] = {
    tasks.get(taskId).map(_.folder.resolve(SOLUTION_DIR))
  }

  override def hintGenerator(taskId: TaskId): Option[HintPipelineWrapper] = {
    hintGenerators.get(taskId)
  }

  private def isSb3File(path: Path): Boolean = {
    path.toFile.isFile && path.getFileName.toString.endsWith(".sb3")
  }
}
