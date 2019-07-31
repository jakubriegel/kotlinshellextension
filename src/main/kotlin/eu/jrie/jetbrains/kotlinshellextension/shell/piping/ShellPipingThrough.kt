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
    infix fun Pipeline.pipe(process: ProcessExecutable) = throughProcess(process)

    /**
     * Adds [lambda] to this pipeline.
     * Part of piping DSL
     *
     * @return this [Pipeline]
     */
    @ExperimentalCoroutinesApi
    infix fun Pipeline.pipe(lambda: PipelineContextLambda) = throughLambda(lambda = lambda)

    fun contextLambda(lambda: PipelineContextLambda) = lambda

    fun packetLambda(
        lambda: PipelinePacketLambda
    ): PipelineContextLambda = { ctx ->
        ctx.stdin.consumeEach { packet ->
            val out = lambda(packet)
            ctx.stdout.send(out.first)
            ctx.stderr.send(out.second)
        }
    }

    fun packet(bytes: ByteArray) = buildPacket { writeFully(bytes) }
    fun packet(string: String) = packet(string.toByteArray())
    fun emptyPacket() = packet("")

    fun byteArrayLambda(
        lambda: PipelineByteArrayLambda
    ) = packetLambda { p ->
        lambda(p.readBytes()).let { packet(it.first) to packet(it.second) }
    }

    fun emptyByteArray() = ByteArray(0)

    fun stringLambda(
        lambda: PipelineStringLambda
    ) = packetLambda { b ->
        lambda(b.readText()).let { packet(it.first) to packet(it.second) }
    }

    fun streamLambda(lambda: PipelineStreamLambda): PipelineContextLambda = { ctx ->
        val inStream = ProcessChannelInputStream(ctx.stdin, this.commander.scope)
        val stdStream = ProcessChannelOutputStream(ctx.stdout, this.commander.scope)
        val errStream = ProcessChannelOutputStream(ctx.stderr, this.commander.scope)
        lambda(inStream, stdStream, errStream)
    }

}

