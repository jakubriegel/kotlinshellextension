package eu.jrie.jetbrains.kotlinshellextension.shell.piping

import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.PrintStream

@ExperimentalCoroutinesApi
class PipingIntegrationTest : PipingBaseIntegrationTest() {

    @Test
    fun `should pipe "echo abc | cat | lambda"`() {
        // when
        shell {
            val echo = systemProcess {
                cmd { "echo" withArg content }
            }
            val cat = systemProcess { cmd = "cat" }

            pipeline { echo pipe cat pipe storeResult }
        }
        // then
        assertEquals("$content\n", readResult())
    }

    @Test
    fun `should pipe file to "grep "Llorem""`() {
        // given
        val file = testFile(content = LOREM_IPSUM)
        val pattern = "[Ll]orem"

        // when
        shell {
            val grep = systemProcess {
                cmd { "grep" withArg pattern }
            }

            pipeline { file pipe grep pipe storeResult }
        }

        // then
        assertEquals(LOREM_IPSUM.grep(pattern), readResult())
    }

    @Test
    fun `should pipe "file to grep "Llorem" | wc -m"`() {
        // given
        val file = testFile(content = LOREM_IPSUM)
        val pattern = "[Ll]orem"

        // when
        shell {
            val grep = systemProcess {
                cmd { "grep" withArg pattern }
            }

            val wc = systemProcess {
                cmd { "wc" withArg "-m" }
            }

            pipeline { file pipe grep pipe wc pipe storeResult }
        }

        // then
        val expected = LOREM_IPSUM.grep(pattern).count()
        assertRegex(Regex("[\n\t\r ]+$expected[\n\t\r ]+"), readResult())
    }

    @Test
    fun `should pipe "file to grep "Llorem" | wc --chars to file"`() {
        // given
        val file = testFile(content = LOREM_IPSUM)
        val resultFile = testFile("result")
        val pattern = "[Ll]orem"

        // when
        shell {
            val grep = systemProcess {
                cmd { "grep" withArg pattern }
            }

            val wc = systemProcess {
                cmd { "wc" withArg "-m" }
            }

            file pipe grep pipe wc pipe resultFile
        }

        // then
        val expected = LOREM_IPSUM.grep(pattern).count()
        assertRegex(Regex("[\n\t\r ]+$expected[\n\t\r ]+"), resultFile.readText())
    }

    @Test
    fun `should pipe to console`() {
        // given
        val outFile = testFile("console")
        System.setOut(PrintStream(outFile))

        // when
        shell {
            val echo = systemProcess {
                cmd { "echo" withArg content }
            }

            echo pipe stdout join it
        }

        // then
        assertRegex(Regex(content), outFile.readText())
    }


    @Test
    fun `should pipe to console by default`() {
        // given
        val outFile = testFile("console")
        System.setOut(PrintStream(outFile))

        // when
        shell {
            val echo = systemProcess {
                cmd { "echo" withArg content }
            }

            pipeline { echo pipe "cat".process() }
        }

        // then
        assertRegex(Regex(content), outFile.readText())
    }

    @Test
    fun `should pipe long stream`() {
        // given
        val n = 100_000
        val file = scriptFile(n)
        val pattern = "2"

        // when
        shell {
            val script = systemProcess { cmd = "./${file.name}" }
            val cat = systemProcess { cmd = "cat" }
            val grep = systemProcess { cmd { "grep" withArg pattern } }

            (script forkErr nullout) pipe cat pipe grep pipe storeResult
        }

        // then
        assertEquals(scriptStdOut(n).grep(pattern), readResult())
    }

    @Test
    fun `should make pipeline with non DSL api`() {
        // when
        shell {
            val echo = systemProcess {
                cmd { "echo" withArg "abc\ndef" }
            }
            val grep = systemProcess {
                cmd { "grep" withArg "c" }
            }

            from(echo)
                .throughProcess(grep)
                .throughProcess(systemProcess { cmd = "cat" })
                .throughLambda { storeResult(it) }
                .join()
        }

        // then
        assertEquals("abc\n", readResult())
    }
}
