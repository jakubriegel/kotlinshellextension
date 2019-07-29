package eu.jrie.jetbrains.kotlinshellextension.processes.process.system

import eu.jrie.jetbrains.kotlinshellextension.processes.process.ProcessBuilder
import kotlinx.coroutines.ExperimentalCoroutinesApi

class SystemProcessBuilder (
    val command: String,
    val arguments: List<String> = emptyList()
) : ProcessBuilder() {

    @ExperimentalCoroutinesApi
    override fun build() = SystemProcess(
        vPID,
        command,
        arguments,
        environment,
        directory,
        stdin,
        stdout,
        stderr,
        scope
    )

    companion object {
        fun fromCommandLine(commandLine: String): ProcessBuilder {
            val separated = commandLine.split(" ")
            return SystemProcessBuilder(separated.first() , separated.drop(1))
        }
    }
}
