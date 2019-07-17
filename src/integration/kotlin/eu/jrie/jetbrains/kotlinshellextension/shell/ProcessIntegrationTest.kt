package eu.jrie.jetbrains.kotlinshellextension.shell

import eu.jrie.jetbrains.kotlinshellextension.shell
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ProcessIntegrationTest : ProcessBaseIntegrationTest() {

    @Test
    @ExperimentalCoroutinesApi
    fun `should execute "echo hello world"`() {
        // when
        shell {
            val echo = launchSystemProcess {
                cmd {
                    "echo" withArgs listOf("hello", "world")
                }
            }

            consumeResult(echo)
        }

        // then
        assertEquals("hello world\n", readResult())
    }

    @Test
    @ExperimentalCoroutinesApi
    fun `should execute "ls -l"`() {
        // given
        file("file1")
        file("file2")
        dir()

        val dirRegex = Regex("drw.+testdir\n")
        val fileRegex = Regex("-rw.+file[0-9]\n")

        // when
        shell {
            val ls = launchSystemProcess {
                cmd {
                    "ls" withArg "-l"
                }
                dir(directory)
            }

            consumeResult(ls)
        }

        // then
        val result = readResult()
        assertRegex(dirRegex, result)
        assertRegex(fileRegex, result)
    }

    @Test
    @ExperimentalCoroutinesApi
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
                dir(directory)
            }
            commander.awaitProcess(chmod)

            val script = launchSystemProcess {
                cmd = "./$scriptName"
                dir(directory)
            }

            // TODO: fix awaiting without reading channel [probably buffer size]
            consumeResult(script)
            commander.awaitProcess(script)
        }
    }
}
