package eu.jrie.jetbrains.kotlinshellextension.processes.process.stream

import java.io.File

interface ProcessInputStream {

    fun write(line: String)

    fun write(line: ByteArray)

    fun fromFile(file: File): ProcessStream

    suspend fun write(b: Byte)

    fun writeBlocking(b: Byte)

    fun writeNewLine()

    fun writeAsLine(data: String)

    fun writeAsLine(data: ByteArray)
}
