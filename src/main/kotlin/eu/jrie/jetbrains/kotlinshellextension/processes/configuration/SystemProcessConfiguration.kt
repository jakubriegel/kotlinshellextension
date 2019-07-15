package eu.jrie.jetbrains.kotlinshellextension.processes.configuration

import eu.jrie.jetbrains.kotlinshellextension.processes.process.system.SystemProcessBuilder

class SystemProcessConfiguration : ProcessConfiguration() {

    override fun createBuilder() = SystemProcessBuilder(cmd, args)

    var cmd: String = ""

    var args: List<String> = emptyList()
        private set

    fun cmd(config: ProcessConfiguration.() -> Unit) = config()

    infix fun String.withArgs(arguments: List<String>) {
        cmd = this
        args = arguments
    }

    infix fun String.withArg(argument: String) {
        cmd = this
        args = listOf(argument)
    }
}
