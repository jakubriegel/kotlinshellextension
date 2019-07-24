package eu.jrie.jetbrains.kotlinshellextension.processes.process

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import kotlinx.io.core.ByteReadPacket
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*

typealias ProcessChannel = Channel<ByteReadPacket>
typealias ProcessReceiveChannel = ReceiveChannel<ByteReadPacket>
typealias ProcessSendChannel = SendChannel<ByteReadPacket>

@ExperimentalCoroutinesApi
abstract class Process @ExperimentalCoroutinesApi
protected constructor (
    val vPID: Int,
    val environment: Map<String, String>,
    val directory: File,
    private val stdinBuffer: ProcessIOBuffer? = null,
    private val stdoutBuffer: ProcessIOBuffer? = null,
    private val stderrBuffer: ProcessIOBuffer? = null,
    protected val scope: CoroutineScope
) {

    protected val stdin: ProcessReceiveChannel?
    protected val stdout: ProcessSendChannel
    protected val stderr: ProcessSendChannel?
    private val ioJobs = mutableListOf<Job>()

    abstract val pcb: PCB

    open val name: String
        get() = "[${this::class.simpleName} $vPID]"

    val status: String
        get() = "$vPID\t${since(pcb.startTime)}\t$statusCmd\t$statusOther state=${pcb.state}"

    protected abstract val statusCmd: String
    protected abstract val statusOther: String

    init {
        stdin = initIn()

        val out = initOut()
        stdout = out.first
        stderr = out.second
    }

    private fun initIn(): ProcessReceiveChannel? {
        return if (stdinBuffer != null) {
            channel().also { launchIO { stdinBuffer.receiveTo(it) } }
        } else null
    }

    private fun initOut(): Pair<ProcessSendChannel, ProcessSendChannel?> {
        val std: ProcessChannel = channel()
        var err: ProcessChannel? = channel()
        when {
            stdoutBuffer == null && stderrBuffer == null -> {
                launchIO { consumeAndPrint(std) }
                err = null
            }
            stdoutBuffer != null && stderrBuffer == null -> {
                launchIO { stdoutBuffer.consumeFrom(std) }
                launchIO { consumeAndPrint(err!!) }
            }
            stdoutBuffer == null && stderrBuffer != null -> {
                launchIO { consumeAndPrint(std) }
                launchIO { stderrBuffer.consumeFrom(err!!) }
            }
            stdoutBuffer != null && stderrBuffer != null -> {
                launchIO { stdoutBuffer.consumeFrom(std) }
                launchIO { stderrBuffer.consumeFrom(err!!) }
            }
        }
        return std to err
    }

    private suspend fun consumeAndPrint(channel: ProcessReceiveChannel) {
        channel.consumeEach { print(it.readText()) }
    }

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

    internal suspend fun await(timeout: Long = 0) {
        if (isAlive()) {
            expect(timeout)
            ioJobs.forEach { it.join() }
            pcb.endTime = Instant.now()
            pcb.state = ProcessState.TERMINATED
        }
    }

    protected abstract suspend fun expect(timeout: Long)

    internal fun closeOut() {
        stdout.close()
        stderr?.close()
        logger.debug("closed out of $name")
    }

    internal fun kill() {
        destroy()
        pcb.state = ProcessState.TERMINATED
        logger.debug("killed process $name")
    }

    protected abstract fun destroy()

    private fun launchIO(ioBlock: suspend CoroutineScope.() -> Unit) {
        ioJobs.add(scope.launch(block = ioBlock))
    }

    companion object {

        private const val DEFAULT_PROCESS_CHANNEL_SIZE = 128

        internal fun channel(): ProcessChannel = Channel(DEFAULT_PROCESS_CHANNEL_SIZE)

        @JvmStatic
        internal val logger: Logger = LoggerFactory.getLogger(Process::class.java)

        private val formatter = SimpleDateFormat("HH:mm:ss")

        private fun since(instant: Instant?): String {
            return if (instant == null) "n/a"
            else formatter.format(Date.from(instant))
        }
    }
}
