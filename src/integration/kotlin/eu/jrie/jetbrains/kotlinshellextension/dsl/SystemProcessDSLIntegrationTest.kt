package eu.jrie.jetbrains.kotlinshellextension.dsl

import eu.jrie.jetbrains.kotlinshellextension.BaseIntegrationTest
import eu.jrie.jetbrains.kotlinshellextension.processes.configuration.SystemProcessConfiguration
import eu.jrie.jetbrains.kotlinshellextension.processes.process.stream.NullProcessStream
import eu.jrie.jetbrains.kotlinshellextension.processes.process.stream.ProcessStream
import eu.jrie.jetbrains.kotlinshellextension.processes.process.system.SystemProcessBuilder
import eu.jrie.jetbrains.kotlinshellextension.shell
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.jetbrains.annotations.TestOnly
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertIterableEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
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
    fun `should create system process followed input`() {
        // when
        val process = create {
            input {
                follow()
            }
        }

        // then
        assertEquals(ProcessStream::class, process.input::class)
    }

    @Test
    fun `should create system process with given input`() {
        // given
        val stream = ProcessStream()

        // when
        val process = create {
            input {
                from(stream)
            }
        }

        // then
        assertEquals(process.input, stream)
    }

    @Test
    fun `should create system process with given file as input`() {
        // given
        val file = file()

        // when
        val process = create {
            input {
                from(file)
            }
        }

        // then
        assertEquals(ProcessStream::class, process.input::class)
        // TODO: check file
    }

    @Test
    fun `should create system process with given String input`() {
        // given
        val given = "abc"

        // when
        val result = testBlocking {
            val process = create {
                input {
                    set(given)
                }
            }
            process.input.initialize(vPID, scope)
            "${process.input.read().toChar()}${process.input.read().toChar()}${process.input.read().toChar()}"
        }

        // then
        assertEquals(given, result)
    }

    @Test
    fun `should create system process with given ByteArray input`() {
        // given
        val given = "abc".toByteArray()

        // when
        val result = testBlocking {
            val process = create {
                input {
                    set(given)
                }
            }
            process.input.initialize(vPID, scope)
            ByteArray(given.size) { process.input.read() }
        }

        // then
        assertEquals(given.contentToString(), result.contentToString())
    }

    @Test
    fun `should create system process with given Byte input`() {
        // given
        val given = 'a'.toByte()

        // when
        val result = testBlocking {
            val process = create {
                input {
                    set(given)
                }
            }
            process.input.initialize(vPID, scope)
            process.input.read()
        }

        // then
        assertEquals(given, result)
    }

    @Test
    fun `should create system process followed outputs`() {
        // when
        val process = create {
            output {
                follow()
            }
        }

        // then
        assertEquals(ProcessStream::class, process.stdout::class)
        assertEquals(ProcessStream::class, process.stderr::class)
        assertNotEquals(process.stdout, process.stderr)
    }

    @Test
    fun `should create system process with given output streams`() {
        // given
        val std = ProcessStream()
        val err = ProcessStream()

        // when
        val process = create {
            output {
                followTo(std, err)
            }
        }

        // then
        assertEquals(process.stdout, std)
        assertEquals(process.stderr, err)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun `should create system process with given output files`() {
        // given
        val std = file("std")
        val err = file("err")

        // when
        val process = create {
            output {
                followTo(std, err)
            }
        }

        // then
        assertEquals(ProcessStream::class, process.stdout::class)
        assertEquals(ProcessStream::class, process.stderr::class)
        // TODO: check file
    }

    @Test
    fun `should create system process with followed merged out`() {
        // when
        val process = create {
            output {
                followMerged()
            }
        }

        // then
        assertEquals(ProcessStream::class, process.stdout::class)
        assertEquals(NullProcessStream::class, process.stderr::class)
    }

    @Test
    fun `should create system process with output merged to given stream`() {
        // given
        val stream = ProcessStream()

        // when
        val process = create {
            output {
                followMergedTo(stream)
            }
        }

        // then
        assertEquals(stream, process.stdout)
        assertEquals(NullProcessStream::class, process.stderr::class)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun `should create system process with output merged to given file`() {
        // given
        val file = file()

        // when
        val process = create {
            output {
                followMergedTo(file)
            }
        }

        // then
        assertEquals(ProcessStream::class, process.stdout::class)
        assertEquals(NullProcessStream::class, process.stderr::class)
        // TODO: verify file
    }

    @Test
    fun `should create system process with followed stdout`() {
        // when
        val process = create {
            output {
                followStd()
            }
        }

        // then
        assertEquals(ProcessStream::class, process.stdout::class)
        assertEquals(NullProcessStream::class, process.stderr::class)
    }

    @Test
    fun `should create system process with stdout followed to given stream`() {
        // given
        val stream = ProcessStream()

        // when
        val process = create {
            output {
                followStdTo(stream)
            }
        }

        // then
        assertEquals(stream, process.stdout)
        assertEquals(NullProcessStream::class, process.stderr::class)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun `should create system process with stdout followed to given file`() {
        // given
        val file = file()

        // when
        val process = create {
            output {
                followStdTo(file)
            }
        }

        // then
        assertEquals(ProcessStream::class, process.stdout::class)
        assertEquals(NullProcessStream::class, process.stderr::class)
        // TODO: verify file
    }

    @Test
    fun `should create system process with followed stderr`() {
        // when
        val process = create {
            output {
                followErr()
            }
        }

        // then
        assertEquals(NullProcessStream::class, process.stdout::class)
        assertEquals(ProcessStream::class, process.stderr::class)
    }

    @Test
    fun `should create system process with stderr followed to given stream`() {
        // given
        val stream = ProcessStream()

        // when
        val process = create {
            output {
                followErrTo(stream)
            }
        }

        // then
        assertEquals(stream, process.stderr)
        assertEquals(NullProcessStream::class, process.stdout::class)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun `should create system process with stderr followed to given file`() {
        // given
        val file = file()

        // when
        val process = create {
            output {
                followErrTo(file)
            }
        }

        // then
        assertEquals(NullProcessStream::class, process.stdout::class)
        assertEquals(ProcessStream::class, process.stderr::class)
        // TODO: verify file
    }

    @Test
    @ExperimentalCoroutinesApi
    fun `should subscribe to stdout`() {
        // when
        val process = create {
            output {
                followStd() andDo { /* nothing */  }
            }
        }

        // then
        assertEquals(ProcessStream::class, process.stdout::class)
        assertEquals(NullProcessStream::class, process.stderr::class)
        // TODO: verify subscription
    }

    @Test
    @ExperimentalCoroutinesApi
    fun `should subscribe to stderr`() {
        // when
        val process = create {
            output {
                followErr() andDo { /* nothing */  }
            }
        }

        // then
        assertEquals(NullProcessStream::class, process.stdout::class)
        assertEquals(ProcessStream::class, process.stderr::class)
        // TODO: verify subscription
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
            dir(directory)
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
