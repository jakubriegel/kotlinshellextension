package eu.jrie.jetbrains.kotlinshellextension

import eu.jrie.jetbrains.kotlinshellextension.processes.Pipeline
import eu.jrie.jetbrains.kotlinshellextension.processes.PipelineLambda
import eu.jrie.jetbrains.kotlinshellextension.processes.ProcessCommander
import eu.jrie.jetbrains.kotlinshellextension.processes.process.ProcessBuilder
import eu.jrie.jetbrains.kotlinshellextension.processes.process.ProcessIOBuffer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.io.core.ByteReadPacket
import java.io.File

typealias PipelineFork = (ProcessIOBuffer) -> Unit

typealias PipelineForkPair = Pair<ProcessBuilder, PipelineFork>

@ExperimentalCoroutinesApi
abstract class ShellPiping (
    private val commander: ProcessCommander
) {
    /**
     * ****************** | ****************** | ******************
     */

    infix fun (() -> ByteReadPacket).pipe(process: ProcessBuilder) {}

    infix fun ProcessBuilder.pipe(fork: PipelineForkPair) = fork.first
        .let { process ->
            forkErr(process, fork.second)
            from(this) pipe process
        }


    infix fun PipelineForkPair.pipe(process: ProcessBuilder): Pipeline {
        forkErr(first, second)
        return from(first) pipe process
    }

    infix fun PipelineForkPair.pipe(lambda: PipelineLambda): Pipeline {
        forkErr(first, second)
        return from(first) pipe lambda
    }

    infix fun PipelineForkPair.pipe(file: File): Pipeline {
        forkErr(first, second)
        return from(first) pipe file
    }

    private fun forkErr(process: ProcessBuilder, fork: PipelineFork) {
        ProcessIOBuffer().let {
            process.withStderrBuffer(it)
            fork.invoke(it)
        }
    }

    infix fun ProcessIOBuffer.pipe(lambda: PipelineLambda) = from(this) pipe lambda

    infix fun ProcessIOBuffer.pipe(process: ProcessBuilder) = from(this) pipe process

    private fun from(buffer: ProcessIOBuffer) = Pipeline.fromBuffer(buffer, commander)

    infix fun ProcessBuilder.forkErr(fork: PipelineFork) = this to fork


    fun forkErr(fork: PipelineFork) = fork

    /**
     * ****************** | ****************** | ******************
     */

    /**
     * Starts new [Pipeline] fromBuffer process specified by given [ProcessBuilder].
     * Shall be wrapped with piping DSL
     *
     * @see ProcessBuilder.pipe
     * @return this [Pipeline]
     */
    @ExperimentalCoroutinesApi
    fun from(process: ProcessBuilder) = Pipeline.from(process, commander)

    /**
     * Starts new [Pipeline] fromBuffer this process process one specified by [process].
     * Part of piping DSL
     *
     * @see ProcessBuilder.pipe
     * @return this [Pipeline]
     */
    @ExperimentalCoroutinesApi
    infix fun ProcessBuilder.pipe(process: ProcessBuilder) = from(this) pipe process

    /**
     * Starts new [Pipeline] fromBuffer this process tp [lambda] function.
     * Part of piping DSL
     *
     * @see ProcessBuilder.pipe
     * @return this [Pipeline]
     */
    @ExperimentalCoroutinesApi
    infix fun ProcessBuilder.pipe(lambda: PipelineLambda) = from(this) pipe lambda

    /**
     * Starts new pipeline with [file] as an input of given [process].
     * Shall be wrapped with piping DSL
     *
     * @return this [Pipeline]
     */
    @ExperimentalCoroutinesApi
    fun from(file: File, process: ProcessBuilder) = Pipeline.fromFile(file, process, commander)

    /**
     * Starts new pipeline with this [File] as an input of given [process].
     * Shall be wrapped with piping DSL
     *
     * @return this [Pipeline]
     */
    @ExperimentalCoroutinesApi
    infix fun File.pipe(process: ProcessBuilder) = from(this, process)

    /**
     * Adds [process] process to this pipeline.
     * Shall be wrapped with piping DSL
     *
     * @return this [Pipeline]
     */
    @ExperimentalCoroutinesApi
    infix fun Pipeline.pipe(process: ProcessBuilder) = toProcess(process)

    /**
     * Ends this [Pipeline] with [lambda] function
     * Shall be wrapped with piping DSL
     *
     * @return this [Pipeline]
     */
    @ExperimentalCoroutinesApi // TODO: implement KtsProcess
    infix fun Pipeline.pipe(lambda: PipelineLambda) = toLambda(lambda)

    /**
     * Ends this [Pipeline] by writing its output to [file].
     * Shall be wrapped with piping DSL
     *
     * @return this [Pipeline]
     */
    @ExperimentalCoroutinesApi
    infix fun Pipeline.pipe(file: File) = toFile(file)

    /**
     * Ends this [Pipeline] by appending its output file [file].
     * Shall be wrapped with piping DSL
     *
     * @return this [Pipeline]
     */
    @ExperimentalCoroutinesApi
    infix fun Pipeline.append(file: File) = appendFile(file)

    /**
     * Awaits this [Pipeline]
     * Shall be wrapped with piping DSL
     *
     * @see Pipeline.await
     * @return this [Pipeline]
     */
    @ExperimentalCoroutinesApi
    suspend infix fun Pipeline.await(all: All) = await()
}

object All
typealias all = All

/**
 * Alias for piping output to print().
 * To be use with [ShellPiping.pipe]
 *
 * @see ShellPiping
 * @see Pipeline
 */
val stdout: PipelineLambda = { print(it.readText()) }

/**
 * Alias for piping output to nowhere. Works line `> /dev/null`.
 * To be use with [ShellPiping.pipe]
 *
 * @see ShellPiping
 * @see Pipeline
 */
val nullout: PipelineLambda = {}
