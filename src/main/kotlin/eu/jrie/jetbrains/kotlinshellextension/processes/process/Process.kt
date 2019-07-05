package eu.jrie.jetbrains.kotlinshellextension.processes.process

abstract class Process protected constructor (
    val virtualPID: Int,
    val command: String,
    val arguments: List<String> = emptyList()
) {

    abstract val pcb: PCB

    lateinit var input: ProcessInputStream protected set
    lateinit var stdout: ProcessOutputStream protected set
    lateinit var stderr: ProcessOutputStream protected set

    abstract fun redirectIn(source: ProcessInputStream): Process

    abstract fun followMergedOut(): Process

    abstract fun followMergedOut(destination: ProcessOutputStream): Process

    fun followOut() = apply {
        followStdOut()
        followStdErr()
    }

    fun followOut(stdDestination: ProcessOutputStream, errDestination: ProcessOutputStream) = apply {
        followStdOut(stdDestination)
        followStdErr(errDestination)
    }

    abstract fun followStdOut(): Process

    abstract fun followStdOut(destination: ProcessOutputStream): Process

    abstract fun followStdErr(): Process

    abstract fun followStdErr(destination: ProcessOutputStream): Process

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
