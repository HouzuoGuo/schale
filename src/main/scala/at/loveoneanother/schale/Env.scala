package at.loveoneanother.schale

import java.io.File

/**
 * Extra (or override) environment variables for running script or command.
 */
class Env(var vars: Map[String, String] = Map(), var pwd: String = System getProperty "user.dir") {
  implicit var self = this

  override def toString() = pwd + (vars toString)

  /**
   * Change current working directory in this environment.
   */
  def cd(newPwd: String)(fun: => Unit) {
    val oldPwd = pwd
    pwd = newPwd
    try {
      fun
    } finally {
      pwd = oldPwd
    }
  }

  /**
   * Give more environment variables to this environment.
   */
  def env(extra: Map[String, String])(fun: => Unit) {
    val oldVars = vars
    vars ++= extra
    try {
      fun
    } finally {
      vars = oldVars
    }
  }

  def applyTo(pb: ProcessBuilder) {
    val env = pb.environment()
    vars foreach { kv => env.put(kv._1, kv._2) }
    pb.directory(new File(pwd))
  }
}