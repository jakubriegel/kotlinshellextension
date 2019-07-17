package eu.jrie.jetbrains.kotlinshellextension.processes

import eu.jrie.jetbrains.kotlinshellextension.ShellPiping
import eu.jrie.jetbrains.kotlinshellextension.processes.process.Process
import eu.jrie.jetbrains.kotlinshellextension.processes.process.ProcessBuilder
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import java.io.File

/**
 * The entity representing pipeline.
 * Should be used with piping DSL
 *
 * @see ShellPiping
 */
class Pipeline private constructor (
    private val commander: ProcessCommander
) {

    private val processLine = mutableListOf<Process>()

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
                process.followIn(processLine.last().stdoutChannel)
            ).apply { start() }
        )
    }

    /**
     * Ends this [Pipeline] with [lambda]
     *
     * @see ShellPiping
     * @return this [Pipeline]
     */
    @ExperimentalCoroutinesApi
    fun toLambda(lambda: (Byte) -> Unit) = apply {
       commander.scope.launch { processLine.last().stdoutChannel.consumeEach(lambda) }
    }

    /**
     * Ends this [Pipeline] with writing its output to [file]
     *
     * @see ShellPiping
     * @return this [Pipeline]
     */
    @ExperimentalCoroutinesApi
    fun toFile(file: File) = appendFile(
        file.apply { delete() }
    )

    /**
     * Ends this [Pipeline] with appending its output to [file]
     *
     * @see ShellPiping
     * @return this [Pipeline]
     */
    @ExperimentalCoroutinesApi
    fun appendFile(file: File) = apply {
        TODO()
        //        processLine.last().stdout.subscribe {
//            file.appendBytes(ByteArray(1) { _ -> it })
//        }
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

    companion object {
        /**
         * Starts new [Pipeline] with process specified by given [start] [ProcessBuilder]
         *
         * @see ShellPiping
         */
        internal fun from(start: ProcessBuilder, commander: ProcessCommander): Pipeline {
            val process = commander.process(start).apply { start() }
            val pipeline = Pipeline(commander)
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
        internal fun fromFile(file: File, process: ProcessBuilder, commander: ProcessCommander) = from(process.followFile(file), commander)
    }

}
