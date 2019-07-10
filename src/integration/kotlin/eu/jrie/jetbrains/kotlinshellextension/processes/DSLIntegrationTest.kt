package eu.jrie.jetbrains.kotlinshellextension.processes

import eu.jrie.jetbrains.kotlinshellextension.BaseIntegrationTest
import eu.jrie.jetbrains.kotlinshellextension.processes.process.ProcessConfiguration
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertIterableEquals
import org.junit.jupiter.api.Test

class DSLIntegrationTest : BaseIntegrationTest() {

    @Test
    fun `should create system process with given command`() {
        // when
        val process = systemProcess {
            cmd(COMMAND)
        }

        // then
        assertEquals(process.command, COMMAND)
    }

    @Test
    fun `should create system process with given command and single argument`() {
        // when
        val process = systemProcess {
            cmd {
                COMMAND withArg ARGUMENT
            }
        }

        // then
        assertEquals(process.command, COMMAND)
        assertIterableEquals(process.arguments, listOf(ARGUMENT))
    }

    @Test
    fun `should create system process with given command and arguments`() {
        // when
        val process = systemProcess {
            cmd {
                COMMAND withArgs ARGUMENTS
            }
        }

        // then
        assertEquals(process.command, COMMAND)
        assertIterableEquals(process.arguments, ARGUMENTS)
    }

    @Test
    fun `should create system process with given enviroment variable`() {
        // when
        val process = systemProcess {
            env(ENV_VAR_1)
        }

        // then
        assertEquals(process.environment().size, 1)
        assertEquals(process.environment()[ENV_VAR_1.first], ENV_VAR_1.second)
    }

    @Test
    fun `should create system process with given enviroment variables`() {
        // when
        val process = systemProcess {
            env {
                ENV_VAR_1.first to ENV_VAR_1.second
                ENV_VAR_2.first to ENV_VAR_2.second
                ENV_VAR_3.first to ENV_VAR_3.second
            }
        }

        // then
        val env = process.environment()
        assertEquals(process.environment().size, 3)
        assertEquals(env[ENV_VAR_1.first], ENV_VAR_1.second)
        assertEquals(env[ENV_VAR_2.first], ENV_VAR_2.second)
        assertEquals(env[ENV_VAR_3.first], ENV_VAR_3.second)
    }

    @Test
    fun `should create system process with given enviroment`() {
        // when
        val process = systemProcess {
            env (ENVIRONMENT)
        }

        // then
        val env = process.environment()
        assertEquals(process.environment().size, 3)
        assertEquals(env[ENV_VAR_1.first], ENV_VAR_1.second)
        assertEquals(env[ENV_VAR_2.first], ENV_VAR_2.second)
        assertEquals(env[ENV_VAR_3.first], ENV_VAR_3.second)
    }

    @Test
    fun `should create system process with given input`() {
        // given
        val inputStream = lazy { commander.processInputStream() }

        // when
        val process = systemProcess {
            input(inputStream.value)
        }

        // then
        assertEquals(process.vPID, inputStream.value.vPID)
        assertEquals(process.input, inputStream.value)
    }

    @Test
    fun `should create system process with output merged to given stream`() {
        // given
        val outputStream = lazy { commander.processOutputStream() }

        // when
        val process = systemProcess {
            output {
                mergeOutTo(outputStream.value)
            }
        }

        // then
        assertEquals(process.vPID, outputStream.value.vPID)
        assertEquals(process.stdout, outputStream.value)
    }

    @Test
    fun `should create system process with output redirected to given stream`() {
        // given
        val outputStream = lazy { commander.processOutputStream() }

        // when
        val process = systemProcess {
            output {
                mergeOutTo(outputStream.value)
            }
        }

        // then
        assertEquals(process.vPID, outputStream.value.vPID)
        assertEquals(process.stdout, outputStream.value)
    }

    @Test
    fun `should create system process with output redirected to given streams`() {
        // given
        val stdStream = lazy { commander.processOutputStream() }
        val errStream = lazy { commander.processOutputStream() }

        // when
        val process = systemProcess {
            output {
                redirectOutTo(stdStream.value, errStream.value)
            }
        }

        // then
        assertEquals(process.vPID, stdStream.value.vPID)
        assertEquals(process.vPID, errStream.value.vPID)
        assertEquals(process.stdout, stdStream.value)
        assertEquals(process.stderr, errStream.value)
    }

    @Test
    fun `should create system process with std output redirected to given stream`() {
        // given
        val stdStream = lazy { commander.processOutputStream() }

        // when
        val process = systemProcess {
            output {
                redirectStdOutTo(stdStream.value)
            }
        }

        // then
        assertEquals(process.vPID, stdStream.value.vPID)
        assertEquals(process.stdout, stdStream.value)
    }

    @Test
    fun `should create system process with err output redirected to given stream`() {
        // given
        val errStream = lazy { commander.processOutputStream() }

        // when
        val process = systemProcess {
            output {
                redirectStdErrTo(errStream.value)
            }
        }

        // then
        assertEquals(process.vPID, errStream.value.vPID)
        assertEquals(process.stderr, errStream.value)
    }

    private fun systemProcess(config: ProcessConfiguration.() -> Unit) = runTest {
        val vPID = commander.systemProcess(config)
        commander.getProcessByVirtualPID(vPID)
    }
}