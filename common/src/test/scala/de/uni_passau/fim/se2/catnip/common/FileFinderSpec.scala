package de.uni_passau.fim.se2.catnip.common

import de.uni_passau.fim.se2.catnip.common.defs.UnitSpec

class FileFinderSpec extends UnitSpec {
  "The File Finder" should "load the program from file example_programs/simple/simple_if.sb3" in {
    val p =
      FileFinder.loadProgramFromFile("example_programs/simple/simple_if.sb3")
    p.getIdent.getName shouldBe "simple_if"

    val p2 = FileFinder.loadExampleProgramFromFile("simple/simple_if.sb3")
    p2.getIdent.getName shouldBe "simple_if"
  }

  it should "find three files if searching for files containing simple_if in the name" in {
    val ps = FileFinder.matchingSb3Files("", "simple_if.*".r)
    ps should have length 3
  }

  it should "find no files in the regressions folder if regression files are excluded" in {
    val ps = FileFinder.matchingSb3Files("example_programs/regression/", ".*".r)
    ps shouldBe empty
  }

  it should "find a file if the regression files are not excluded" in {
    val ps = FileFinder.matchingSb3Files(
      "example_programs/regression/",
      "horse_solution.*".r,
      includeRegressionTestFiles = true
    )
    ps should have length 1
  }
}
