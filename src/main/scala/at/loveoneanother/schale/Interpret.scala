package main.scala.at.loveoneanother.schale

/**
 * Call operating system's script interpreter to run a script.
 */
object Interpret {

  /**
   * Run a script using operating system's script interpreter. Currently only support *nix operating systems.
   */
  def apply(script: String, interpreter: String = "/bin/sh"): Proc = {
    System.getProperty("os.name").toLowerCase match {
      case os if os.contains("nux") || os.contains("sun") || os.contains("mac") => Sh(interpreter, "-c", script)
      case _ => throw new UnsupportedOperationException("Interpret does not work in your OS")
    }
  }
}