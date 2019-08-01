package eu.jrie.jetbrains.kotlinshellextension.processes.execution

import eu.jrie.jetbrains.kotlinshellextension.processes.ProcessCommander
import eu.jrie.jetbrains.kotlinshellextension.processes.process.ProcessReceiveChannel
import eu.jrie.jetbrains.kotlinshellextension.processes.process.ProcessSendChannel
import kotlinx.coroutines.ExperimentalCoroutinesApi

interface ExecutionContext {
    val stdin: ProcessReceiveChannel
    val stdout: ProcessSendChannel
    val stderr: ProcessSendChannel
}

interface ProcessExecutionContext : ExecutionContext {
    @ExperimentalCoroutinesApi
    val commander: ProcessCommander
}
