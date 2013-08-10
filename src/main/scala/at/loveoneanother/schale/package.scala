package at.loveoneanother

import com.typesafe.config.ConfigFactory

import akka.actor.ActorSystem
import at.loveoneanother.schale.Env

package object schale {
  implicit val defaultEnv = new Env(Map(), System getProperty "user.dir")

  /** The actor system is responsible for monitoring process interaction actors. */
  implicit lazy val actorSystem: ActorSystem = ActorSystem("schale", ConfigFactory.parseString("akka.daemonic=on"))
}