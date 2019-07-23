package eu.jrie.jetbrains.kotlinshellextension.shell

import eu.jrie.jetbrains.kotlinshellextension.nullout
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.PrintStream

@ExperimentalCoroutinesApi
class PSIntegrationTest : ProcessBaseIntegrationTest() {

    @Test
    fun `should show processes data`() {
        // given
        val n = 10
        val scriptCode = scriptFile(n)
        val outFile = file("console")
        System.setOut(PrintStream(outFile))

        val psHeaderRegex = Regex("PID\\s+TIME\\s+CMD\\s*")
        val psProcessRegex = Regex("[\\d]+\\s+\\d\\d:\\d\\d:\\d\\d\\s(.+/)*[^/]+\\s(\\w=\\w)*")

        // when
        shell {
            systemProcess { cmd = "ls" } pipe nullout
            val script = systemProcess {
                cmd = "./${scriptCode.name}"
            }

            (script forkErr nullout) pipe nullout

            ps()
            commander.awaitAll()
        }

        // then
        val result = outFile.withoutLogs().lines()

        assertRegex(psHeaderRegex, result.first())
        result
            .subList(1, result.lastIndex-1)
            .forEachIndexed { i, it ->
                if (i != 0) assertRegex(psProcessRegex, it)
            }
        assertEquals("", result.last())
    }
}