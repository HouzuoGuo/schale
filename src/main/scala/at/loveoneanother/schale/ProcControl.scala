package at.loveoneanother.schale

abstract class ProcControl
// Output control
case object ProcStdoutReadLine
case object ProcStdoutClose
case object ProcStderrReadLine
case object ProcStdErrClose
// Input control
case object ProcStdinFlush
case object ProcStdinClose
// Process control
case object ProcDestroy
case object ProcWaitFor
