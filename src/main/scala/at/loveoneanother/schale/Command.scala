package at.loveoneanother.schale

/**
 * Run a single program in default current working directory and default environment.
 */
object Command {
  def apply(args: String*)(implicit env: Env, pwd: Pwd): Proc = new Proc(args: _*)(env, pwd)
}