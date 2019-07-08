package eu.jrie.jetbrains.kotlinshellextension.processes.process.system

import org.zeroturnaround.exec.listener.ProcessListener

class SystemProcessListener (
    private val systemProcess: SystemProcess
) : ProcessListener() {
    override fun afterStop(process: Process?) = finalizeProcess()

    private fun finalizeProcess() {
        systemProcess.closeOut()
    }
}
