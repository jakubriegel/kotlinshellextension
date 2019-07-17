package eu.jrie.jetbrains.kotlinshellextension.processes.process.system

import eu.jrie.jetbrains.kotlinshellextension.processes.process.PCB
import eu.jrie.jetbrains.kotlinshellextension.processes.process.Process
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.apache.commons.io.output.NullOutputStream
import org.jetbrains.annotations.TestOnly
import org.zeroturnaround.exec.ProcessExecutor
import org.zeroturnaround.exec.listener.ProcessListener
import org.zeroturnaround.exec.stream.LogOutputStream
import java.io.File
import java.io.InputStream
import java.time.Instant
import java.util.concurrent.TimeUnit

class SystemProcess @TestOnly internal constructor (
    vPID: Int,
    val command: String,
    val arguments: List<String>,
    input: ReceiveChannel<Byte>?,
    environment: Map<String, String>,
    directory: File,
    scope: CoroutineScope,
    private val executor: ProcessExecutor
) : Process(vPID, input, environment, directory, scope) {

    constructor(vPID: Int,
                command: String,
                arguments: List<String>,
                input: ReceiveChannel<Byte>?,
                environment: Map<String, String>,
                directory: File,
                scope: CoroutineScope
    ) : this(vPID, command, arguments, input, environment, directory, scope, ProcessExecutor())

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

    override fun destroy() = ifAlive {
        with(pcb.startedProcess!!) {
            val result = future.cancel(true)
            if (!result) throw Exception("cannot kill process $name")
        }
    }

    override fun toString() = name

    internal class SystemProcessLogOutputStream (
        private val sink: SendChannel<Byte>
    ) : LogOutputStream() {
        override fun processLine(line: String?) {
            line?.plus('\n')?.forEach { sink.sendBlocking(it.toByte()) }
        }
    }

    internal class SystemProcessInputStream (
        private val tap: ReceiveChannel<Byte>,
        private val scope: CoroutineScope
    ) : InputStream() {
        override fun read(): Int {
            return try {
                runBlocking(scope.coroutineContext) { tap.receive().toInt() }
            }
            catch (e: ClosedReceiveChannelException) {
                close()
                -1
            }
        }
    }

    class SystemProcessListener (
        private val process: SystemProcess
    ) : ProcessListener() {
        override fun afterStop(process: java.lang.Process?) = finalizeProcess()

        private fun finalizeProcess() {
            process.stdout.close()
            process.stderr.close()
            logger.debug("finalized $process}")
        }
    }

    private fun ProcessExecutor.redirectInput() = apply {
        if (input != null) redirectInput(SystemProcessInputStream(input, scope))
    }

    private fun ProcessExecutor.redirectOutput() = apply {
        when (stdout) {
            is NullOutputStream -> if (stderr !is NullOutputStream) redirectStdErr()
            else -> {
                when (stderr) {
                    is NullOutputStream -> redirectStdOut()
                    else -> {
                        redirectStdOut()
                        redirectStdErr()
                    }
                }
            }
        }
    }

    private fun ProcessExecutor.redirectStdOut() = redirectOutput(SystemProcessLogOutputStream(stdout))

    private fun ProcessExecutor.redirectStdErr() = redirectError(SystemProcessLogOutputStream(stderr))
}
