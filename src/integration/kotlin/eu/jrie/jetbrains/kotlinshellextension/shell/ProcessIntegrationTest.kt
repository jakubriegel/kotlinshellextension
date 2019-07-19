package eu.jrie.jetbrains.kotlinshellextension.shell

import eu.jrie.jetbrains.kotlinshellextension.shell
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

@ExperimentalCoroutinesApi
class ProcessIntegrationTest : ProcessBaseIntegrationTest() {

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
                dir(directory)
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
                dir(directory)
            }
            commander.awaitProcess(chmod)

            val script = launchSystemProcess {
                cmd = "./$scriptName"
                dir(directory)
            }

            commander.awaitProcess(script)
        }
    }

    @Test
    fun `should consume result of long process`() {
        TODO("fix when pipeErr done")
        // given
        val n = 100_000
        val scriptCode = printScript(n)

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

            systemProcess {
                cmd = "./$scriptName"
                dir(directory)
            } pipe storeResult

            commander.awaitAll()
        }

        // then
        val expected = StringBuilder()
            .also { repeat(n) { _ ->  it.append("$printScriptMessage\n") } }
            .toString()
        assertEquals(expected, readResult())
    }
}
