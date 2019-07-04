package eu.jrie.jetbrains.kotlinshellextension.processes.process

import java.time.Instant

abstract class PCB {
    var startTime: Instant? = null
    internal set
}
