package at.loveoneanother.schale

/**
 * *Additional* (or override) environmental variables for running a script interpreter or a single process.
 */
class Env(vars: Map[String, String]) {
  /**
   * Run a program with command line arguments.
   */
  def Sh(args: String*): Proc = new Proc(vars, System getProperty "user.dir", args)

  override def toString() = vars toString
}