package at.loveoneanother.schale

import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import scala.util.control.Breaks.break
import scala.util.control.Breaks.breakable
import akka.actor.Actor
import scala.concurrent.duration._
import scala.concurrent._
import akka.actor.Actor
import akka.actor.ActorDSL._
import akka.actor.ActorSystem
import akka.pattern._
import akka.util.Timeout
import akka.actor.ActorRef
/**
 * An operating system process.
 */
class Proc(args: String*)(env: Env, pwd: Pwd) extends Traversable[String] {
  private val pb = new ProcessBuilder(args: _*)
  private var proc: Process = null

  /**
   * Start process and traverse lines in standard output.
   */
  def stdout = new Traversable[String] {
    def foreach[U](fun: String => U) {
      startProc()
      collectOutput(fun, new BufferedReader(new InputStreamReader(proc getInputStream)))
    }
  }

  /**
   * Start process and traverse lines in standard error.
   */
  def stderr = new Traversable[String] {
    def foreach[U](fun: String => U) {
      startProc()
      collectOutput(fun, new BufferedReader(new InputStreamReader(proc getErrorStream)))
    }
  }

  /**
   * Start process and traverse lines in both standard output and error.
   */
  def foreach[U](fun: String => U) {
    pb.redirectErrorStream(true)
    startProc()
    collectOutput(fun, new BufferedReader(new InputStreamReader(proc getInputStream)))
  }

  /**
   * Start process and return all output in standard output and error.
   */
  override def toString = (this collect { case s: String => s }).mkString(String format "%n")

  /**
   * Feed data to standard input (and return new Proc object).
   */
  def input(lines: String*) = new Proc(args: _*)(env, pwd) {
    /**
     * Start process and feed input into its stdin.
     */
    override protected def startProc() {
      super.startProc()
      val stdin = new BufferedWriter(new OutputStreamWriter(proc getOutputStream))
      try {
        for (s <- lines) {
          stdin.write(s)
        }
      } finally {
        stdin close
      }
    }
  }

  /**
   * Wait for process to finish and return its exit code.
   * If process has not been started, it will be started and then waited.
   */
  def waitFor(): Int = (proc match {
    case null =>
      startProc(); proc
    case _ => proc
  }) waitFor

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
    case _ => proc.destroy(); proc.waitFor()
  }

  /**
   * Start this process in background and commence interactive IO with it.
   */
  def interact(fun: ActorRef => Unit) {
    startProc()
    var stdin = new BufferedWriter(new OutputStreamWriter(proc getOutputStream))
    var stdout = new BufferedReader(new InputStreamReader(proc getInputStream))
    var stderr = new BufferedReader(new InputStreamReader(proc getErrorStream))
    val interactiveActor = actor(new Act {
      become {
        // Output control
        case ProcStdoutReadLine => {
          val line = stdout.readLine()
          println("a line has been read", line)
          sender ! line
        }
        case ProcStdoutClose =>
          if (stdout != null) { stdout.close(); stdout = null }
        case ProcStderrReadLine => sender ! stderr.readLine()
        case ProcStdErrClose =>
          if (stderr != null) { stderr.close(); stderr = null }
        // Input control
        case s: String => stdin.write(s)
        case c: Char => stdin.write(c.toInt)
        case ProcStdinFlush => stdin.flush()
        case ProcStdinClose =>
          if (stdin != null) { stdin.close() }
        // Process control
        case ProcDestroy => {
          destroy()
          context.stop(self)
        }
        case ProcWaitFor => {
          sender ! waitFor()
          context.stop(self)
        }
      }
    })
    try {
      fun(interactiveActor)
    } finally {
      if (stdin != null) stdin.close()
      if (stdout != null) stdout.close()
      if (stderr != null) stderr.close()
    }
  }

  /**
   * Start the process.
   */
  protected def startProc() {
    if (proc == null) {
      env.applyTo(pb)
      pwd.applyTo(pb)
      proc = pb.start()
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