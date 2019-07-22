package eu.jrie.jetbrains.kotlinshellextension.shell

import eu.jrie.jetbrains.kotlinshellextension.all
import eu.jrie.jetbrains.kotlinshellextension.nullout
import eu.jrie.jetbrains.kotlinshellextension.processes.process.ProcessState
import eu.jrie.jetbrains.kotlinshellextension.shell
import eu.jrie.jetbrains.kotlinshellextension.stdout
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.File
import java.io.PrintStream

@ExperimentalCoroutinesApi
class PipingIntegrationTest : ProcessBaseIntegrationTest() {

    @Test
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

            echo pipe cat pipe storeResult
        }

        // then
        assertEquals("$content\n", readResult())
    }

    @Test
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
    fun `should write to file`() {
        // given
        val file = file()
        val initialContent = "abc"
        val writtenContent = "def"
        file.appendText(initialContent)


        // when
        shell {
            systemProcess {
                cmd {
                    "echo" withArg writtenContent
                }
            } pipe file
        }

        // then
        assertEquals(writtenContent.plus('\n'), file.readText())
    }

    @Test
    fun `should append file`() {
        // given
        val file = file()
        val initialContent = "abc"
        val appendedContent = "def"
        file.appendText(initialContent)


        // when
        shell {
            systemProcess {
                cmd {
                    "echo" withArg appendedContent
                }
            } append file
        }

        // then
        assertEquals(initialContent.plus(appendedContent).plus('\n'), file.readText())
    }

    @Test
    fun `should fork stderr for single process`() {
        // given
        val n = 5
        val code = scriptFile(n)

        // when
        shell {
            val script = systemProcess {
                cmd {
                    cmd = "./${code.name}"
                    dir(directory)
                }
            }

            (script forkErr { it pipe storeResult }) pipe nullout await all
        }

        // then
        assertEquals(scriptStdErr(n), readResult())
    }

    @Test
    fun `should pipe forked stderr`() {
        // given
        val n = 5
        val code = scriptFile(n)
        val pattern = "2"

        // when
        shell {
            val script = systemProcess {
                cmd {
                    cmd = "./${code.name}"
                    dir(directory)
                }
            }

            val grep = systemProcess {
                cmd {
                    "grep" withArg pattern
                }
            }

            (script forkErr { it pipe grep pipe storeResult }) pipe nullout await all
        }

        // then
        assertEquals(scriptStdErr(n).grep(pattern), readResult())
    }

    @Test
    fun `should fork stderr in pipeline`() {
        // given
        val n = 5
        val code = scriptFile(n)

        // when
        shell {
            val ls = systemProcess { cmd = "ls" }

            val script = systemProcess {
                cmd {
                    cmd = "./${code.name}"
                    dir(directory)
                }
            }

            ls pipe (script forkErr { it pipe storeResult }) pipe nullout await all
        }

        // then
        assertEquals(scriptStdErr(n), readResult())
    }

    @Test
    fun `should pipe stdout to process correctly after forking`() {
        // given
        val n = 5
        val code = scriptFile(n)
        val pattern = "2"

        // when
        shell {
            val grep = systemProcess {
                cmd {
                    "grep" withArg pattern
                }
            }

            val script = systemProcess {
                cmd {
                    cmd = "./${code.name}"
                    dir(directory)
                }
            }

            (script forkErr nullout) pipe grep pipe storeResult await all
        }

        // then
        assertEquals(scriptStdOut(n).grep(pattern), readResult())
    }

    @Test
    fun `should pipe stdout to file correctly after forking`() {
        // given
        val n = 5
        val code = scriptFile(n)
        val file = file()

        // when
        shell {
            val script = systemProcess {
                cmd {
                    cmd = "./${code.name}"
                    dir(directory)
                }
            }

            (script forkErr nullout) pipe file await all
        }

        // then
        assertEquals(scriptStdOut(n), file.readText())
    }

    @Test
    fun `should await pipeline`() {
        // given
        val file = file(content = LOREM_IPSUM)
        val statesAfterAwait = mutableListOf<String>()

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

            val pipeline = file pipe grep pipe wc pipe storeResult await all
            pipeline.processes.forEach { statesAfterAwait.add(it.pcb.state.name) }
        }

        // then
        statesAfterAwait.forEach { assertEquals(ProcessState.TERMINATED.name, it) }
    }

    @Test
    fun `should kill pipeline`() {
        // given
        val file = file(content = "$LOREM_IPSUM$LOREM_IPSUM$LOREM_IPSUM$LOREM_IPSUM")
        val states = mutableListOf<ProcessState>()

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

            val pipeline = file pipe grep pipe wc pipe file("result")
            pipeline.kill()
            pipeline.processes.forEach { states.add(it.pcb.state) }
        }

        // then
        states.forEach { assertEquals(ProcessState.TERMINATED, it) }
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

            echo pipe stdout
        }

        // then
        assertRegex(Regex(content), outFile.readText())
    }

    @Test
    fun `should pipe stdout to null`() {
        // given
        val outFile = file("console")
        System.setOut(PrintStream(outFile))

        val n = 5
        val code = scriptFile(n)

        // when
        shell {
            val script = systemProcess {
                cmd {
                    cmd = "./${code.name}"
                    dir(directory)
                }
            }

            (script forkErr { it pipe storeResult }) pipe nullout await all
        }

        // then
        assertEquals("", outFile.withoutLogs())
    }

    @Test
    fun `should pipe stderr to null`() {
        // given
        val outFile = file("console")
        System.setOut(PrintStream(outFile))

        val n = 5
        val code = scriptFile(n)

        // when
        shell {
            val script = systemProcess {
                cmd {
                    cmd = "./${code.name}"
                    dir(directory)
                }
            }

            (script forkErr nullout) pipe storeResult await all
        }

        // then
        assertEquals("", outFile.withoutLogs())
    }

    @Test
    fun `should pipe stdout and stderr to null`() {
        // given
        val outFile = file("console")
        System.setOut(PrintStream(outFile))

        val n = 5
        val code = scriptFile(n)

        // when
        shell {
            val script = systemProcess {
                cmd {
                    cmd = "./${code.name}"
                    dir(directory)
                }
            }

            (script forkErr nullout) pipe nullout await all
        }

        // then
        assertEquals("", outFile.withoutLogs())
    }

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
                .toProcess(grep)
                .toProcess(systemProcess { cmd = "cat" })
                .toLambda(storeResult)
                .await()
        }

        // then
        assertEquals("abc\n", readResult())
    }
}
