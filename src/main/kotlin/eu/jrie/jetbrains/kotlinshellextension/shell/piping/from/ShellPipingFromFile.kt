package eu.jrie.jetbrains.kotlinshellextension.shell.piping.from

import eu.jrie.jetbrains.kotlinshellextension.processes.execution.ProcessExecutable
import eu.jrie.jetbrains.kotlinshellextension.processes.pipeline.Pipeline
import eu.jrie.jetbrains.kotlinshellextension.processes.pipeline.PipelineContextLambda
import eu.jrie.jetbrains.kotlinshellextension.processes.process.ProcessSendChannel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.io.core.BytePacketBuilder
import java.io.File
import java.io.OutputStream

@ExperimentalCoroutinesApi
interface ShellPipingFromFile : ShellPipingFromStream {
    /**
     * Starts new [Pipeline] from this [File] to [process].
     * Shall be wrapped with piping DSL
     *
     * @return this [Pipeline]
     */
    infix fun File.pipe(process: ProcessExecutable) = from(this.inputStream()) pipe process

    /**
     * Starts new [Pipeline] from this [File] to [lambda].
     * Shall be wrapped with piping DSL
     *
     * @return this [Pipeline]
     */
    infix fun File.pipe(lambda: PipelineContextLambda) = from(this.inputStream()) pipe lambda

    /**
     * Starts new [Pipeline] from this [File] to [channel].
     * Shall be wrapped with piping DSL
     *
     * @return this [Pipeline]
     */
    suspend infix fun File.pipe(channel: ProcessSendChannel) = from(this.inputStream()) pipe channel

    /**
     * Starts new [Pipeline] from this [File] to [packetBuilder].
     * Shall be wrapped with piping DSL
     *
     * @return this [Pipeline]
     */
    suspend infix fun File.pipe(packetBuilder: BytePacketBuilder) = from(this.inputStream()) pipe packetBuilder

    /**
     * Starts new [Pipeline] from this [File] to [stream].
     * Shall be wrapped with piping DSL
     *
     * @return this [Pipeline]
     */
    suspend infix fun File.pipe(stream: OutputStream) = from(this.inputStream()) pipe stream

    /**
     * Starts new [Pipeline] from this [File] to [file].
     * Shall be wrapped with piping DSL
     *
     * @return this [Pipeline]
     */
    suspend infix fun File.pipe(file: File) = from(this.inputStream()) pipe file

    /**
     * Starts new [Pipeline] from this [File] and appends [file].
     * Shall be wrapped with piping DSL
     *
     * @return this [Pipeline]
     */
    suspend infix fun File.pipeAppend(file: File) = from(this.inputStream()) pipeAppend  file

    /**
     * Starts new [Pipeline] from this [File] to [stringBuilder].
     * Shall be wrapped with piping DSL
     *
     * @return this [Pipeline]
     */
    suspend infix fun File.pipe(stringBuilder: StringBuilder) = from(this.inputStream()) pipe stringBuilder
}
