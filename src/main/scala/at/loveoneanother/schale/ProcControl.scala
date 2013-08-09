package at.loveoneanother.schale

abstract class ProcControl
// Output control
case object ProcStdoutReadLine
case object ProcStdoutReadChar
case object ProcStderrReadLine
case object ProcStderrReadChar
// Input control
case object ProcStdinFlush
case object ProcStdinClose