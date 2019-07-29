package eu.jrie.jetbrains.kotlinshellextension.shell

import eu.jrie.jetbrains.kotlinshellextension.processes.configuration.KtsProcessConfiguration
import eu.jrie.jetbrains.kotlinshellextension.processes.configuration.ProcessConfiguration
import eu.jrie.jetbrains.kotlinshellextension.processes.configuration.SystemProcessConfiguration
import eu.jrie.jetbrains.kotlinshellextension.processes.execution.ProcessExecutable
import eu.jrie.jetbrains.kotlinshellextension.processes.process.Process
import eu.jrie.jetbrains.kotlinshellextension.processes.process.ProcessBuilder
import eu.jrie.jetbrains.kotlinshellextension.processes.process.ProcessState
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
interface ShellProcess : ShellBase {

    /**
     * Creates executable system process
     */
    fun systemProcess(config: SystemProcessConfiguration.() -> Unit)/*: ProcessExec*/ = process(systemBuilder(config))

    /**
     * Creates builder for system process
     */
    fun systemBuilder(config: SystemProcessConfiguration.() -> Unit) = SystemProcessConfiguration()
        .apply(config)
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
    suspend operator fun String.invoke() = process().invoke()

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

    suspend fun joinDetached()

    val jobs: ShellExecutable get() = exec {
        StringBuilder().let {
            detached.forEachIndexed { i, p -> it.append("[${i+1}] ${p.name}") }
            it.toString()
        }
    }

    val detached: List<Process>

    suspend fun fg(index: Int = 1) = fg(detached[index-1])

    suspend fun fg(process: Process)

    suspend fun awaitAll() = commander.awaitAll()

    suspend fun killAll() = commander.killAll()

    /**
     * Retrieves all process data
     */
    val ps: ShellExecutable get() = exec { commander.status() }

    /**
     * Retrieves all processes
     */
    val processes: List<Process>
        get() = commander.processes.toList()

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
    fun List<Process>.terminated() = first { it.pcb.state == ProcessState.TERMINATED }

}
