package at.loveoneanother.schale

import akka.actor.Actor
import akka.actor.Props

object ProcInteraction {
  def props(proc: Process): Props = Props(classOf[ProcInteraction], proc)
}

class ProcInteraction(proc: Process) extends Actor {
  override def receive = {
    case "end" =>
      context.stop(self)
    case a: Any => println(a)
  }
}