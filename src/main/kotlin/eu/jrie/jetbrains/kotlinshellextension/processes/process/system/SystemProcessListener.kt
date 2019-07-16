package eu.jrie.jetbrains.kotlinshellextension.processes.process.system

import org.slf4j.LoggerFactory
import org.zeroturnaround.exec.listener.ProcessListener

class SystemProcessListener (
    private val systemProcess: SystemProcess
) : ProcessListener() {
    override fun afterStop(process: Process?) = finalizeProcess()

    private fun finalizeProcess() {
        systemProcess.closeOut()
        logger.debug("finalized ${systemProcess.name}")
    }

    companion object {
        @JvmStatic
        private val logger = LoggerFactory.getLogger(SystemProcessListener::class.java)
    }
}
