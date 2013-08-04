schale
======

A subprocess interface for Scala. The project was inspired by a popular Python module called "sh".

Status
======

Schale is still at very early stage of development, but these examples (taken from test cases) may give you an idea of how it works:

    // Print command output
    println(Sh("echo", "a"))

    // Run a script and get its output
    println(Interpret("echo a"))

    // Run a command, feed to its standard input, and consume its standard output/error
    for (output <- Sh("cat")("input line 1", "input line 2")) {
      println(output)
    }
