package eu.jrie.jetbrains.kotlinshellextension.processes

import eu.jrie.jetbrains.kotlinshellextension.processes.process.Process
import eu.jrie.jetbrains.kotlinshellextension.processes.process.ProcessBuilder
import eu.jrie.jetbrains.kotlinshellextension.processes.process.ProcessConfiguration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory

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

    fun awaitProcess(vPID: Int, timeout: Long = 0) {
        awaitProcess(getProcessByVirtualPID(vPID), timeout)
    }

    fun awaitProcess(process: Process, timeout: Long = 0) = runBlocking (scope.coroutineContext) {
        process.await(timeout).join()
    }

    fun awaitAll() {
        logger.debug("awaiting all processes")
        processes.forEach { awaitProcess(it) }
        logger.debug("all processes awaited")
    }

    fun killProcess(vPID: Int) {
        killProcess(getProcessByVirtualPID(vPID))
    }

    fun killProcess(process: Process) {
        process.kill()
    }

    @ExperimentalCoroutinesApi
    fun pipe(vPID1: Int, vPID2: Int) = scope.launch{
        pipe(
            getProcessByVirtualPID(vPID1),
            getProcessByVirtualPID(vPID2)
        )
    }

    @ExperimentalCoroutinesApi
    fun pipe(vPID1: Int, vPID2: Int, tap: (Byte) -> Unit) = scope.launch{
        pipe(
            getProcessByVirtualPID(vPID1),
            getProcessByVirtualPID(vPID2),
            tap
        )
    }

    @ExperimentalCoroutinesApi
    suspend fun pipe(first: Process, second: Process, tap: (Byte) -> Unit) {
        pipe(first, second)
        second.stdout.subscribe(tap)
    }

    @ExperimentalCoroutinesApi
    suspend fun pipe(first: Process, second: Process) {
        if (!first.isAlive()) startProcess(first)
        first.stdout.subscribe(
            { second.input.writeBlocking(it) },
            { second.input.close() }
        )

        if (!second.isAlive()) startProcess(second)
    }

    @ExperimentalCoroutinesApi
    fun pipe(vararg vPIDs: Int, tap: (Byte) -> Unit) = runBlocking(scope.coroutineContext) {
        val processes = vPIDs
            .map { getProcessByVirtualPID(it) }
            .toTypedArray()

        pipe(*processes) { tap(it) }
    }

    @ExperimentalCoroutinesApi
    fun pipe(vararg processes: Process, tap: (Byte) -> Unit) = runBlocking(scope.coroutineContext) {
        // TODO choose implementation
//        pipeIt(*processes) { tap(it) }
        pipeRec(*processes) { tap(it) }
    }

    @ExperimentalCoroutinesApi
    private suspend fun pipeIt(vararg processes: Process, tap: (Byte) -> Unit) {
        for (i in processes.indices.drop(1).dropLast(1)) {
            pipe(processes[i-1], processes[i])
        }
        pipe(processes[processes.size-2], processes[processes.size-1], tap)
    }

    @ExperimentalCoroutinesApi
    private suspend fun pipeRec(vararg processes: Process, tap: (Byte) -> Unit) {
        pipeRec(0, 1, tap, processes)
    }

    @ExperimentalCoroutinesApi
    private tailrec suspend fun pipeRec(p1: Int, p2: Int, tap: (Byte) -> Unit, processes: Array<out Process>) {
        if (p2 == processes.lastIndex) {
            pipe(processes[p1], processes[p2], tap)
        }
        else {
            pipe(processes[p1], processes[p2])
            pipeRec(p2, p2+1, tap, processes)
        }
    }

    private fun getProcessByVirtualPID(vPID: Int) =
        processes.find { it.vPID == vPID } ?: throw Exception("no processes with given virtual PID: $vPID")

    companion object {
        private var nextVirtualPID = 1
        private fun virtualPID() = nextVirtualPID++

        private val logger = LoggerFactory.getLogger(ProcessCommander::class.java)
    }
}
