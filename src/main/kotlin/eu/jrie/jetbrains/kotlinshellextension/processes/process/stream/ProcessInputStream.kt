package eu.jrie.jetbrains.kotlinshellextension.processes.process.stream

interface ProcessInputStream {

    fun write(line: String)

    fun write(line: ByteArray)

    suspend fun write(b: Byte)

    fun writeBlocking(b: Byte)

    fun writeNewLine()

    fun writeAsLine(data: String)

    fun writeAsLine(data: ByteArray)
}
