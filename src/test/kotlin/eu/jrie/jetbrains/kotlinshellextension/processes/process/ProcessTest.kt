package eu.jrie.jetbrains.kotlinshellextension.processes.process

import eu.jrie.jetbrains.kotlinshellextension.testutils.TestDataFactory.ENVIRONMENT
import eu.jrie.jetbrains.kotlinshellextension.testutils.TestDataFactory.VIRTUAL_PID
import io.mockk.every
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import java.io.File

class ProcessTest {

    private val process = spyk<SampleProcess>()

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
        every { process.isAlive() } returns true

        // when
        process.await(timeout)

        // then
        assertEquals(ProcessState.TERMINATED, process.pcb.state)
    }

    @Test
    fun `should close stdout and stderr`() {
        // when
        process.closeOut()
        TODO("implement channels")

        // then
    }

    private open class SampleProcess : Process(
        VIRTUAL_PID,
        null,
        ENVIRONMENT,
        File(""),
        spyk()
    ) {
        override val pcb: PCB = spyk()
        override val statusCmd = ""
        override val statusOther = ""

        override fun execute(): PCB = spyk()
        override fun isAlive(): Boolean = false
        override suspend fun expect(timeout: Long) {}
        override fun destroy() {}
    }

}
