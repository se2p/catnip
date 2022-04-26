package de.uni_passau.fim.se2.catnip.hint_application

import de.uni_passau.fim.se2.catnip.common.FieldSetter
import de.uni_passau.fim.se2.litterbox.ast.model.{ActorDefinition, ASTNode}
import de.uni_passau.fim.se2.catnip.util.{ActorDefinitionExt, ASTNodeExt}
import org.slf4j.LoggerFactory

import java.util
import scala.jdk.CollectionConverters.*

/** Applies a
  * [[de.uni_passau.fim.se2.catnip.model.MissingActorHint MissingActorHint]] to
  * a program.
  */
object MissingActorHintApplicator {
  private val logger = LoggerFactory.getLogger(this.getClass)

  /** Tries to insert the new `actor` into the program `node belongs` to.
    *
    * Does nothing and just returns `node` if,
    *   - `node` does not belong to a program.
    *   - the program `node` belongs to already contains an actor with the same
    *     type and name as `actor`.
    *
    * @param node
    *   any node in a program.
    * @param actor
    *   that should be added to the program `node` belongs to.
    * @tparam A
    *   the concrete type of `node`.
    * @return
    *   `node` unchanged, but with the program it belongs to possibly changed.
    */
  def apply[A <: ASTNode](node: A, actor: ActorDefinition): A = {
    node.program match {
      case None =>
        logger.warn(
          "Could not apply MissingActorHint as the node does not belong to a program!"
        )
      case Some(program) =>
        val actors = program.getActorDefinitionList
        if (actors.getDefinitions.asScala.exists(_.same(actor))) {
          logger.warn(
            "Could not apply MissingActorHint as an identical actor already exists in the program!"
          )
        } else {
          val updatedList = new util.ArrayList(actors.getDefinitions)
          updatedList.add(actor)
          FieldSetter.setField(actors, "actorDefinitionList", updatedList)
        }
    }

    node
  }
}
