package eu.jrie.jetbrains.kotlinshellextension.processes.process.stream

import eu.jrie.jetbrains.kotlinshellextension.processes.process.stream.ProcessStream.Companion.CHANNEL_BUFFER_SIZE
import io.mockk.confirmVerified
import io.mockk.spyk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

class NullProcessStreamTest {

    private val channelSpy = spyk(Channel<Byte>(CHANNEL_BUFFER_SIZE))
    private val stream = NullProcessStream(channelSpy)

    @AfterEach
    fun verifyNoAction() {
        confirmVerified(channelSpy)
    }

    @Test
    fun `should do nothing when write string`() {
        // given
        val data = "abc"

        // when
        stream.write(data)
    }

    @Test
    fun `should do nothing when write string as line`() {
        // given
        val data = "abc"

        // when
        stream.writeAsLine(data)
    }

    @Test
    fun `should do nothing when write byte array`() {
        // given
        val data = "abc".toByteArray()

        // when
        stream.write(data)
    }

    @Test
    fun `should do nothing when write byte array as line`() {
        // given
        val data = "abc".toByteArray()

        // when
        stream.writeAsLine(data)
    }

    @Test
    fun `should do nothing when write new line`() {
        // when
        stream.writeNewLine()
    }

    @Test
    fun `should do nothing when write byte`() {
        // given
        val data = 'a'.toByte()

        // when
        runBlocking { stream.write(data) }
    }

    @Test
    fun `should do nothing when write byte blocking`() {
        // given
        val data = 'a'.toByte()

        // when
        stream.writeBlocking(data)
    }

    @Test
    fun `should do nothing when read`() {
        // when
        stream.read()
    }

    @Test
    @ExperimentalCoroutinesApi
    fun `should do nothing when subscribe`() {
        // when
        stream.subscribe {  }
    }

    @Test
    @ExperimentalCoroutinesApi
    fun `should do nothing when subscribe with afterLast`() {
        // when
        stream.subscribe({}, {})
    }
}