package eu.jrie.jetbrains.kotlinshellextension.processes.process.system

import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Test

class SystemProcessListenerTest {

    private val processMock = mockk<SystemProcess> {
        every { name } returns "process"
    }

    private val listener = SystemProcessListener(processMock)

    @Test
    fun `should close outputs of the process after stop`() {
        // given
        every { processMock.closeOut() } just runs

        // when
        listener.afterStop(mockk())

        // then
        verify { processMock.closeOut() }
    }
}
