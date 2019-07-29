package eu.jrie.jetbrains.kotlinshellextension.processes.process

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.io.core.ByteReadPacket
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*

typealias ProcessChannelUnit = ByteReadPacket
typealias ProcessChannel = Channel<ProcessChannelUnit>
typealias ProcessReceiveChannel = ReceiveChannel<ProcessChannelUnit>
typealias ProcessSendChannel = SendChannel<ProcessChannelUnit>

@ExperimentalCoroutinesApi
abstract class Process @ExperimentalCoroutinesApi
protected constructor (
    val vPID: Int,
    val environment: Map<String, String>,
    val directory: File,
    protected val stdin: ProcessReceiveChannel,
    protected val stdout: ProcessSendChannel,
    protected val stderr: ProcessSendChannel,
    protected val scope: CoroutineScope
) {

    private val ioJobs = mutableListOf<Job>()

    abstract val pcb: PCB

    open val name: String
        get() = "[${this::class.simpleName} $vPID]"

    final override fun toString() = name

    val status: String
        get() = "$vPID\t${since(pcb.startTime)}\t$statusCmd\t$statusOther state=${pcb.state}"

    protected abstract val statusCmd: String
    protected abstract val statusOther: String

    internal suspend fun start() {
        logger.debug("started $this")
        if (pcb.state != ProcessState.READY) {
            throw Exception("only READY process can be started")
        }
        else {
            pcb.state = ProcessState.RUNNING
            execute().join()
            finalize()
            logger.debug("executed $name")
            pcb
        }
    }

    protected abstract suspend fun execute(): Job

    abstract fun isAlive(): Boolean

    internal suspend fun await(timeout: Long = 0) {
        if (isAlive()) {
            expect(timeout)
            finalize()
        }
    }

    protected abstract suspend fun expect(timeout: Long)

    internal fun closeOut() {
//        TODO: disconnect closing out with process execution
//        stdout.close()
//        stderr.close()
        logger.debug("closed out of $name")
    }

    internal suspend fun kill() {
        destroy()
        finalize()
        logger.debug("killed $name")
    }

    protected abstract fun destroy()

    private suspend fun finalize() {
        ioJobs.forEach { it.join() }
        pcb.endTime = Instant.now()
        pcb.state = ProcessState.TERMINATED
    }

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
