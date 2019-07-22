package eu.jrie.jetbrains.kotlinshellextension.processes.process

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.io.core.BytePacketBuilder
import kotlinx.io.core.ByteReadPacket
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

@ExperimentalCoroutinesApi
class ProcessIOBufferTest {
    private val buffer = ProcessIOBuffer()

    @Test
    fun `should consume all of given channel`() = runBlocking {
        // given
        val producer = Channel<ByteReadPacket>()
        val data = BytePacketBuilder().apply { writeStringUtf8("some text") } .build()

        // when
        val consume = launch { buffer.consumeFrom(producer) }
        producer.send(data)

        // then
        assertTrue(producer.isEmpty)

        producer.close()
        consume.join()
    }

    @Test
    fun `should pass all contents to given channel`() = runBlocking {
        // given
        val consumer = Channel<ByteReadPacket>(4)

        val producer = Channel<ByteReadPacket>()
        val data = BytePacketBuilder().apply { writeStringUtf8("some text") } .build()
        val consume = launch { buffer.consumeFrom(producer) }
        producer.send(data)
        producer.close()
        consume.join()

        // when
        buffer.receiveTo(consumer)

        // then
        assertFalse(consumer.isEmpty)
        assertTrue(consumer.isClosedForSend)
    }
}
