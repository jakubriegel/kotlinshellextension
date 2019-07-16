package eu.jrie.jetbrains.kotlinshellextension.processes.process.stream

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import org.jetbrains.annotations.TestOnly

class NullProcessStream @TestOnly internal constructor(
    channel: Channel<Byte>
) : ProcessStream(channel) {

    constructor() : this(Channel(CHANNEL_BUFFER_SIZE))

    override fun write(line: String) {}

    override fun writeAsLine(data: String) {}

    override fun write(line: ByteArray) {}

    override fun writeAsLine(data: ByteArray) {}

    override fun writeNewLine() {}

    override suspend fun write(b: Byte) {}

    override fun writeBlocking(b: Byte) {}

    override fun read(): Byte = -1

    @ExperimentalCoroutinesApi
    override fun subscribe(onNext: (Byte) -> Unit): Job = Job()

    @ExperimentalCoroutinesApi
    override fun subscribe(onNext: (Byte) -> Unit, afterLast: () -> Unit) = Job()
}
