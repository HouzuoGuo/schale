package test.at.loveoneanother.schale

import java.io.IOException

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
import scala.util.Failure
import scala.util.Success

import org.scalatest.FunSuite

import akka.actor.actorRef2Scala
import akka.pattern.ask
import akka.util.Timeout
import at.loveoneanother.schale.Command
import at.loveoneanother.schale.Env
import at.loveoneanother.schale.ProcStderrReadChar
import at.loveoneanother.schale.ProcStdinClose
import at.loveoneanother.schale.ProcStdinFlush
import at.loveoneanother.schale.ProcStdoutReadLine
import at.loveoneanother.schale.Shell

class ProcTest extends FunSuite {
  test("run process and use exit status") {
    expectResult(0) { Command("echo", "a").waitFor() }
  }

  test("run process without IO") {
    Command("echo", "a").waitFor()
    intercept[IOException] {
      Command("does not exist").waitFor()
    }
  }

  test("single command and collect stdout/stderr") {
    expectResult("a") { Command("echo", "a").toString() }
  }

  test("consume stdout") {
    for (line <- Command("echo", "a").stdout)
      expectResult("a") { line }
  }

  test("consume stderr") {
    for (line <- Command("echo", "a").stderr)
      expectResult("") { line }
  }

  test("consume stdout and stderr") {
    for (line <- Command("echo", "a"))
      expectResult("a") { line }
  }

  test("feed to stdin") {
    expectResult(String.format("a%nb")) {
      (Command("cat").input(String.format("a%n"), String.format("b%n"))).toString()
    }
  }

  test("feed to stdin and consume stdout/stderr") {
    for (line <- Command("cat").input("a"))
      expectResult("a") { line }
  }

  test("interpret and collect stdout/stderr") {
    expectResult(String.format("a")) {
      Shell("echo a").toString()
    }
  }

  test("interpret using other interpreter") {
    expectResult(String.format("a")) {
      Shell("echo a", "/bin/ksh").toString()
    }
  }

  test("interpret and feed to stdin, then consume stdout/stderr") {
    for (line <- Shell("cat").input("a")) {
      expectResult("a") { line }
    }
  }

  test("interpret and use exit status") {
    val interpreter = Shell("cat").input("a")
    for (line <- interpreter) {
      expectResult("a") { line }
    }
    expectResult(0) { interpreter.waitFor() }
  }

  test("destroy process") {
    val proc = Shell("sleep 100")
    intercept[IllegalStateException] {
      proc.destroy()
    }
    proc.bg()
    expectResult(143) { proc.destroy() }
  }

  test("run in specified cwd") {
    new Env(pwd = "/") {
      expectResult("/") { Command("pwd").toString }
      expectResult("/") { Shell("pwd").toString }
    }
  }

  test("run in specified environment") {
    new Env(Map("newvar" -> "a")) {
      expectResult("a") { Shell("echo $newvar").toString }
    }
  }

  test("combine both env and pwd") {
    new Env(Map("newvar" -> "a")) {
      expectResult("a") { Shell("echo $newvar").toString }
      cd("/") {
        expectResult("a") { Shell("echo $newvar").toString }
        expectResult("/") { Command("pwd").toString }
      }
      cd("/tmp") {
        expectResult("a") { Shell("echo $newvar").toString }
        expectResult("/tmp") { Command("pwd").toString }
        env(Map("newvar2" -> "b")) {
          expectResult("/tmp") { Command("pwd").toString }
          cd("/") {
            expectResult("a") { Shell("echo $newvar").toString }
            expectResult("b") { Shell("echo $newvar2").toString }
            expectResult("/") { Command("pwd").toString }
          }
        }
      }
    }
  }

  test("interactive IO") {
    val proc = Command("grep", "a")
    proc interact { io =>
      import scala.concurrent.ExecutionContext.Implicits.global
      import at.loveoneanother.schale.actorSystem
      implicit val timeout = Timeout(2 seconds)

      io ! "a"
      io ! ProcStdinFlush
      io ! ProcStdinClose
      val future = io ? ProcStdoutReadLine
      future onComplete {
        case Success(line) => {
          expectResult("a") { line }
          expectResult(0) { proc.waitFor() }
        }
        case Failure(e) =>
          ProcTest.this.fail("cannot read proc output")
      }
      Await.result(future, 4 seconds)
    }
  }

  test("interactive IO (read character from stderr)") {
    val proc = Shell("grep a 1>&2")
    proc interact { io =>
      import scala.concurrent.ExecutionContext.Implicits.global
      import at.loveoneanother.schale.actorSystem
      implicit val timeout = Timeout(2 seconds)

      io ! "a"
      io ! ProcStdinFlush
      io ! ProcStdinClose
      val future = io ? ProcStderrReadChar
      future onComplete {
        case Success(char) => {
          expectResult('a') { char }
          expectResult(0) { proc.waitFor() }
        }
        case Failure(e) =>
          ProcTest.this.fail("cannot read proc output")
      }
      Await.result(future, 4 seconds)
    }
  }
}
