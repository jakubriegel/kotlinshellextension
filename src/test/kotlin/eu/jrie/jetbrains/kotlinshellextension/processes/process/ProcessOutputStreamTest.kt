package eu.jrie.jetbrains.kotlinshellextension.processes.process

import eu.jrie.jetbrains.kotlinshellextension.processes.process.stream.ProcessOutputStream
import eu.jrie.jetbrains.kotlinshellextension.processes.process.stream.ProcessStream
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProcessOutputStreamTest {

    private val channelSpy = spyk(Channel<Byte>(ProcessStream.CHANNEL_BUFFER_SIZE))

    private lateinit var stream: ProcessOutputStream

    @Test
    @ExperimentalCoroutinesApi
    fun `should send line`() = runBlocking {
        // given
        stream = ProcessOutputStream(this, channelSpy)
        val line = "abcABC"

        // when
        stream.send(line)

        // then
        channelSpy.close()
        val sent = withContext(Dispatchers.Default) {
            val bytes = mutableListOf<Byte>()
            channelSpy.consumeEach { bytes.add(it) }
            bytes
        }

        assertTrue(line.plus('\n') == String(sent.toByteArray()))
    }

    @Test
    @ExperimentalCoroutinesApi
    fun `should send byte`() = runBlocking {
        // given
        stream = ProcessOutputStream(this, channelSpy)
        val b = 'a'.toByte()

        // when
        stream.send(b)

        // then
        val sent = channelSpy.receive()
        assertEquals(b, sent)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun `should subscribe to channel`() = runBlocking {
        // given
        stream = ProcessOutputStream(this, channelSpy)

        val sent = listOf('a', 'b', 'C').map { it.toByte() }
        sent.forEach { channelSpy.send(it) }

        val received = mutableListOf<Byte>()
        val onNext: (Byte) -> Unit = { received.add(it) }

        // when
        val job = stream.subscribe(onNext)

        // then
        channelSpy.close()
        job.join()
        assertTrue(received == sent)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun `should close channel`() {
        // given
        stream =
            ProcessOutputStream(mockk(), channelSpy)

        // when
        stream.close()

        // then
        assertTrue(channelSpy.isClosedForReceive)
        assertTrue(channelSpy.isClosedForSend)
    }
}
