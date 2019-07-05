package eu.jrie.jetbrains.kotlinshellextension.testutils

import eu.jrie.jetbrains.kotlinshellextension.processes.process.ProcessConfiguration

object TestDataFactory {

    const val PROCESS_COMMAND = "some_cmd"
    val PROCESS_ARGS = listOf("arg1", "arg2", "arg3")

    fun processConfigFunction(): ProcessConfiguration.() -> Unit {
        return {
            cmd {
                PROCESS_COMMAND withArgs PROCESS_ARGS
            }
        }
    }
}