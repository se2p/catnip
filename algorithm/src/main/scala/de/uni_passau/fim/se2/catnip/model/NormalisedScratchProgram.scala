package de.uni_passau.fim.se2.catnip.model

import de.uni_passau.fim.se2.catnip.normalisation.NormalisationVisitor
import de.uni_passau.fim.se2.catnip.util.ASTNodeExtTyped
import de.uni_passau.fim.se2.litterbox.ast.model.Program

/** Wrapper class for a [[ScratchProgram]] that has been normalised.
  * @param program
  *   a program as parsed by LitterBox.
  * @param variableNameMap
  *   a map from original variable name to the one after normalisation.
  */
class NormalisedScratchProgram private (
    program: Program,
    val variableNameMap: Map[String, String]
) extends ScratchProgram(program)

object NormalisedScratchProgram {
  def apply(program: Program): NormalisedScratchProgram = {
    val p                    = program.cloned
    val (normalised, varMap) = NormalisationVisitor.applyWithVariableMap(p)
    new NormalisedScratchProgram(normalised, varMap)
  }
}
