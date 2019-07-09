package eu.jrie.jetbrains.kotlinshellextension.processes

import eu.jrie.jetbrains.kotlinshellextension.processes.process.Process
import eu.jrie.jetbrains.kotlinshellextension.processes.process.ProcessBuilder
import eu.jrie.jetbrains.kotlinshellextension.processes.process.ProcessConfiguration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch

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

    fun startProcess(vPID: Int) = startProcess(getProcessByVirtualPID(vPID))

    fun startProcess(process: Process) {
        process.start()
    }

    fun awaitProcess(vPID: Int, timeout: Long = 0) = getProcessByVirtualPID(vPID).await(timeout)

    fun killProcess(vPID: Int) = getProcessByVirtualPID(vPID).kill()

    @ExperimentalCoroutinesApi
    fun pipe(vPID1: Int, vPID2: Int) = scope.launch {
        val p1 = getProcessByVirtualPID(vPID1)
        val p2 = getProcessByVirtualPID(vPID2)

        if (!p1.isAlive()) startProcess(p1)
        p1.stdout.subscribe(
            { p2.input.writeBlocking(it) },
            { p2.input.close() }
        )

        if (!p2.isAlive()) startProcess(p2)
    }

    @ExperimentalCoroutinesApi
    fun pipe(vPID1: Int, vPID2: Int, tap: (Byte) -> Unit) = scope.launch {
        pipe(vPID1, vPID2)
        getProcessByVirtualPID(vPID2).stdout.subscribe(tap)
    }

    @ExperimentalCoroutinesApi
    fun pipe(vararg vPIDs: Int, tap: (Byte) -> Unit) {
        for (i in vPIDs.indices.drop(1).dropLast(1)) {
            pipe(vPIDs[i-1], vPIDs[i])
        }
        pipe(vPIDs[vPIDs.size-2], vPIDs[vPIDs.size-1], tap)
    }

    @ExperimentalCoroutinesApi
    fun pipeRec(vararg vPIDs: Int, tap: (Byte) -> Unit) {
        pipe(0, 1, tap, vPIDs)
    }

    @ExperimentalCoroutinesApi
    private tailrec fun pipe(p1: Int, p2: Int, tap: (Byte) -> Unit, vPIDs: IntArray) {
        if (p2 == vPIDs.lastIndex) {
            pipe(vPIDs[p1], vPIDs[p2], tap)
        }
        else {
            pipe(vPIDs[p1], vPIDs[p2])
            pipe(p2, p2+1, tap, vPIDs)
        }
    }

    private fun getProcessByVirtualPID(vPID: Int) =
        processes.find { it.vPID == vPID } ?: throw Exception("no processes with given virtual PID: $vPID")

    companion object {
        private var nextVirtualPID = 1
        private fun virtualPID() = nextVirtualPID++

    }
}
