package at.loveoneanother.schale

import akka.actor.Actor
import akka.actor.ActorSystem

object Main {
  def main(args: Array[String]): Unit = {
    val echo = actorSystem.actorOf(ProcInteraction.props(null))
    echo ! "end"
  }
}