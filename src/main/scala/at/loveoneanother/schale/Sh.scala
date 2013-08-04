package main.scala.at.loveoneanother.schale

import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter

class Proc(args: String*) extends Traversable[String] {
  val pb = new ProcessBuilder(args: _*)
  var proc: Process = null

  /**
   * Traverse lines in stdout only.
   */
  def stdout = new Traversable[String] {
    def foreach[U](fun: String => U) {
      startProc()
      collectOutput(fun, new BufferedReader(new InputStreamReader(proc getInputStream)))
    }
  }

  /**
   * Start process and traverse lines in stderr only.
   */
  def stderr = new Traversable[String] {
    def foreach[U](fun: String => U) {
      startProc()
      collectOutput(fun, new BufferedReader(new InputStreamReader(proc getErrorStream)))
    }
  }

  /**
   * Start process and traverse lines in both stdout and stderr.
   */
  def foreach[U](fun: String => U) {
    pb.redirectErrorStream(true)
    startProc()
    collectOutput(fun, new BufferedReader(new InputStreamReader(proc getInputStream)))

  }

  /**
   * Start process and return all output in stdout and stderr.
   */
  override def toString = {
    val output = new StringBuffer
    for (line <- this) {
      output append line append String.format("%n")
    }
    output toString
  }

  /**
   * Feed input to the process.
   */
  def apply(input: String*) = new ProcWithInput(args, input)

  /**
   * Start process without any IO interaction.
   */
  def apply() = {
    startProc()
    this
  }

  /**
   * Wait for process to finish and return its exit code.
   * If process has not been started, it will be started and then waited.
   */
  def waitFor(): Int = (proc match {
    case null => this().proc
    case _ => proc
  }) waitFor

  /**
   * Start the process.
   */
  protected def startProc() {
    proc = pb.start()
  }

  /**
   * Kill the process and return its exit value.
   * If process has not been started, an IllegalStateException is thrown.
   */
  def destroy(): Int = proc match {
    case null => throw new IllegalStateException("Process has not started")
    case _ => proc.destroy(); proc.waitFor()
  }

  /**
   * Collect traversable output from the running process via the Reader.
   */
  protected def collectOutput[U](fun: String => U, reader: BufferedReader) {
    try {
      var line = reader.readLine()
      while (line != null) {
        fun(line)
        line = reader.readLine()
      }
    } finally {
      reader close
    }
  }

  class ProcWithInput(args: Seq[String], input: Seq[String]) extends Proc(args: _*) {
    /**
     * Start process and feed input into its stdin. EOL characters are automatically appended to each input line.
     */
    override protected def startProc() {
      proc = pb.start()
      val stdin = new BufferedWriter(new OutputStreamWriter(proc getOutputStream))
      try {
        for (s <- input) {
          stdin.write(s)
          stdin.write(String format "%n")
        }
      } finally {
        stdin close
      }
    }
  }
}

/**
 * Run a command with command line arguments.
 */
object Sh {
  def apply(args: String*): Proc = new Proc(args: _*)
}

/**
 * Run a command in background with command line arguments.
 */
object AsyncSh {

}
