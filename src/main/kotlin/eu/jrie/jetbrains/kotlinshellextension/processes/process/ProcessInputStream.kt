package eu.jrie.jetbrains.kotlinshellextension.processes.process

import java.io.PipedInputStream
import java.io.PipedOutputStream
import kotlin.concurrent.thread

class ProcessInputStream {

    private val input = PipedOutputStream()
    val tap = PipedInputStream(input)

    fun write(data: String) = write(data.toByteArray())

    fun write(data: ByteArray) = data.forEach { write(it) }

    fun write(b: Byte) = write(b.toInt())

    fun write(b: Int) {
        // TODO: implement in less heavy way
        thread { input.write(b) }
           .join()
    }

    fun writeNewLine() = write('\n'.toInt())

    fun writeAsLine(data: String) = writeAsLine(data.toByteArray())

    fun writeAsLine(data: ByteArray) {
        data.forEach { write(it) }
        writeNewLine()
    }

}
