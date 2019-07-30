package eu.jrie.jetbrains.kotlinshellextension.shell.piping

import eu.jrie.jetbrains.kotlinshellextension.processes.pipeline.Pipeline
import eu.jrie.jetbrains.kotlinshellextension.processes.process.ProcessSendChannel
import eu.jrie.jetbrains.kotlinshellextension.shell.ShellBase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.io.core.BytePacketBuilder
import java.io.File
import java.io.OutputStream

@ExperimentalCoroutinesApi
interface ShellPipingTo : ShellBase {
    /**
     * Ends this [Pipeline] with [channel]
     * Part of piping DSL
     *
     * @return this [Pipeline]
     */
    @ExperimentalCoroutinesApi
    suspend infix fun Pipeline.pipe(channel: ProcessSendChannel) = toEndChannel(channel)

    /**
     * Ends this [Pipeline] with [packetBuilder]
     * Part of piping DSL
     *
     * @return this [Pipeline]
     */
    @ExperimentalCoroutinesApi
    suspend infix fun Pipeline.pipe(packetBuilder: BytePacketBuilder) = toEndPacket(packetBuilder)

    /**
     * Ends this [Pipeline] with [stream]
     * Part of piping DSL
     *
     * @return this [Pipeline]
     */
    @ExperimentalCoroutinesApi
    suspend infix fun Pipeline.pipe(stream: OutputStream) = toEndStream(stream)

    /**
     * Ends this [Pipeline] with [file]
     * Part of piping DSL
     *
     * @return this [Pipeline]
     */
    @ExperimentalCoroutinesApi
    suspend infix fun Pipeline.pipe(file: File) = toEndFile(file)

    /**
     * Ends this [Pipeline] with [stringBuilder]
     * Part of piping DSL
     *
     * @return this [Pipeline]
     */
    @ExperimentalCoroutinesApi
    suspend infix fun Pipeline.pipe(stringBuilder: StringBuilder) = toEndStringBuilder(stringBuilder)
}
