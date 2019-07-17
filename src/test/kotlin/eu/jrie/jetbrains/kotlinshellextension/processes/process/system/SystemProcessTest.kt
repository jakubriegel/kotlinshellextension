package eu.jrie.jetbrains.kotlinshellextension.processes.process.system

import eu.jrie.jetbrains.kotlinshellextension.testutils.TestDataFactory.ENVIRONMENT
import eu.jrie.jetbrains.kotlinshellextension.testutils.TestDataFactory.PROCESS_ARGS
import eu.jrie.jetbrains.kotlinshellextension.testutils.TestDataFactory.PROCESS_COMMAND
import eu.jrie.jetbrains.kotlinshellextension.testutils.TestDataFactory.SYSTEM_PID
import eu.jrie.jetbrains.kotlinshellextension.testutils.TestDataFactory.VIRTUAL_PID
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.zeroturnaround.exec.ProcessExecutor
import org.zeroturnaround.exec.ProcessResult
import org.zeroturnaround.exec.StartedProcess
import java.io.File
import java.util.*
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

class SystemProcessTest {
    private val executorMock = spyk<ProcessExecutor>()
    private val input = Channel<Byte>()
    private val directory = File("")

    private val process = SystemProcess(
        VIRTUAL_PID,
        PROCESS_COMMAND,
        PROCESS_ARGS,
        input,
        ENVIRONMENT,
        directory,
        spyk(),
        executorMock
    )

    @Test
    fun `should initialize the executor`() {
        verify (exactly = 1) {
            executorMock.command(listOf(PROCESS_COMMAND).plus(PROCESS_ARGS))
            executorMock.destroyOnExit()
            executorMock.addListener(ofType(SystemProcess.SystemProcessListener::class))
            executorMock.redirectInput(ofType(SystemProcess.SystemProcessInputStream::class))
            executorMock.redirectOutput(ofType(SystemProcess.SystemProcessLogOutputStream::class))
            executorMock.redirectError(ofType(SystemProcess.SystemProcessLogOutputStream::class))
            executorMock.environment(ENVIRONMENT)
            executorMock.directory(directory)
        }
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
        val p = processWithGivenScope(this)
        val futureMock = mockk<Future<ProcessResult>> {
            every { get() } returns mockk {
                every { exitValue } returns 0
            }
        }

        p.pcb.startedProcess = mockk {
            every { process.isAlive } returns true
            every { future } returns futureMock
        }

        // when
        p.await()

        // then
        verify (exactly = 1) { futureMock.get() }
    }

    @Test
    @ObsoleteCoroutinesApi
    fun `should await process with given timeout`() = runBlocking {
        // given
        val p = processWithGivenScope(this)
        val timeout: Long = 500

        val futureMock = mockk<Future<ProcessResult>> {
            every { get(timeout, TimeUnit.MILLISECONDS) } returns mockk {
                every { exitValue } returns 0
            }
        }

        p.pcb.startedProcess = mockk {
            every { process.isAlive } returns true
            every { future } returns futureMock
        }

        // when
        p.await(timeout)

        // then
        verify (exactly = 1) { futureMock.get(timeout, TimeUnit.MILLISECONDS) }
    }


    @Test
    @ObsoleteCoroutinesApi
    fun `should not await not alive process`() = runBlocking {
        // given
        val p = processWithGivenScope(this)
        val futureMock = mockk<Future<ProcessResult>> {
            every { get() } returns mockk()
        }

        p.pcb.startedProcess = mockk {
            every { process.isAlive } returns false
            every { future } returns futureMock
        }

        // when
        p.await()

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
        process.kill()

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
        assertThrows<Exception> { process.kill() }

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
        process.kill()

        // then
        verify { processMock.isAlive }
        verify (exactly = 0) { futureMock.cancel(true) }
        verify (exactly = 0 ){
            processMock.destroy()
            processMock.destroyForcibly()
        }
        confirmVerified(futureMock)
    }

    private fun processWithGivenScope(scope: CoroutineScope) = SystemProcess(
            VIRTUAL_PID,
            PROCESS_COMMAND,
            PROCESS_ARGS,
            input,
            ENVIRONMENT,
            directory,
            scope,
            spyk()
        )
}
