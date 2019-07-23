package eu.jrie.jetbrains.kotlinshellextension.dsl

import eu.jrie.jetbrains.kotlinshellextension.BaseIntegrationTest
import eu.jrie.jetbrains.kotlinshellextension.processes.configuration.SystemProcessConfiguration
import eu.jrie.jetbrains.kotlinshellextension.processes.process.system.SystemProcessBuilder
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.jetbrains.annotations.TestOnly
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertIterableEquals
import org.junit.jupiter.api.Test

@ExperimentalCoroutinesApi
class SystemProcessDSLIntegrationTest : BaseIntegrationTest() {
    @Test
    fun `should create system process with given command`() {
        // when
        val process = create {
            cmd = command
        }

        // then
        assertEquals(process.command, command)
    }

    @Test
    fun `should create system process with given command and single argument`() {
        // when
        val process = create {
            cmd {
                command withArg argument
            }
        }

        // then
        assertEquals(process.command, command)
        assertIterableEquals(process.arguments, listOf(argument))
    }

    @Test
    fun `should create system process with given command and arguments`() {
        // when
        val process = create {
            cmd {
                command withArgs arguments
            }
        }

        // then
        assertEquals(process.command, command)
        assertIterableEquals(process.arguments, arguments)
    }

    @Test
    fun `should create system process with given enviroment variable`() {
        // when
        val process = create {
            env(env1)
        }

        // then
        assertEquals(process.environment().size, 1)
        assertEquals(process.environment()[env1.first], env1.second)
    }

    @Test
    fun `should create system process with given enviroment variables`() {
        // when
        val process = create {
            env {
                env1.first to env1.second
                env2.first to env2.second
                env3.first to env3.second
            }
        }

        // then
        val env = process.environment()
        assertEquals(env.size, 3)
        assertEquals(env[env1.first], env1.second)
        assertEquals(env[env2.first], env2.second)
        assertEquals(env[env3.first], env3.second)
    }

    @Test
    fun `should create system process with given environment`() {
        // when
        val process = create {
            env (this@SystemProcessDSLIntegrationTest.environment)
        }

        // then
        val env = process.environment()
        assertEquals(env.size, 3)
        assertEquals(env[env1.first], env1.second)
        assertEquals(env[env2.first], env2.second)
        assertEquals(env[env3.first], env3.second)
    }

    @Test
    fun `should create system process with given directory as String`() {
        // when
        val process = create {
            dir(directoryPath)
        }

        // then
        assertEquals(directoryPath, process.directory.path )
    }

    @Test
    fun `should create system process with given directory as File`() {
        // when
        val process = create {
        }

        // then
        assertEquals(directoryPath, process.directory.path)
    }

    @TestOnly
    private fun create(config: SystemProcessConfiguration.() -> Unit): SystemProcessBuilder {
        var processBuilder: SystemProcessBuilder? = null
        shell {
            processBuilder = systemProcess(config) as SystemProcessBuilder
        }
        return processBuilder ?: throw NullPointerException("process builder did not initialized correctly")
    }
}
