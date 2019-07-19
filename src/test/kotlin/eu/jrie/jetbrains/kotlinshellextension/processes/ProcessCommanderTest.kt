package eu.jrie.jetbrains.kotlinshellextension.processes

import eu.jrie.jetbrains.kotlinshellextension.processes.process.Process
import eu.jrie.jetbrains.kotlinshellextension.processes.process.ProcessBuilder
import eu.jrie.jetbrains.kotlinshellextension.processes.process.system.SystemProcess
import eu.jrie.jetbrains.kotlinshellextension.processes.process.system.SystemProcessBuilder
import eu.jrie.jetbrains.kotlinshellextension.testutils.TestDataFactory.PROCESS_COMMAND
import eu.jrie.jetbrains.kotlinshellextension.testutils.TestDataFactory.PROCESS_NAME
import eu.jrie.jetbrains.kotlinshellextension.testutils.TestDataFactory.VIRTUAL_PID
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.spyk
import io.mockk.verify
import io.mockk.verifyOrder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@ExperimentalCoroutinesApi
class ProcessCommanderTest {
    private val scopeSpy = spyk<CoroutineScope>()

    private val commander = ProcessCommander(scopeSpy)

    @Test
    fun `should create new SystemProcess`() = runBlocking {
        // given
        val scope = this
        val c = ProcessCommander(scope)
        val builder = spyk(SystemProcessBuilder(PROCESS_COMMAND))

        // when
        val process = c.process(builder)
        process.closeOut()

        // then
        verifyOrder {
            builder.withVirtualPID(ofType(Int::class))
            builder.withScope(scope)
            builder.build()
        }

        assertTrue(process.vPID > 0)
    }

    @Test
    fun `should assign unique vPID to process`() = runBlocking {
        // given
        val c = ProcessCommander(this)
        val builder1 = spyk(SystemProcessBuilder(PROCESS_COMMAND))
        val builder2 = spyk(SystemProcessBuilder(PROCESS_COMMAND))

        // when
        val process1 = c.process(builder1)
        val process2 = c.process(builder2)
        process1.closeOut()
        process2.closeOut()

        // then
        verify (exactly = 1) {
            builder1.withVirtualPID(ofType(Int::class))
            builder2.withVirtualPID(ofType(Int::class))
        }
        assertTrue(process1.vPID > 0)
        assertTrue(process2.vPID > 0)
        assertTrue(process1.vPID != process2.vPID)
    }

    @Test
    fun `should start process`() {
        // given
        val processMock = mockk<Process> {
            every { start() } returns mockk()
            every { vPID } returns VIRTUAL_PID
        }

        // when
        commander.startProcess(processMock)

        // then
        verify (exactly = 1) { processMock.start() }

    }

    @Test
    fun `should start process by vPID`() {
        // given
        val processMock = mockk<SystemProcess> {
            every { start() } returns mockk()
            every { vPID } returns VIRTUAL_PID
        }

        val builderSpy = spyk<ProcessBuilder> {
            every { build() } returns processMock
        }
        commander.process(builderSpy)

        // when
        commander.startProcess(VIRTUAL_PID)

        // then
        verify (exactly = 1) { processMock.start() }

    }

    @Test
    fun `should await process`() = runBlocking {
        // given
        val c = ProcessCommander(this)
        val timeout: Long = 500
        val processMock = mockk<Process> {
            coEvery { await(timeout) } just runs
            every { vPID } returns VIRTUAL_PID
            every { name } returns PROCESS_NAME
        }

        val builderSpy = spyk<ProcessBuilder> {
            every { build() } returns processMock
        }
        c.process(builderSpy)

        // when
        c.awaitProcess(processMock, timeout)

        // then
        coVerify (exactly = 1) { processMock.await(timeout) }
    }

    @Test
    fun `should await process by vPID`() = runBlocking {
        // given
        val c = ProcessCommander(this)
        val timeout: Long = 500
        val processMock = mockk<Process> {
            coEvery { await(timeout) } just runs
            every { vPID } returns VIRTUAL_PID
            every { name } returns PROCESS_NAME
        }

        val builderSpy = spyk<ProcessBuilder> {
            every { build() } returns processMock
        }
        c.process(builderSpy)

        // when
        c.awaitProcess(VIRTUAL_PID, timeout)

        // then
        coVerify (exactly = 1) { processMock.await(timeout) }
    }

    @Test
    fun `should throw exception when await unknown process`() = runBlocking {
        // given
        val c = ProcessCommander(this)
        val timeout: Long = 500
        val processMock = mockk<Process> {
            coEvery { await(timeout) } just runs
            every { vPID } returns VIRTUAL_PID
        }

        // when
        val result = runCatching { c.awaitProcess(processMock, timeout) }

        // then
        coVerify (exactly = 0) { processMock.await(timeout) }
        assertTrue(result.isFailure)
    }

    @Test
    fun `should throw exception when await unknown process by vPID`() = runBlocking {
        // given
        val c = ProcessCommander(this)
        val timeout: Long = 500
        val processMock = mockk<Process> {
            coEvery { await(timeout) } just runs
            every { vPID } returns VIRTUAL_PID
        }

        // when
        val result = runCatching { c.awaitProcess(processMock, timeout) }

        // then
        coVerify (exactly = 0) { processMock.await(timeout) }
        assertTrue(result.isFailure)
    }

    @Test
    fun `should await all processes`() = runBlocking {
        // given
        val c = ProcessCommander(this)
        val processMock1 = mockk<Process> {
            coEvery { await() } just runs
            every { name } returns PROCESS_NAME
        }
        val processMock2 = mockk<Process> {
            coEvery { await() } just runs
            every { name } returns PROCESS_NAME
        }

        val builder1 = spyk<ProcessBuilder> {
            every { build() } returns processMock1
        }
        val builder2 = spyk<ProcessBuilder> {
            every { build() } returns processMock2
        }

        c.process(builder1)
        c.process(builder2)

        // when
        c.awaitAll()

        // then
        coVerify (exactly = 1) {
            processMock1.await()
            processMock2.await()
        }
    }

    @Test
    fun `should kill process`() = runBlocking {
        // given
        val c = ProcessCommander(this)
        val processMock = mockk<Process> {
            every { kill() } just runs
        }

        val builderSpy = spyk<ProcessBuilder> {
            every { build() } returns processMock
        }
        c.process(builderSpy)

        // when
        c.killProcess(processMock)

        // then
        verify (exactly = 1) { processMock.kill() }
    }

    @Test
    fun `should kill process by vPID`() = runBlocking {
        // given
        val c = ProcessCommander(this)
        val processMock = mockk<Process> {
            every { kill() } just runs
            every { vPID } returns VIRTUAL_PID
        }

        val builderSpy = spyk<ProcessBuilder> {
            every { build() } returns processMock
        }
        c.process(builderSpy)

        // when
        c.killProcess(VIRTUAL_PID)

        // then
        verify (exactly = 1) { processMock.kill() }
    }

    @Test
    fun `should throw exception when kill unknown process`() = runBlocking {
        // given
        val c = ProcessCommander(this)
        val processMock = mockk<Process> {
            every { kill() } just runs
        }

        // when
        assertThrows<Exception> { c.killProcess(processMock) }

        // then
        verify (exactly = 0) { processMock.kill() }
    }

    @Test
    fun `should throw exception when kill unknown process by vPID`() = runBlocking {
        // given
        val c = ProcessCommander(this)
        val processMock = mockk<Process> {
            every { kill() } just runs
            every { vPID } returns VIRTUAL_PID
        }

        // when
        assertThrows<Exception> { c.killProcess(VIRTUAL_PID) }

        // then
        verify (exactly = 0) { processMock.kill() }
    }

    @Test
    fun `should kill all processes`() = runBlocking {
        // given
        val c = ProcessCommander(this)
        val processMock1 = mockk<Process> {
            every { kill() } just runs
        }
        val processMock2 = mockk<Process> {
            every { kill() } just runs
        }

        val builder1 = spyk<ProcessBuilder> {
            every { build() } returns processMock1
        }
        val builder2 = spyk<ProcessBuilder> {
            every { build() } returns processMock2
        }

        c.process(builder1)
        c.process(builder2)

        // when
        c.killAll()

        // then
        verify (exactly = 1) {
            processMock1.kill()
            processMock2.kill()
        }
    }
}
