package sbt.inc

import sbt.util._
import sbt.internal.util.{ BufferedLogger, ConsoleLogger }

object TestLogger {
  def apply[T](f: Logger => T): T = {
    val log = new BufferedLogger(ConsoleLogger())
    log.setLevel(Level.Debug)
    log.bufferQuietly(f(log))
  }
}
