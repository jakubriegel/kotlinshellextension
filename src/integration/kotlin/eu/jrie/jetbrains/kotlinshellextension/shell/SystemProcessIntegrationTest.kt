package eu.jrie.jetbrains.kotlinshellextension.shell

import eu.jrie.jetbrains.kotlinshellextension.processes.process.ProcessState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertIterableEquals
import org.junit.jupiter.api.Test

@ExperimentalCoroutinesApi
class SystemProcessIntegrationTest : ProcessBaseIntegrationTest() {

//    @Test
//    fun `should execute "echo hello world"`() {
//        // when
//        shell {
//            systemProcess {
//                cmd {
//                    "echo" withArgs listOf("hello", "world")
//                }
//            } pipe storeResult
//        }
//
//        // then
//        assertEquals("hello world\n", readResult())
//    }

//    @Test
//    fun `should run process from command line"`() {
//        // when
//        shell {
//            "echo hello world"() pipe ...
//        }
//
//        // then
//        // assert
//    }

//    @Test
//    fun `should execute "ls -l"`() {
//        // given
//        file("file1")
//        file("file2")
//        dir()
//
//        val dirRegex = Regex("drw.+testdir\n")
//        val fileRegex = Regex("-rw.+file[0-9]\n")
//
//        // when
//        shell {
//            systemProcess {
//                cmd {
//                    "ls" withArg "-l"
//                }
//            } pipe storeResult
//
//            commander.awaitAll()
//        }
//
//        // then
//        val result = readResult()
//        assertRegex(dirRegex, result)
//        assertRegex(fileRegex, result)
//    }

    @Test
    fun `should run process sequentially`() {
        // given
        val file = scriptFile(250)
        var stateAfterCall: ProcessState? = null

        // when
        shell {
            val script = systemProcess { cmd = "./${file.name}" }
            script()
            stateAfterCall = processes.first().pcb.state
        }

        // then
        assertEquals(ProcessState.TERMINATED, stateAfterCall)
    }

    @Test
    fun `should detach process`() {
        // given
        val file = scriptFile(500)
        var stateAfterCall: ProcessState? = null

        // when
        shell {
            val script = systemProcess { cmd = "./${file.name}" }
            detach(script)
            delay(50)
            stateAfterCall = processes.first().pcb.state
        }

        // then
        assertEquals(ProcessState.RUNNING, stateAfterCall)
    }

    @Test
    fun `should detach process with extension function`() {
        // given
        val file = scriptFile(500)
        var stateAfterCall: ProcessState? = null

        // when
        shell {
            "./${file.name}"(ExecutionMode.DETACHED)
            delay(50)
            stateAfterCall = processes.first().pcb.state
        }

        // then
        assertEquals(ProcessState.RUNNING, stateAfterCall)
    }

    @Test
    fun `should list detached processes`() {
        // given
        val file = scriptFile(5)

        // when
        shell {
            val p1 = "./${file.name}".process()
            val p2 = "./${file.name}".process()
            val p3 = "./${file.name}".process()
            detach(p1, p2, p3)

            // then
            assertIterableEquals(listOf(p1.process, p2.process, p3.process), detached)
            // TODO: check "jobs()"
        }
    }

    @Test
    fun `should attach process`() {
        // given
        val file = scriptFile(250)
        var stateAfterAttach: ProcessState? = null

        // when
        shell {
            val script = systemProcess { cmd = "./${file.name}" }
            detach(script)
            delay(50)
            fg()
            stateAfterAttach = processes.first().pcb.state
        }

        // then
        assertEquals(ProcessState.TERMINATED, stateAfterAttach)
    }

    @Test
    fun `should crate a daemon process`() {
        // given
        val file = scriptFile(500)
        var stateAfterCall: ProcessState? = null

        // when
        shell {
            val script = systemProcess { cmd = "./${file.name}" }
            daemon(script)
            delay(50)
            stateAfterCall = processes.first().pcb.state
        }

        // then
        assertEquals(ProcessState.RUNNING, stateAfterCall)
    }

    @Test
    fun `should crate a daemon process with extension function`() {
        // given
        val file = scriptFile(500)
        var stateAfterCall: ProcessState? = null

        // when
        shell {
            "./${file.name}"(ExecutionMode.DAEMON)
            delay(50)
            stateAfterCall = processes.first().pcb.state
        }

        // then
        assertEquals(ProcessState.RUNNING, stateAfterCall)
    }

    @Test
    fun `should list daemon processes`() {
        // given
        val file = scriptFile(5)

        // when
        shell {
            val p1 = "./${file.name}".process()
            val p2 = "./${file.name}".process()
            val p3 = "./${file.name}".process()
            daemon(p1, p2, p3)
            delay(100)

            // then
            assertIterableEquals(listOf(p1.process, p2.process, p3.process), daemons)
        }
    }

    @Test
    fun `should await running process`() {
        // given
        val file = scriptFile(250)
        var stateAfterAttach: ProcessState? = null

        // when
        shell {
            val script = systemProcess { cmd = "./${file.name}" }
            detach(script)
            delay(50)
            script.process.await()
            stateAfterAttach = processes.first().pcb.state
        }

        // then
        assertEquals(ProcessState.TERMINATED, stateAfterAttach)
    }

    @Test
    fun `should kill running process`() {
        // given
        val n = 1_000
        val scriptCode = scriptFile(n)

        var beforeKill: ProcessState? = null
        var afterKill: ProcessState? = null

        // when
        shell {
            val script = systemProcess { cmd = "./${scriptCode.name}" }
            detach(script)

            beforeKill = script.process.pcb.state
            script.process.kill()
            afterKill =script.process.pcb.state
        }

        // then
        assertEquals(ProcessState.RUNNING, beforeKill)
        assertEquals(ProcessState.TERMINATED, afterKill)
    }

    @Test
    fun `should await all running processes`() {
        // given
        val n = 150
        val scriptCode = scriptFile(n)

        val states = mutableListOf<ProcessState>()

        // when
        shell {
            "./${scriptCode.name}"()
            "./${scriptCode.name}"()
            "./${scriptCode.name}"()

            awaitAll()
            states.addAll(processes.map { it.pcb.state })
        }

        // then
        states.forEach { assertEquals(ProcessState.TERMINATED, it) }
    }

    @Test
    fun `should kill all running processes`() {
        // given
        val n = 1_000
        val scriptCode = scriptFile(n)

        val states = mutableListOf<ProcessState>()

        // when
        shell {
            "./${scriptCode.name}"()
            "./${scriptCode.name}"()
            "./${scriptCode.name}"()

            killAll()
            states.addAll(processes.map { it.pcb.state })
        }

        // then
        states.forEach { assertEquals(ProcessState.TERMINATED, it) }
    }

//    @Test
//    fun `should consume long line`() {
//        // given
//        val line = StringBuilder().let { b ->
//            repeat(1024) { b.append("a") }
//            b.toString()
//        }
//
//        val code = "echo $line"
//        val file = file(content = code)
//
//        // when
//        shell {
//            val chmod = launchSystemProcess {
//                cmd {
//                    "chmod" withArgs listOf("+x", file.name)
//                }
//            }
//            commander.awaitProcess(chmod)
//
//            val echo = systemProcess { cmd = "./${file.name}" }
//            val cat = systemProcess { cmd = "cat" }
//            echo pipe cat pipe storeResult await all
//        }
//
//        // then
//        assertEquals(line.plus('\n'), readResult())
//    }

}
