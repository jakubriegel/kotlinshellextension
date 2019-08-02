package eu.jrie.jetbrains.kotlinshellextension.shell

import eu.jrie.jetbrains.kotlinshellextension.processes.ProcessCommander
import eu.jrie.jetbrains.kotlinshellextension.testutils.TestDataFactory.ENV_VAR_1
import io.mockk.every
import io.mockk.spyk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.File

@ExperimentalCoroutinesApi
class ShellTest {
    private lateinit var shell: Shell

    @Test
    fun `should change the directory to given one`() = runTest {
        // given
        val dir = spyk(File("/some/path")) {
            every { isDirectory } returns true
        }

        // when
        shell.cd(dir)

        // then
        assertEquals(dir, shell.directory)
    }

    @Test
    fun `should add new shell variable`() = runTest {
        // when
        shell.variable(ENV_VAR_1)

        // then
        assertEquals(ENV_VAR_1.second, shell.variables[ENV_VAR_1.first])
    }

    @Test
    fun `should add new environment variable`() = runTest {
        // when
        shell.export(ENV_VAR_1)

        // then
        assertEquals(ENV_VAR_1.second, shell.environment[ENV_VAR_1.first])
    }

    @Test
    fun `should unset shell variable`() = runTest {
        // when
        shell.variable(ENV_VAR_1)
        shell.unset(ENV_VAR_1.first)

        // then
        assertEquals(null, shell.variables[ENV_VAR_1.first])
    }

    @Test
    fun `should unset environment variable`() = runTest {
        // when
        shell.export(ENV_VAR_1)
        shell.unset(ENV_VAR_1.first)

        // then
        assertEquals(null, shell.environment[ENV_VAR_1.first])
    }

    private fun <T> runTest(test: suspend ShellTest.() -> T) = runBlocking {
        shell = Shell.build(null, null, spyk(ProcessCommander(this)), 1, 1, 1)
        val result = test()
        shell.finalize()
        result
    }

}
