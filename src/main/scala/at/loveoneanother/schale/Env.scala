package main.scala.at.loveoneanother.schale

/**
 * *Additional* (or override) environmental variables for running a script interpreter or a single process.
 */
class Env(vars: Map[String, String]) {
  override def toString() = vars toString
}