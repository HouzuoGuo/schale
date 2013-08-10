Schale
------

Schale is a subprocess interface for Scala. Make all your system programs easily available to Scala, call those programs and interact with their input and output!

## Usage

### A gentle start

    import at.loveoneanother.schale._
    println(Command("ls", "-l", "/"))
    println(Shell("ls -l /"))

`Command` runs any process, `Shell` runs system shell interpreter. The function return values have exactly the same Schale API.

This is the only use case in which `.waitFor()` call is unnecessary.

### Run in background

    val proc = Shell("sleep 100")
    proc.bg()
    val exitStatus = proc.waitFor()

Both simple and interactive IO may be used on background process.

Process `.destroy()` can be used any time to destory a process before its completion, even during simple or interactive IO.

### Simple IO

    val grepper = Shell("grep Scala")
    grepper.input("Java API\n", "Scala API\n", "Scala Doc\n")
    for (line <- grepper.stdout) { println(line) }
    for (line <- grepper.stderr) { println(line) }
    grepper.waitFor()

`.input`, `.stdout` and `.stderr` may only be used once!

### Advanced (interactive) IO

    implicit val readTimeout = Timeout(2 seconds)
    Shell("grep Scala") interact { io =>
      // Stdin: write chars and strings
      io ! "Java API"; io ! '\n'
      io ! "Scala API\n"
      io ! ProcStdinClose // EOF

      // Stdout: read chars and strings
      println(Await.result(io ? ProcStdoutReadChar, 2 seconds).asInstanceOf[Int].toChar)
      println(Await.result(io ? ProcStdoutReadLine, 2 seconds).asInstanceOf[String])
    } waitFor

`interact` may be used any number of times. For reading from error output, there are `ProcStderrReadChar` and `ProcStderrReadLine`.

### Process environment

    // Do not import implicit default environment
    import at.loveoneanother.schale.{ Env, Command, Shell }
    new Env() {
      cd("/") {
        println(Command("pwd")) // root
        env(Map("mylang" -> "Scala")) { println(Shell("echo $mylang")) } // Scala
      }
      cd("/tmp") {
        println(Command("pwd")) // tmp
      }
      env(Map("hobby" -> "gliding")) {
        // Override value
        env(Map("hobby" -> "programming")) { println(Shell("echo $hobby")) } // programming
      }
    }

Start from a `new Env()` object, use `cd()` to change directory and `env()` to add/override variables, __stack__ those calls to easily manage hierarchical process environment!

## Some notes

__Is `Shell()` cross platform?__

`Comamnd()` is cross platform, however `Shell("script")` assumes \*nix OS platform and the availability of `/bin/sh`. Alternative call `Shell("script", "interpreter path")` uses the specified interpreter, but still cannot be guaranteed to run cross-platform.

__Can I `cd()` to relative paths?__

In a process environment, `cd()` call only accepts absolute path, this due to JVM's inability to determine whether a path is relative or absolute. You may find `java.nio.file.Paths` easy to work with `cd()`.

__Can I use Schale to interact with SSH/SFTP/SCP?__

JVM cannot interact with TTY and Schale itself is not a terminal emulator, therefore it cannot be used on programs which require advanced terminal features, such as SSH/SFTP/SCP. Sorry!

## Version History

<table>
<tr>
  <th>Version</th>
  <th>Branch</th>
  <th>Release Date</th>
  <th>Comment</th>
</tr>
<tr>
  <td>Alpha</td>
  <td>alpha</td>
  <td>2013-08-10</td>
  <td>First release</td>
</tr>
</table>

## Contact and License

You may want to check out [Issues] section for future plans, and please feel very free to contact [Howard] if you have any feedback / questions. I also have [Twitter] and [blog], please check them out as well.

The following copyright notice and disclaimers apply to all files in the project repository:
<pre>
Copyright (c) 2013, Howard Guo
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
- Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
- Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
</pre>

## Project Background

Subprocess management was traditionally carried out by using `Runtime.getRuntime().exec` series of calls. Although JDK introduced `ProcessBuilder` later on, but process building and IO interactivity could still be cumbersome.

Schale takes advantage of advanced features and syntactic sugar offered by Scala, and brings to you:

- Easy process creation
- Background process management
- Simplified process input/output interface
- Advanced, interactive, non-blocking process IO
- Hierarchical process environment (variables, working directory) management

Schale was inspired by Python third-party package "sh".

And shoutout to NumberFour AG!

[Howard]: mailto:guohouzuo@gmail.com
[Twitter]: https://twitter.com/hzguo
[blog]: http://allstarnix.blogspot.com.au
[Issues]: https://github.com/HouzuoGuo/schale/issues
