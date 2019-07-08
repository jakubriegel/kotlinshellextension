package eu.jrie.jetbrains.kotlinshellextension.processes

import eu.jrie.jetbrains.kotlinshellextension.processes.process.Process
import eu.jrie.jetbrains.kotlinshellextension.processes.process.ProcessBuilder
import eu.jrie.jetbrains.kotlinshellextension.processes.process.ProcessConfiguration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import org.apache.log4j.BasicConfigurator

class ProcessCommander (
    val scope: CoroutineScope
) {

    private val processes = mutableSetOf<Process>()

    fun systemProcess(config: ProcessConfiguration.() -> Unit) =
        createSystemProcess(config).let {
            processes.add(it)
            it.virtualPID
        }

    private fun createSystemProcess(config: ProcessConfiguration.() -> Unit) =
        with(ProcessConfiguration().apply(config)) {
            ProcessBuilder.createSystemProcess(this, virtualPID(), scope)
        }

    fun startProcess(vPID: Int) = getProcessByVirtualPID(vPID).start()

    fun awaitProcess(vPID: Int, timeout: Long = 0) = getProcessByVirtualPID(vPID).await(timeout)

    fun killProcess(vPID: Int) = getProcessByVirtualPID(vPID).kill()

    private fun getProcessByVirtualPID(vPID: Int) =
        processes.find { it.virtualPID == vPID } ?: throw Exception("no processes with given virtual PID: $vPID")

    companion object {
        init {
            // TODO: move to better place and implement logger
            BasicConfigurator.configure()
        }

        private var nextVirtualPID = 1
        private fun virtualPID() = nextVirtualPID++

    }
}