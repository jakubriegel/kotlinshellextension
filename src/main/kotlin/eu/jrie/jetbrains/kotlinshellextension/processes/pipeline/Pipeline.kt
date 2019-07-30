package eu.jrie.jetbrains.kotlinshellextension.processes.pipeline

import eu.jrie.jetbrains.kotlinshellextension.processes.ProcessCommander
import eu.jrie.jetbrains.kotlinshellextension.processes.execution.ExecutionContext
import eu.jrie.jetbrains.kotlinshellextension.processes.execution.ProcessExecutable
import eu.jrie.jetbrains.kotlinshellextension.processes.execution.ProcessExecutionContext
import eu.jrie.jetbrains.kotlinshellextension.processes.process.Process
import eu.jrie.jetbrains.kotlinshellextension.processes.process.ProcessChannelUnit
import eu.jrie.jetbrains.kotlinshellextension.processes.process.ProcessReceiveChannel
import eu.jrie.jetbrains.kotlinshellextension.processes.process.ProcessSendChannel
import eu.jrie.jetbrains.kotlinshellextension.shell.piping.ShellPiping
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.io.core.BytePacketBuilder
import kotlinx.io.core.ByteReadPacket
import kotlinx.io.streams.readPacketAtMost
import kotlinx.io.streams.writePacket
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream

typealias PipelineContextLambda = suspend (
    context: ExecutionContext
) -> Unit

/**
 * The entity representing pipeline.
 * Should be used with piping DSL
 *
 * @see ShellPiping
 */
@ExperimentalCoroutinesApi
class Pipeline private constructor (
    private val context: ProcessExecutionContext
) {

    var ended = false
        private set

    private val processLine = mutableListOf<Process>()

    private lateinit var lastOut: ProcessReceiveChannel

    private val asyncJobs = mutableListOf<Job>()

    /**
     * Read only list of [Process]es added to this [Pipeline].
     * Most recent [Process] is at the end of the list
     */
    val processes: List<Process>
        get() = processLine.toList()

    /**
     * Starts new [Pipeline] with process specified by given [ProcessExecutable]
     *
     * @see ShellPiping
     */
    internal constructor(process: ProcessExecutable, context: ProcessExecutionContext) : this(context) {
        addProcess(process)
    }

    /**
     * Starts new [Pipeline] with [lambda]
     *
     * @see ShellPiping
     */
    internal constructor(lambda: PipelineContextLambda, context: ProcessExecutionContext) : this(context) {
        addLambda(lambda, end = false, closeOut = true)
    }

    /**
     * Starts new [Pipeline] with [lambda]
     *
     * @see ShellPiping
     */
    internal constructor(channel: ProcessReceiveChannel, context: ProcessExecutionContext) : this(context) {
        lastOut = channel
    }

    /**
     * Starts new [Pipeline] with given [stream]
     *
     * @see ShellPiping
     */
    internal constructor(stream: InputStream, context: ProcessExecutionContext) : this(context) {
        addLambda(
            { ctx ->
                stream.use {
                    while (it.available() > 0) {
                        val packet = it.readPacketAtMost(PIPELINE_CHANNEL_PACKET_SIZE)
                        ctx.stdout.send(packet)
                    }
                }
            },
            end = false, closeOut = true
        )
    }

    /**
     * Adds [process] to this [Pipeline]
     *
     * @see ShellPiping
     * @return this [Pipeline]
     */
    fun throughProcess(process: ProcessExecutable) = apply {
        addProcess(process.updateContext(newIn = lastOut))
    }

    private fun addProcess(exec: ProcessExecutable) = ifNotEnded {
        exec.updateContext(newOut = channel()).init()

        launch {
            exec.exec()
            exec.await()
            exec.context.stdout.close()
        }

        processLine.add(exec.process)
    }

    /**
     * Adds [lambda] to this [Pipeline]
     *
     * @see ShellPiping
     * @return this [Pipeline]
     */
    fun throughLambda(end: Boolean = false, closeOut: Boolean = true, lambda: PipelineContextLambda) = apply {
        addLambda(lambda, context.updated(newIn = lastOut), end, closeOut)
    }

    private fun addLambda(
        lambda: PipelineContextLambda,
        lambdaContext: PipelineExecutionContext = PipelineExecutionContext(context),
        end: Boolean,
        closeOut: Boolean
    ) = ifNotEnded {
        lambdaContext
            .let { if (!end) it.updated(newOut = channel()) else it }
            .let { ctx ->
                launch {
                    lambda(ctx)
                    if (closeOut) ctx.stdout.close()
                }
            }
    }

    private suspend fun toEndLambda(
        closeOut: Boolean = true, lambda: suspend (ByteReadPacket) -> Unit
    ) = apply {
        throughLambda(end = true, closeOut = closeOut) { ctx ->
            ctx.stdin.consumeEach { lambda(it) }
        }
        ended = true
        await()
    }

    /**
     * Ends this [Pipeline] with [channel]
     *
     * @see ShellPiping
     * @return this [Pipeline]
     */
    suspend fun toEndChannel(channel: ProcessSendChannel) = toEndLambda { channel.send(it) }

    internal suspend fun toDefaultEndChannel(channel: ProcessSendChannel) = toEndLambda(false) {
        channel.send(it)
    }

    /**
     * Ends this [Pipeline] with [packetBuilder]
     *
     * @see ShellPiping
     * @return this [Pipeline]
     */
    suspend fun toEndPacket(packetBuilder: BytePacketBuilder) = toEndLambda { packetBuilder.writePacket(it) }

    /**
     * Ends this [Pipeline] with [stream]
     *
     * @see ShellPiping
     * @return this [Pipeline]
     */
    suspend fun toEndStream(stream: OutputStream) = toEndLambda { stream.writePacket(it) }

    /**
     * Ends this [Pipeline] with [file]
     *
     * @see ShellPiping
     * @return this [Pipeline]
     */
    suspend fun toEndFile(file: File, append: Boolean = false) = toEndStream(FileOutputStream(file, append))

    /**
     * Ends this [Pipeline] with [stringBuilder]
     *
     * @see ShellPiping
     * @return this [Pipeline]
     */
    suspend fun toEndStringBuilder(stringBuilder: StringBuilder) = toEndLambda { stringBuilder.append(it) }

    /**
     * Awaits all processes in this [Pipeline]
     *
     * @see ShellPiping
     * @return this [Pipeline]
     */
    suspend fun await() = apply {
        logger.debug("awaiting pipeline $this")
        processLine.forEach { context.commander.awaitProcess(it) }
        asyncJobs.forEach { it.join() }
        logger.debug("awaited pipeline $this")
    }

    /**
     * Kills all processes in this [Pipeline]
     *
     * @see ShellPiping
     * @return this [Pipeline]
     */
    suspend fun kill() = apply {
        logger.debug("killing pipeline $this")
        processLine.forEach { context.commander.killProcess(it) }
        asyncJobs.forEach { it.join() }
        logger.debug("killed pipeline $this")
    }

    /**
     * Returns new [ProcessSendChannel] and sets it as [lastOut]
     */
    private fun channel(): ProcessSendChannel = Channel<ProcessChannelUnit>(PIPELINE_CHANNEL_BUFFER_SIZE).also { lastOut = it }

    private fun launchIO(ioBlock: suspend CoroutineScope.() -> Unit) = launch {
        withContext(Dispatchers.IO, ioBlock)
    }

    private fun launch(block: suspend CoroutineScope.() -> Unit) {
        asyncJobs.add(context.commander.scope.launch(block = block))
    }

    private fun ifNotEnded(block: () -> Unit) {
        if (ended) throw Exception("Pipeline ended")
        else block()
    }

    companion object {
        private const val PIPELINE_CHANNEL_PACKET_SIZE: Long = 256
        private const val PIPELINE_CHANNEL_BUFFER_SIZE = 16

        private val logger = LoggerFactory.getLogger(Pipeline::class.java)
    }

    private class PipelineExecutionContext (
        override val stdin: ProcessReceiveChannel,
        override val stdout: ProcessSendChannel,
        override val stderr: ProcessSendChannel,
        override val commander: ProcessCommander
    ) : ProcessExecutionContext {
        constructor(context: ProcessExecutionContext)
                : this(context.stdin, context.stdout, context.stderr, context.commander)
    }

    private fun ProcessExecutable.updateContext(
        newIn: ProcessReceiveChannel = this.context.stdin,
        newOut: ProcessSendChannel = this.context.stdout,
        newErr: ProcessSendChannel = this.context.stderr
    ) = apply {
        this.context = PipelineExecutionContext(
            newIn, newOut, newErr, (this.context as ProcessExecutionContext).commander
        )
    }

    private fun ProcessExecutionContext.updated(
        newIn: ProcessReceiveChannel = this.stdin,
        newOut: ProcessSendChannel = this.stdout,
        newErr: ProcessSendChannel = this.stderr
    ) = PipelineExecutionContext(newIn, newOut, newErr, commander)
}
