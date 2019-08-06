package eu.jrie.jetbrains.kotlinshellextension.shell

import eu.jrie.jetbrains.kotlinshellextension.processes.configuration.KtsProcessConfiguration
import eu.jrie.jetbrains.kotlinshellextension.processes.configuration.ProcessConfiguration
import eu.jrie.jetbrains.kotlinshellextension.processes.configuration.SystemProcessConfiguration
import eu.jrie.jetbrains.kotlinshellextension.processes.execution.ProcessExecutable
import eu.jrie.jetbrains.kotlinshellextension.processes.process.Process
import eu.jrie.jetbrains.kotlinshellextension.processes.process.ProcessBuilder
import eu.jrie.jetbrains.kotlinshellextension.processes.process.ProcessReceiveChannel
import eu.jrie.jetbrains.kotlinshellextension.processes.process.ProcessSendChannel
import eu.jrie.jetbrains.kotlinshellextension.processes.process.ProcessState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.io.File

@ExperimentalCoroutinesApi
interface ShellProcess : ShellBase {

    /**
     * All processes
     */
    val processes: List<Process>
        get() = commander.processes.toList()
    /**
     * List of detached processes
     */
    val detached: List<Process>

    /**
     * List of daemon processes
     */
    val daemons: List<Process>

    val nullin: ProcessReceiveChannel
    val nullout: ProcessSendChannel

    /**
     * Creates executable system process
     */
    fun systemProcess(config: SystemProcessConfiguration.() -> Unit) = process(systemBuilder(config))

    /**
     * Creates builder for system process
     */
    fun systemBuilder(config: SystemProcessConfiguration.() -> Unit) = SystemProcessConfiguration()
        .apply(config)
        .apply { systemProcessInputStreamBufferSize = SYSTEM_PROCESS_INPUT_STREAM_BUFFER_SIZE }
        .configureBuilder()

    /**
     * Creates executable system process from this command line
     */
    fun String.process() = with(split(" ")) {
        when (size) {
            1 -> systemProcess { cmd = first() }
            else -> systemProcess { cmd { first() withArgs subList(1, size) } }
        }
    }

    /**
     * Executes system process from this command line
     */
    suspend operator fun String.invoke(mode: ExecutionMode = ExecutionMode.ATTACHED) = process().invoke(mode)

    /**
     * Creates executable system process from contents of this [File]
     */
    fun File.process(vararg args: String) = when (args.size) {
        0 -> systemProcess { cmd = canonicalPath }
        else -> systemProcess { cmd { canonicalPath withArgs args.toList() } }
    }

    /**
     * Executes system process from from contents of this [File]
     */
    suspend operator fun File.invoke(mode: ExecutionMode = ExecutionMode.ATTACHED, vararg args: String) = process(*args).invoke(mode)

    /**
     * Creates executable KotlinScript process
     */
    fun ktsProcess(config: KtsProcessConfiguration.() -> Unit) = process(ktsBuilder(config))

    /**
     * Creates builder for KotlinScript process
     */
    fun ktsBuilder(config: KtsProcessConfiguration.() -> Unit) = KtsProcessConfiguration()
        .apply(config)
        .configureBuilder()

    /**
     * Creates executable KotlinScript process from given script
     */
    fun String.kts(): ProcessExecutable { TODO("implement kts processes") }

    private fun process(builder: ProcessBuilder) = ProcessExecutable(this, builder)

    private fun ProcessConfiguration.configureBuilder(): ProcessBuilder {
        env(environment.plus(variables))
        dir(directory)
        return builder()
    }

    suspend fun detach(executable: ProcessExecutable)

    suspend fun detach(vararg executable: ProcessExecutable) = executable.forEach { detach(it) }

    suspend fun joinDetached()

    val jobs: ShellExecutable get() = exec {
        StringBuilder().let {
            detached.forEachIndexed { i, p -> it.append("[${i+1}] ${p.name}") }
            it.toString()
        }
    }

    suspend fun fg(index: Int = 1) = fg(detached[index-1])

    suspend fun fg(process: Process)

    suspend fun daemon(executable: ProcessExecutable)

    suspend fun daemon(vararg executable: ProcessExecutable) = executable.forEach { daemon(it) }

    suspend fun Process.join() = commander.awaitProcess(this)

    suspend fun joinAll() = commander.awaitAll()

    suspend fun Process.kill() = kill(this)

    suspend fun kill(vararg process: Process) = process.forEach { killProcess(it) }

    private suspend fun killProcess(process: Process) = commander.killProcess(process)

    suspend fun killAll() = commander.killAll()

    suspend operator fun ProcessExecutable.invoke(mode: ExecutionMode = ExecutionMode.ATTACHED) {
        when (mode) {
            ExecutionMode.ATTACHED -> this()
            ExecutionMode.DETACHED -> this@ShellProcess.detach(this)
            ExecutionMode.DAEMON -> this@ShellProcess.daemon(this)
        }
    }

    /**
     * Retrieves all process data
     */
    val ps: ShellExecutable get() = exec { commander.status() }

    /**
     * Retrieves [Process] by its vPID
     */
    fun List<Process>.byVPID(vPID: Int) = first { it.vPID == vPID }

    /**
     * Retrieves all running processes
     */
    fun List<Process>.running() = filter { it.pcb.state == ProcessState.RUNNING }

    /**
     * Retrieves all terminated processes
     */
    fun List<Process>.terminated() = filter { it.pcb.state == ProcessState.TERMINATED }

}

enum class ExecutionMode {
    /**
     * Runs process in foreground.
     * Will close the process when closing shell
     */
    ATTACHED,

    /**
     * Runs process in the background.
     * Will close the process when closing shell
     */
    DETACHED,

    /**
     * Runs process in the background.
     * Will **not** close the process when closing shell
     */
    DAEMON
}
