package at.loveoneanother.schale

import java.io.File

/**
 * A non-default current working directory for running script or command.
 */
class Pwd(pwd: String, env: Map[String, String] = Map()) extends Env(env) {
  override def toString = pwd

  override def applyTo(pb: ProcessBuilder) {
    super.applyTo(pb)
    pb.directory(new File(pwd))
  }
}