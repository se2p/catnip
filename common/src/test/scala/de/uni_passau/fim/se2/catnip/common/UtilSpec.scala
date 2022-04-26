package de.uni_passau.fim.se2.catnip.common

import de.uni_passau.fim.se2.catnip.common.defs.UnitSpec
import java.time.Duration

class UtilSpec extends UnitSpec {
  "Common Utility Functions" should "execute a function while measuring time" in {
    val (res, t) = timed {
      (1 to 100).sum
    }
    res shouldBe 5050
    t shouldBe a[Duration]
  }

  it should "format a duration shorter than an hour" in {
    val d = Duration.ofSeconds(200).plusNanos(1239)
    formatDuration(d) shouldBe "200.1239s"
  }

  it should "format a duration longer than an hour" in {
    val d = Duration.ofSeconds(3650).plusNanos(1239)
    formatDuration(d) shouldBe "3650.1239s"
  }
}
