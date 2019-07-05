package eu.jrie.jetbrains.kotlinshellextension.processes.process.system

import eu.jrie.jetbrains.kotlinshellextension.processes.process.PCB
import eu.jrie.jetbrains.kotlinshellextension.processes.process.Process
import eu.jrie.jetbrains.kotlinshellextension.processes.process.ProcessInputStream
import eu.jrie.jetbrains.kotlinshellextension.processes.process.ProcessOutputStream
import org.apache.commons.io.output.NullOutputStream
import org.jetbrains.annotations.TestOnly
import org.zeroturnaround.exec.ProcessExecutor
import java.time.Instant
import java.util.concurrent.TimeUnit

class SystemProcess @TestOnly internal constructor (
    vPID: Int,
    command: String,
    arguments: List<String>,
    private val executor: ProcessExecutor
) : Process(vPID, command, arguments) {

    constructor(vPID: Int, command: String, arguments: List<String>)
            : this(vPID, command, arguments, ProcessExecutor())

    override val pcb = SystemPCB()

    init {
        executor
            .command(listOf(command).plus(arguments))
            .destroyOnExit()
            .addListener(SystemProcessListener(this))
    }

    override fun redirectIn(source: ProcessInputStream) = apply { executor.redirectInput(source.tap) }

    override fun followMergedOut() = followMergedOut(ProcessOutputStream())

    override fun followMergedOut(destination: ProcessOutputStream) = apply {
        stdout = destination
        executor.redirectMergedOut(destination)
    }

    override fun followStdOut() = followStdOut(ProcessOutputStream())

    override fun followStdOut(destination: ProcessOutputStream) = apply {
        stdout = destination
        executor.redirectStdOut(destination)
    }

    override fun followStdErr() = followStdErr(ProcessOutputStream())

    override fun followStdErr(destination: ProcessOutputStream) = apply {
        stderr = destination
        executor.redirectStdErr(destination)
    }

    override fun setEnvironment(env: Map<String, String>) = apply { env.forEach { (e, v) -> setEnvironment(e to v) } }

    override fun setEnvironment(env: Pair<String, String>) = apply { executor.environment(env.first, env.second) }

    override fun start(): PCB {
        val started = executor.start()!!

        pcb.startTime = started.process.info().startInstant().orElse(Instant.MIN)
        pcb.systemPID = started.process.pid()
        pcb.startedProcess = started

        return pcb
    }

    override fun isAlive() = if (pcb.startedProcess != null ) pcb.startedProcess!!.process.isAlive else false

    override fun await(timeout: Long) = ifAlive {
        with(pcb.startedProcess!!) {
            if (timeout.compareTo(0) == 0) future.get()
            else future.get(timeout , TimeUnit.MILLISECONDS)
        }
    }

    override fun kill() = ifAlive {
        with(pcb.startedProcess!!) {
            process.destroy()
            ifAlive { process.destroyForcibly() }
        }
    }

    private fun ProcessExecutor.redirectMergedOut(destination: ProcessOutputStream) = redirectOutput(destination)

    private fun ProcessExecutor.redirectStdOut(destination: ProcessOutputStream) =
        redirectOutput(destination).redirectError(NullOutputStream())

    private fun ProcessExecutor.redirectStdErr(destination: ProcessOutputStream) = redirectError(destination)
}
