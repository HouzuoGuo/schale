package main.scala.at.loveoneanother.schale

/**
 * Run a script.
 */
object Interpret {
  def apply(script: String, interpreter: String = "/bin/sh"): Proc = {
    System.getProperty("os.name").toLowerCase match {
      case os if os.contains("nux") || os.contains("sun") || os.contains("mac") => Sh(interpreter, "-c", script)
      case _ => throw new UnsupportedOperationException("Interpret does not work in your OS")
    }
  }
}