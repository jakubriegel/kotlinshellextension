package eu.jrie.jetbrains.kotlinshellextension.processes.process.kts

import eu.jrie.jetbrains.kotlinshellextension.processes.process.Process
import eu.jrie.jetbrains.kotlinshellextension.processes.process.ProcessBuilder

class KtsProcessBuilder (
    val script: String
) : ProcessBuilder() {
    override fun build(): Process {
        TODO("not implemented")
    }
}