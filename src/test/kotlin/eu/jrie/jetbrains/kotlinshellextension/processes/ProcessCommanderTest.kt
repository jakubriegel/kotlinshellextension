package eu.jrie.jetbrains.kotlinshellextension.processes

import eu.jrie.jetbrains.kotlinshellextension.processes.process.Process
import eu.jrie.jetbrains.kotlinshellextension.processes.process.ProcessBuilder
import eu.jrie.jetbrains.kotlinshellextension.processes.process.ProcessConfiguration
import eu.jrie.jetbrains.kotlinshellextension.testutils.TestDataFactory
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify
import kotlinx.coroutines.CoroutineScope
import org.junit.Assert.assertTrue
import org.junit.Test

class ProcessCommanderTest {
    private val commander = ProcessCommander(mockk())

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
        val vPID = 1
        val processMock = mockk<Process> {
            every { start() } returns mockk()
            every { virtualPID } returns vPID
        }

        mockkObject(ProcessBuilder)
        every {
            ProcessBuilder.createSystemProcess(
                ofType(ProcessConfiguration::class), ofType(Int::class), ofType(CoroutineScope::class)
            )
        } returns processMock

        commander.systemProcess(config)

        // when
        commander.startProcess(vPID)

        // then
        verify (exactly = 1) { processMock.start() }

    }

    @Test
    fun `should await process`() {
        // given
        val vPID = 1
        val timeout: Long = 500
        val processMock = mockk<Process> {
            every { await(timeout) } returns mockk()
            every { virtualPID } returns vPID
        }
        mockkObject(ProcessBuilder)
        every {
            ProcessBuilder.createSystemProcess(
                ofType(ProcessConfiguration::class), ofType(Int::class), ofType(CoroutineScope::class)
            )
        } returns processMock

        commander.systemProcess(config)

        // when
        commander.awaitProcess(vPID, timeout)

        // then
        verify (exactly = 1) { processMock.await(timeout) }

    }

    @Test
    fun `should kill process`() {
        // given
        val vPID = 1
        val processMock = mockk<Process> {
            every { kill() } just Runs
            every { virtualPID } returns vPID
        }

        mockkObject(ProcessBuilder)
        every {
            ProcessBuilder.createSystemProcess(
                ofType(ProcessConfiguration::class), ofType(Int::class), ofType(CoroutineScope::class)
            )
        } returns processMock

        commander.systemProcess(config)

        // when
        commander.killProcess(vPID)

        // then
        verify (exactly = 1) { processMock.kill() }

    }

}
