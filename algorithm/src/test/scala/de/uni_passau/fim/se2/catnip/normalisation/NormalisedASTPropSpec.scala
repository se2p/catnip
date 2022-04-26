package de.uni_passau.fim.se2.catnip.normalisation

import de.uni_passau.fim.se2.catnip.common.FileFinder
import de.uni_passau.fim.se2.catnip.common.defs.PropSpec
import de.uni_passau.fim.se2.catnip.model.{NodeRootPath, ScratchProgram}
import de.uni_passau.fim.se2.catnip.util.{ASTNodeExt, NodeListVisitor}
import de.uni_passau.fim.se2.litterbox.ast.parser.Scratch3Parser
import de.uni_passau.fim.se2.litterbox.jsonCreation.JSONFileCreator
import org.scalatest.BeforeAndAfterEach

import java.io.IOException
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.{Files, FileVisitor, FileVisitResult, Path}
import scala.jdk.CollectionConverters.*

class NormalisedASTPropSpec extends PropSpec with BeforeAndAfterEach {
  val TEMP_PATH: Path = Path
    .of(System.getProperty("java.io.tmpdir"), "HintGenTestRun")
  val EXAMPLES_FOLDER = "example_programs"

  override def beforeEach(): Unit = Files.createDirectories(TEMP_PATH)

  override def afterEach(): Unit = deleteDirectory(TEMP_PATH)

  property("All simple files should be normalised after one pass") {
    val simpleFiles = Table("path") ++
      FileFinder.matchingSb3Files(EXAMPLES_FOLDER, "simple_.*".r)
    simpleFiles.length should be > 0
    forAll(simpleFiles) { path => verifyNormalisedOnlyRead(path) }
  }

  property(
    "All normalised simple files should still be normalised after reading"
  ) {
    val simpleFiles = Table("path") ++
      FileFinder.matchingSb3Files(EXAMPLES_FOLDER, "simple_.*".r)
    simpleFiles.length should be > 0
    forAll(simpleFiles) { path => verifyNormalisedReadWriteRead(path) }
  }

  property("All files with NumExpr should be normalised after one pass") {
    val files = Table("path") ++
      FileFinder
        .matchingSb3Files(EXAMPLES_FOLDER, "num_expr_normalise_test_.*".r)
    files.length should be > 0
    forAll(files) { path => verifyNormalisedOnlyRead(path) }
  }

  property("All files with NumExpr should still be normalised after reading") {
    val files = Table("path") ++
      FileFinder
        .matchingSb3Files(EXAMPLES_FOLDER, "num_expr_normalise_test_.*".r)
    files.length should be > 0
    forAll(files) { path => verifyNormalisedReadWriteRead(path) }
  }

  property(
    "All root paths of nodes should still have the program as root after normalising"
  ) {
    val files = FileFinder.matchingSb3Files(EXAMPLES_FOLDER, "num_expr.*".r)
    files.length should be > 0

    forAll(Table("path") ++ files) { path =>
      val parser  = new Scratch3Parser
      val program = parser.parseSB3File(path.toFile)

      // normalise program
      val normalised = NormalisationVisitor(program)

      val nodes = NodeListVisitor(ScratchProgram(normalised), !_.isMetadata)
      forAll(Table("nodes") ++ nodes) { node =>
        val rp = NodeRootPath(node)
        withClue(rp) {
          rp.head.node shouldEqual normalised
        }
      }
    }
  }

  def verifyNormalisedOnlyRead(path: Path): Unit = {
    val parser  = new Scratch3Parser
    val program = parser.parseSB3File(path.toFile)

    // normalise program
    val normalised = NormalisationVisitor(program)

    // check that it is fully normalised
    val v = new NormalisedCheckVisitor(
      normalised.getSymbolTable.getVariables.asScala.toMap
    )
    normalised.accept(v)
  }

  def verifyNormalisedReadWriteRead(path: Path): Unit = {
    val parser  = new Scratch3Parser
    val program = parser.parseSB3File(path.toFile)

    // normalise program
    val normalised = NormalisationVisitor(program)

    JSONFileCreator
      .writeSb3FromProgram(normalised, TEMP_PATH.toString, path.toFile)

    val newFilename = path.getFileName.toString
      .replace(".sb3", "_annotated.sb3")
    val newPath  = Path.of(TEMP_PATH.toString, newFilename)
    val program2 = parser.parseSB3File(newPath.toFile)

    // check that it is fully normalised
    val v = new NormalisedCheckVisitor(
      program2.getSymbolTable.getVariables.asScala.toMap
    )
    program2.accept(v)
  }

  def deleteDirectory(dir: Path): Unit = {
    Files.walkFileTree(
      dir,
      new FileVisitor[Path]() {
        override def preVisitDirectory(
            dir: Path,
            attrs: BasicFileAttributes
        ): FileVisitResult = FileVisitResult.CONTINUE

        override def visitFile(
            file: Path,
            attrs: BasicFileAttributes
        ): FileVisitResult = {
          Files.delete(file)
          FileVisitResult.CONTINUE
        }

        override def visitFileFailed(
            file: Path,
            exc: IOException
        ): FileVisitResult = FileVisitResult.CONTINUE

        override def postVisitDirectory(
            dir: Path,
            exc: IOException
        ): FileVisitResult = {
          Files.delete(dir)
          FileVisitResult.CONTINUE
        }
      }
    )
  }
}
