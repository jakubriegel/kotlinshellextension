package eu.jrie.jetbrains.kotlinshellextension.processes.process.system

import eu.jrie.jetbrains.kotlinshellextension.processes.process.ProcessBuilder

class SystemProcessBuilder (
    private val command: String,
    private val arguments: List<String> = emptyList()
) : ProcessBuilder() {

    override fun build() = SystemProcess(
        vPID,
        command,
        arguments,
        input,
        stdout,
        stderr,
        environment,
        directory,
        scope
    )

    companion object {
        fun fromCommandLine(commandLine: String): ProcessBuilder {
            val separated = commandLine.split(" ")
            return SystemProcessBuilder(separated.first() , separated.drop(1))
        }
    }
}
