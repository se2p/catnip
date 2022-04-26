package controllers.model_converters

import de.uni_passau.fim.se2.catnip.hint_application.StructuralHintApplicator
import de.uni_passau.fim.se2.catnip.hint_generation.model.HintGenerationResult
import de.uni_passau.fim.se2.catnip.model.{
  BlockId,
  DeletionHint,
  Hint,
  InsertInFieldHint,
  InsertionHint,
  MissingActorHint,
  MoveInFieldHint,
  MoveToPosHint,
  ReorderHint,
  ReplaceFieldHint,
  ReplaceStmtHint,
  StructuralHint,
  StudentNode
}
import de.uni_passau.fim.se2.catnip.util.{ActorDefinitionExt, ASTNodeExt}
import de.uni_passau.fim.se2.litterbox.ast.model.{ASTNode, Script, StmtList}
import io.circe.*
import io.circe.generic.auto.*
import io.circe.syntax.*
import play.api.Logger
import play.api.i18n.{I18nSupport, Lang, Langs, MessagesApi}
import play.api.libs.json.{Json, JsValue, Writes}

import javax.inject.{Inject, Singleton}
import scala.annotation.tailrec

final case class Actor(name: String)                     extends AnyVal
final case class HintId(value: Int)                      extends AnyVal
final case class HintExplanationComment(comment: String) extends AnyVal

final case class Hints(hints: List[ActorHint])
final case class ActorHint(
    actorName: Actor,
    comments: List[BlockComment],
    explanations: List[HintExplanation]
)
final case class BlockComment(
    blockId: BlockId,
    hintIds: List[HintId],
    additionalRemarks: Option[String] = None
)

object BlockComment {
  def apply(
      blockId: BlockId,
      hintId: HintId,
      additionalRemarks: Option[String]
  ): BlockComment = {
    new BlockComment(blockId, List(hintId), additionalRemarks)
  }

  def apply(
      blockId: BlockId,
      hintIds: List[HintId],
      additionalRemarks: Option[String]
  ): BlockComment = {
    new BlockComment(blockId, hintIds, additionalRemarks)
  }
}

final case class HintExplanation(
    hintId: HintId,
    hintType: String,
    explanation: HintExplanationComment,
    scratchBlocks: Option[ScratchBlocksFigure]
)

@Singleton
class HintGenerationResultConverter @Inject() (langs: Langs)(implicit
    val messagesApi: MessagesApi
) extends I18nSupport {
  private val log = Logger(this.getClass)

  implicit val lang: Lang =
    langs.availables.headOption.getOrElse(Lang.defaultLang)

  implicit val keyEncoderActor: KeyEncoder[Actor] = KeyEncoder.instance(_.name)

  implicit val hintGenerationResultWrites: Writes[HintGenerationResult] =
    new Writes[HintGenerationResult]() {
      override def writes(result: HintGenerationResult): JsValue = {
        val hints = buildHintsResponse(result)
        Json.parse(hints.asJson.noSpaces)
      }
    }

  implicit val hintsWrites: Writes[Hints] = (h: Hints) => {
    Json.parse(h.asJson.noSpaces)
  }

  def buildHintsResponse(result: HintGenerationResult): Hints = {
    val (hintsWithActor, hintsWithoutActor) = result.sortedByActor
    val hints = transformHints(hintsWithActor).sortBy(_.actorName.name)

    if (hintsWithoutActor.isEmpty) {
      Hints(hints)
    } else {
      val missingActorHint = generateActorHints(
        messagesApi("hints.genericActorName"),
        hintsWithoutActor
      )
      Hints(missingActorHint :: hints)
    }
  }

  private def transformHints(
      hints: Map[String, List[Hint]]
  ): List[ActorHint] = {
    hints.map { case (actor, actorHints) =>
      generateActorHints(actor, actorHints)
    }.toList
  }

  private def generateActorHints(
      actorName: String,
      hints: List[Hint]
  ): ActorHint = {
    val comments     = generateComments(hints)
    val explanations = generateExplanations(hints)
    ActorHint(Actor(actorName), comments, explanations)
  }

  private def generateComments(hints: List[Hint]): List[BlockComment] = {
    def commentsForMoveHint(
        idx: Int,
        nodeToMove: ASTNode,
        newParent: ASTNode
    ): List[BlockComment] = {
      val hintId = HintId(idx)

      def blockComment(id: BlockId, msgTag: String) = {
        BlockComment(id, hintId, Some(messagesApi(msgTag)))
      }

      val commentOldBlock = closestBlockId(nodeToMove).map(
        blockComment(_, "hints.moveHint.remarkMoveFrom")
      )
      val commentNewBlock = closestBlockId(newParent).map(
        blockComment(_, "hints.moveHint.remarkMoveTo")
      )
      List(commentOldBlock, commentNewBlock).flatten
    }

    def comment(node: ASTNode, hintId: Int): Option[BlockComment] = {
      closestBlockId(node).map(BlockComment(_, HintId(hintId), None))
    }

    val comments = hints.zipWithIndex
      .map { case (h, idx) => (h, idx + 1) }
      .flatMap { case (hint, idx) =>
        hint match {
          case InsertionHint(StudentNode(parent: StmtList), _, _) =>
            comment(parent, idx)
          case MoveToPosHint(StudentNode(newParent), StudentNode(node), _) =>
            commentsForMoveHint(idx, node, newParent)
          case MoveInFieldHint(StudentNode(newParent), StudentNode(node), _) =>
            commentsForMoveHint(idx, node, newParent)
          case ReorderHint(_, StudentNode(node), _) => comment(node, idx)
          case ReplaceStmtHint(_, StudentNode(oldNode), _, _) =>
            comment(oldNode, idx)
          case _ => comment(hint.references.node, idx)
        }
      }

    deduplicateComments(comments)
  }

  /** Merges the contents of multiple comments referencing the same block into a
    * single comment containing both contents.
    * @param comments
    *   some comments to be attached to Scratch blocks.
    * @return
    *   a deduplicated list of comments as per definition above.
    */
  private def deduplicateComments(
      comments: List[BlockComment]
  ): List[BlockComment] = {
    comments
      .groupMapReduce(_.blockId)(identity) { case (commentA, commentB) =>
        val remarks =
          (commentA.additionalRemarks, commentB.additionalRemarks) match {
            case (None, None) => None
            case (a, b)       => Some(List(a, b).flatten.mkString("; "))
          }

        BlockComment(
          commentA.blockId,
          commentA.hintIds ++ commentB.hintIds,
          remarks
        )
      }
      .values
      .toList
  }

  private def generateExplanations(hints: List[Hint]): List[HintExplanation] = {
    def hintType(hint: Hint): String = {
      def msg(t: String): String = messagesApi(s"hints.$t.type")

      hint match {
        case _: DeletionHint      => msg("deletionHint")
        case _: InsertionHint     => msg("insertionHint")
        case _: InsertInFieldHint => msg("insertInFieldHint")
        case _: MoveToPosHint     => msg("moveToPosHint")
        case _: MoveInFieldHint   => msg("moveInFieldHint")
        case _: ReorderHint       => msg("reorderHint")
        case _: ReplaceFieldHint  => msg("replaceFieldHint")
        case _: ReplaceStmtHint   => msg("replaceStmtHint")
        case _: MissingActorHint  => msg("missingActorHint")
      }
    }

    hints.zipWithIndex
      .map { case (h, idx) => (h, idx + 1) }
      .map { case (hint, idx) =>
        val comment = explanationComment(hint)
        val figure  = scratchBlocksFigure(hint)
        HintExplanation(HintId(idx), hintType(hint), comment, figure)
      }
  }

  private def explanationComment(hint: Hint): HintExplanationComment = {
    val msg = messagesApi

    def hintMsgId(hintType: String): String = s"hints.$hintType.explanation"

    def nodeName(node: ASTNode): String = node.getClass.getSimpleName

    val comment = hint match {
      case DeletionHint(StudentNode(node)) =>
        msg(hintMsgId("deletionHint"), nodeName(node))
      case InsertionHint(_, node, position) =>
        msg(hintMsgId("insertionHint"), nodeName(node), position + 1)
      case InsertInFieldHint(_, node, fieldName) =>
        msg(hintMsgId("insertInFieldHint"), nodeName(node), fieldName)
      case MoveToPosHint(_, StudentNode(node), position) =>
        msg(hintMsgId("moveToPosHint"), nodeName(node), position + 1)
      case MoveInFieldHint(_, StudentNode(node), fieldName) =>
        msg(hintMsgId("moveInFieldHint"), nodeName(node), fieldName)
      case ReorderHint(parent, StudentNode(node), newPosition) =>
        val oldPos = parent.node.getChildren.indexOf(node)
        msg(hintMsgId("reorderHint"), oldPos + 1, newPosition + 1)
      case ReplaceFieldHint(_, fieldName, newNode) =>
        msg(hintMsgId("replaceFieldHint"), fieldName, nodeName(newNode))
      case ReplaceStmtHint(_, StudentNode(oldNode), newNode, index) =>
        msg(
          hintMsgId("replaceStmtHint"),
          nodeName(oldNode),
          nodeName(newNode),
          index + 1
        )
      case MissingActorHint(_, actor) =>
        msg(hintMsgId("missingActorHint"), actor.name)
    }

    HintExplanationComment(comment)
  }

  private def scratchBlocksFigure(hint: Hint): Option[ScratchBlocksFigure] = {
    hint match {
      case _: MissingActorHint => None
      case h => Some(ScratchBlocksBuilder(chooseNodeToShow(h)))
    }
  }

  private def chooseNodeToShow(hint: Hint): ASTNode = {
    hint match {
      case InsertionHint(_, script: Script, _) => script.getEvent
      case InsertionHint(_, newNode, _)        => newNode
      case ReplaceStmtHint(_, _, newNode, _)   => newNode
      case h: StructuralHint => StructuralHintApplicator(h.references.node, h)
      case h                 => h.references.node
    }
  }

  private def closestBlockId(node: ASTNode): Option[BlockId] = {
    @tailrec
    def inner(n: ASTNode): Option[BlockId] = {
      n.blockIdOpt match {
        case id @ Some(_)                   => id
        case None if n.parentNode.isDefined => inner(n.getParentNode)
        case None                           => None
      }
    }

    inner(node)
  }
}
