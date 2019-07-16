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
            launchSystemProcess {
                cmd {
                    "echo" withArgs listOf("hello", "world")
                }
                output {
                    followStd() andDo storeResult
                }
                dir(directory)
            }
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
            launchSystemProcess {
                cmd {
                    "ls" withArg "-l"
                }
                output {
                    followStd() andDo storeResult
                }
                dir(directory)
            }
        }

        // then
        val result = readResult()
        assertRegex(dirRegex, result)
        assertRegex(fileRegex, result)
    }
}
