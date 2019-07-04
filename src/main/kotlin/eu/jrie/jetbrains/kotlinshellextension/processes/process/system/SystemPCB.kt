package eu.jrie.jetbrains.kotlinshellextension.processes.process.system

import eu.jrie.jetbrains.kotlinshellextension.processes.process.PCB
import org.zeroturnaround.exec.StartedProcess

class SystemPCB : PCB() {
    var systemPID: Long = -1
        internal set

    var startedProcess: StartedProcess? = null
        internal set
}
