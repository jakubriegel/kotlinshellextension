package eu.jrie.jetbrains.kotlinshellextension.processes.process.system

import eu.jrie.jetbrains.kotlinshellextension.processes.process.stream.ProcessInputStream
import eu.jrie.jetbrains.kotlinshellextension.processes.process.stream.ProcessOutputStream
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
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.apache.commons.io.output.NullOutputStream
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.zeroturnaround.exec.ProcessExecutor
import org.zeroturnaround.exec.ProcessResult
import org.zeroturnaround.exec.StartedProcess
import org.zeroturnaround.exec.listener.DestroyerListenerAdapter
import org.zeroturnaround.exec.listener.ShutdownHookProcessDestroyer
import java.util.*
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

class SystemProcessTest {
    private val executorMock = spyk<ProcessExecutor>()
    private val process = SystemProcess(VIRTUAL_PID, PROCESS_COMMAND, PROCESS_ARGS, mockk(), executorMock)

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
        val inputSpy = spyk(ProcessInputStream(mockk()))

        // when
        process.followIn(inputSpy)

        // then
        verify (exactly = 1) { executorMock.redirectInput(ofType(SystemProcess.SystemProcessInputStream::class)) }

        assertEquals(inputSpy.vPID, VIRTUAL_PID)
    }

    @Test
    fun `should redirect merged output`() {
        // given
        val outputSpy = spyk(ProcessOutputStream(mockk()))

        // when
        process.followMergedOut(outputSpy)

        // then
        verify (exactly = 1) { executorMock.redirectOutput(ofType(SystemProcess.SystemProcessLogOutputStream::class)) }
        verify (exactly = 0) { executorMock.redirectError(any()) }

        assertEquals(outputSpy.vPID, VIRTUAL_PID)
    }

    @Test
    fun `should redirect only stdout`() {
        // given
        val outputSpy = spyk(ProcessOutputStream(mockk()))

        // when
        process.followStdOut(outputSpy)

        // then
        verify (exactly = 0) { executorMock.redirectError(ofType(SystemProcess.SystemProcessLogOutputStream::class)) }
        verify (exactly = 1) {
            executorMock.redirectOutput(ofType(SystemProcess.SystemProcessLogOutputStream::class))
            executorMock.redirectError(ofType(NullOutputStream::class))
        }

        assertEquals(outputSpy.vPID, VIRTUAL_PID)
    }

    @Test
    fun `should redirect only stderr`() {
        // given
        val outputSpy = spyk(ProcessOutputStream(mockk()))

        // when
        process.followStdErr(outputSpy)

        // then
        verify (exactly = 1) { executorMock.redirectError(ofType(SystemProcess.SystemProcessLogOutputStream::class)) }
        verify (exactly = 0) { executorMock.redirectError(ofType(NullOutputStream::class)) }
        verify (exactly = 0) { executorMock.redirectOutput(ofType(SystemProcess.SystemProcessLogOutputStream::class)) }

        assertEquals(outputSpy.vPID, VIRTUAL_PID)
    }

    @Test
    fun `should redirect stdout and stderr to separated streams`() {
        // given
        val stdOutputSpy = spyk(ProcessOutputStream(mockk()))
        val errOutputSpy = spyk(ProcessOutputStream(mockk()))

        // when
        process.followOut(stdOutputSpy, errOutputSpy)

        // then
        verify (exactly = 1) { executorMock.redirectOutput(ofType(SystemProcess.SystemProcessLogOutputStream::class)) }
        verify (exactly = 1) { executorMock.redirectError(ofType(SystemProcess.SystemProcessLogOutputStream::class)) }

        assertEquals(stdOutputSpy.vPID, VIRTUAL_PID)
        assertEquals(errOutputSpy.vPID, VIRTUAL_PID)
        assertEquals(process.stdout, stdOutputSpy)
        assertEquals(process.stderr, errOutputSpy)
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
    @ObsoleteCoroutinesApi
    fun `should blocking await process`() = runBlocking {
        // given
        val p = SystemProcess(VIRTUAL_PID, PROCESS_COMMAND, PROCESS_ARGS, this, spyk())
        val futureMock = mockk<Future<ProcessResult>> {
            every { get() } returns mockk()
        }

        p.pcb.startedProcess = mockk {
            every { process.isAlive } returns true
            every { future } returns futureMock
        }

        // when
        p.await().join()

        // then
        verify (exactly = 1) { futureMock.get() }
    }

    @Test
    @ObsoleteCoroutinesApi
    fun `should await process with given timeout`() = runBlocking {
        // given
        val p = SystemProcess(VIRTUAL_PID, PROCESS_COMMAND, PROCESS_ARGS, this, spyk())
        val timeout: Long = 500

        val futureMock = mockk<Future<ProcessResult>> {
            every { get(timeout, TimeUnit.MILLISECONDS) } returns mockk()
        }

        p.pcb.startedProcess = mockk {
            every { process.isAlive } returns true
            every { future } returns futureMock
        }

        // when
        p.await(timeout).join()

        // then
        verify (exactly = 1) { futureMock.get(timeout, TimeUnit.MILLISECONDS) }
    }


    @Test
    @ObsoleteCoroutinesApi
    fun `should not await not alive process`() = runBlocking {
        // given
        val p = SystemProcess(VIRTUAL_PID, PROCESS_COMMAND, PROCESS_ARGS, this, spyk())
        val futureMock = mockk<Future<ProcessResult>> {
            every { get() } returns mockk()
        }

        p.pcb.startedProcess = mockk {
            every { process.isAlive } returns false
            every { future } returns futureMock
        }

        // when
        p.await().join()

        // then
        verify (exactly = 0) { futureMock.get() }
        verify (exactly = 0) { futureMock.get(ofType(Long::class), ofType(TimeUnit::class)) }
    }

    @Test
    @Suppress("RemoveRedundantQualifierName")
    fun `should kill alive process`() {
        // given
        val processMock = mockk<java.lang.Process> {
            every { isAlive } returns true
        }
        val futureMock = mockk<Future<ProcessResult>> {
            every { cancel(true) } returns true
        }

        val startedProcessMock = spyk(StartedProcess(processMock, futureMock))
        process.pcb.startedProcess = startedProcessMock

        // when
        process.destroy()

        // then
        verify { processMock.isAlive }
        verify (exactly = 1) { futureMock.cancel(true) }
        verify (exactly = 0 ){
            processMock.destroy()
            processMock.destroyForcibly()
        }
        confirmVerified(futureMock)
    }

    @Test
    @Suppress("RemoveRedundantQualifierName")
    fun `should throw exception when cannot kill process`() {
        // given
        val processMock = mockk<java.lang.Process> {
            every { isAlive } returns true
        }
        val futureMock = mockk<Future<ProcessResult>> {
            every { cancel(true) } returns false
        }

        val startedProcessMock = spyk(StartedProcess(processMock, futureMock))
        process.pcb.startedProcess = startedProcessMock

        // when
        assertThrows<Exception> { process.destroy() }

        // then
        verify { processMock.isAlive }
        verify (exactly = 1) { futureMock.cancel(true) }
        verify (exactly = 0 ){
            processMock.destroy()
            processMock.destroyForcibly()
        }
        confirmVerified(futureMock)
    }

    @Test
    @Suppress("RemoveRedundantQualifierName")
    fun `should not kill not alive process`() {
        // given
        val processMock = mockk<java.lang.Process> {
            every { isAlive } returns false
        }
        val futureMock = mockk<Future<ProcessResult>>()

        val startedProcessMock = spyk(StartedProcess(processMock, futureMock))
        process.pcb.startedProcess = startedProcessMock

        // when
        process.destroy()

        // then
        verify { processMock.isAlive }
        verify (exactly = 0) { futureMock.cancel(true) }
        verify (exactly = 0 ){
            processMock.destroy()
            processMock.destroyForcibly()
        }
        confirmVerified(futureMock)
    }

}
