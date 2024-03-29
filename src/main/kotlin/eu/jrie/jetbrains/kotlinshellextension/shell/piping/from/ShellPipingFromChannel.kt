package eu.jrie.jetbrains.kotlinshellextension.shell.piping.from

import eu.jrie.jetbrains.kotlinshellextension.processes.execution.ProcessExecutable
import eu.jrie.jetbrains.kotlinshellextension.processes.pipeline.Pipeline
import eu.jrie.jetbrains.kotlinshellextension.processes.pipeline.PipelineContextLambda
import eu.jrie.jetbrains.kotlinshellextension.processes.process.ProcessReceiveChannel
import eu.jrie.jetbrains.kotlinshellextension.processes.process.ProcessSendChannel
import eu.jrie.jetbrains.kotlinshellextension.shell.piping.ShellPipingThrough
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.io.core.BytePacketBuilder
import java.io.File
import java.io.OutputStream

@ExperimentalCoroutinesApi
interface ShellPipingFromChannel : ShellPipingThrough {
    /**
     * Starts new [Pipeline] from [channel].
     * Shall be wrapped with piping DSL
     *
     * @return this [Pipeline]
     */
    fun from(channel: ProcessReceiveChannel) = Pipeline.fromChannel(channel, this, PIPELINE_CHANNEL_BUFFER_SIZE)

    /**
     * Starts new [Pipeline] from this [ProcessReceiveChannel] to [process].
     * Shall be wrapped with piping DSL
     *
     * @return this [Pipeline]
     */
    suspend infix fun ProcessReceiveChannel.pipe(process: ProcessExecutable) = from(this) pipe process

    /**
     * Starts new [Pipeline] from this [ProcessReceiveChannel] to [lambda].
     * Shall be wrapped with piping DSL
     *
     * @return this [Pipeline]
     */
    suspend infix fun ProcessReceiveChannel.pipe(lambda: PipelineContextLambda) = from(this) pipe lambda

    /**
     * Starts new [Pipeline] from this [ProcessReceiveChannel] to [channel].
     * Shall be wrapped with piping DSL
     *
     * @return this [Pipeline]
     */
    suspend infix fun ProcessReceiveChannel.pipe(channel: ProcessSendChannel) = from(this) pipe channel

    /**
     * Starts new [Pipeline] from this [ProcessReceiveChannel] to [packetBuilder].
     * Shall be wrapped with piping DSL
     *
     * @return this [Pipeline]
     */
    suspend infix fun ProcessReceiveChannel.pipe(packetBuilder: BytePacketBuilder) = from(this) pipe packetBuilder

    /**
     * Starts new [Pipeline] from this [ProcessReceiveChannel] to [stream].
     * Shall be wrapped with piping DSL
     *
     * @return this [Pipeline]
     */
    suspend infix fun ProcessReceiveChannel.pipe(stream: OutputStream) = from(this) pipe stream

    /**
     * Starts new [Pipeline] from this [ProcessReceiveChannel] to [file].
     * Shall be wrapped with piping DSL
     *
     * @return this [Pipeline]
     */
    suspend infix fun ProcessReceiveChannel.pipe(file: File) = from(this) pipe file

    /**
     * Starts new [Pipeline] from this [ProcessReceiveChannel] and appends [file].
     * Shall be wrapped with piping DSL
     *
     * @return this [Pipeline]
     */
    suspend infix fun ProcessReceiveChannel.pipeAppend(file: File) = from(this) pipeAppend  file

    /**
     * Starts new [Pipeline] from this [ProcessReceiveChannel] to [stringBuilder].
     * Shall be wrapped with piping DSL
     *
     * @return this [Pipeline]
     */
    suspend infix fun ProcessReceiveChannel.pipe(stringBuilder: StringBuilder) = from(this) pipe stringBuilder

}
