package de.uni_passau.fim.se2.catnip

import java.time.{Duration, Instant}

package object common {
  def timed[Return](f: => Return): (Return, Duration) = {
    val start = Instant.now()
    val res   = f
    val end   = Instant.now()
    (res, Duration.between(start, end))
  }

  def formatDuration(duration: Duration): String = {
    s"${duration.getSeconds}.${duration.getNano}s"
  }
}
