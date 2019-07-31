package eu.jrie.jetbrains.kotlinshellextension.processes.pipeline

import eu.jrie.jetbrains.kotlinshellextension.processes.execution.ProcessExecutable
import eu.jrie.jetbrains.kotlinshellextension.processes.execution.ProcessExecutionContext
import eu.jrie.jetbrains.kotlinshellextension.processes.process.Process
import eu.jrie.jetbrains.kotlinshellextension.processes.process.ProcessChannel
import eu.jrie.jetbrains.kotlinshellextension.processes.process.ProcessReceiveChannel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import kotlinx.io.core.BytePacketBuilder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertIterableEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.InputStream
import java.io.OutputStream

@ExperimentalCoroutinesApi
class PipelineTest {

    private lateinit var contextMock: ProcessExecutionContext

    @Test
    fun `should create new pipeline from process`() {
        // given
        val processMock = mockk<Process>()
        val executableMock = processExecutableMock(processMock)

        // when
        val pipeline = runTest { Pipeline(executableMock, contextMock) }

        // then
        verify {
            executableMock.context
            executableMock.process
        }
        verify (exactly = 1) { executableMock setProperty("context") value ofType(ProcessExecutionContext::class) }
        coVerify (exactly = 1) {
            executableMock.init()
            executableMock.exec()
            executableMock.await()
        }
        confirmVerified(executableMock)

        assertEquals(processMock, pipeline.processes.last())
        assertFalse(pipeline.closed)
    }

    @Test
    fun `should create new pipeline from lambda`() {
        // given
        var started: Boolean? = null
        val lambda: PipelineContextLambda = { started = true }

        // when
        val pipeline = runTest { Pipeline(lambda, contextMock) }

        // then
        verify {
            contextMock.stdin
            contextMock.stdout
            contextMock.stderr
            contextMock.commander
        }

        assertTrue(started!!)
        assertIterableEquals(emptyList<Process>(), pipeline.processes)
        assertFalse(pipeline.closed)
    }

    @Test
    fun `should create new pipeline from channel`() {
        // given
        val channel: ProcessReceiveChannel = spyk()

        // when
        val pipeline = runTest { Pipeline(channel, contextMock) }

        // then
        confirmVerified(channel)
        confirmVerified(contextMock)

        assertIterableEquals(emptyList<Process>(), pipeline.processes)
        assertFalse(pipeline.closed)
    }

    @Test
    fun `should create new pipeline from stream`() {
        // given
        val data = sequenceOf(1, -1).iterator()
        val stream = object : InputStream() {
            override fun read(): Int = data.next()
        }

        // when
        val pipeline = runTest {
            Pipeline(stream, contextMock).apply { await() }
        }

        // then
        verify {
            contextMock.stdin
            contextMock.stdout
            contextMock.stderr
            contextMock.commander
        }

        assertFalse(data.hasNext())
        assertIterableEquals(emptyList<Process>(), pipeline.processes)
        assertFalse(pipeline.closed)
    }

    @Test
    fun `should add process to pipeline`() {
        // given
        val processMock = mockk<Process>()
        val executableMock = processExecutableMock(processMock)

        // when
        val pipeline = runTest {
            Pipeline(processExecutableMock(), contextMock).throughProcess(executableMock)
        }


        // then
        verify {
            executableMock.context
            executableMock.process
            executableMock setProperty("context") value ofType(ProcessExecutionContext::class)
        }
        coVerify (exactly = 1) {
            executableMock.init()
            executableMock.exec()
            executableMock.await()
        }
        confirmVerified(executableMock)

        assertEquals(processMock, pipeline.processes.last())
        assertEquals(2, pipeline.processes.size)
        assertFalse(pipeline.closed)
    }

    @Test
    fun `should add lambda to pipeline`() {
        // given
        var started: Boolean? = null
        val lambda: PipelineContextLambda = { started = true }

        // when
        val pipeline = runTest {
            Pipeline(processExecutableMock(), contextMock).throughLambda(lambda = lambda).apply { await() }
        }

        // then
        verify {
            contextMock.stdout
            contextMock.stderr
            contextMock.commander
        }

        assertTrue(started!!)
        assertEquals(1, pipeline.processes.size)
        assertFalse(pipeline.closed)
    }

    @Test
    fun `should end pipeline with channel`() {
        // given
        val channel: ProcessChannel = spyk()

        // when
        val pipeline = runTest {
            Pipeline(processExecutableMock(), contextMock).toEndChannel(channel)
        }

        // then
        confirmVerified(channel)

        verify {
            contextMock.stdout
            contextMock.stderr
            contextMock.commander
        }
        confirmVerified(contextMock)

        assertEquals(1, pipeline.processes.size)
        assertTrue(pipeline.closed)
    }

    @Test
    fun `should end pipeline with channel and do not close it`() {
        // given
        val channel: ProcessChannel = spyk()

        // when
        val pipeline = runTest {
            Pipeline(processExecutableMock(), contextMock).toEndChannel(channel)
        }

        // then
        confirmVerified(channel)

        verify {
            contextMock.stdout
            contextMock.stderr
            contextMock.commander
        }
        confirmVerified(contextMock)

        assertEquals(1, pipeline.processes.size)
        assertTrue(pipeline.closed)
    }

    @Test
    fun `should end pipeline with packet builder`() {
        // given
        val builder = BytePacketBuilder()

        // when
        val pipeline = runTest {
            Pipeline(processExecutableMock(), contextMock).toEndPacket(builder)
        }

        // then
        verify {
            contextMock.stdout
            contextMock.stderr
            contextMock.commander
        }
        confirmVerified(contextMock)

        assertEquals(1, pipeline.processes.size)
        assertTrue(pipeline.closed)
    }

    @Test
    fun `should end pipeline with stream`() {
        // given
        val stream = mockk<OutputStream>()

        // when
        val pipeline = runTest {
            Pipeline(processExecutableMock(), contextMock).toEndStream(stream)
        }

        // then
        verify {
            contextMock.stdout
            contextMock.stderr
            contextMock.commander
        }
        confirmVerified(contextMock)

        assertEquals(1, pipeline.processes.size)
        assertTrue(pipeline.closed)
    }

    @Test
    fun `should end pipeline with string builder`() {
        // given
        val builder = StringBuilder()

        // when
        val pipeline = runTest {
            Pipeline(processExecutableMock(), contextMock).toEndStringBuilder(builder)
        }

        // then
        verify {
            contextMock.stdout
            contextMock.stderr
            contextMock.commander
        }
        confirmVerified(contextMock)

        assertEquals(1, pipeline.processes.size)
        assertTrue(pipeline.closed)
    }

    private fun <T> runTest(test: suspend PipelineTest.() -> T): T = runBlocking {
        contextMock = contextMock(this)
        test()
    }

    private fun contextMock(scopeMock: CoroutineScope = mockk()) = mockk<ProcessExecutionContext> {
        every { stdin } returns Channel()
        every { stdout } returns Channel()
        every { stderr } returns Channel()
        every { commander } returns  mockk {
            every { scope } returns scopeMock
            coEvery { awaitProcess(any()) } just runs
        }
    }

    private fun processExecutableMock(processMock: Process = mockk()) = spyk (ProcessExecutable(contextMock(), mockk())) {
        every { process } returns processMock
        every { init() } just runs
        coEvery { exec() } just runs
        coEvery { await() } just runs
    }
}
