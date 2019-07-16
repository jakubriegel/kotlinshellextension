package eu.jrie.jetbrains.kotlinshellextension.processes.process

import eu.jrie.jetbrains.kotlinshellextension.processes.process.stream.ProcessStream
import eu.jrie.jetbrains.kotlinshellextension.testutils.TestDataFactory.ENVIRONMENT
import eu.jrie.jetbrains.kotlinshellextension.testutils.TestDataFactory.VIRTUAL_PID
import io.mockk.every
import io.mockk.just
import io.mockk.runs
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
import java.io.File

class ProcessTest {

    private val process = spyk<SampleProcess>()

    companion object {
        val stdMock = spyk<ProcessStream> {
            every { close() } just runs
        }
        val errMock = spyk<ProcessStream> {
            every { close() } just runs
        }
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
    fun `should await process`() = runBlocking {
        val timeout: Long = 500
        every { process.expect(timeout) } returns launch { }

        // when
        process.await(timeout).join()

        // then
        assertEquals(process.pcb.state, ProcessState.TERMINATED)
    }

    @Test
    fun `should close stdout and stderr`() {
        // when
        process.closeOut()

        // then
        verify (exactly = 1) { stdMock.close() }
        verify (exactly = 1) { errMock.close() }
    }

    private open class SampleProcess : Process(
        VIRTUAL_PID,
        spyk(),
        stdMock,
        errMock,
        ENVIRONMENT,
        File(""),
        spyk()
    ) {
        override val pcb: PCB = spyk()
        override val statusCmd = ""
        override val statusOther = ""

        override fun execute(): PCB = spyk()
        override fun isAlive(): Boolean = false
        override fun expect(timeout: Long): Job = spyk()
        override fun destroy() {}
    }

}
