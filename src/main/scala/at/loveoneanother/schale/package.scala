package at.loveoneanother

package object schale {
  implicit val defaultPwd = new Pwd(System getProperty "user.dir")
  implicit val defaultEnv = new Env(Map())
}