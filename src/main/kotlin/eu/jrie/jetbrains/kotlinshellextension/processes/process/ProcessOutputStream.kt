package eu.jrie.jetbrains.kotlinshellextension.processes.process

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.zeroturnaround.exec.stream.LogOutputStream

class ProcessOutputStream (
    private val scope: CoroutineScope
) : LogOutputStream() {

    private val channel = Channel<Byte>(CHANNEL_BUFFER)

    override fun processLine(line: String?) {
        if (line != null) addToChannel(line)
    }

    private fun addToChannel(line: String) = runBlocking {
        line.plus(LINE_END)
            .map { it.toByte() }
            .forEach { channel.send(it) }
    }

    @ExperimentalCoroutinesApi
    suspend fun subscribe(onLine: (Byte) -> Unit) = scope.launch { channel.consumeEach { onLine(it) } }

    override fun close() {
        super.close()
        channel.close()
    }

    companion object {
        const val CHANNEL_BUFFER = 512
        private const val LINE_END = '\n'
    }
}
