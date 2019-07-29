package eu.jrie.jetbrains.kotlinshellextension.shell

import eu.jrie.jetbrains.kotlinshellextension.processes.ProcessCommander
import eu.jrie.jetbrains.kotlinshellextension.processes.execution.ProcessExecutable
import eu.jrie.jetbrains.kotlinshellextension.processes.process.NullSendChannel
import eu.jrie.jetbrains.kotlinshellextension.processes.process.Process
import eu.jrie.jetbrains.kotlinshellextension.processes.process.ProcessChannel
import eu.jrie.jetbrains.kotlinshellextension.processes.process.ProcessChannelUnit
import eu.jrie.jetbrains.kotlinshellextension.processes.process.ProcessReceiveChannel
import eu.jrie.jetbrains.kotlinshellextension.processes.process.ProcessSendChannel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import kotlinx.io.streams.writePacket
import org.slf4j.LoggerFactory
import java.io.File

@ExperimentalCoroutinesApi
open class Shell private constructor (
    environment: Map<String, String>,
    variables: Map<String, String>,
    directory: File,
    final override val commander: ProcessCommander
) : ShellPiping, ShellProcess, ShellManagement {

    val nullin: ProcessReceiveChannel = Channel<ProcessChannelUnit>().apply { close() }
    val nullout: ProcessSendChannel = NullSendChannel()

    final override val stdin: ProcessReceiveChannel = nullin
    final override val stdout: ProcessSendChannel
    final override val stderr: ProcessSendChannel

    override var environment: Map<String, String> = environment
        protected set

    override var variables: Map<String, String> = variables
        protected set

    override var directory: File = directory
        protected set

    private val detachedJobs = mutableListOf<Pair<Process, Job>>()

    init {
        val systemOutChannel: ProcessChannel = Channel()
        commander.scope.launch { systemOutChannel.consumeEach { System.out.writePacket(it) } }
        stdout = systemOutChannel
        stderr = systemOutChannel
    }

    override fun cd(dir: File) {
        directory = assertDir(dir).canonicalFile
    }

    override fun variable(variable: Pair<String, String>) {
        variables = variables.plus(variable)
    }

    override fun export(env: Pair<String, String>) {
        environment = environment.plus(env)
    }

    override fun unset(key: String) {
        variables = variables.without(key)
        environment = environment.without(key)
    }

    private fun Map<String, String>.without(key: String) = toMutableMap()
        .apply { remove(key) }
        .toMap()

    override suspend fun detach(executable: ProcessExecutable) {
        executable.init()
        println("INIT ${executable.process.vPID}")
        val job = commander.scope.launch { executable.exec() }
        detachedJobs.add(executable.process to job)
    }

    override val detached: List<Process>
        get() = detachedJobs.map { it.first }

    override suspend fun joinDetached() = detachedJobs.forEach { it.second.join() }

    override suspend fun fg(process: Process) {
        detachedJobs.first { it.first == process }
            .apply {
                commander.awaitProcess(first)
                second.join()
            }
    }

    override suspend fun finalize() {
        joinDetached()
        closeOut()
    }

    override fun exec(block: Shell.() -> String) = ShellExecutable(this, block)

    suspend fun shell(
        vars: Map<String, String> = emptyMap(),
        dir: File = directory,
        script: suspend Shell.() -> Unit
    ) = Shell(environment, vars, dir, commander)
        .apply { script() }
        .finalize()

    companion object {

        internal fun build(env: Map<String, String>?, dir: File?, commander: ProcessCommander) =
            Shell(
                env ?: emptyMap(),
                emptyMap(),
                assertDir(dir?.canonicalFile ?: currentDir()),
                commander
            )

        private fun currentDir(): File {
            val path = System.getProperty("user.dir")
            return File(path)
        }

        private fun assertDir(dir: File) = dir.also { assert(it.isDirectory) }

        internal val logger = LoggerFactory.getLogger(Shell::class.java)
    }
}
