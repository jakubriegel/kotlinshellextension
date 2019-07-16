package eu.jrie.jetbrains.kotlinshellextension

import eu.jrie.jetbrains.kotlinshellextension.processes.Pipeline
import eu.jrie.jetbrains.kotlinshellextension.processes.ProcessCommander
import eu.jrie.jetbrains.kotlinshellextension.processes.process.ProcessBuilder
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.io.File

abstract class ShellPiping (
    private val commander: ProcessCommander
) {

    /**
     * Starts new [Pipeline] from process specified by given [ProcessBuilder].
     * Shall be wrapped with piping DSL
     *
     * @see ProcessBuilder.pipe
     * @return this [Pipeline]
     */
    fun from(process: ProcessBuilder) = Pipeline.from(process, commander)

    /**
     * Starts new [Pipeline] from this process process one specified by [process].
     * Part of piping DSL
     *
     * @see ProcessBuilder.pipe
     * @return this [Pipeline]
     */
    infix fun ProcessBuilder.pipe(process: ProcessBuilder) = from(this) pipe process

    /**
     * Starts new [Pipeline] from this process tp [tap] function.
     * Part of piping DSL
     *
     * @see ProcessBuilder.pipe
     * @return this [Pipeline]
     */
    @ExperimentalCoroutinesApi
    infix fun ProcessBuilder.pipe(tap: (Byte) -> Unit) = from(this) pipe tap

    /**
     * Starts new pipeline with [file] as an input of given [process].
     * Shall be wrapped with piping DSL
     *
     * @return this [Pipeline]
     */
    fun from(file: File, process: ProcessBuilder) = Pipeline.fromFile(file, process, commander)

    /**
     * Starts new pipeline with this [File] as an input of given [process].
     * Shall be wrapped with piping DSL
     *
     * @return this [Pipeline]
     */
    infix fun File.pipe(process: ProcessBuilder) = from(this, process)

    /**
     * Adds [process] process to this pipeline.
     * Shall be wrapped with piping DSL
     *
     * @return this [Pipeline]
     */
    infix fun Pipeline.pipe(process: ProcessBuilder) = toProcess(process)

    /**
     * Ends this [Pipeline] with [tap] function
     * Shall be wrapped with piping DSL
     *
     * @return this [Pipeline]
     */
    @ExperimentalCoroutinesApi // TODO: implement KtsProcess
    infix fun Pipeline.pipe(tap: (Byte) -> Unit) = toLambda(tap)

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
}

/**
 * Alias for piping output to print().
 * To be use with [ShellPiping.pipe]
 *
 * @see ShellPiping
 * @see Pipeline
 */
val stdout = { it: Byte -> print(it.toChar()) }
