package at.loveoneanother.schale

import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter

import scala.util.control.Breaks.break
import scala.util.control.Breaks.breakable

import akka.actor.ActorDSL.Act
import akka.actor.ActorDSL.actor
import akka.actor.ActorRef
import akka.actor.actorRef2Scala
/**
 * An operating system process.
 */
class Proc(args: String*)(env: Env) extends Traversable[String] {
  protected val pb = new ProcessBuilder(args: _*)
  protected var proc: Process = null
  protected var inputWriter: BufferedWriter = null
  protected var outputReader: BufferedReader = null
  protected var errorReader: BufferedReader = null

  /**
   * Start process and traverse lines in standard output.
   */
  def stdout = new Traversable[String] {
    def foreach[U](fun: String => U) {
      startProc()
      collectOutput(fun, outputReader)
    }
  }

  /**
   * Start process and traverse lines in standard error.
   */
  def stderr = new Traversable[String] {
    def foreach[U](fun: String => U) {
      startProc()
      collectOutput(fun, errorReader)
    }
  }

  /**
   * Start process and traverse lines in both standard output and error.
   */
  def foreach[U](fun: String => U) {
    pb.redirectErrorStream(true)
    startProc()
    collectOutput(fun, outputReader)
  }

  /**
   * Start process and return all output in standard output and error.
   */
  override def toString = {
    val output = (collect { case s: String => s }).mkString(String format "%n")
    waitFor()
    output
  }

  /**
   * Feed data to standard input.
   */
  def input(lines: String*) = {
    startProc()
    try {
      for (s <- lines) {
        inputWriter.write(s)
      }
    } finally {
      inputWriter.close()
    }
    this
  }

  /**
   * Wait for process to finish and return its exit code.
   * If process has not been started, it will be started and then waited.
   */
  def waitFor(): Int = {
    if (proc == null)
      startProc()
    try {
      if (inputWriter != null)
        inputWriter.close()
      // there is no need to close output readers
    } finally {
    }
    proc.waitFor()
  }

  /**
   * Start this process in background (does not block main thread).
   */
  def bg() = this.startProc()

  /**
   * Destroy the process and return its exit value.
   * If process has not been started, an IllegalStateException is thrown.
   */
  def destroy(): Int = proc match {
    case null => throw new IllegalStateException("Process has not started")
    case _ => proc.destroy(); waitFor()
  }

  /**
   * Start this process in background and commence interactive IO with it.
   */
  def interact(fun: ActorRef => Unit) = {
    startProc()
    fun(actor(new Act {
      become {
        // Output control
        case ProcStdoutReadLine =>
          sender ! outputReader.readLine()
        case ProcStdoutReadChar =>
          sender ! outputReader.read()
        case ProcStderrReadLine =>
          sender ! errorReader.readLine()
        case ProcStderrReadChar =>
          sender ! errorReader.read()
        // Input control
        case s: String =>
          inputWriter.write(s)
        case c: Char =>
          inputWriter.write(c.toInt)
        case ProcStdinFlush =>
          inputWriter.flush()
        case ProcStdinClose =>
          inputWriter.close(); inputWriter = null
      }
    }))
    this
  }

  /**
   * Start the process.
   */
  protected def startProc() {
    if (proc == null) {
      env.applyTo(pb)
      proc = pb.start()
      inputWriter = new BufferedWriter(new OutputStreamWriter(proc getOutputStream))
      outputReader = new BufferedReader(new InputStreamReader(proc getInputStream))
      errorReader = new BufferedReader(new InputStreamReader(proc getErrorStream))
    }
  }

  /**
   * Collect process output from the reader, and feel them to the function.
   */
  protected def collectOutput[U](fun: String => U, reader: BufferedReader) {
    try {
      breakable {
        while (true) {
          val line = reader.readLine()
          if (line == null)
            break
          fun(line)
        }
      }
    } finally {
      reader close
    }
  }
}