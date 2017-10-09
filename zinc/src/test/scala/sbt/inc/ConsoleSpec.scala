package sbt.inc

import java.io.File
import java.net.URLClassLoader

import sbt.internal.inc._
import sbt.internal.inc.classpath.ClassLoaderCache
import sbt.internal.util.ConsoleLogger
import sbt.io.IO
import sbt.io.syntax._
import sbt.util.Logger
import xsbti.compile._
import xsbti.{ InteractiveConsoleResult, InteractiveConsoleResponse }

class ConsoleSpec extends BridgeProviderSpecification {

  "console session" should "evaluate arithmetic expression" in {
    IO.withTemporaryDirectory { tempDir =>
      val res = consoleSession(tempDir, logger).interpret("1 + 1", false)
      assert(output(res) == "res0: Int = 2" && res.result == InteractiveConsoleResult.Success)
    }
  }

  it should "evaluate list constructor" in {
    IO.withTemporaryDirectory { tempDir =>
      TestLogger { logger =>
        val session = consoleSession(tempDir, logger)
        val res = session.interpret("List(1, 2)", false)
        res.output.trim shouldBe "res0: List[Int] = List(1, 2)"
        res.result shouldBe InteractiveConsoleResult.Success
      }
    }
  }

  it should "evaluate import" in {
    IO.withTemporaryDirectory { tempDir =>
      val response = consoleSession(tempDir, logger).interpret("import scala.collection._", false)
      response.output.trim shouldBe "import scala.collection._"
      response.result shouldBe InteractiveConsoleResult.Success
    }
  }

  it should "mark partial expression as incomplete" in {
    IO.withTemporaryDirectory { tempDir =>
      val response = consoleSession(tempDir, logger).interpret("val a =", false)
      response.result shouldBe InteractiveConsoleResult.Incomplete
    }
  }

  it should "not evaluate incorrect expression" in {
    IO.withTemporaryDirectory { tempDir =>
      val response = consoleSession(tempDir, logger).interpret("1 ++ 1", false)
      response.result shouldBe InteractiveConsoleResult.Error
    }
  }

  def consoleSession(tempDir: File, log: Logger): ConsoleSession = {
    val scalaVersion = "2.12.4"
    val compilerBridge = getCompilerBridge(tempDir, noLogger, scalaVersion)
    val si = scalaInstance(scalaVersion, tempDir, log)
    val sc = scalaCompiler(si, compilerBridge)
    val w = ConsoleSession(sc, si.allJars, Nil, "", "", log)
    w
  }

  def scalaCompiler(instance: xsbti.compile.ScalaInstance, bridgeJar: File): AnalyzingCompiler = {
    val bridgeProvider = ZincUtil.constantBridgeProvider(instance, bridgeJar)
    val classpath = ClasspathOptionsUtil.boot
    val cache = Some(new ClassLoaderCache(new URLClassLoader(Array())))
    new AnalyzingCompiler(instance, bridgeProvider, classpath, _ => (), cache)
  }

  def output(res: InteractiveConsoleResponse): String = {
    res.output.replaceAll("""\e\[\d+m""", "").trim
  }

  def noLogger = Logger.Null
  lazy val logger = ConsoleLogger()
}
