package eu.jrie.jetbrains.kotlinshellextension.shell

import eu.jrie.jetbrains.kotlinshellextension.processes.pipeline.Pipeline
import eu.jrie.jetbrains.kotlinshellextension.processes.pipeline.PipelineLambda
import eu.jrie.jetbrains.kotlinshellextension.processes.process.ProcessBuilder
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.io.core.BytePacketBuilder
import java.io.File

@ExperimentalCoroutinesApi
interface ShellPiping : ShellBase {
    /**
     * Starts new [Pipeline] from process specified by given [ProcessBuilder].
     * Shall be wrapped with piping DSL
     *
     * @see ProcessBuilder.pipe
     * @return this [Pipeline]
     */
    @ExperimentalCoroutinesApi
    fun from(process: ProcessBuilder) =
        Pipeline(process, commander)

    /**
     * Starts new [Pipeline] from this process one specified by [process].
     * Part of piping DSL
     *
     * @see ProcessBuilder.pipe
     * @return this [Pipeline]
     */
    @ExperimentalCoroutinesApi
    infix fun ProcessBuilder.pipe(process: ProcessBuilder) = from(this) pipe process

    /**
     * Starts new [Pipeline] from this process to [lambda] function.
     * Part of piping DSL
     *
     * @see ProcessBuilder.pipe
     * @return this [Pipeline]
     */
    @ExperimentalCoroutinesApi
    infix fun ProcessBuilder.pipe(lambda: PipelineLambda) = from(this) pipe lambda

    /**
     * Writes process output to [file].
     * Part of piping DSL
     *
     * @return this [Pipeline]
     */
    @ExperimentalCoroutinesApi
    infix fun ProcessBuilder.pipe(file: File) = from(this) pipe file

    /**
     * Writes process output to [packetBuilder].
     * Part of piping DSL
     *
     * @return this [Pipeline]
     */
    @ExperimentalCoroutinesApi
    infix fun ProcessBuilder.pipe(packetBuilder: BytePacketBuilder) = from(this) pipe packetBuilder

    /**
     * Writes process output to [stringBuilder].
     * Part of piping DSL
     *
     * @return this [Pipeline]
     */
    @ExperimentalCoroutinesApi
    infix fun ProcessBuilder.pipe(stringBuilder: StringBuilder) = from(this) pipe stringBuilder

    /**
     * Appends process output [file].
     * Part of piping DSL
     *
     * @return this [Pipeline]
     */
    @ExperimentalCoroutinesApi
    infix fun ProcessBuilder.append(file: File) = from(this) append file

//    /**
//     * Starts new [Pipeline] from this [ProcessIOBuffer].
//     * Shall be wrapped with piping DSL
//     *
//     * @see ProcessBuilder.pipe
//     * @return this [Pipeline]
//     */
//    private fun from(buffer: ProcessIOBuffer) =
//        Pipeline(buffer, commander)
//
//    /**
//     * Starts new [Pipeline] from this [ProcessIOBuffer] to [lambda].
//     * Part of piping DSL
//     *
//     * @see ProcessBuilder.pipe
//     * @return this [Pipeline]
//     */
//    infix fun ProcessIOBuffer.pipe(lambda: PipelineLambda) = from(this) pipe lambda
//
//    /**
//     * Starts new [Pipeline] from this [ProcessIOBuffer] to [process].
//     * Part of piping DSL
//     *
//     * @see ProcessBuilder.pipe
//     * @return this [Pipeline]
//     */
//    infix fun ProcessIOBuffer.pipe(process: ProcessBuilder) = from(this) pipe process

    /**
     * Starts new pipeline with [file].
     * Shall be wrapped with piping DSL
     *
     * @return this [Pipeline]
     */
    @ExperimentalCoroutinesApi
    private fun from(file: File) = Pipeline(file, commander)

    /**
     * Starts new pipeline with this [File] as an input of given [process].
     * Part of piping DSL
     *
     * @return this [Pipeline]
     */
    @ExperimentalCoroutinesApi
    infix fun File.pipe(process: ProcessBuilder) = from(this) pipe process

    /**
     * Starts new pipeline with this [String].
     * Shall be wrapped with piping DSL
     *
     * @return this [Pipeline]
     */
    private fun from(string: String) =
        Pipeline(string, commander)

    /**
     * Starts new pipeline with this [String] as an input of given [process].
     * Part of piping DSL
     *
     * @return this [Pipeline]
     */
    infix fun String.pipe(process: ProcessBuilder) = from(this) pipe process

    /**
     * Adds [process] to this pipeline.
     * Part of piping DSL
     *
     * @return this [Pipeline]
     */
    @ExperimentalCoroutinesApi
    infix fun Pipeline.pipe(process: ProcessBuilder) = toProcess(process)

    /**
     * Ends this [Pipeline] with [lambda] function
     * Part of piping DSL
     *
     * @return this [Pipeline]
     */
    @ExperimentalCoroutinesApi // TODO: implement KtsProcess
    infix fun Pipeline.pipe(lambda: PipelineLambda) = toLambda(lambda)

    /**
     * Ends this [Pipeline] by writing its output to [file].
     * Part of piping DSL
     *
     * @return this [Pipeline]
     */
    @ExperimentalCoroutinesApi
    infix fun Pipeline.pipe(file: File) = toFile(file)

    /**
     * Ends this [Pipeline] by passing result to given [BytePacketBuilder]
     * Part of piping DSL
     *
     * @return this [Pipeline]
     */
    @ExperimentalCoroutinesApi
    infix fun Pipeline.pipe(packetBuilder: BytePacketBuilder) = toLambda {
        packetBuilder.writePacket(it)
    }

    /**
     * Ends this [Pipeline] by passing result to given [StringBuilder]
     * Part of piping DSL
     *
     * @return this [Pipeline]
     */
    @ExperimentalCoroutinesApi
    infix fun Pipeline.pipe(stringBuilder: StringBuilder) = toLambda {
        stringBuilder.append(it.readText())
    }

    /**
     * Ends this [Pipeline] by appending its output file [file].
     * Part of piping DSL
     *
     * @return this [Pipeline]
     */
    @ExperimentalCoroutinesApi
    infix fun Pipeline.append(file: File) = appendFile(file)

//    private fun forkErr(process: ProcessBuilder, fork: PipelineFork) {
//        ProcessIOBuffer().let {
//            process.withStderrBuffer(it)
//            fork.invoke(it)
//        }
//    }
//
//    /**
//     * Forks current [Pipeline] by creating new [Pipeline] with stderr from last process as an input
//     * Part of piping DSL
//     *
//     * @return this [ProcessBuilder]
//     */
//    infix fun ProcessBuilder.forkErr(fork: PipelineFork) = this.also { forkErr(this, fork) }

    /**
     * Awaits this [Pipeline]
     * Part of piping DSL
     *
     * @see Pipeline.await
     * @return this [Pipeline]
     */
    @Suppress("UNUSED_PARAMETER")
    @ExperimentalCoroutinesApi
    suspend infix fun Pipeline.await(all: All) = await()

    fun BytePacketBuilder.readString() = use { build().readText() }
}

/**
 * Object for [all] alias
 */
object All
/**
 * Alias to be used in piping DSL with [Shella.await]
 *
 * Ex: `p1 pipe p2 await all`
 *
 * @see ShellPiping
 * @see Pipeline
 */
typealias all = All

/**
 * Alias for piping output to print().
 *
 * @see ShellPiping
 * @see Pipeline
 */
val stdout: PipelineLambda = { print(it.readText()) }

/**
 * Alias for piping output to nowhere. Works like `> /dev/null`.

 *
 * @see ShellPiping
 * @see Pipeline
 */
@Deprecated("migration to ExecutorContext")
val nulloutOld: (Any) -> Unit = {}
