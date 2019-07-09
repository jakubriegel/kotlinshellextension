package eu.jrie.jetbrains.kotlinshellextension.processes.process.stream

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import org.jetbrains.annotations.TestOnly

class ProcessInputStream @TestOnly internal constructor(
    scope: CoroutineScope,
    channel: Channel<Byte>
) : ProcessStream(scope, channel) {

    constructor(scope: CoroutineScope) : this(scope, Channel<Byte>(CHANNEL_BUFFER_SIZE))

    fun write(line: String) = write(line.toByteArray())

    fun write(line: ByteArray) = runBlocking (scope.coroutineContext) {
        line.forEach { write(it) }
    }

    suspend fun write(b: Byte) {
        channel.send(b)
    }

    fun writeNewLine() = writeAsLine(ByteArray(0))

    fun writeAsLine(data: String) = writeAsLine(data.toByteArray())

    fun writeAsLine(data: ByteArray) = write(data.plus(LINE_END.toByte()))


    fun read() = runBlocking (scope.coroutineContext) {
        channel.receive()
    }
}
