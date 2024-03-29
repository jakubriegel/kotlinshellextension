package eu.jrie.jetbrains.kotlinshellextension.shell.piping.from

import eu.jrie.jetbrains.kotlinshellextension.processes.execution.ProcessExecutable
import eu.jrie.jetbrains.kotlinshellextension.processes.pipeline.Pipeline
import eu.jrie.jetbrains.kotlinshellextension.processes.pipeline.PipelineContextLambda
import eu.jrie.jetbrains.kotlinshellextension.processes.process.ProcessBuilder
import eu.jrie.jetbrains.kotlinshellextension.processes.process.ProcessSendChannel
import eu.jrie.jetbrains.kotlinshellextension.shell.piping.ShellPipingThrough
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.io.core.BytePacketBuilder
import java.io.File
import java.io.OutputStream

@ExperimentalCoroutinesApi
interface ShellPipingFromProcess : ShellPipingThrough {
    /**
     * Starts new [Pipeline] from process specified by given [ProcessBuilder].
     * Shall be wrapped with piping DSL
     *
     * @return this [Pipeline]
     */
    suspend fun from(process: ProcessExecutable) = Pipeline.fromProcess(process, this, PIPELINE_CHANNEL_BUFFER_SIZE)

    /**
     * Starts new [Pipeline] from process ran by this [ProcessExecutable] to one specified by [process].
     * Part of piping DSL
     *
     * @return this [Pipeline]
     */
    @ExperimentalCoroutinesApi
    suspend infix fun ProcessExecutable.pipe(process: ProcessExecutable) = from(this) pipe process

    /**
     * Starts new [Pipeline] from process ran by this [ProcessExecutable] to [lambda].
     * Part of piping DSL
     *
     * @return this [Pipeline]
     */
    @ExperimentalCoroutinesApi
    suspend infix fun ProcessExecutable.pipe(lambda: PipelineContextLambda) = from(this) pipe lambda

    /**
     * Starts new [Pipeline] from process ran by this [ProcessExecutable] to [channel].
     * Part of piping DSL
     *
     * @return this [Pipeline]
     */
    @ExperimentalCoroutinesApi
    suspend infix fun ProcessExecutable.pipe(channel: ProcessSendChannel) = from(this) pipe channel

    /**
     * Starts new [Pipeline] from process ran by this [ProcessExecutable] to [packetBuilder].
     * Part of piping DSL
     *
     * @return this [Pipeline]
     */
    @ExperimentalCoroutinesApi
    suspend infix fun ProcessExecutable.pipe(packetBuilder: BytePacketBuilder) = from(this) pipe packetBuilder

    /**
     * Starts new [Pipeline] from process ran by this [ProcessExecutable] to [stream].
     * Part of piping DSL
     *
     * @return this [Pipeline]
     */
    @ExperimentalCoroutinesApi
    suspend infix fun ProcessExecutable.pipe(stream: OutputStream) = from(this) pipe stream

    /**
     * Starts new [Pipeline] from process ran by this [ProcessExecutable] to [file].
     * Part of piping DSL
     *
     * @return this [Pipeline]
     */
    @ExperimentalCoroutinesApi
    suspend infix fun ProcessExecutable.pipe(file: File) = from(this) pipe file

    /**
     * Starts new [Pipeline] from this [ProcessExecutable] and appends [file].
     * Shall be wrapped with piping DSL
     *
     * @return this [Pipeline]
     */
    suspend infix fun ProcessExecutable.pipeAppend(file: File) = from(this) pipeAppend  file

    /**
     * Starts new [Pipeline] from process ran by this [ProcessExecutable] to [stringBuilder].
     * Part of piping DSL
     *
     * @return this [Pipeline]
     */
    @ExperimentalCoroutinesApi
    suspend infix fun ProcessExecutable.pipe(stringBuilder: StringBuilder) = from(this) pipe stringBuilder
}
