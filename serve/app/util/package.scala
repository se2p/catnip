import com.typesafe.config.Config
import play.api.ConfigLoader

import java.nio.file.{Files, Path, StandardCopyOption}

package object util {

  /** Used to convert a string configuration parameter from `application.conf`
    * directly to a path object.
    */
  implicit val pathLoader: ConfigLoader[Path] = new ConfigLoader[Path] {
    override def load(config: Config, path: String): Path = {
      Path.of(config.getString(path)).toAbsolutePath
    }
  }

  /** Copy the student program from the temporary files location to the
    * permanent storage location.
    * @param program
    *   the student program as received.
    * @return
    *   the new location of the program.
    */
  def copyFileToPermanentDirectory(program: Path, targetDir: Path): Path = {
    val target = targetDir.resolve(program.getFileName)
    Files.copy(program, target, StandardCopyOption.REPLACE_EXISTING)
  }
}
