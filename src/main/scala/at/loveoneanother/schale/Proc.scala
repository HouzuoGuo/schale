package at.loveoneanother.schale

import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
/**
 * A process - could be a script interpreter, or a single process.
 */
class Proc(args: String*) extends Traversable[String] {
  private val pb = new ProcessBuilder(args: _*)
  private var proc: Process = null

  /**
   * Start process and traverse lines in standard output.
   */
  def stdout = new Traversable[String] {
    def foreach[U](fun: String => U) {
      startProc
      collectOutput(fun, new BufferedReader(new InputStreamReader(proc getInputStream)))
    }
  }

  /**
   * Start process and traverse lines in standard error.
   */
  def stderr = new Traversable[String] {
    def foreach[U](fun: String => U) {
      startProc
      collectOutput(fun, new BufferedReader(new InputStreamReader(proc getErrorStream)))
    }
  }

  /**
   * Start process and traverse lines in both standard output and error.
   */
  def foreach[U](fun: String => U) {
    pb.redirectErrorStream(true)
    startProc
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
    startProc
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
   * Destroy the process and return its exit value.
   * If process has not been started, an IllegalStateException is thrown.
   */
  def destroy(): Int = proc match {
    case null => throw new IllegalStateException("Process has not started")
    case _ => proc.destroy(); proc.waitFor()
  }

  /**
   * Start the process.
   */
  protected def startProc()(implicit env: Env, cwd: Cwd) {
    println(args, cwd, env)
    proc = pb.start()
  }

  /**
   * Collect process output from the reader, and feel them to the function.
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

  /**
   * A process with additional standard input.
   */
  class ProcWithInput(args: Seq[String], input: Seq[String]) extends Proc(args: _*) {
    /**
     * Start process and feed input into its stdin.
     */
    override protected def startProc()(implicit env: Env, cwd: Cwd) {
      proc = pb.start()
      val stdin = new BufferedWriter(new OutputStreamWriter(proc getOutputStream))
      try {
        for (s <- input) {
          stdin.write(s)
        }
      } finally {
        stdin close
      }
    }
  }
}