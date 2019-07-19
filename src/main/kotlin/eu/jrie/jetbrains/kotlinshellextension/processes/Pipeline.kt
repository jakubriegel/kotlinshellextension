package eu.jrie.jetbrains.kotlinshellextension.processes

import eu.jrie.jetbrains.kotlinshellextension.ShellPiping
import eu.jrie.jetbrains.kotlinshellextension.processes.process.Process
import eu.jrie.jetbrains.kotlinshellextension.processes.process.ProcessBuilder
import eu.jrie.jetbrains.kotlinshellextension.processes.process.ProcessChannel
import eu.jrie.jetbrains.kotlinshellextension.processes.process.ProcessIOBuffer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.io.core.ByteReadPacket
import kotlinx.io.streams.readPacketAtMost
import kotlinx.io.streams.writePacket
import java.io.File

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

    /**
     * Read only list of [Process]es added to this [Pipeline].
     * Most recent [Process] is at the end of the list
     */
    val processes: List<Process>
        get() = processLine.toList()

    /**
     * Adds [process] to this [Pipeline]
     *
     * @see ShellPiping
     * @return this [Pipeline]
     */
    fun toProcess(process: ProcessBuilder) = apply {
        processLine.add(
            commander.process(
                process
                    .withStdinBuffer(lastBuffer)
                    .withStdoutBuffer(buffer())
            ).apply { start() }
        )
    }

    /**
     * Ends this [Pipeline] with [lambda]
     *
     * @see ShellPiping
     * @return this [Pipeline]
     */
    fun toLambda(lambda: PipelineLambda) = apply {
        val lambdaChannel: ProcessChannel = Channel(128) // TODO: size
        commander.scope.launch { lastBuffer.receiveTo(lambdaChannel) }
        commander.scope.launch { lambdaChannel.consumeEach(lambda) }
    }

    /**
     * Ends this [Pipeline] with writing its output to [file]
     *
     * @see ShellPiping
     * @return this [Pipeline]
     */
    fun toFile(file: File) = appendFile(
        file.apply { delete() }
    )

    /**
     * Ends this [Pipeline] with appending its output to [file]
     *
     * @see ShellPiping
     * @return this [Pipeline]
     */
    fun appendFile(file: File) = apply {
        val fileWriteChannel: ProcessChannel = Channel(FILE_RW_CHANNEL_BUFFER_SIZE)
        commander.scope.launchIO {
            file.outputStream().use {
                fileWriteChannel.consumeEach { p ->
                    it.writePacket(p)
                    it.flush()
                }
                it.close()
            }
        }
        commander.scope.launch { lastBuffer.receiveTo(fileWriteChannel) }

    }

    /**
     * Awaits all processes in this [Pipeline]
     *
     * @see ShellPiping
     * @return this [Pipeline]
     */
    suspend fun await() = apply {
        processLine.forEach { commander.awaitProcess(it) }
    }

    private fun buffer() = ProcessIOBuffer().also { lastBuffer = it }

    companion object {
        /**
         * Starts new [Pipeline] with process specified by given [start] [ProcessBuilder]
         *
         * @see ShellPiping
         */
        internal fun from(start: ProcessBuilder, commander: ProcessCommander): Pipeline {
            val pipeline = Pipeline(commander)
            start.withStdoutBuffer(pipeline.buffer())
            val process = commander.process(start).apply { start() }
            pipeline.processLine.add(process)
            return pipeline
        }

        /**
         * Starts new [Pipeline] with [File] specified by [path] as input of [process]
         *
         * @see ShellPiping
         */
        internal fun fromFile(path: String, process: ProcessBuilder, commander: ProcessCommander) = fromFile(File(path), process, commander)

        /**
         * Starts new [Pipeline] with [file] as input of [process]
         *
         * @see ShellPiping
         */
        internal fun fromFile(file: File, process: ProcessBuilder, commander: ProcessCommander): Pipeline {
            val buffer = ProcessIOBuffer()
            val fileReadChannel: ProcessChannel = Channel(FILE_RW_CHANNEL_BUFFER_SIZE)
            commander.scope.launchIO {
                file.inputStream().use {
                    while (it.available() > 0) {
                        val packet = it.readPacketAtMost(FILE_RW_PACKET_SIZE)
                        fileReadChannel.send(packet)
                    }
                    fileReadChannel.close()
                }
            }
            commander.scope.launch { buffer.consumeFrom(fileReadChannel) }

            return from(process.withStdinBuffer(buffer), commander)
        }

        private const val FILE_RW_PACKET_SIZE: Long = 256
        private const val FILE_RW_CHANNEL_BUFFER_SIZE = 16

        private fun CoroutineScope.launchIO(ioBlock: suspend () -> Unit) = launch {
            withContext(Dispatchers.IO) {
                ioBlock()
            }
        }

    }

}
