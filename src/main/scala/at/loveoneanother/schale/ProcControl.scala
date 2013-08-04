package main.scala.at.loveoneanother.schale

abstract class ProcControl
case object ProcEOF extends ProcControl
case object ProcDone extends ProcControl