package eu.jrie.jetbrains.kotlinshellextension.processes.process

abstract class Process protected constructor (
    val virtualPID: Int,
    val command: String,
    val arguments: List<String> = emptyList()
) {

    abstract fun redirectIn(source: ProcessInputStream): Process

    abstract fun redirectOut(destination: ProcessOutputStream): Process

    fun redirectOut(stdDestination: ProcessOutputStream, errDestination: ProcessOutputStream): Process {
        redirectStdOut(stdDestination)
        redirectStdErr(errDestination)
        return this
    }

    abstract fun redirectStdOut(destination: ProcessOutputStream): Process

    abstract fun redirectStdErr(destination: ProcessOutputStream): Process

    abstract fun setEnvironment(env: Map<String, String>): Process

    abstract fun setEnvironment(env: Pair<String, String>): Process

    abstract fun start(): PCB

    abstract fun isAlive(): Boolean

    abstract fun await(timeout: Long = 0)

    abstract fun kill()

    fun ifAlive(action: () -> Unit) {
        if (isAlive()) action()
    }

}
