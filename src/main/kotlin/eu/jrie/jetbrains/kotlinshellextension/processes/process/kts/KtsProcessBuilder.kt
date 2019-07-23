package eu.jrie.jetbrains.kotlinshellextension.processes.process.kts

import eu.jrie.jetbrains.kotlinshellextension.processes.process.Process
import eu.jrie.jetbrains.kotlinshellextension.processes.process.ProcessBuilder
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
class KtsProcessBuilder (
    val script: String
) : ProcessBuilder() {
    override fun build(): Process {
        TODO("implement KtsProcess")
    }
}