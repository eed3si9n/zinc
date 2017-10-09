/*
 * Zinc - The incremental compiler for Scala.
 * Copyright 2011 - 2017, Lightbend, Inc.
 * Copyright 2008 - 2010, Mark Harrah
 * This software is released under the terms written in LICENSE.
 */

package sbt
package internal
package inc

import java.io.File
import xsbti.{ InteractiveConsoleInterface, InteractiveConsoleResponse }
import sbt.util.Logger

class ConsoleSession(intf: InteractiveConsoleInterface) {
  def interpret(line: String, synthetic: Boolean): InteractiveConsoleResponse =
    intf.interpret(line, synthetic)
}

object ConsoleSession {
  def apply(analyzingCompiler: AnalyzingCompiler,
            classpath: Seq[File],
            options: Seq[String],
            initialCommands: String,
            cleanupCommands: String,
            log: Logger): ConsoleSession = {
    apply(analyzingCompiler,
          classpath,
          options,
          initialCommands,
          cleanupCommands,
          log,
          None,
          Vector())
  }

  def apply(analyzingCompiler: AnalyzingCompiler,
            classpath: Seq[File],
            options: Seq[String],
            initialCommands: String,
            cleanupCommands: String,
            log: Logger,
            loader: Option[ClassLoader],
            bindings: Seq[(String, Any)]): ConsoleSession = {
    val intf = analyzingCompiler
      .interactiveConsole(classpath, options, initialCommands, cleanupCommands, log)(loader,
                                                                                     bindings)
    apply(intf)
  }

  def apply(intf: InteractiveConsoleInterface): ConsoleSession =
    new ConsoleSession(intf)
}
