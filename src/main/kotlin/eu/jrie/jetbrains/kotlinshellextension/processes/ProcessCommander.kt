package eu.jrie.jetbrains.kotlinshellextension.processes

import eu.jrie.jetbrains.kotlinshellextension.processes.process.Process
import eu.jrie.jetbrains.kotlinshellextension.processes.process.ProcessBuilder
import kotlinx.coroutines.CoroutineScope
import org.slf4j.LoggerFactory

class ProcessCommander (
    val scope: CoroutineScope
) {

    private val processes = mutableSetOf<Process>()

    fun process(builder: ProcessBuilder): Process {
        return builder
            .withVirtualPID(virtualPID())
            .withScope(scope)
            .build()
            .also { processes.add(it) }
    }

    fun startProcess(vPID: Int) = startProcess(getProcessByVirtualPID(vPID))

    fun startProcess(process: Process) {
        process.start()
    }

    suspend fun awaitProcess(vPID: Int, timeout: Long = 0) {
        awaitProcess(getProcessByVirtualPID(vPID), timeout)
    }

    suspend fun awaitProcess(process: Process, timeout: Long = 0) {
        logger.debug("awaiting process ${process.name}")
        if (!processes.contains(process)) throw Exception("unknown process")
        process.await(timeout)
        logger.debug("awaited process ${process.name}")
    }

    suspend fun awaitAll() {
        logger.debug("awaiting all processes")
        processes.forEach { awaitProcess(it) }
        logger.debug("all processes awaited")
    }

    fun killProcess(vPID: Int) {
        killProcess(getProcessByVirtualPID(vPID))
    }

    fun killProcess(process: Process) {
        if (!processes.contains(process)) throw Exception("unknown process")
        process.kill()
    }

    fun killAll() {
        logger.debug("killing all processes")
        processes.forEach { killProcess(it) }
        logger.debug("all processes killed")
    }

    private fun getProcessByVirtualPID(vPID: Int) =
        processes.find { it.vPID == vPID } ?: throw Exception("no processes with given virtual PID: $vPID")

    internal fun status() = processes.joinToString (
        "\n",
        "PID\tTIME\t    CMD\n"
    ) { it.status }

    companion object {
        private var nextVirtualPID = 1
        private fun virtualPID() = nextVirtualPID++

        private val logger = LoggerFactory.getLogger(ProcessCommander::class.java)
    }
}
