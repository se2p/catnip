package de.uni_passau.fim.se2.catnip.model

import scala.util.{Random, Try}

/** Represents a Scratch 3 block ID
  * @param id
  *   the internal representation of the ID in Scratch 3 itself.
  */
final case class BlockId private (id: String) extends AnyVal {
  override def toString: String = id
}

object BlockId {

  /** A dummy BlockID that can be used when no proper one is present.
    */
  val dummy = new BlockId("00000000000000000000")

  /** Length of a block ID.
    */
  private val length = 20

  /** Legal characters for block IDs.
    */
  private val charChoices =
    "$!#%()*+,-./:;=?@[]^_`{|}~ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"

  /** Creates a new [[BlockId]] if `id` is in a valid format.
    * @param id
    *   a [[BlockId]] in its inner String form.
    * @return
    *   `id` wrapped in [[BlockId]] if `id` is in a valid format.
    */
  def apply(id: String): Try[BlockId] = {
    Try {
      if (id.length != length) {
        throw new IllegalArgumentException(
          s"BlockId has to have length $length (was ${id.length})!"
        )
      }
      if (!id.forall(charChoices.contains(_))) {
        throw new IllegalArgumentException(
          s"All characters in BlockId have to be one of '$charChoices'!"
        )
      }
      new BlockId(id)
    }
  }

  /** Create a new random block ID.
    *
    * Uses the algorithm from
    * https://github.com/LLK/scratch-vm/blob/develop/src/util/uid.js
    *
    * @return
    *   a valid Scratch 3 block ID.
    */
  def random(): BlockId = {
    val r = new Random()

    new BlockId(
      (0 until length)
        .map(_ => charChoices.charAt(r.nextInt(charChoices.length)))
        .mkString
    )
  }
}
