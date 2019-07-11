package eu.jrie.jetbrains.kotlinshellextension.processes.process.stream

import eu.jrie.jetbrains.kotlinshellextension.testutils.TestDataFactory.VIRTUAL_PID
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertIterableEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ProcessStreamTest {

    private val channelSpy = spyk(Channel<Byte>(ProcessStream.CHANNEL_BUFFER_SIZE))

    private val stream = ProcessStream(channelSpy)

    @Test
    fun `should initialize the stream`() = runBlocking {
        // given
        val context = this.coroutineContext
        val scopeMock = mockk<CoroutineScope> {
            every { coroutineContext } returns context
        }
        coEvery { channelSpy.receive() } returns 1

        // when
        stream.initialize(VIRTUAL_PID, scopeMock)
        stream.read()

        // then
        verify (exactly = 1) { scopeMock.coroutineContext }

        assertEquals("[ProcessStream $VIRTUAL_PID]", stream.name)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun `should close channel`() {
        // when
        stream.close()

        // then
        assertTrue(channelSpy.isClosedForReceive)
        assertTrue(channelSpy.isClosedForSend)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun `should write string`() {
        // given
        val line = "abcABC"

        // when
        runTest { stream.write(line) }

        // then
        val sent = readFromChannel()
        assertEquals(String(sent), line)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun `should write string as new line`() {
        // given
        val line = "abcABC"

        // when
        runTest { stream.writeAsLine(line) }

        // then
        val sent = readFromChannel()
        assertEquals(String(sent), line.plus(NEW_LINE))
    }

    @Test
    @ExperimentalCoroutinesApi
    fun `should write ByteArray`() {
        // given
        val line = "abcABC".toByteArray()

        // when
        runTest { stream.write(line) }

        // then
        val sent = readFromChannel()
        assertArraysEquals(sent, line)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun `should write ByteArray as new line`() {
        // given
        val line = "abcABC".toByteArray()

        // when
        runTest { stream.writeAsLine(line) }

        // then
        val sent = readFromChannel()
        assertArraysEquals(sent, line.plus(NEW_LINE.toByte()))
    }

    @Test
    @ExperimentalCoroutinesApi
    fun `should write new line`() {
        // when
        runTest { stream.writeNewLine() }

        // then
        val sent = readFromChannel()
        assertEquals(sent.size, 1)
        assertEquals(sent.first(), NEW_LINE.toByte())
    }

    @Test
    @ExperimentalCoroutinesApi
    fun `should write byte`() {
        // given
        val a = 'a'.toByte()

        // when
        runTest { stream.write(a) }

        // then
        val sent = readFromChannel()
        assertEquals(sent.size, 1)
        assertEquals(sent.first(), a)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun `should write byte blocking`() {
        // given
        val a = 'a'.toByte()

        // when
        stream.writeBlocking(a)

        // then
        channelSpy.close()
        val sent = readFromChannel()
        assertEquals(sent.size, 1)
        assertEquals(sent.first(), a)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun `should read byte blocking`() {
        // given
        val a = 'a'.toByte()
        coEvery { channelSpy.receive() } returns a

        // when
        val result = runTest { stream.read() }

        // then
        assertEquals(result, a)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun `should subscribe to channel`() {
        // given
        val sent = listOf('a', 'b', 'C').map { it.toByte() }
        runBlocking { sent.forEach { channelSpy.send(it) } }

        val received = mutableListOf<Byte>()
        val onNext: (Byte) -> Unit = { received.add(it) }

        // when
        runTest { stream.subscribe(onNext) }

        // then
        channelSpy.close()
        assertTrue(received == sent)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun `should subscribe to channel with end callback`() {
        // given
        val sent = listOf('a', 'b', 'C').map { it.toByte() }
        runBlocking { sent.forEach { channelSpy.send(it) } }

        val received = mutableListOf<Byte>()
        val onNext: (Byte) -> Unit = { received.add(it) }

        var afterLastExecuted = false
        val afterLast = { afterLastExecuted = true }

        // when
        runTest { stream.subscribe(onNext, afterLast) }

        // then
        assertEquals(received, sent)
        assertTrue(afterLastExecuted)
    }

    private fun <T> runTest(test: suspend () -> T) = runBlocking {
        stream.initialize(VIRTUAL_PID, this)
        val result = test()
        channelSpy.close()
        result
    }

    @ExperimentalCoroutinesApi
    private fun readFromChannel() = runBlocking {
            val bytes = mutableListOf<Byte>()
            channelSpy.consumeEach { bytes.add(it) }
            bytes
        }.toByteArray()

    private fun assertArraysEquals(a1: ByteArray, a2: ByteArray) {
        assertIterableEquals(a1.toList(), a2.toList())
    }

    private companion object {
        private const val NEW_LINE = '\n'
    }
}
