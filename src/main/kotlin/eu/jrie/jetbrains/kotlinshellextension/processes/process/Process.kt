package eu.jrie.jetbrains.kotlinshellextension.processes.process

import eu.jrie.jetbrains.kotlinshellextension.processes.process.stream.ProcessInputStream
import eu.jrie.jetbrains.kotlinshellextension.processes.process.stream.ProcessOutputStream
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import org.slf4j.Logger
import org.slf4j.LoggerFactory

abstract class Process protected constructor (
    val vPID: Int,
    val command: String,
    val arguments: List<String> = emptyList(),
    val scope: CoroutineScope
) {

    abstract val pcb: PCB

    open val name: String
        get() = "[$vPID $command]"

    lateinit var input: ProcessInputStream private set
    lateinit var stdout: ProcessOutputStream private set
    lateinit var stderr: ProcessOutputStream private set

    fun followIn() = followIn(ProcessInputStream(scope))

    fun followIn(source: ProcessInputStream) = apply {
        input = source
        input.vPID = vPID
        redirectIn(source)
        logger.trace("followed in of $name")
    }

    protected abstract fun redirectIn(source: ProcessInputStream)

    fun followMergedOut() = followMergedOut(
        ProcessOutputStream(scope)
    )

    fun followMergedOut(destination: ProcessOutputStream) = apply {
        stdout = destination
        stdout.vPID = vPID
        redirectMergedOut(stdout)
        logger.trace("followed merged out of $name")
    }

    protected abstract fun redirectMergedOut(destination: ProcessOutputStream)

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
        stdout.vPID = vPID
        redirectStdOut(stdout)
        logger.trace("followed stdout of $name")
    }

    protected abstract fun redirectStdOut(destination: ProcessOutputStream)

    fun followStdErr() = followStdErr(ProcessOutputStream(scope))

    fun followStdErr(destination: ProcessOutputStream) = apply {
        stderr = destination
        stderr.vPID = vPID
        redirectStdErr(stderr)
        logger.trace("followed stderr of $name")
    }

    protected abstract fun redirectStdErr(destination: ProcessOutputStream)

    abstract fun setEnvironment(env: Map<String, String>): Process

    abstract fun setEnvironment(env: Pair<String, String>): Process

    abstract fun start(): PCB

    abstract fun isAlive(): Boolean

    abstract fun await(timeout: Long = 0): Job

    internal fun closeOut() {
        if (::stdout.isInitialized) stdout.close()
        if (::stderr.isInitialized) stderr.close()
        logger.debug("closed out of $name")
    }

    abstract fun kill()

    fun ifAlive(action: () -> Unit) {
        if (isAlive()) action()
    }

    companion object {
        @JvmStatic
        protected val logger: Logger = LoggerFactory.getLogger(Process::class.java)
    }
}
