package at.loveoneanother

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory

package object schale {
  implicit val defaultPwd = new Pwd(System getProperty "user.dir")
  implicit val defaultEnv = new Env(Map())

  /** The actor system is responsible for monitoring process interaction actors. */
  implicit lazy val actorSystem: ActorSystem = ActorSystem("schale", ConfigFactory.parseString("akka.daemonic=on"))
}