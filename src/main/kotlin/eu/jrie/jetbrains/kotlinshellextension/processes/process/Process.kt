package eu.jrie.jetbrains.kotlinshellextension.processes.process

import eu.jrie.jetbrains.kotlinshellextension.processes.process.stream.ProcessStream
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*

abstract class Process protected constructor (
    val vPID: Int,
    val input: ProcessStream,
    val stdout: ProcessStream,
    val stderr: ProcessStream,
    val environment: Map<String, String>,
    val directory: File,
    val scope: CoroutineScope
) {

    abstract val pcb: PCB

    open val name: String
        get() = "[${this::class.simpleName} $vPID]"

    val status: String
        get() = "$vPID\t${since(pcb.startTime)}\t$statusCmd\t$statusOther state=${pcb.state}"

    protected abstract val statusCmd: String
    protected abstract val statusOther: String

    init {
        input.initialize(vPID, scope)
        stdout.initialize(vPID, scope)
        stderr.initialize(vPID, scope)
    }

    fun start(): PCB {
        return if (pcb.state != ProcessState.READY) {
            throw Exception("only READY process can be started")
        }
        else {
            execute()
            pcb.state = ProcessState.RUNNING
            logger.debug("started $name")
            pcb
        }
    }

    protected abstract fun execute(): PCB

    abstract fun isAlive(): Boolean

    fun ifAlive(action: () -> Unit) {
        if (isAlive()) action()
    }

    fun await(timeout: Long = 0): Job {
        val awaitJob = expect(timeout)
        awaitJob.invokeOnCompletion {
            pcb.state = ProcessState.TERMINATED
            logger.debug("awaited process $name")
        }
        return awaitJob
    }

    abstract fun expect(timeout: Long): Job

    internal fun closeOut() {
        stdout.close()
        stderr.close()
        logger.debug("closed out of $name")
    }

    fun kill() {
        destroy()
        pcb.state = ProcessState.TERMINATED
        logger.debug("killed process $name")
    }

    abstract fun destroy()

    companion object {
        @JvmStatic
        internal val logger: Logger = LoggerFactory.getLogger(Process::class.java)

        private val formatter = SimpleDateFormat("HH:mm:ss")

        private fun since(instant: Instant?): String {
            return if (instant == null) "n/a"
            else formatter.format(Date.from(instant))
        }
    }
}
