package eu.jrie.jetbrains.kotlinshellextension.processes.execution

import eu.jrie.jetbrains.kotlinshellextension.processes.process.Process
import eu.jrie.jetbrains.kotlinshellextension.processes.process.ProcessBuilder
import kotlinx.coroutines.ExperimentalCoroutinesApi

abstract class Executable (
    protected var context: ExecutionContext
) {
    internal open fun init() = Unit

    internal abstract suspend fun exec()

    suspend operator fun invoke() = invoke(context)

    suspend operator fun invoke(context: ExecutionContext) {
        this.context = context
        init()
        exec()
    }
}

@ExperimentalCoroutinesApi
class ProcessExecutable (
    context: ProcessExecutionContext,
    private val builder: ProcessBuilder
) : Executable(context) {

    internal lateinit var process: Process

    override fun init() = with(context as ProcessExecutionContext) {
        builder
            .withStdin(stdin)
            .withStdout(stdout)
            .withStderr(stderr)
        process = commander.createProcess(builder)
    }

    override suspend fun exec() = with(context as ProcessExecutionContext) {
        commander.startProcess(process)
    }
}
