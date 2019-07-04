package eu.jrie.jetbrains.kotlinshellextension.processes

import eu.jrie.jetbrains.kotlinshellextension.processes.process.PCB
import eu.jrie.jetbrains.kotlinshellextension.processes.process.Process
import eu.jrie.jetbrains.kotlinshellextension.processes.process.ProcessBuilder
import eu.jrie.jetbrains.kotlinshellextension.processes.process.ProcessConfiguration
import org.apache.log4j.BasicConfigurator
import java.lang.Exception

class ProcessCommander {

    private val processes = mutableSetOf<Process>()

    fun systemProcess(config: ProcessConfiguration.() -> Unit): Int {
        val process = with(ProcessConfiguration().apply(config)) {
            ProcessBuilder.createSystemProcess(this, virtualPID())
        }

        processes.add(process)
        return process.virtualPID
    }

    fun startProcess(vPID: Int): PCB {
        val process = getProcessByVirtualPID(vPID)
        return process.start()
    }

    fun awaitProcess(vPID: Int, timeout: Long = 0) {
        getProcessByVirtualPID(vPID).await(timeout)
    }

    fun killProcess(vPID: Int) {
        val process = getProcessByVirtualPID(vPID)
        process.kill()
    }

    private fun getProcessByVirtualPID(vPID: Int) =
        processes.find { it.virtualPID == vPID } ?: throw Exception("no processes with given virtual PID; $vPID")

    companion object {
        init {
            // TODO: move to better place
            BasicConfigurator.configure()
        }

        private var nextVirtualPID = 1
        private fun virtualPID() = nextVirtualPID++

    }
}