package de.uni_passau.fim.se2.catnip.model

import de.uni_passau.fim.se2.catnip.common.defs.UnitSpec
import scala.util.{Failure, Success}

class BlockIdSpec extends UnitSpec {
  "BlockID" should "generate IDs of length 20" in {
    val length = 20
    BlockId.random().toString.length should be(length)
  }

  it should "create an ID from string if the string is a valid id" in {
    val id = "ABC^_`{|}~DEFGHIJKLM"
    BlockId(id).isSuccess should be(true)
  }

  it should "fail creating an ID from string if the length is not 20" in {
    val id = "!#"
    BlockId(id) match {
      case Failure(ex) =>
        ex.getMessage should be(
          "BlockId has to have length 20 (was 2)!"
        )
      case Success(_) =>
        fail(
          "Creating an ID from string if the length is not 20 should not succeed!"
        )
    }
  }

  it should "fail creating an ID from string if is contains invalid chars" in {
    val id = "ABCDEFGHIJKLMNOPQRSß" // ß not allowed
    BlockId(id) match {
      case Failure(ex) =>
        ex.getMessage should be(
          "All characters in BlockId have to be one of '$!#%()*+,-./:;=?@[]^_`{|}~ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789'!"
        )
      case Success(_) =>
        fail(
          "Creating an ID from string with illegal characters should not succeed!"
        )
    }
  }
}
