package at.loveoneanother.schale

import java.io.IOException

import org.scalatest.FunSuite

class ShTest extends FunSuite {
  test("run process and use exit status") {
    println("start")
    new Cwd("/") {
      expectResult(0) {
        Sh("echo", "a")().waitFor()
      }
    }
    println("done")
  }

  test("run process without IO") {
    Sh("echo", "a")
    intercept[IOException] {
      Sh("does not exist")()
    }
  }

  test("single command and collect stdout/stderr") {
    expectResult(String.format("a%n")) {
      Sh("echo", "a").toString()
    }
  }

  test("consume stdout") {
    for (line <- Sh("echo", "a").stdout)
      expectResult("a") { line }
  }

  test("consume stderr") {
    for (line <- Sh("echo", "a").stderr)
      expectResult("") { line }
  }

  test("consume stdout and stderr") {
    for (line <- Sh("echo", "a"))
      expectResult("a") { line }
  }

  test("feed to stdin") {
    expectResult(String.format("a%nb%n")) {
      (Sh("cat")(String.format("a%n"), String.format("b%n"))).toString()
    }
  }

  test("feed to stdin and consume stdout/stderr") {
    for (line <- Sh("cat")("a"))
      expectResult("a") { line }
  }

  test("interpret and collect stdout/stderr") {
    expectResult(String.format("a%n")) {
      Interpret("echo a").toString()
    }
  }

  test("interpret using other interpreter") {
    expectResult(String.format("a%n")) {
      Interpret("echo a", "/bin/ksh").toString()
    }
  }

  test("interpret and feed to stdin, then consume stdout/stderr") {
    for (line <- Interpret("cat")("a")) {
      expectResult("a") { line }
    }
  }

  test("interpret and use exit status") {
    val interpreter = Interpret("cat")("a")
    for (line <- interpreter) {
      expectResult("a") { line }
    }
    expectResult(0) { interpreter.waitFor() }
  }

  test("destroy process") {
    val proc = Interpret("sleep 100")
    intercept[IllegalStateException] {
      proc.destroy()
    }
    proc()
    expectResult(143) { proc.destroy() }
  }
}
