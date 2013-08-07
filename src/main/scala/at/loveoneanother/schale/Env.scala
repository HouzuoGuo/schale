package at.loveoneanother.schale

import java.util.HashMap

/**
 * Extra (or override) environment variables for running script or command.
 */
class Env(vars: Map[String, String]) {
  implicit val self = this

  override def toString() = vars toString

  /**
   * Change current working directory in this environment.
   */
  def pwd(pwd: String)(fun: => Unit): Pwd = new Pwd(pwd, vars)

  /**
   * Give more environment variables to this environment.
   */
  def moreEnv(moreVars: Map[String, String])(fun: => Unit): Env = new Env(moreVars ++ vars)

  def applyTo(pb: ProcessBuilder) {
    val env = pb.environment()
    vars foreach { kv => env.put(kv._1, kv._2) }
  }
}