package eu.jrie.jetbrains.kotlinshellextension.processes.process

import eu.jrie.jetbrains.kotlinshellextension.testutils.TestDataFactory.ENVIRONMENT
import eu.jrie.jetbrains.kotlinshellextension.testutils.TestDataFactory.VIRTUAL_PID
import io.mockk.every
import io.mockk.spyk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import java.io.File

@ExperimentalCoroutinesApi
class ProcessTest {

    private lateinit var process: Process// = spyk<SampleProcess>()

    @Test
    fun `should start process`() {
        // when
        runTest { process.start() }

        // then
        assertEquals(process.pcb.state, ProcessState.RUNNING)
    }

    @ParameterizedTest(name = "{index} {0} should throw exception when tried to start not READY process")
    @EnumSource(ProcessState::class)
    fun `should throw exception when tried to start not READY process`(state: ProcessState) {
        runTest {
            // given
            process.pcb.state = state

            // when
            if (state != ProcessState.READY) {
                // then
                assertThrows<Exception> { process.start() }
            }
        }
    }

    @Test
    fun `should await process`() = runBlocking {
        val timeout: Long = 500

        // when
        runTest {
            every { process.isAlive() } returns true
            process.await(timeout)
        }

        // then
        assertEquals(ProcessState.TERMINATED, process.pcb.state)
    }

    @Test
    fun `should kill process`() = runBlocking {
        // when
        runTest {
            process.kill()
        }

        // then
        assertEquals(ProcessState.TERMINATED, process.pcb.state)
    }

    private fun runTest(test: suspend ProcessTest.() -> Unit) = runBlocking {
        process = spyk(SampleProcess(this))
        test()
        process.closeOut()
    }

    private open class SampleProcess (
        scope: CoroutineScope
    ) : Process(
        VIRTUAL_PID,
        ENVIRONMENT,
        File(""),
        null,
        null,
        null,
        scope
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
