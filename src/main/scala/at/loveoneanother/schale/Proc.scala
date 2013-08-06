package at.loveoneanother.schale

import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.File
/**
 * A process - could be a script interpreter, or a single process.
 */
class Proc(env: Map[String, String], cwd: String, args: Seq[String]) extends Traversable[String] {
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
   * Feed data to standard input (and return new Proc object).
   */
  def apply(input: String*) = new Proc(env, cwd, args) {
    /**
     * Start process and feed input into its stdin.
     */
    override protected def startProc() {
      super.startProc()
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
  protected def startProc() {
    val environment = pb.environment()
    env foreach { kv =>
      environment.put(kv._1, kv._2)
    }
    pb.directory(new File(cwd))
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
}