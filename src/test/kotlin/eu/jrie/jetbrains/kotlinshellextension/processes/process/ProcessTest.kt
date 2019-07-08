package eu.jrie.jetbrains.kotlinshellextension.processes.process

import io.mockk.Runs
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.Job
import org.junit.Assert.assertTrue
import org.junit.Test

class ProcessTest {

    private val process = spyk<SampleProcess>()

    private companion object {
        const val COMMAND = "cmd"
        const val VIRTUAL_PID = 1
    }

    @Test
    fun `should redirect merged output to stdout`() {
        // when
        process.followMergedOut()

        // then
        verify (exactly = 1) {
            process.followMergedOut()
            process.followMergedOut(ofType(ProcessOutputStream::class))
        }
        confirmVerified(process)
    }

    @Test
    fun `should redirect merged output to given stdout`() {
        // given
        val outMock = mockk<ProcessOutputStream>()

        // when
        process.followMergedOut(outMock)

        // then
        verify (exactly = 1) { process.followMergedOut(outMock) }
        confirmVerified(process)
        assertTrue(process.stdout == outMock)
    }

    @Test
    fun `should redirect outputs to correct destinations`() {
        // when
        process.followOut()

        // then
        verify (exactly = 1) { process.followOut() }
        verify (exactly = 1) { process.followStdOut() }
        verify (exactly = 1) { process.followStdErr() }
        verify (exactly = 1) { process.followStdOut(ofType(ProcessOutputStream::class)) }
        verify (exactly = 1) { process.followStdErr(ofType(ProcessOutputStream::class)) }
        confirmVerified(process)
    }

    @Test
    fun `should redirect outputs to given destinations`() {
        // given
        val stdMock = mockk<ProcessOutputStream>()
        val errMock = mockk<ProcessOutputStream>()

        // when
        process.followOut(stdMock, errMock)

        // then
        verify (exactly = 1) { process.followOut(stdMock, errMock) }
        verify (exactly = 1) { process.followStdOut(stdMock) }
        verify (exactly = 1) { process.followStdErr(errMock) }
        confirmVerified(process)
        assertTrue(process.stdout == stdMock)
        assertTrue(process.stderr == errMock)
    }

    @Test
    fun `should close stdout and stderr`() {
        // given
        val stdMock = mockk<ProcessOutputStream> {
            every { close() } just Runs
        }
        val errMock = mockk<ProcessOutputStream> {
            every { close() } just Runs
        }

        process.followOut(stdMock, errMock)

        // when
        process.closeOut()

        // then
        verify (exactly = 1) { stdMock.close() }
        verify (exactly = 1) { errMock.close() }
        confirmVerified(stdMock, errMock)
    }

    @Test
    fun `should perform given action when process is alive`() {
        // given
        val action = { process.kill() }

        every { process.isAlive() } returns true

        // when
        process.ifAlive(action)

        // then
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
        verify (exactly = 0) { process.kill() }

    }

    private open class SampleProcess : Process(VIRTUAL_PID, COMMAND, scope = mockk()) {

        override val pcb: PCB = mockk()

        override fun redirectIn(source: ProcessInputStream): Process = mockk()

        override fun redirectMergedOut(destination: ProcessOutputStream) {}

        override fun redirectStdOut(destination: ProcessOutputStream) {}

        override fun redirectStdErr(destination: ProcessOutputStream) {}

        override fun setEnvironment(env: Map<String, String>): Process = mockk()

        override fun setEnvironment(env: Pair<String, String>): Process = mockk()

        override fun start(): PCB = mockk()

        override fun isAlive(): Boolean = false

        override fun await(timeout: Long): Job = mockk()

        override fun kill() {}
    }

}
