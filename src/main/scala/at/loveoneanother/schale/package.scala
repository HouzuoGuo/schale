package at.loveoneanother

package object schale {
  implicit val defaultEnv = new Env(Map())
  implicit val defaultCwd = new Cwd(System getProperty "user.dir")
}
