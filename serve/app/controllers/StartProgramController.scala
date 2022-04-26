package controllers

import config.{KnownTasks, TaskId}
import play.api.mvc.{
  AbstractController,
  Action,
  AnyContent,
  ControllerComponents
}

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class StartProgramController @Inject() (
    knownTasks: KnownTasks,
    components: ControllerComponents
)(implicit ec: ExecutionContext)
    extends AbstractController(components) {

  /** Finds the initial program for students based on the task identification.
    * @param taskId
    *   of the task for which an initial program is fetched.
    * @return
    *   a sb3-file.
    */
  def get(taskId: Option[String]): Action[AnyContent] = Action { _ =>
    val sb3File = taskId.map(TaskId(_)).flatMap(knownTasks.taskStartupProgram)
    sb3File match {
      case Some(path) => Ok.sendPath(path).as("application/x.scratch.sb3")
      case None =>
        InternalServerError("Unknown Task, cannot send start configuration!")
    }
  }
}
