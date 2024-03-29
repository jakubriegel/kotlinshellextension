package eu.jrie.jetbrains.kotlinshellextension.shell.piping

import eu.jrie.jetbrains.kotlinshellextension.processes.execution.ProcessExecutable
import eu.jrie.jetbrains.kotlinshellextension.processes.pipeline.Pipeline
import eu.jrie.jetbrains.kotlinshellextension.processes.pipeline.PipelineContextLambda
import eu.jrie.jetbrains.kotlinshellextension.processes.process.ProcessChannelInputStream
import eu.jrie.jetbrains.kotlinshellextension.processes.process.ProcessChannelOutputStream
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.consumeEach
import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.buildPacket
import kotlinx.io.core.readBytes
import kotlinx.io.core.writeFully
import java.io.InputStream
import java.io.OutputStream

typealias PipelinePacketLambda = (ByteReadPacket) -> Pair<ByteReadPacket, ByteReadPacket>
typealias PipelineByteArrayLambda = (ByteArray) -> Pair<ByteArray, ByteArray>
typealias PipelineStringLambda = (String) -> Pair<String, String>
typealias PipelineStreamLambda = (InputStream, OutputStream, OutputStream) -> Unit

@ExperimentalCoroutinesApi
interface ShellPipingThrough : ShellPipingTo {
    /**
     * Adds [process] to this pipeline.
     * Part of piping DSL
     *
     * @return this [Pipeline]
     */
    @ExperimentalCoroutinesApi
    suspend infix fun Pipeline.pipe(process: ProcessExecutable) = throughProcess(process)

    /**
     * Adds [lambda] to this pipeline.
     * Part of piping DSL
     *
     * @return this [Pipeline]
     */
    @ExperimentalCoroutinesApi
    suspend infix fun Pipeline.pipe(lambda: PipelineContextLambda) = throughLambda(lambda = lambda)

    /**
     * Constructs [PipelineContextLambda] to be used in piping
     * Part of piping DSL
     */
    fun contextLambda(lambda: PipelineContextLambda) = lambda

    /**
     * Constructs [PipelinePacketLambda] to be used in piping
     * Part of piping DSL
     */
    fun packetLambda(
        lambda: PipelinePacketLambda
    ) = contextLambda { ctx ->
        ctx.stdin.consumeEach { packet ->
            val out = lambda(packet)
            ctx.stdout.send(out.first)
            ctx.stderr.send(out.second)
        }
    }

    /**
     * Constructs [ByteReadPacket] from given [bytes]
     * Part of piping DSL
     *
     * @see packetLambda
     */
    fun packet(bytes: ByteArray) = buildPacket { writeFully(bytes) }

    /**
     * Constructs [ByteReadPacket] from given [string]
     * Part of piping DSL
     *
     * @see packetLambda
     * @see stringLambda
     */
    fun packet(string: String) = packet(string.toByteArray())

    /**
     * Constructs empty [ByteReadPacket]
     * Part of piping DSL
     *
     * @see packetLambda
     * @see stringLambda
     */
    fun emptyPacket() = packet(emptyByteArray())

    /**
     * Constructs [PipelineByteArrayLambda] to be used in piping
     * Part of piping DS
     */
    fun byteArrayLambda(
        lambda: PipelineByteArrayLambda
    ) = packetLambda { p ->
        lambda(p.readBytes()).let { packet(it.first) to packet(it.second) }
    }

    fun emptyByteArray() = ByteArray(0)

    /**
     * Constructs [PipelineStringLambda] to be used in piping
     * Part of piping DSL
     */
    fun stringLambda(
        lambda: PipelineStringLambda
    ) = packetLambda { b ->
        lambda(b.readText()).let { packet(it.first) to packet(it.second) }
    }

    /**
     * Constructs [PipelineStreamLambda] to be used in piping
     * Part of piping DSL
     */
    fun streamLambda(lambda: PipelineStreamLambda) = contextLambda { ctx ->
        val inStream = ProcessChannelInputStream(ctx.stdin, this.commander.scope, PIPELINE_CHANNEL_BUFFER_SIZE)
        val stdStream = ProcessChannelOutputStream(ctx.stdout, this.commander.scope, PIPELINE_CHANNEL_BUFFER_SIZE)
        val errStream = ProcessChannelOutputStream(ctx.stderr, this.commander.scope, PIPELINE_CHANNEL_BUFFER_SIZE)
        lambda(inStream, stdStream, errStream)
    }

}

