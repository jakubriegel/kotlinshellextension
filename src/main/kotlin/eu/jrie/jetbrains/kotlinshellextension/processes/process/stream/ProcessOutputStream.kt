package eu.jrie.jetbrains.kotlinshellextension.processes.process.stream

import kotlinx.coroutines.Job

interface ProcessOutputStream {

    fun read(): Byte

     fun subscribe(onNext: (Byte) -> Unit): Job

    fun subscribe(onNext: (Byte) -> Unit, afterLast: () -> Unit): Job

}
