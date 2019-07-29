package eu.jrie.jetbrains.kotlinshellextension.processes.process

import eu.jrie.jetbrains.kotlinshellextension.testutils.TestDataFactory.ENVIRONMENT
import eu.jrie.jetbrains.kotlinshellextension.testutils.TestDataFactory.VIRTUAL_PID
import io.mockk.every
import io.mockk.spyk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import java.io.File

@ExperimentalCoroutinesApi
class ProcessTest {

    private lateinit var process: Process

    @Test
    fun `should start and execute process`() {
        // when
        runTest { process.start() }

        // then
        assertEquals(process.pcb.state, ProcessState.TERMINATED)
    }

    @ParameterizedTest(name = "{index} {0} should throw exception when tried to start not READY process")
    @EnumSource(ProcessState::class)
    fun `should throw exception when tried to start not READY process`(state: ProcessState) = runTest {
        // given
        process.pcb.state = state

        // when
        if (state != ProcessState.READY) {
            // expect
            val e  = runCatching { process.start() }
            assertEquals(Exception::class, e.exceptionOrNull()!!::class)
        }
    }

    @Test
    fun `should await process`() = runBlocking {
        val timeout: Long = 500

        // when
        runTest {
            every { process.isAlive() } returns true

            process.closeOut()
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

    @Test
    fun `should get process name`() {
        // when
        runTest { /* initialize the process */ }
        val name = process.name
        val string = process.toString()

        // then
        // TODO: assertEquals(name, string())
    }

    private fun <T> runTest(test: suspend ProcessTest.() -> T): T = runBlocking {
        process = spyk(SampleProcess(this))
        val result = test()
        process.closeOut()
        result
    }

    private class SampleProcess (
        scope: CoroutineScope
    ) : Process(
        VIRTUAL_PID,
        ENVIRONMENT,
        File(""),
        Channel(),
        Channel(),
        Channel(),
        scope
    ) {
        override val pcb: PCB = spyk()
        override val statusCmd = ""
        override val statusOther = ""

        override suspend fun execute() = scope.launch { delay(1) }
        override fun isAlive(): Boolean = false
        override suspend fun expect(timeout: Long) {}
        override fun destroy() {}
    }

}
