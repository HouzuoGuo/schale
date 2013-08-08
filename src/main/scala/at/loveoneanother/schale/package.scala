package at.loveoneanother

import akka.actor.ActorSystem

package object schale {
  implicit val defaultPwd = new Pwd(System getProperty "user.dir")
  implicit val defaultEnv = new Env(Map())
  implicit val actorSystem = ActorSystem("schale")
}