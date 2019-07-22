package eu.jrie.jetbrains.kotlinshellextension

import eu.jrie.jetbrains.kotlinshellextension.processes.Pipeline
import eu.jrie.jetbrains.kotlinshellextension.processes.PipelineLambda
import eu.jrie.jetbrains.kotlinshellextension.processes.ProcessCommander
import eu.jrie.jetbrains.kotlinshellextension.processes.process.ProcessBuilder
import eu.jrie.jetbrains.kotlinshellextension.processes.process.ProcessIOBuffer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.io.File

typealias PipelineFork = (@Suppress("EXPERIMENTAL_API_USAGE") ProcessIOBuffer) -> Unit

@ExperimentalCoroutinesApi
abstract class ShellPiping (
    private val commander: ProcessCommander
) {
    /**
     * ****************** | ****************** | ******************
     */

    private fun forkErr(process: ProcessBuilder, fork: PipelineFork) {
        ProcessIOBuffer().let {
            process.withStderrBuffer(it)
            fork.invoke(it)
        }
    }

    infix fun ProcessIOBuffer.pipe(lambda: PipelineLambda) = from(this) pipe lambda

    infix fun ProcessIOBuffer.pipe(process: ProcessBuilder) = from(this) pipe process

    private fun from(buffer: ProcessIOBuffer) = Pipeline.fromBuffer(buffer, commander)

    infix fun ProcessBuilder.forkErr(fork: PipelineFork) = this.also { forkErr(this, fork) }

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
     * Writes process output [file].
     * Part of piping DSL
     *
     * @return this [Pipeline]
     */
    @ExperimentalCoroutinesApi
    infix fun ProcessBuilder.pipe(file: File) = from(this) pipe file

    /**
     * Appends process output [file].
     * Part of piping DSL
     *
     * @return this [Pipeline]
     */
    @ExperimentalCoroutinesApi
    infix fun ProcessBuilder.append(file: File) = from(this) append file

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
    @Suppress("UNUSED_PARAMETER")
    @ExperimentalCoroutinesApi
    suspend infix fun Pipeline.await(all: All) = await()
}

/**
 * Object for [all] alias
 */
object All
/**
 * Alias to be used in piping DSL with [Shell.await]
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
val nullout: (Any) -> Unit = {}
