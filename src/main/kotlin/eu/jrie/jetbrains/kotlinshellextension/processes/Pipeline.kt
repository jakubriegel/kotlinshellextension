package eu.jrie.jetbrains.kotlinshellextension.processes

import eu.jrie.jetbrains.kotlinshellextension.ShellPiping
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
        val lambdaChannel: ProcessChannel = Channel(128) // TODO: size
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
        val fileWriteChannel: ProcessChannel = Channel(FILE_RW_CHANNEL_BUFFER_SIZE)
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

    private fun buffer() = ProcessIOBuffer().also { lastBuffer = it }

    private fun launchIO(ioBlock: suspend CoroutineScope.() -> Unit) = launch {
        withContext(Dispatchers.IO, ioBlock)
    }

    private fun launch(block: suspend CoroutineScope.() -> Unit) {
        asyncJobs.add(commander.scope.launch(block = block))
    }

    companion object {
        /**
         * Starts new [Pipeline] with process specified by given [start] [ProcessBuilder]
         *
         * @see ShellPiping
         */
        internal fun from(start: ProcessBuilder, commander: ProcessCommander): Pipeline {
            val pipeline = Pipeline(commander)
            val b = pipeline.buffer()
            start.withStdoutBuffer(b)
            val process = commander.process(start).also { commander.startProcess(it) }
            pipeline.processLine.add(process)
            return pipeline
        }

        /**
         * Starts new [Pipeline] with [file] as input of [process]
         *
         * @see ShellPiping
         */
        internal fun fromFile(file: File, process: ProcessBuilder, commander: ProcessCommander): Pipeline {
            val pipeline = Pipeline(commander)

            val buffer = ProcessIOBuffer()
            pipeline.lastBuffer = buffer
            val fileReadChannel: ProcessChannel = Channel(FILE_RW_CHANNEL_BUFFER_SIZE)
            pipeline.launchIO {
                file.inputStream().use {
                    while (it.available() > 0) {
                        val packet = it.readPacketAtMost(FILE_RW_PACKET_SIZE)
                        fileReadChannel.send(packet)
                    }
                    fileReadChannel.close()
                }
            }
            pipeline.launch { buffer.consumeFrom(fileReadChannel) }

            return pipeline.toProcess(process)
        }

        /**
         * Starts new [Pipeline] with given [buffer] as start
         *
         * @see ShellPiping
         */
        internal fun fromBuffer(buffer: ProcessIOBuffer, commander: ProcessCommander): Pipeline {
            val pipeline = Pipeline(commander)
            pipeline.lastBuffer = buffer
            return pipeline
        }

        private const val FILE_RW_PACKET_SIZE: Long = 256
        private const val FILE_RW_CHANNEL_BUFFER_SIZE = 16
    }

}
