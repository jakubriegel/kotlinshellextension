package eu.jrie.jetbrains.kotlinshellextension.processes.process.system

import eu.jrie.jetbrains.kotlinshellextension.processes.process.PCB
import eu.jrie.jetbrains.kotlinshellextension.processes.process.Process
import eu.jrie.jetbrains.kotlinshellextension.processes.process.ProcessInputStream
import eu.jrie.jetbrains.kotlinshellextension.processes.process.ProcessOutputStream
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import org.apache.commons.io.output.NullOutputStream
import org.jetbrains.annotations.TestOnly
import org.zeroturnaround.exec.ProcessExecutor
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

    init {
        executor
            .command(listOf(command).plus(arguments))
            .destroyOnExit()
            .addListener(SystemProcessListener(this))
    }

    override fun redirectIn(source: ProcessInputStream) = apply { executor.redirectInput(source.tap) }

    override fun redirectMergedOut(destination: ProcessOutputStream) {
        executor.redirectMergedOut(destination)
    }

    override fun redirectStdOut(destination: ProcessOutputStream) {
        executor.redirectStdOut(destination)
    }

    override fun redirectStdErr(destination: ProcessOutputStream)  {
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

    @ObsoleteCoroutinesApi // TODO: implement process management in nonblocking way
    override fun await(timeout: Long) = scope.launch (newSingleThreadContext("$virtualPID $command join thread")) {
        ifAlive {
            with(pcb.startedProcess!!) {
                if (timeout.compareTo(0) == 0) future.get()
                else future.get(timeout, TimeUnit.MILLISECONDS)
            }
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
