package eu.jrie.jetbrains.kotlinshellextension.processes.pipeline

import eu.jrie.jetbrains.kotlinshellextension.ShellPiping
import eu.jrie.jetbrains.kotlinshellextension.processes.ProcessCommander
import eu.jrie.jetbrains.kotlinshellextension.processes.process.Process
import eu.jrie.jetbrains.kotlinshellextension.processes.process.ProcessBuilder
import eu.jrie.jetbrains.kotlinshellextension.processes.process.ProcessChannel
import eu.jrie.jetbrains.kotlinshellextension.processes.process.ProcessIOBuffer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.io.core.ByteReadPacket
import kotlinx.io.streams.readPacketAtMost
import kotlinx.io.streams.writePacket
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

typealias PipelineLambda = (ByteReadPacket) -> Unit

/**
 * The entity representing pipeline.
 * Should be used with piping DSL
 *
 * @see ShellPiping
 */
@ExperimentalCoroutinesApi
class Pipeline private constructor (
    private val commander: ProcessCommander
) {

    private val processLine = mutableListOf<Process>()

    private lateinit var lastBuffer: ProcessIOBuffer

    private val asyncJobs = mutableListOf<Job>()

    /**
     * Read only list of [Process]es added to this [Pipeline].
     * Most recent [Process] is at the end of the list
     */
    val processes: List<Process>
        get() = processLine.toList()

    /**
     * Starts new [Pipeline] with process specified by given [ProcessBuilder]
     *
     * @see ShellPiping
     */
    internal constructor(process: ProcessBuilder, commander: ProcessCommander) : this(commander) {
        addProcess(process)
    }

    /**
     * Starts new [Pipeline] with [file]
     *
     * @see ShellPiping
     */
    internal constructor(file: File, commander: ProcessCommander) : this(file.inputStream(), commander)

    /**
     * Starts new [Pipeline] with [string]
     *
     * @see ShellPiping
     */
    internal constructor(string: String, commander: ProcessCommander) : this(string.byteInputStream(), commander)

    private constructor(stream: InputStream, commander: ProcessCommander) : this(commander) {
        val buffer = buffer()
        val channel: ProcessChannel = Channel(STREAM_RW_CHANNEL_BUFFER_SIZE)
        launch { buffer.consumeFrom(channel) }
        launch {
            stream.use {
                while (it.available() > 0) {
                    val packet = it.readPacketAtMost(STREAM_RW_PACKET_SIZE)
                    channel.send(packet)
                }
                channel.close()
            }
        }
    }

    /**
     * Starts new [Pipeline] with given [buffer] as start
     *
     * @see ShellPiping
     */
    internal constructor(buffer: ProcessIOBuffer, commander: ProcessCommander) : this(commander) {
        lastBuffer = buffer
    }

    /**
     * Adds [process] to this [Pipeline]
     *
     * @see ShellPiping
     * @return this [Pipeline]
     */
    fun toProcess(process: ProcessBuilder) = apply {
        addProcess(process.withStdinBuffer(lastBuffer))
    }

    private fun addProcess(process: ProcessBuilder) = apply {
        processLine.add(
            commander.createProcess(
                process.withStdoutBuffer(buffer())
            ).also { commander.startProcess(it) }
        )
    }

    /**
     * Ends this [Pipeline] with [lambda]
     *
     * @see ShellPiping
     * @return this [Pipeline]
     */
    fun toLambda(lambda: PipelineLambda) = apply {
        val lambdaChannel: ProcessChannel = Channel(STREAM_RW_CHANNEL_BUFFER_SIZE)
        launch { lastBuffer.receiveTo(lambdaChannel) }
        launch { lambdaChannel.consumeEach(lambda) }
    }

    /**
     * Ends this [Pipeline] with writing its output to [file]
     *
     * @see ShellPiping
     * @return this [Pipeline]
     */
    fun toFile(file: File) = apply { writeFile(file, false) }

    /**
     * Ends this [Pipeline] with appending its output to [file]
     *
     * @see ShellPiping
     * @return this [Pipeline]
     */
    fun appendFile(file: File) = apply { writeFile(file, true) }

    private fun writeFile(file: File, append: Boolean) {
        val fileWriteChannel: ProcessChannel = Channel(STREAM_RW_CHANNEL_BUFFER_SIZE)
        launchIO {
            FileOutputStream(file, append).use {
                fileWriteChannel.consumeEach { p ->
                    it.writePacket(p)
                    it.flush()
                }
                it.close()
            }
        }
        launch { lastBuffer.receiveTo(fileWriteChannel) }
    }

    /**
     * Awaits all processes in this [Pipeline]
     *
     * @see ShellPiping
     * @return this [Pipeline]
     */
    suspend fun await() = apply {
        processLine.forEach { commander.awaitProcess(it) }
        asyncJobs.forEach { it.join() }
    }

    /**
     * Kills all processes in this [Pipeline]
     *
     * @see ShellPiping
     * @return this [Pipeline]
     */
    suspend fun kill() = apply {
        processLine.forEach { commander.killProcess(it) }
        asyncJobs.forEach { it.join() }
    }

    /**
     * Returns new [ProcessIOBuffer] and sets it as [lastBuffer]
     */
    private fun buffer() = ProcessIOBuffer().also { lastBuffer = it }

    private fun launchIO(ioBlock: suspend CoroutineScope.() -> Unit) = launch {
        withContext(Dispatchers.IO, ioBlock)
    }

    private fun launch(block: suspend CoroutineScope.() -> Unit) {
        asyncJobs.add(commander.scope.launch(block = block))
    }

    companion object {
        private const val STREAM_RW_PACKET_SIZE: Long = 256
        private const val STREAM_RW_CHANNEL_BUFFER_SIZE = 16
    }

}
