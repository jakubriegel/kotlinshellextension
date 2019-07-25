package eu.jrie.jetbrains.kotlinshellextension.shell

import eu.jrie.jetbrains.kotlinshellextension.processes.ProcessCommander
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.File

@ExperimentalCoroutinesApi
class ShellManagementTest {

    private val shell = SampleShell()

    @Test
    fun `should change the directory to user root`() {
        // given
        val userRoot = File(System.getProperty("user.home"))

        // when
        shell.cd()

        // then
        assertEquals(userRoot, shell.cdArg)
    }

    @Test
    fun `should change the directory to its parent`() {
        // given
        val userRoot = File(System.getProperty("user.home"))
        shell.directory = userRoot

        // when
        shell.cd(up)

        // then
        assertEquals(userRoot.parentFile, shell.cdArg)
    }

    @Test
    fun `should change the directory to given path`() {
        // given
        val path = "/some/path"

        // when
        shell.cd(path)

        // then
        assertEquals(File(path), shell.cdArg)
    }

    @Test
    fun `should change the directory to given one`() {
        // given
        val path = "/some/path"
        val file = File(path)

        // when
        shell.cd(file)

        // then
        assertEquals(file, shell.cdArg)
    }

    @Test
    fun `should retrieve system environment variable`() {
        // given
        val key = "HOME"

        // when
        val result = shell.env(key)

        // then
        assertEquals(System.getenv(key), result)
    }

    @ExperimentalCoroutinesApi
    private class SampleShell : ShellManagement {

        internal var cdArg: File? = null

        override fun cd(dir: File) {
            cdArg = dir
        }

        override fun variable(variable: Pair<String, String>) {}
        override fun export(env: Pair<String, String>) {}
        override fun unset(key: String) {}

        override var environment: Map<String, String> = emptyMap()
        override var variables: Map<String, String> = emptyMap()
        override var directory: File = File("")
        override val commander: ProcessCommander = mockk()
    }
}
