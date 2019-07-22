package eu.jrie.jetbrains.kotlinshellextension.processes.process

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import org.slf4j.LoggerFactory

@ExperimentalCoroutinesApi
class ProcessIOBuffer {
    private val channel: ProcessChannel = Channel(DEFAULT_CHANNEL_BUFFER_SIZE)

    internal suspend fun consumeFrom(input: ProcessReceiveChannel) {
        input.consumeEach { channel.send(it) }
        channel.close()
        logger.debug("closed $this")
    }

    suspend fun receiveTo(output: ProcessSendChannel) {
        channel.consumeEach { output.send(it) }
        output.close()
        logger.debug("closed out $output")
    }

    companion object {
        private const val DEFAULT_CHANNEL_BUFFER_SIZE = 4096

        private val logger = LoggerFactory.getLogger(ProcessIOBuffer::class.java)
    }
}
