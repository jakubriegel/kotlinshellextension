package eu.jrie.jetbrains.kotlinshellextension.processes.process

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.runBlocking
import org.zeroturnaround.exec.stream.LogOutputStream

class ProcessOutputStream : LogOutputStream() {

    private val channel = Channel<String>()

    init {
        println("output init")
    }

    override fun processLine(line: String?) {
        if (line != null) addToChannel(line)
    }


    private fun addToChannel(line: String) = runBlocking {
        channel.send(line)
    }

    @ExperimentalCoroutinesApi
    suspend fun subscribe(onLine: (String) -> Unit) = channel.consumeEach { onLine(it) }

    override fun close() {
        super.close()
        channel.close()
    }
}
