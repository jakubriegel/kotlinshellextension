package eu.jrie.jetbrains.kotlinshellextension.processes.process.stream

import kotlinx.coroutines.Job
import java.io.File

interface ProcessOutputStream {

    fun read(): Byte

    fun subscribe(onNext: (Byte) -> Unit): Job

    fun subscribe(onNext: (Byte) -> Unit, afterLast: () -> Unit): Job

    fun subscribe(file: File): Job

}
