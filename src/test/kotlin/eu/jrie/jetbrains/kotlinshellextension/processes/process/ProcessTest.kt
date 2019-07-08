package eu.jrie.jetbrains.kotlinshellextension.processes.process

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.Job
import org.junit.Test

class ProcessTest {

    private val process = spyk<SampleProcess>()

    private companion object {
        const val COMMAND = "cmd"
        const val VIRTUAL_PID = 1
    }

    @Test
    fun `should redirect outputs to correct destinations`() {
        // when
        process.followOut()

        // then

        verify (exactly = 1) { process.followOut() }
        verify (exactly = 1) { process.followStdOut() }
        verify (exactly = 1) { process.followStdErr() }
        confirmVerified(process)
    }

    @Test
    fun `should redirect outputs to given destinations`() {
        // given
        val stdout = mockk<ProcessOutputStream>()
        val stderr = mockk<ProcessOutputStream>()

        // when
        process.followOut(stdout, stderr)

        // then

        verify (exactly = 1) { process.followOut(stdout, stderr) }
        verify (exactly = 1) { process.followStdOut(stdout) }
        verify (exactly = 1) { process.followStdErr(stderr) }
        confirmVerified(process)
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
