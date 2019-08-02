package eu.jrie.jetbrains.kotlinshellextension.shell

import eu.jrie.jetbrains.kotlinshellextension.processes.ProcessCommander
import eu.jrie.jetbrains.kotlinshellextension.processes.process.ProcessReceiveChannel
import eu.jrie.jetbrains.kotlinshellextension.processes.process.ProcessSendChannel
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
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

    @Test
    fun `should retrieve all system environment variables`() {
        // expect
        assertEquals(System.getenv(), shell.systemEnv)
    }

    @Test
    fun `should retrieve all environment variables`() {
        // expect
        assertEquals(System.getenv(), shell.shellEnv)
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

        override val stdout: ProcessSendChannel = Channel()
        override val stderr: ProcessSendChannel = Channel()
        override val stdin: ProcessReceiveChannel = Channel()

        override suspend fun finalize() {}

        override fun exec(block: Shell.() -> String): ShellExecutable = mockk()

        override val SYSTEM_PROCESS_INPUT_STREAM_BUFFER_SIZE: Int = 1
        override val PIPELINE_RW_PACKET_SIZE: Long = 1
        override val PIPELINE_CHANNEL_BUFFER_SIZE: Int = 1
    }
}
