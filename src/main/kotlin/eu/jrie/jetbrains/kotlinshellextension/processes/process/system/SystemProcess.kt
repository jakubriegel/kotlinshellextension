package eu.jrie.jetbrains.kotlinshellextension.processes.process.system

import eu.jrie.jetbrains.kotlinshellextension.processes.process.PCB
import eu.jrie.jetbrains.kotlinshellextension.processes.process.Process
import eu.jrie.jetbrains.kotlinshellextension.processes.process.stream.ProcessInputStream
import eu.jrie.jetbrains.kotlinshellextension.processes.process.stream.ProcessOutputStream
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import org.apache.commons.io.output.NullOutputStream
import org.jetbrains.annotations.TestOnly
import org.zeroturnaround.exec.ProcessExecutor
import org.zeroturnaround.exec.stream.LogOutputStream
import java.io.InputStream
import java.time.Instant
import java.util.concurrent.TimeUnit

class SystemProcess @TestOnly internal constructor (
    vPID: Int,
    command: String,
    arguments: List<String>,
    scope: CoroutineScope,
    private val executor: ProcessExecutor
) : Process(vPID, command, arguments, scope) {

    constructor(vPID: Int, command: String, arguments: List<String>, scope: CoroutineScope)
            : this(vPID, command, arguments, scope, ProcessExecutor())

    override val pcb = SystemPCB()

    override val name: String
        get() = "[${super.name} pid=${pcb.systemPID}]"

    init {
        executor
            .command(listOf(command).plus(arguments))
            .destroyOnExit()
            .addListener(SystemProcessListener(this))
    }

    override fun redirectIn(source: ProcessInputStream) {
        executor.redirectInput(SystemProcessInputStream(source))
    }

    override fun redirectMergedOut(destination: ProcessOutputStream) {
        executor.redirectOutput(SystemProcessLogOutputStream(destination))
    }

    override fun redirectStdOut(destination: ProcessOutputStream) {
        executor
            .redirectOutput(SystemProcessLogOutputStream(destination))
            .redirectError(NullOutputStream())
    }

    override fun redirectStdErr(destination: ProcessOutputStream)  {
        executor.redirectError(SystemProcessLogOutputStream(destination))
    }

    override fun setEnvironment(env: Map<String, String>) = apply { env.forEach { (e, v) -> setEnvironment(e to v) } }

    override fun setEnvironment(env: Pair<String, String>) = apply { executor.environment(env.first, env.second) }

    override fun start(): PCB {
        val started = executor.start()!!

        pcb.startTime = started.process.info().startInstant().orElse(Instant.MIN)
        pcb.systemPID = started.process.pid()
        pcb.startedProcess = started

        logger.debug("started SystemProcess $name with PID=${pcb.systemPID}")
        return pcb
    }

    override fun isAlive() = if (pcb.startedProcess != null ) pcb.startedProcess!!.process.isAlive else false

    @ObsoleteCoroutinesApi // TODO: implement process management in nonblocking way
    override fun await(timeout: Long) = scope.launch (newSingleThreadContext("$vPID $command join")) {
        ifAlive {
            with(pcb.startedProcess!!) {
                if (timeout.compareTo(0) == 0) future.get()
                else future.get(timeout, TimeUnit.MILLISECONDS)
            }
        }
        logger.debug("awaited SystemProcess $name")
    }

    override fun kill() = ifAlive {
        with(pcb.startedProcess!!) {
            process.destroy()
            ifAlive { process.destroyForcibly() }
        }
        logger.debug("killed SystemProcess $name")
    }

    internal class SystemProcessLogOutputStream (
        private val processOutputStream: ProcessOutputStream
    ) : LogOutputStream() {
        override fun processLine(line: String?) {
            if (line != null) processOutputStream.send(line)
        }
    }

    internal class SystemProcessInputStream (
        private val processInputStream: ProcessInputStream
    ) : InputStream() {
        override fun read(): Int {
            return try {
                processInputStream.read().toInt()
            }
            catch (e: ClosedReceiveChannelException) {
                close()
                -1
            }
        }
    }
}
