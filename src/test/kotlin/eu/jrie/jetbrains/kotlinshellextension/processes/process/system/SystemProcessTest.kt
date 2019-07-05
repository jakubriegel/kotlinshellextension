package eu.jrie.jetbrains.kotlinshellextension.processes.process.system

import eu.jrie.jetbrains.kotlinshellextension.processes.process.ProcessInputStream
import eu.jrie.jetbrains.kotlinshellextension.processes.process.ProcessOutputStream
import eu.jrie.jetbrains.kotlinshellextension.testutils.TestDataFactory
import eu.jrie.jetbrains.kotlinshellextension.testutils.TestDataFactory.PROCESS_ARGS
import eu.jrie.jetbrains.kotlinshellextension.testutils.TestDataFactory.PROCESS_COMMAND
import eu.jrie.jetbrains.kotlinshellextension.testutils.TestDataFactory.SYSTEM_PID
import eu.jrie.jetbrains.kotlinshellextension.testutils.TestDataFactory.VIRTUAL_PID
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.apache.commons.io.output.NullOutputStream
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.zeroturnaround.exec.ProcessExecutor
import org.zeroturnaround.exec.ProcessResult
import org.zeroturnaround.exec.StartedProcess
import org.zeroturnaround.exec.listener.DestroyerListenerAdapter
import org.zeroturnaround.exec.listener.ShutdownHookProcessDestroyer
import java.io.PipedInputStream
import java.util.*
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

class SystemProcessTest {
    private val executorMock = spyk<ProcessExecutor>()
    private val process = SystemProcess(VIRTUAL_PID, PROCESS_COMMAND, PROCESS_ARGS, executorMock)

    @Before
    fun before() {

    }

    @After
    fun after() {

    }

    @Test
    fun `should initialize the executor`() {
        verify (exactly = 1) {

            // custom calls
            executorMock.command(listOf(PROCESS_COMMAND).plus(PROCESS_ARGS))
            executorMock.destroyOnExit()
            executorMock.addListener(ofType(SystemProcessListener::class))

            // default calls
            executorMock.destroyer(ofType(ShutdownHookProcessDestroyer::class))
            executorMock.removeListeners(any())
            executorMock.addListener(ofType(DestroyerListenerAdapter::class))
        }
        confirmVerified(executorMock)
    }

    @Test
    fun `should redirect input`() {
        // given
        val tapMock = mockk<PipedInputStream>()
        val inputMock = mockk<ProcessInputStream> {
            every { tap } returns tapMock
        }

        // when
        process.redirectIn(inputMock)

        // then
        verify (exactly = 1) { executorMock.redirectInput(any()) }
    }

    @Test
    fun `should redirect merged output`() {
        // given
        val outputMock = mockk<ProcessOutputStream>()

        // when
        process.followMergedOut(outputMock)

        // then
        verify (exactly = 1) { executorMock.redirectOutput(outputMock) }
        verify (exactly = 0) { executorMock.redirectError(any()) }
    }

    @Test
    fun `should redirect only stdout`() {
        // given
        val outputMock = mockk<ProcessOutputStream>()

        // when
        process.followStdOut(outputMock)

        // then
        verify (exactly = 1) {
            executorMock.redirectOutput(outputMock)
            executorMock.redirectError(ofType(NullOutputStream::class))
        }
    }

    @Test
    fun `should redirect only stderr`() {
        // given
        val outputMock = mockk<ProcessOutputStream>()

        // when
        process.followStdOut(outputMock)

        // then
        verify (exactly = 0) { executorMock.redirectError(outputMock) }
        verify (exactly = 1) { executorMock.redirectOutput(any()) }
    }

    @Test
    fun `should redirect stdout adn stderr to separated streams`() {
        // given
        val stdOutputMock = mockk<ProcessOutputStream>()
        val errOutputMock = mockk<ProcessOutputStream>()

        // when
        process.followOut(stdOutputMock, errOutputMock)

        // then
        verify (exactly = 1) { executorMock.redirectOutput(stdOutputMock) }
        verify (exactly = 1) { executorMock.redirectError(errOutputMock) }
    }

    @Test
    fun `should set environment variables`() {
        // given
        val env = TestDataFactory.ENVIRONMENT

        // when
        process.setEnvironment(env)

        // then
        env.forEach { (k, v) -> verify (exactly = 1) { executorMock.environment(k, v) } }
    }

    @Test
    fun `should set single environment variable`() {
        // given
        val env = TestDataFactory.ENV_VAR_1

        // when
        process.setEnvironment(env)

        // then
        verify (exactly = 1) { executorMock.environment(env.first, env.second) }
    }

    @Test
    fun `should start process`() {
        // given
        val startedProcessMock = mockk<StartedProcess> {
            every { process } returns mockk {
                every { info().startInstant() } returns Optional.empty()
                every { pid() } returns SYSTEM_PID
            }
        }
        every { executorMock.start() } returns startedProcessMock

        // when
        process.start()

        // then
        verify (exactly = 1) { executorMock.start() }
    }

    @Test
    fun `should return true if process if alive`() {
        // given
        process.pcb.startedProcess = mockk {
            every { process.isAlive } returns true
        }

        // when
        val result = process.isAlive()

        // then
        assertTrue(result)
    }

    @Test
    fun `should return false if process is not alive`() {
        // given
        process.pcb.startedProcess = mockk {
            every { process.isAlive } returns false
        }

        // when
        val result = process.isAlive()

        // then
        assertFalse(result)
    }

    @Test
    fun `should return false if process has not started yet`() {
        // given
        process.pcb.startedProcess = null

        // when
        val result = process.isAlive()

        // then
        assertFalse(result)
    }

    @Test
    fun `should blocking await process`() {
        // given
        val futureMock = mockk<Future<ProcessResult>> {
            every { get() } returns mockk()
        }

        process.pcb.startedProcess = mockk {
            every { process.isAlive } returns true
            every { future } returns futureMock
        }

        // when
        process.await()

        // then
        verify (exactly = 1) { futureMock.get() }
        confirmVerified(futureMock)
    }

    @Test
    fun `should await process with given timeout`() {
        // given
        val timeout: Long = 500

        val futureMock = mockk<Future<ProcessResult>> {
            every { get(timeout, TimeUnit.MILLISECONDS) } returns mockk()
        }

        process.pcb.startedProcess = mockk {
            every { process.isAlive } returns true
            every { future } returns futureMock
        }

        // when
        process.await(timeout)

        // then
        verify (exactly = 1) { futureMock.get(timeout, TimeUnit.MILLISECONDS) }
        confirmVerified(futureMock)
    }

    @Test
    fun `should not await not alive process`() {
        // given
        val futureMock = mockk<Future<ProcessResult>> {
            every { get() } returns mockk()
        }

        process.pcb.startedProcess = mockk {
            every { process.isAlive } returns false
            every { future } returns futureMock
        }

        // when
        process.await()

        // then
        verify (exactly = 0) { futureMock.get() }
        verify (exactly = 0) { futureMock.get(ofType(Long::class), ofType(TimeUnit::class)) }
        confirmVerified(futureMock)
    }

    @Test
    @Suppress("RemoveRedundantQualifierName")
    fun `should kill alive process`() {
        // given
        val processMock = mockk<java.lang.Process> {
            every { isAlive } returns true
            every { destroy() } returns mockk()
            every { destroyForcibly() } returns mockk()
        }

        val startedProcessMock = spyk(StartedProcess(processMock, mockk()))
        process.pcb.startedProcess = startedProcessMock

        // when
        process.kill()

        // then
        verify { processMock.isAlive }
        verify (exactly = 1) { processMock.destroy() }
        verify (atMost = 1) { processMock.destroyForcibly() }
        confirmVerified(processMock)
    }

}
