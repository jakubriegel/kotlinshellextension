package eu.jrie.jetbrains.kotlinshellextension.shell

import eu.jrie.jetbrains.kotlinshellextension.processes.process.ProcessState
import eu.jrie.jetbrains.kotlinshellextension.shell
import eu.jrie.jetbrains.kotlinshellextension.stdout
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.File

class PipingIntegrationTest : ProcessBaseIntegrationTest() {

    @Test
    @ExperimentalCoroutinesApi
    fun `should pipe to function`() {
        // given
        val content = "abc"

        // when
        shell {
            val echo = systemProcess {
                cmd {
                    "echo" withArg content
                }
            }

            echo pipe storeResult
        }

        // then
        assertEquals("$content\n", readResult())
    }

    @Test
    @ExperimentalCoroutinesApi
    fun `should pipe file to "cat"`() {
        // given
        val file = file(content = LOREM_IPSUM)

        // when
        shell {
            val cat = systemProcess { cmd = "cat" }

            file pipe cat pipe storeResult
        }

        // then
        assertEquals(LOREM_IPSUM, readResult())
    }

    @Test
    @ExperimentalCoroutinesApi
    fun `should pipe file to "grep "Llorem""`() {
        // given
        val file = file(content = LOREM_IPSUM)

        // when
        shell {
            val grep = systemProcess {
                cmd {
                    "grep" withArg "[Ll]orem"
                }
            }

            file pipe grep pipe storeResult
        }

        // then
        val expected = "Lorem ipsum dolor sit amet,\n" +
                       "Duis rhoncus purus sed lorem finibus,\n"
        assertEquals(expected, readResult())
    }

    @Test
    @ExperimentalCoroutinesApi
    fun `should pipe "file to grep "Llorem" | wc --chars"`() {
        // given
        val file = file(content = LOREM_IPSUM)

        // when
        shell {
            val grep = systemProcess {
                cmd {
                    "grep" withArg "[Ll]orem"
                }
            }

            val wc = systemProcess {
                cmd {
                    "wc" withArg "-m"
                }
            }

            file pipe grep pipe wc pipe storeResult
        }

        // then
        val expected = "Lorem ipsum dolor sit amet,\nDuis rhoncus purus sed lorem finibus,\n".count()
        assertRegex(Regex("[\n\t\r ]+$expected[\n\t\r ]+"), readResult())
    }

    @Test
    @ExperimentalCoroutinesApi
    fun `should pipe "file to grep "Llorem" | wc --chars to file"`() {
        // given
        val file = file(content = LOREM_IPSUM)

        // when
        shell {
            val grep = systemProcess {
                cmd {
                    "grep" withArg "[Ll]orem"
                }
            }

            val wc = systemProcess {
                cmd {
                    "wc" withArg "-m"
                }
            }

            file pipe grep pipe wc pipe file("result")
        }

        // then
        val expected = "Lorem ipsum dolor sit amet,\nDuis rhoncus purus sed lorem finibus,\n".count()
        val result = File("$directoryPath/result").readText()
        assertRegex(Regex("[\n\t\r ]+$expected[\n\t\r ]+"), result)
    }

    @Test
    @ExperimentalCoroutinesApi
    fun `should await pipeline`() {
        // given
        val file = file(content = LOREM_IPSUM)
        val states = mutableListOf<String>()

        // when
        shell {
            val grep = systemProcess {
                cmd {
                    "grep" withArg "[Ll]orem"
                }
            }

            val wc = systemProcess {
                cmd {
                    "wc" withArg "-m"
                }
            }

            val pipeline = file pipe grep pipe wc pipe storeResult
            pipeline.await()
            pipeline.processes.forEach { states.add(it.pcb.state.name) }
        }

        // then
        states.forEach { assertEquals(ProcessState.TERMINATED.name, it) }
    }

    @Test
    @ExperimentalCoroutinesApi
    fun `should pipe to console`() {
        // when
        shell {
            val echo = systemProcess {
                cmd {
                    "echo" withArg "abc"
                }
            }

            echo pipe stdout
        }
    }

    @Test
    @ExperimentalCoroutinesApi
    fun `should make pipeline with non DSL api`() {
        // when
        shell {
            val echo = systemProcess {
                cmd {
                    "echo" withArg "abc\ndef"
                }
            }
            val grep = systemProcess {
                cmd {
                    "grep" withArg "c"
                }
            }

            from(echo)
                .toProcess(grep)
                .toProcess(systemProcess { cmd = "cat" })
                .toLambda(storeResult)
                .await()
        }

        // then
        assertEquals("abc\n", readResult())
    }
}
