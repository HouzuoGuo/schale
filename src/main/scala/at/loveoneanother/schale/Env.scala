package at.loveoneanother.schale

import java.util.HashMap

/**
 * Extra (or override) environment variables for running script or command.
 */
class Env(vars: Map[String, String]) {
  override def toString() = vars toString

  def Pwd(pwd: String): Pwd = new Pwd(pwd, vars)

  def applyTo(pb: ProcessBuilder) {
    val env = pb.environment()
    vars foreach { kv => env.put(kv._1, kv._2) }
  }
}