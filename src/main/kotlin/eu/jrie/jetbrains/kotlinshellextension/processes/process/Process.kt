package eu.jrie.jetbrains.kotlinshellextension.processes.process

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*

abstract class Process protected constructor (
    val vPID: Int,
    protected val input: ReceiveChannel<Byte>? = null,
    val environment: Map<String, String>,
    val directory: File,
    protected val scope: CoroutineScope
) {

    protected val stdout = channel()
    val stdoutChannel: ReceiveChannel<Byte>
        get() = stdout

    protected val stderr = channel()
    val stderrChannel: ReceiveChannel<Byte>
        get() = stderr

    abstract val pcb: PCB

    open val name: String
        get() = "[${this::class.simpleName} $vPID]"

    val status: String
        get() = "$vPID\t${since(pcb.startTime)}\t$statusCmd\t$statusOther state=${pcb.state}"

    protected abstract val statusCmd: String
    protected abstract val statusOther: String

    internal fun start(): PCB {
        return if (pcb.state != ProcessState.READY) {
            throw Exception("only READY process can be started")
        }
        else {
            pcb.state = ProcessState.RUNNING
            execute()
            logger.debug("started $name")
            pcb
        }
    }

    protected abstract fun execute(): PCB

    abstract fun isAlive(): Boolean

    @Deprecated("isAlive() should be checked directly due to suspend functions", ReplaceWith("if (isAlive()) action()"))
    fun ifAlive(action: () -> Unit) {
        if (isAlive()) action()
    }

    internal suspend fun await(timeout: Long = 0) {
        if (isAlive()) {
            expect(timeout)
            pcb.endTime = Instant.now()
            pcb.state = ProcessState.TERMINATED
        }
    }

    protected abstract suspend fun expect(timeout: Long)

    internal fun closeOut() {
        stdout.close()
        stderr.close()
        logger.debug("closed out of $name")
    }

    internal fun kill() {
        destroy()
        pcb.state = ProcessState.TERMINATED
        logger.debug("killed process $name")
    }

    protected abstract fun destroy()

    companion object {

        const val DEFAULT_PROCESS_CHANNEL_SIZE = 512

        private fun channel() = Channel<Byte>(DEFAULT_PROCESS_CHANNEL_SIZE)
        fun inChannel() = channel() as ReceiveChannel<Byte>
        fun outChannel() = channel() as SendChannel<Byte>

        @JvmStatic
        internal val logger: Logger = LoggerFactory.getLogger(Process::class.java)

        private val formatter = SimpleDateFormat("HH:mm:ss")

        private fun since(instant: Instant?): String {
            return if (instant == null) "n/a"
            else formatter.format(Date.from(instant))
        }
    }
}
