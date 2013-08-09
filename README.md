# schale

schale is a subprocess interface for Scala that allows you to call any other programs and interact with their input/output. A quick example (system log monitor):

    for (line <- Command("tail", "-f", "/var/log/messages")) {
        println(line)
    }

## Features and examples

Remember to `import at.loveoneanother.schale._`

### Simple 

    println(Command("ls", "/").toString) // command with arguments
    println(Shell("ls -l /").toString)   // use shell to interpret

### Background command

    val job = Shell("sleep 100") // `Command` and `Shell` both will work
    job.bg()                     // start in backgorund
    // do something else ...
    val exitStatus = job.waitFor() // wait for completion

Call `job.destroy()` if you wish to terminite it before its completion.

### Stdin, stdout and stderr
