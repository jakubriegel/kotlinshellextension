package eu.jrie.jetbrains.kotlinshellextension.shell

import eu.jrie.jetbrains.kotlinshellextension.processes.process.ProcessState
import eu.jrie.jetbrains.kotlinshellextension.processes.process.system.SystemProcessBuilder
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

@ExperimentalCoroutinesApi
class SystemProcessIntegrationTest : ProcessBaseIntegrationTest() {

    @Test
    fun `should execute "echo hello world"`() {
        // when
        shell {
            systemProcess {
                cmd {
                    "echo" withArgs listOf("hello", "world")
                }
            } pipe storeResult
        }

        // then
        assertEquals("hello world\n", readResult())
    }

    @Test
    fun `should build process from command line"`() {
        // when
        shell {
            SystemProcessBuilder
                .fromCommandLine("echo hello world") pipe storeResult
        }

        // then
        assertEquals("hello world\n", readResult())
    }

    @Test
    fun `should execute "ls -l"`() {
        // given
        file("file1")
        file("file2")
        dir()

        val dirRegex = Regex("drw.+testdir\n")
        val fileRegex = Regex("-rw.+file[0-9]\n")

        // when
        shell {
            systemProcess {
                cmd {
                    "ls" withArg "-l"
                }
            } pipe storeResult

            commander.awaitAll()
        }

        // then
        val result = readResult()
        assertRegex(dirRegex, result)
        assertRegex(fileRegex, result)
    }

    @Test
    fun `should await process`() {
        // given
        val scriptCode = "for (( i = 0; i < 1000; ++i )); do\n" +
                         "    echo hello\n" +
                         "done\n"

        val scriptName = "script"
        file(scriptName, scriptCode)

        // when
        shell {
            val chmod = launchSystemProcess {
                cmd {
                    "chmod" withArgs listOf("+x", scriptName)
                }
            }
            commander.awaitProcess(chmod)

            val script = launchSystemProcess {
                cmd = "./$scriptName"
            }

            commander.awaitProcess(script)
        }
    }

    @Test
    fun `should await process by vPID`() {
        // given
        val scriptCode = "for (( i = 0; i < 1000; ++i )); do\n" +
                "    echo hello\n" +
                "done\n"

        val scriptName = "script"
        file(scriptName, scriptCode)

        // when
        shell {
            val chmod = launchSystemProcess {
                cmd {
                    "chmod" withArgs listOf("+x", scriptName)
                }
            }
            commander.awaitProcess(chmod)

            val script = launchSystemProcess {
                cmd = "./$scriptName"
            }

            commander.awaitProcess(script.vPID)
        }
    }

    @Test
    fun `should kill running process`() {
        // when
        shell {
            // given
            val n = 1_000
            val scriptCode = scriptFile(n)

            var beforeKill: ProcessState? = null
            var afterKill: ProcessState? = null

            // when
            shell {
                val script = launchSystemProcess {
                    cmd = "./${scriptCode.name}"
                }

                beforeKill = script.pcb.state
                commander.killProcess(script)
                afterKill = script.pcb.state
            }

            // then
            assertEquals(ProcessState.RUNNING, beforeKill)
            assertEquals(ProcessState.TERMINATED, afterKill)
        }
    }

    @Test
    fun `should kill running process by vPID`() {
        // when
        shell {
            // given
            val n = 1_000
            val scriptCode = scriptFile(n)

            var beforeKill: ProcessState? = null
            var afterKill: ProcessState? = null

            // when
            shell {
                val script = launchSystemProcess {
                    cmd = "./${scriptCode.name}"
                }

                beforeKill = script.pcb.state
                commander.killProcess(script.vPID)
                afterKill = script.pcb.state
            }

            // then
            assertEquals(ProcessState.RUNNING, beforeKill)
            assertEquals(ProcessState.TERMINATED, afterKill)
        }
    }

    @Test
    fun `should kill all running processes`() {
        // when
        shell {
            // given
            val n = 1_000
            val scriptCode = scriptFile(n)

            val states = mutableListOf<ProcessState>()

            // when
            shell {
                val script1 = launchSystemProcess {
                    cmd = "./${scriptCode.name}"
                }

                val script2 = launchSystemProcess {
                    cmd = "./${scriptCode.name}"
                }

                val script3 = launchSystemProcess {
                    cmd = "./${scriptCode.name}"
                }

                commander.killAll()
                states.addAll(listOf(script1.pcb.state, script2.pcb.state, script3.pcb.state))
            }

            // then
            states.forEach { assertEquals(ProcessState.TERMINATED, it) }
        }
    }

}
