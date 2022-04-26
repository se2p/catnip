package controllers.model_converters

import de.uni_passau.fim.se2.litterbox.ast.model.Program
import de.uni_passau.fim.se2.litterbox.ast.parser.Scratch3Parser

import java.nio.file.Path
import scala.util.Try

object ProgramConverters {
  private val parser = new Scratch3Parser

  implicit class ProgramFromPath(path: Path) {
    def program: Try[Program] = {
      // the parser cannot handle parsing multiple programs at the same time
      this.synchronized {
        Try(parser.parseSB3File(path.toFile))
      }
    }
  }
}
