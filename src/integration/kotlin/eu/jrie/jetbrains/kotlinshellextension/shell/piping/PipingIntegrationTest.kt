package eu.jrie.jetbrains.kotlinshellextension.shell.piping

import eu.jrie.jetbrains.kotlinshellextension.shell.ProcessBaseIntegrationTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.File
import java.io.PrintStream

@ExperimentalCoroutinesApi
class PipingIntegrationTest : ProcessBaseIntegrationTest() {


    @Test
    fun `should pipe multiple processes to function`() {
        // given
        val content = "abc"
        // when
        shell {
            val echo = systemProcess {
                cmd {
                    "echo" withArg content
                }
            }
            val cat = systemProcess {
                cmd = "cat"
            }

            pipeline { echo pipe cat pipe storeResult }
        }
        // then
        assertEquals("$content\n", readResult())
    }

    @Test
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

            pipeline { file pipe grep pipe storeResult }
        }

        // then
        val expected = "Lorem ipsum dolor sit amet,\n" +
                       "Duis rhoncus purus sed lorem finibus,\n"
        assertEquals(expected, readResult())
    }

    @Test
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

            pipeline { file pipe grep pipe wc pipe storeResult }
        }

        // then
        val expected = "Lorem ipsum dolor sit amet,\nDuis rhoncus purus sed lorem finibus,\n".count()
        assertRegex(Regex("[\n\t\r ]+$expected[\n\t\r ]+"), readResult())
    }

    @Test
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
    fun `should pipe to console`() {
        // given
        val outFile = file("console")
        System.setOut(PrintStream(outFile))
        val content = "abc"

        // when
        shell {
            val echo = systemProcess {
                cmd {
                    "echo" withArg content
                }
            }

            echo pipe stdout await all
        }

        // then
        assertRegex(Regex(content), outFile.readText())
    }


    @Test
    fun `should pipe to console by default`() {
        // given
        val outFile = file("console")
        System.setOut(PrintStream(outFile))
        val content = "abc"

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

//    @Test
//    fun `should pipe long stream`() {
//        // given
//        val n = 1000
//        val file = scriptFile(n)
//        val pattern = "2"
//
//        // when
//        shell {
//            val script = systemProcess {
//                cmd = "./${file.name}"
//            }
//
//            val grep = systemProcess {
//                cmd { "grep" withArg pattern }
//            }
//
//            (script forkErr nulloutOld) pipe grep pipe storeResult
//        }
//
//        // then
//        assertEquals(scriptStdOut(n).grep(pattern), readResult())
//    }

    @Test
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
                .throughProcess(grep)
                .throughProcess(systemProcess { cmd = "cat" })
                .throughLambda { storeResult(it) }
                .await()
        }

        // then
        assertEquals("abc\n", readResult())
    }
}
