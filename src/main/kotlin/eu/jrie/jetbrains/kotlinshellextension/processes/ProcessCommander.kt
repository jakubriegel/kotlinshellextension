package eu.jrie.jetbrains.kotlinshellextension.processes

import eu.jrie.jetbrains.kotlinshellextension.processes.process.Process
import eu.jrie.jetbrains.kotlinshellextension.processes.process.ProcessBuilder
import eu.jrie.jetbrains.kotlinshellextension.processes.process.ProcessConfiguration
import kotlinx.coroutines.CoroutineScope

class ProcessCommander (
    val scope: CoroutineScope
) {

    private val processes = mutableSetOf<Process>()

    fun systemProcess(config: ProcessConfiguration.() -> Unit) =
        createSystemProcess(config).let {
            processes.add(it)
            it.vPID
        }

    private fun createSystemProcess(config: ProcessConfiguration.() -> Unit) =
        with(ProcessConfiguration().apply(config)) {
            ProcessBuilder.createSystemProcess(this, virtualPID(), scope)
        }

    fun startProcess(vPID: Int) = getProcessByVirtualPID(vPID).start()

    fun awaitProcess(vPID: Int, timeout: Long = 0) = getProcessByVirtualPID(vPID).await(timeout)

    fun killProcess(vPID: Int) = getProcessByVirtualPID(vPID).kill()

    private fun getProcessByVirtualPID(vPID: Int) =
        processes.find { it.vPID == vPID } ?: throw Exception("no processes with given virtual PID: $vPID")

    companion object {
        private var nextVirtualPID = 1
        private fun virtualPID() = nextVirtualPID++

    }
}
