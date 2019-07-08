package eu.jrie.jetbrains.kotlinshellextension.processes.process

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job

abstract class Process protected constructor (
    val virtualPID: Int,
    val command: String,
    val arguments: List<String> = emptyList(),
    val scope: CoroutineScope
) {

    abstract val pcb: PCB

    lateinit var input: ProcessInputStream private set
    lateinit var stdout: ProcessOutputStream private set
    lateinit var stderr: ProcessOutputStream private set

    abstract fun redirectIn(source: ProcessInputStream): Process

    fun followMergedOut() = followMergedOut(ProcessOutputStream(scope))

    fun followMergedOut(destination: ProcessOutputStream) = apply {
        stdout = destination
        redirectMergedOut(stdout)
    }

    abstract fun redirectMergedOut(destination: ProcessOutputStream)

    fun followOut() = apply {
        followStdOut()
        followStdErr()
    }

    fun followOut(stdDestination: ProcessOutputStream, errDestination: ProcessOutputStream) = apply {
        followStdOut(stdDestination)
        followStdErr(errDestination)
    }

    fun followStdOut() = followStdOut(ProcessOutputStream(scope))

    fun followStdOut(destination: ProcessOutputStream) = apply {
        stdout = destination
        redirectStdOut(stdout)
    }

    abstract fun redirectStdOut(destination: ProcessOutputStream)

    fun followStdErr() = followStdErr(ProcessOutputStream(scope))

    fun followStdErr(destination: ProcessOutputStream) = apply {
        stderr = destination
        redirectStdOut(stderr)
    }

    abstract fun redirectStdErr(destination: ProcessOutputStream)

    abstract fun setEnvironment(env: Map<String, String>): Process

    abstract fun setEnvironment(env: Pair<String, String>): Process

    abstract fun start(): PCB

    abstract fun isAlive(): Boolean

    abstract fun await(timeout: Long = 0): Job

    internal fun closeOut() {
        if (::stdout.isInitialized) stdout.close()
        if (::stderr.isInitialized) stderr.close()
    }

    abstract fun kill()

    fun ifAlive(action: () -> Unit) {
        if (isAlive()) action()
    }

}
