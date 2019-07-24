package eu.jrie.jetbrains.kotlinshellextension.processes.process.system

import eu.jrie.jetbrains.kotlinshellextension.processes.process.PCB
import eu.jrie.jetbrains.kotlinshellextension.processes.process.Process
import eu.jrie.jetbrains.kotlinshellextension.processes.process.ProcessIOBuffer
import eu.jrie.jetbrains.kotlinshellextension.processes.process.ProcessReceiveChannel
import eu.jrie.jetbrains.kotlinshellextension.processes.process.ProcessSendChannel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.io.core.BytePacketBuilder
import kotlinx.io.streams.inputStream
import org.jetbrains.annotations.TestOnly
import org.zeroturnaround.exec.ProcessExecutor
import org.zeroturnaround.exec.listener.ProcessListener
import org.zeroturnaround.exec.stream.LogOutputStream
import java.io.File
import java.io.InputStream
import java.nio.ByteBuffer
import java.time.Instant
import java.util.concurrent.TimeUnit

@ExperimentalCoroutinesApi
class SystemProcess @TestOnly internal constructor (
    vPID: Int,
    val command: String,
    val arguments: List<String>,
    environment: Map<String, String>,
    directory: File,
    stdinBuffer: ProcessIOBuffer?,
    stdoutBuffer: ProcessIOBuffer?,
    stderrBuffer: ProcessIOBuffer?,
    scope: CoroutineScope,
    private val executor: ProcessExecutor
) : Process(vPID, environment, directory, stdinBuffer, stdoutBuffer, stderrBuffer, scope) {

    constructor(vPID: Int,
                command: String,
                arguments: List<String>,
                environment: Map<String, String>,
                directory: File,
                stdinBuffer: ProcessIOBuffer?,
                stdoutBuffer: ProcessIOBuffer?,
                stderrBuffer: ProcessIOBuffer?,
                scope: CoroutineScope
    ) : this(vPID, command, arguments, environment, directory, stdinBuffer, stdoutBuffer, stderrBuffer, scope, ProcessExecutor())

    override val pcb = SystemPCB()

    override val name: String
        get() = "[${super.name} ${pcb.systemPID} $command]"

    override val statusCmd: String
        get() = command

    override val statusOther: String
        get() = "pid=${pcb.systemPID}"

    init {
        executor
            .command(listOf(command).plus(arguments))
            .destroyOnExit()
            .addListener(SystemProcessListener(this))
            .redirectInput()
            .redirectOutput()
            .environment(environment)
            .directory(directory)
    }

    override fun execute(): PCB {
        val started = executor.start()!!

        pcb.startTime = started.process.info().startInstant().orElse(Instant.now())
        pcb.systemPID = started.process.pid()
        pcb.startedProcess = started

        return pcb
    }

    override fun isAlive() = if (pcb.startedProcess != null ) pcb.startedProcess!!.process.isAlive else false

    @ObsoleteCoroutinesApi
    override suspend fun expect(timeout: Long) {
        withContext(Dispatchers.Default) {
            with(pcb.startedProcess!!) {
                val result = if (timeout.compareTo(0) == 0) future.get()
                else future.get(timeout, TimeUnit.MILLISECONDS)
                pcb.exitCode = result.exitValue
            }
        }
    }

    override fun destroy() {
        if(isAlive()) {
            with(pcb.startedProcess!!) {
                val result = future.cancel(true)
                if (!result) throw Exception("cannot kill process $name")
            }
        }
    }

    override fun toString() = name

    internal class SystemProcessLogOutputStream (
        private val sink: ProcessSendChannel,
        private val scope: CoroutineScope
    ) : LogOutputStream() {
        override fun processLine(line: String?) {
            if (line != null) {
                BytePacketBuilder()
                    .append(line.plus('\n'))
                    .build()
                    .also {
                        runBlocking(scope.coroutineContext) { sink.send(it) }
                    }
            }
        }
    }

    internal class SystemProcessInputStream (
        private val tap: ProcessReceiveChannel,
        private val scope: CoroutineScope
    ) : InputStream() {

        private val buffer = ByteBuffer.allocate(INPUT_STREAM_BUFFER_SIZE).apply { flip() }

        private var stream: InputStream? = null

        override fun read(): Int {
            if (!buffer.hasRemaining()) {
                try {
                    receive()
                } catch (e: ClosedReceiveChannelException) {
                    buffer.put(MINUS_ONE)
                    buffer.flip()
                }
            }

            val b = buffer.get().toInt()
            if (b == -1) {
                close()
            }
            return b
        }

        private fun receive() {
            buffer.clear()
            if (stream == null) {
                val packet = runBlocking (scope.coroutineContext) { tap.receive() }
                stream = packet.inputStream()
            }
            readStream()
            buffer.flip()
        }

        private fun readStream() {
            stream!!.let {
                while (buffer.position() < INPUT_STREAM_BUFFER_SIZE) {
                    val b = it.read().toByte()
                    if (b == MINUS_ONE) break
                    else buffer.put(b)
                }
                if (it.available() == 0) {
                    stream = null
                    it.close()
                }
            }
        }

        companion object {
            private const val MINUS_ONE: Byte = -1
        }
    }

    class SystemProcessListener (
        private val process: SystemProcess
    ) : ProcessListener() {
        override fun afterStop(process: java.lang.Process?) = finalizeProcess()

        private fun finalizeProcess() {
            logger.debug("finalizing $process}")
            process.closeOut()
            logger.debug("finalized $process}")
        }
    }

    private fun ProcessExecutor.redirectInput() = apply {
        if (stdin != null) redirectInput(SystemProcessInputStream(stdin, scope))
    }

    private fun ProcessExecutor.redirectOutput() = apply {
        when (stderr) {
            null -> redirectStdOut()
            else -> {
                redirectStdOut()
                redirectStdErr()
            }
        }
    }

    private fun ProcessExecutor.redirectStdOut() = redirectOutput(SystemProcessLogOutputStream(stdout, scope))

    private fun ProcessExecutor.redirectStdErr() = redirectError(SystemProcessLogOutputStream(stderr!!, scope))

    companion object {
        private const val INPUT_STREAM_BUFFER_SIZE = 512
    }

}
