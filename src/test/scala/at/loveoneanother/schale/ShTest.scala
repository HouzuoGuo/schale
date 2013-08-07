package at.loveoneanother.schale

import java.io.IOException

import org.scalatest.FunSuite
import at.loveoneanother.schale._

class ShTest extends FunSuite {
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
    expectResult(String.format("a%n")) {
      Command("echo", "a").toString()
    }
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
    expectResult(String.format("a%nb%n")) {
      (Command("cat").input(String.format("a%n"), String.format("b%n"))).toString()
    }
  }

  test("feed to stdin and consume stdout/stderr") {
    for (line <- Command("cat").input("a"))
      expectResult("a") { line }
  }

  test("interpret and collect stdout/stderr") {
    expectResult(String.format("a%n")) {
      Shell("echo a").toString()
    }
  }

  test("interpret using other interpreter") {
    expectResult(String.format("a%n")) {
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
    proc.waitFor()
    expectResult(143) { proc.destroy() }
  }

  test("run in specified cwd") {
    new Pwd("/") {
      expectResult(String.format("/%n")) { Command("pwd").toString }
      expectResult(String.format("/%n")) { Shell("pwd").toString }
    }
  }

  test("run in specified environment") {
    new Env(Map("newvar" -> "a")) {
      expectResult(String.format("a%n")) { Shell("echo $newvar").toString }
    }
  }
}
