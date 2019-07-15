package eu.jrie.jetbrains.kotlinshellextension.dsl

import eu.jrie.jetbrains.kotlinshellextension.BaseIntegrationTest
import eu.jrie.jetbrains.kotlinshellextension.processes.configuration.SystemProcessConfiguration
import eu.jrie.jetbrains.kotlinshellextension.processes.process.system.SystemProcessBuilder
import eu.jrie.jetbrains.kotlinshellextension.systemProcess
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertIterableEquals
import org.junit.jupiter.api.Test

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
    fun `should create system process with given enviroment`() {
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
//
//    @Test
//    fun `should create system process with given input`() {
//        // given
//        val inputStream = lazy { commander.processInputStream() }
//
//        // when
//        val create = create {
//            input(inputStream.value)
//        }
//
//        // then
//        Assertions.assertEquals(create.vPID, inputStream.value.vPID)
//        Assertions.assertEquals(create.input, inputStream.value)
//    }
//
//    @Test
//    fun `should create system process with output merged to given stream`() {
//        // given
//        val outputStream = lazy { commander.processOutputStream() }
//
//        // when
//        val create = create {
//            output {
//                mergeOutTo(outputStream.value)
//            }
//        }
//
//        // then
//        Assertions.assertEquals(create.vPID, outputStream.value.vPID)
//        Assertions.assertEquals(create.stdout, outputStream.value)
//    }
//
//    @Test
//    fun `should create system process with output redirected to given stream`() {
//        // given
//        val outputStream = lazy { commander.processOutputStream() }
//
//        // when
//        val create = create {
//            output {
//                mergeOutTo(outputStream.value)
//            }
//        }
//
//        // then
//        Assertions.assertEquals(create.vPID, outputStream.value.vPID)
//        Assertions.assertEquals(create.stdout, outputStream.value)
//    }
//
//    @Test
//    fun `should create system process with output redirected to given streams`() {
//        // given
//        val stdStream = lazy { commander.processOutputStream() }
//        val errStream = lazy { commander.processOutputStream() }
//
//        // when
//        val create = create {
//            output {
//                redirectOutTo(stdStream.value, errStream.value)
//            }
//        }
//
//        // then
//        Assertions.assertEquals(create.vPID, stdStream.value.vPID)
//        Assertions.assertEquals(create.vPID, errStream.value.vPID)
//        Assertions.assertEquals(create.stdout, stdStream.value)
//        Assertions.assertEquals(create.stderr, errStream.value)
//    }
//
//    @Test
//    fun `should create system process with std output redirected to given stream`() {
//        // given
//        val stdStream = lazy { commander.processOutputStream() }
//
//        // when
//        val create = create {
//            output {
//                redirectStdOutTo(stdStream.value)
//            }
//        }
//
//        // then
//        Assertions.assertEquals(create.vPID, stdStream.value.vPID)
//        Assertions.assertEquals(create.stdout, stdStream.value)
//    }
//
//    @Test
//    fun `should create system process with err output redirected to given stream`() {
//        // given
//        val errStream = lazy { commander.processOutputStream() }
//
//        // when
//        val create = create {
//            output {
//                redirectStdErrTo(errStream.value)
//            }
//        }
//
//        // then
//        Assertions.assertEquals(create.vPID, errStream.value.vPID)
//        Assertions.assertEquals(create.stderr, errStream.value)
//    }
//
    
    @Test
    fun `should create system process with given directory as String`() {
        // when
        val process = create {
            dir(directory)
        }

        // then
        assertEquals(directory, process.directory.path )
    }

    @Test
    fun `should create system process with given directory as File`() {
        // when
        val process = create {
            dir(directoryFile)
        }

        // then
        assertEquals(directory, process.directory.path)
    }
    
    private fun create(config: SystemProcessConfiguration.() -> Unit) = systemProcess(config) as SystemProcessBuilder
}
