package eu.jrie.jetbrains.kotlinshellextension.processes.process

import eu.jrie.jetbrains.kotlinshellextension.processes.process.stream.ProcessInputStream
import eu.jrie.jetbrains.kotlinshellextension.processes.process.stream.ProcessOutputStream
import eu.jrie.jetbrains.kotlinshellextension.testutils.TestDataFactory.processInputStreamSpy
import eu.jrie.jetbrains.kotlinshellextension.testutils.TestDataFactory.processOutputStreamSpy
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

class ProcessTest {

    private val process = spyk<SampleProcess>()

    private companion object {
        const val COMMAND = "cmd"
        const val VIRTUAL_PID = 1
    }

    @Test
    fun `should redirect input`() {
        // when
        process.followIn()

        // then
        verify {
            process.followIn(ofType(ProcessInputStream::class))
        }
    }

    @Test
    fun `should redirect input to given stream`() {
        // given
        val inputSpy = processInputStreamSpy()

        // when
        process.followIn(inputSpy)

        // then
        assertEquals(process.input, inputSpy)
        assertEquals(process.vPID, inputSpy.vPID)
    }

    @Test
    fun `should redirect merged output to stdout`() {
        // when
        process.followMergedOut()

        // then
        verify (exactly = 1) {
            process.followMergedOut(ofType(ProcessOutputStream::class))
        }
    }

    @Test
    fun `should redirect merged output to given stream`() {
        // given
        val outSpy = processOutputStreamSpy()

        // when
        process.followMergedOut(outSpy)

        // then
        assertEquals(process.stdout, outSpy)
        assertEquals(process.vPID, outSpy.vPID)
    }

    @Test
    fun `should redirect outputs to correct destinations`() {
        // when
        process.followOut()

        // then
        verify (exactly = 1) { process.followOut() }
        verify (exactly = 1) { process.followStdOut() }
        verify (exactly = 1) { process.followStdErr() }
        verify (exactly = 1) { process.followStdOut(ofType(ProcessOutputStream::class)) }
        verify (exactly = 1) { process.followStdErr(ofType(ProcessOutputStream::class)) }
    }

    @Test
    fun `should redirect outputs to given destinations`() {
        // given
        val stdSpy = processOutputStreamSpy()
        val errSpy = processOutputStreamSpy()

        // when
        process.followOut(stdSpy, errSpy)

        // then
        verify (exactly = 1) { process.followStdOut(stdSpy) }
        verify (exactly = 1) { process.followStdErr(errSpy) }
        assertEquals(process.stdout, stdSpy)
        assertEquals(process.stderr, errSpy)
        assertEquals(process.vPID, stdSpy.vPID)
        assertEquals(process.vPID, errSpy.vPID)
    }

    @Test
    fun `should start process`() {
        // when
        process.start()

        // then
        assertEquals(process.pcb.state, ProcessState.RUNNING)
    }

    @ParameterizedTest(name = "{index} {0} should throw exception when tried to start not READY process")
    @EnumSource(ProcessState::class)
    fun `should throw exception when tried to start not READY process`(state: ProcessState) {
        // given
        process.pcb.state = state

        // when
        if (state != ProcessState.READY) {
            // then
            assertThrows<Exception> { process.start() }
        }
    }

    @Test
    fun `should perform given action when process is alive`() {
        // given
        val action = { process.kill() }

        every { process.isAlive() } returns true

        // when
        process.ifAlive(action)

        // then
        verify (exactly = 1) {
            process.ifAlive(any())
            process.isAlive()
        }
        verify (exactly = 1) { process.kill() }

    }

    @Test
    fun `should not perform given action when process is not alive`() {
        // given
        val action = { process.kill() }

        every { process.isAlive() } returns false

        // when
        process.ifAlive(action)

        // then
        verify (exactly = 1) {
            process.ifAlive(any())
            process.isAlive()
        }
        verify (exactly = 0) { process.kill() }

    }

    @Test
    fun `should await process`(): Unit = runBlocking {
        val timeout: Long = 500
        every { process.expect(timeout) } returns launch { }

        // when
        process.await(timeout).join()

        // then
        assertEquals(process.pcb.state, ProcessState.TERMINATED)
    }

    @Test
    fun `should close stdout and stderr`() {
        // given
        val stdMock = spyk(ProcessOutputStream(mockk())) {
            every { close() } just Runs
        }
        val errMock = spyk(ProcessOutputStream(mockk())) {
            every { close() } just Runs
        }

        process.followOut(stdMock, errMock)

        // when
        process.closeOut()

        // then
        verify (exactly = 1) { stdMock.close() }
        verify (exactly = 1) { errMock.close() }
    }

    private open class SampleProcess : Process(VIRTUAL_PID, COMMAND, scope = mockk()) {

        override val pcb: PCB = spyk()

        override fun redirectIn(source: ProcessInputStream) {}

        override fun redirectMergedOut(destination: ProcessOutputStream) {}

        override fun redirectStdOut(destination: ProcessOutputStream) {}

        override fun redirectStdErr(destination: ProcessOutputStream) {}

        override fun setEnvironment(env: Map<String, String>): Process = mockk()

        override fun setEnvironment(env: Pair<String, String>): Process = mockk()

        override fun execute(): PCB = mockk()

        override fun isAlive(): Boolean = false

        override fun expect(timeout: Long): Job = mockk()

        override fun destroy() {}
    }

}
