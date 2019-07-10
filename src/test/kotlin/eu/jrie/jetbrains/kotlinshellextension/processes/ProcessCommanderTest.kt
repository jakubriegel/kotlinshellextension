package eu.jrie.jetbrains.kotlinshellextension.processes

import eu.jrie.jetbrains.kotlinshellextension.processes.process.Process
import eu.jrie.jetbrains.kotlinshellextension.processes.process.ProcessBuilder
import eu.jrie.jetbrains.kotlinshellextension.processes.process.ProcessConfiguration
import eu.jrie.jetbrains.kotlinshellextension.testutils.TestDataFactory
import eu.jrie.jetbrains.kotlinshellextension.testutils.TestDataFactory.VIRTUAL_PID
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ProcessCommanderTest {
    private val commander = ProcessCommander(spyk())

    private val config = TestDataFactory.processConfigFunction()

    @Test
    fun `should create new SystemProcess`() {
        // given
        mockkObject(ProcessBuilder)

        // when
        val vPID = commander.systemProcess(config)

        // then
        verify (exactly = 1) {
            ProcessBuilder.createSystemProcess(ofType(ProcessConfiguration::class), vPID, ofType(CoroutineScope::class))
        }
        assertTrue(vPID > 0)

    }

    @Test
    fun `should assign unique vPID to process`() {
        // given
        mockkObject(ProcessBuilder)

        // when
        val vPID1 = commander.systemProcess(config)
        val vPID2 = commander.systemProcess(config)

        // then
        verify (exactly = 1) {
            ProcessBuilder.createSystemProcess(ofType(ProcessConfiguration::class), vPID1, ofType(CoroutineScope::class))
            ProcessBuilder.createSystemProcess(ofType(ProcessConfiguration::class), vPID2, ofType(CoroutineScope::class))
        }
        assertTrue(vPID1 > 0)
        assertTrue(vPID2 > 0)
        assertTrue(vPID1 != vPID2)
    }

    @Test
    fun `should start process`() {
        // given
        val processMock = mockk<Process> {
            every { start() } returns mockk()
            every { vPID } returns VIRTUAL_PID
        }

        mockkObject(ProcessBuilder)
        every {
            ProcessBuilder.createSystemProcess(
                ofType(ProcessConfiguration::class), ofType(Int::class), ofType(CoroutineScope::class)
            )
        } returns processMock

        commander.systemProcess(config)

        // when
        commander.startProcess(VIRTUAL_PID)

        // then
        verify (exactly = 1) { processMock.start() }

    }

    @Test
    fun `should await process by vPID`() = runBlocking {
        // given
        val c = ProcessCommander(this)
        val timeout: Long = 500
        val processMock = mockk<Process> {
            every { await(timeout) } returns spyk {
                coEvery { join() } just Runs
            }
            every { vPID } returns VIRTUAL_PID
        }
        mockkObject(ProcessBuilder)
        every {
            ProcessBuilder.createSystemProcess(
                ofType(ProcessConfiguration::class), ofType(Int::class), ofType(CoroutineScope::class)
            )
        } returns processMock

        c.systemProcess(config)

        // when
        c.awaitProcess(VIRTUAL_PID, timeout)

        // then
        verify (exactly = 1) { processMock.await(timeout) }
    }

    @Test
    fun `should await process`() = runBlocking {
        // given
        val c = ProcessCommander(this)
        val timeout: Long = 500
        val processMock = mockk<Process> {
            every { await(timeout) } returns spyk {
                coEvery { join() } just Runs
            }
            every { vPID } returns VIRTUAL_PID
        }
        mockkObject(ProcessBuilder)
        every {
            ProcessBuilder.createSystemProcess(
                ofType(ProcessConfiguration::class), ofType(Int::class), ofType(CoroutineScope::class)
            )
        } returns processMock

        c.systemProcess(config)

        // when
        c.awaitProcess(processMock, timeout)

        // then
        verify (exactly = 1) { processMock.await(timeout) }
    }

    @Test
    fun `should await all processes`() = runBlocking {
        // given
        val c = ProcessCommander(this)
        val processMock = mockk<Process> {
            every { await() } returns spyk {
                coEvery { join() } just Runs
            }
            every { vPID } returns VIRTUAL_PID
        }
        mockkObject(ProcessBuilder)
        every {
            ProcessBuilder.createSystemProcess(
                ofType(ProcessConfiguration::class), ofType(Int::class), ofType(CoroutineScope::class)
            )
        } returns processMock

        c.systemProcess(config)

        // when
        c.awaitAll()

        // then
        verify (exactly = 1) { processMock.await() }
    }

    @Test
    fun `should kill process`() {
        // given
        val processMock = mockk<Process> {
            every { kill() } just Runs
            every { vPID } returns VIRTUAL_PID
        }

        mockkObject(ProcessBuilder)
        every {
            ProcessBuilder.createSystemProcess(
                ofType(ProcessConfiguration::class), ofType(Int::class), ofType(CoroutineScope::class)
            )
        } returns processMock

        commander.systemProcess(config)

        // when
        commander.killProcess(VIRTUAL_PID)

        // then
        verify (exactly = 1) { processMock.kill() }

    }

}
