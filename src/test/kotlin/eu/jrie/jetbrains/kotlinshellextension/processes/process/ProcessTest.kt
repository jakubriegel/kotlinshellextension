package eu.jrie.jetbrains.kotlinshellextension.processes.process

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.junit.Test

class ProcessTest {

    private val process = spyk<SampleProcess>()

    private companion object {
        const val COMMAND = "cmd"
        const val VIRTUAL_PID = 1
    }

    @Test
    fun `should redirect outputs to correct destinations`() {
        // given
        val stdout = mockk<ProcessOutputStream>()
        val stderr = mockk<ProcessOutputStream>()

        // when
        process.redirectOut(stdout, stderr)

        // then

        verify (exactly = 1) { process.redirectOut(stdout, stderr) }
        verify (exactly = 1) { process.redirectStdOut(stdout) }
        verify (exactly = 1) { process.redirectStdErr(stderr) }
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

    private open class SampleProcess : Process(VIRTUAL_PID, COMMAND) {
        override fun redirectIn(source: ProcessInputStream): Process = mockk()

        override fun redirectOut(destination: ProcessOutputStream): Process = mockk()

        override fun redirectStdOut(destination: ProcessOutputStream): Process = mockk()

        override fun redirectStdErr(destination: ProcessOutputStream): Process = mockk()

        override fun setEnvironment(env: Map<String, String>): Process = mockk()

        override fun setEnvironment(env: Pair<String, String>): Process = mockk()

        override fun start(): PCB = mockk()

        override fun isAlive(): Boolean = false

        override fun await(timeout: Long) {}

        override fun kill() {}
    }

}
