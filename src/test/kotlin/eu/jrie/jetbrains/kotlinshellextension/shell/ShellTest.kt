package eu.jrie.jetbrains.kotlinshellextension.shell

import eu.jrie.jetbrains.kotlinshellextension.testutils.TestDataFactory.ENV_VAR_1
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.File

@ExperimentalCoroutinesApi
class ShellTest {
    private val shell = Shell.build(null, null, mockk())


    @Test
    fun `should change the directory to given one`() {
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
    fun `should add new shell variable`() {
        // when
        shell.variable(ENV_VAR_1)

        // then
        assertEquals(ENV_VAR_1.second, shell.variables[ENV_VAR_1.first])
    }

    @Test
    fun `should add new environment variable`() {
        // when
        shell.export(ENV_VAR_1)

        // then
        assertEquals(ENV_VAR_1.second, shell.environment[ENV_VAR_1.first])
    }

    @Test
    fun `should unset shell variable`() {
        // when
        shell.variable(ENV_VAR_1)
        shell.unset(ENV_VAR_1.first)

        // then
        assertEquals(null, shell.variables[ENV_VAR_1.first])
    }

    @Test
    fun `should unset environment variable`() {
        // when
        shell.export(ENV_VAR_1)
        shell.unset(ENV_VAR_1.first)

        // then
        assertEquals(null, shell.environment[ENV_VAR_1.first])
    }

}
