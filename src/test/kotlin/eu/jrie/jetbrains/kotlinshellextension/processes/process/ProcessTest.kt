package eu.jrie.jetbrains.kotlinshellextension.processes.process;

import org.junit.Test
import org.mockito.Mockito.*

class ProcessTest {

    private val process = spy(SampleProcess())

    private companion object {
        const val COMMAND = "cmd"
        const val VIRTUAL_PID = 1
    }

    @Test
    fun `should redirect outputs to correct destinations`() {
        // given
        val stdout = mock(ProcessOutputStream::class.java)
        val stderr = mock(ProcessOutputStream::class.java)

        // when
        process.redirectOut(stdout, stderr)

        // then
        verify(process, times(1)).redirectOut(stdout, stderr)
        verify(process, times(1)).redirectStdOut(stdout)
        verify(process, times(1)).redirectStdErr(stderr)
        verifyNoMoreInteractions(process)
    }

    @Test
    fun `should perform given action when process is alive`() {
        // given
        val action = { process.kill() }

        `when`(process.isAlive()).thenReturn(true)

        // when
        process.ifAlive(action)

        // then
        verify(process, times(1)).kill()

    }

    @Test
    fun `should not perform given action when process is not alive`() {
        // given
        val action = { process.kill() }

        `when`(process.isAlive()).thenReturn(false)

        // when
        process.ifAlive(action)

        // then
        verify(process, never()).kill()

    }

    private open class SampleProcess : Process(
        VIRTUAL_PID,
        COMMAND
    ) {
        override fun redirectIn(source: ProcessInputStream): Process = mock(
            Process::class.java)

        override fun redirectOut(destination: ProcessOutputStream): Process = mock(
            Process::class.java)

        override fun redirectStdOut(destination: ProcessOutputStream): Process = mock(
            Process::class.java)

        override fun redirectStdErr(destination: ProcessOutputStream): Process = mock(
            Process::class.java)

        override fun setEnvironment(env: Map<String, String>): Process = mock(
            Process::class.java)

        override fun setEnvironment(env: Pair<String, String>): Process = mock(
            Process::class.java)

        override fun start(): PCB = mock(PCB::class.java)

        override fun isAlive(): Boolean = false

        override fun await(timeout: Long) {}

        override fun kill() {}
    }

}
