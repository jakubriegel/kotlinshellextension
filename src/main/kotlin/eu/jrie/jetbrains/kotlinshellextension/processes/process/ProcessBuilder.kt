package eu.jrie.jetbrains.kotlinshellextension.processes.process

import eu.jrie.jetbrains.kotlinshellextension.processes.process.system.SystemProcess

object ProcessBuilder {
    fun createSystemProcess(config: ProcessConfiguration, virtualPID: Int): Process {
        val process = SystemProcess(virtualPID, config.command, config.arguments)
        process.setEnvironment(config.environment)
        if (config.inputSource != null)
            process.redirectIn(config.inputSource!!)
        if (config.redirectOutput != {}) process.apply { config.redirectOutput.invoke(this) }
        return process
    }
}
