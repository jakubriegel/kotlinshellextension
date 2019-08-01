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
interface ShellPipingFromString : ShellPipingFromStream {
    /**
     * Starts new [Pipeline] from this [String] to [process].
     * Shall be wrapped with piping DSL
     *
     * @return this [Pipeline]
     */
    suspend infix fun String.pipe(process: ProcessExecutable) = from(this.byteInputStream()) pipe process

    /**
     * Starts new [Pipeline] from this [String] to [lambda].
     * Shall be wrapped with piping DSL
     *
     * @return this [Pipeline]
     */
    suspend infix fun String.pipe(lambda: PipelineContextLambda) = from(this.byteInputStream()) pipe lambda

    /**
     * Starts new [Pipeline] from this [String] to [channel].
     * Shall be wrapped with piping DSL
     *
     * @return this [Pipeline]
     */
    suspend infix fun String.pipe(channel: ProcessSendChannel) = from(this.byteInputStream()) pipe channel

    /**
     * Starts new [Pipeline] from this [String] to [packetBuilder].
     * Shall be wrapped with piping DSL
     *
     * @return this [Pipeline]
     */
    suspend infix fun String.pipe(packetBuilder: BytePacketBuilder) = from(this.byteInputStream()) pipe packetBuilder

    /**
     * Starts new [Pipeline] from this [String] to [stream].
     * Shall be wrapped with piping DSL
     *
     * @return this [Pipeline]
     */
    suspend infix fun String.pipe(stream: OutputStream) = from(this.byteInputStream()) pipe stream

    /**
     * Starts new [Pipeline] from this [String] to [file].
     * Shall be wrapped with piping DSL
     *
     * @return this [Pipeline]
     */
    suspend infix fun String.pipe(file: File) = from(this.byteInputStream()) pipe file

    /**
     * Starts new [Pipeline] from this [String] and appends [file].
     * Shall be wrapped with piping DSL
     *
     * @return this [Pipeline]
     */
    suspend infix fun String.pipeAppend(file: File) = from(this.byteInputStream()) pipeAppend  file

    /**
     * Starts new [Pipeline] from this [String] to [stringBuilder].
     * Shall be wrapped with piping DSL
     *
     * @return this [Pipeline]
     */
    suspend infix fun String.pipe(stringBuilder: StringBuilder) = from(this.byteInputStream()) pipe stringBuilder
}
