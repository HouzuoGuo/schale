package main.scala.at.loveoneanother.schale

/**
 * Run a single program.
 */
object Sh {
  /**
   * Run a program with command line arguments.
   */
  def apply(args: String*): Proc = new Proc(args: _*)
}