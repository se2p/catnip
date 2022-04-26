package de.uni_passau.fim.se2.catnip.model

import de.uni_passau.fim.se2.catnip.util.{NodeListVisitor, ProgramExt}
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition
import de.uni_passau.fim.se2.litterbox.ast.model.{
  ActorDefinition,
  ASTNode,
  Program,
  Script
}
import de.uni_passau.fim.se2.litterbox.ast.visitor.{
  CloneVisitor,
  ScratchVisitor,
  Visitable
}

import scala.jdk.CollectionConverters.*

trait ProgramT[T <: Visitable[?]] extends Visitable[T] {
  val program: ScratchProgram

  final override def accept(scratchVisitor: ScratchVisitor): Unit = {
    program.accept(scratchVisitor)
  }
}

final case class StudentProgram(program: ScratchProgram)
    extends ProgramT[StudentProgram] {
  override def accept(cloneVisitor: CloneVisitor): StudentProgram = {
    StudentProgram(program.accept(cloneVisitor))
  }
}

object StudentProgram {
  def apply(program: Program): StudentProgram = {
    StudentProgram(ScratchProgram(program))
  }
}

final case class SolutionProgram(program: ScratchProgram)
    extends ProgramT[SolutionProgram] {
  override def accept(cloneVisitor: CloneVisitor): SolutionProgram = {
    SolutionProgram(program.accept(cloneVisitor))
  }
}

object SolutionProgram {
  def apply(program: Program): SolutionProgram = {
    SolutionProgram(ScratchProgram(program))
  }
}

/** Wrapper class for a Scratch 3 Program with the important elements of the AST
  * directly exposed.
  * @param program
  *   a program as parsed by LitterBox.
  */
case class ScratchProgram(program: Program) extends Visitable[Program] {

  /** The list of *all* scripts contained in the program.
    */
  def scripts: List[Script] =
    program.getActorDefinitionList.getDefinitions.asScala
      .flatMap(_.getScripts.getScriptList.asScala)
      .toList

  /** The list of *all* procedures contained in the program.
    */
  def procedures: List[ProcedureDefinition] =
    program.getActorDefinitionList.getDefinitions.asScala
      .flatMap(_.getProcedureDefinitionList.getList.asScala)
      .toList

  /** All actors of the program mapped by their name.
    */
  def actors: Map[String, ActorDefinition] =
    program.getActorDefinitionList.getDefinitions.asScala
      .map(actor => actor.getIdent.getName -> actor)
      .toMap

  /** The identifier of the program originally obtained from the filename when
    * parsed.
    * @return
    *   the name of the program.
    */
  @inline
  def name: String = program.name

  /** Finds all potentially reachable scripts in the program.
    *
    * LitterBox marks unreachable `Script`s with event type `Never`.
    * @return
    *   all events that do *not* have event type `Never`.
    */
  def reachableScripts: List[Script] = {
    scripts.filter {
      _.getEvent match {
        case _: Never => false
        case _        => true
      }
    }
  }

  /** Finds all `ASTNode`s that are children of scripts of type `Never` and can
    * therefore never be reached during program execution.
    *
    * @return
    *   a list of all unreachable `ASTNode`s in the program.
    */
  def unreachableBlocks: Set[ASTNode] = {
    scripts
      .filter {
        _.getEvent match {
          case _: Never => true
          case _        => false
        }
      }
      .flatMap(NodeListVisitor(_))
      .toSet
  }

  /** Visits only the `scripts` and `procedures`.
    * @param scratchVisitor
    *   some visitor.
    */
  override def accept(scratchVisitor: ScratchVisitor): Unit = {
    scripts.foreach(_.accept(scratchVisitor))
    procedures.foreach(_.accept(scratchVisitor))
  }

  /** Identical to `program.accept(cloneVisitor)`.
    * @param cloneVisitor
    *   some visitor.
    */
  override def accept(cloneVisitor: CloneVisitor): Program = {
    (program.accept(cloneVisitor): @unchecked) match {
      case p: Program => p
    }
  }

  override def toString: String = {
    s"ScratchProgram { program: ${program.getIdent.getName} }"
  }
}
