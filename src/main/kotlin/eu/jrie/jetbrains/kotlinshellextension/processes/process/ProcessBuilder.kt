package eu.jrie.jetbrains.kotlinshellextension.processes.process

import eu.jrie.jetbrains.kotlinshellextension.processes.process.system.SystemProcess
import kotlinx.coroutines.CoroutineScope

object ProcessBuilder {
    fun createSystemProcess(config: ProcessConfiguration, virtualPID: Int, scope: CoroutineScope): Process {
        val process = SystemProcess(virtualPID, config.command, config.arguments, scope)
        process.setEnv(config.environment)
        if (config.inputSource != null)
            process.followIn(config.inputSource!!)
        if (config.redirectOutput != {}) process.apply { config.redirectOutput.invoke(this) }
        return process
    }
}
