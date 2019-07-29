package eu.jrie.jetbrains.kotlinshellextension.shell

import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertIterableEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File
import java.io.PrintStream

@ExperimentalCoroutinesApi
class ProcessIOIntegrationTest : ProcessBaseIntegrationTest() {

    private lateinit var outFile: File

    private val n = 5
    private lateinit var script: File

    @BeforeEach
    fun redirectSystemOut() {
        outFile = file("console")
        System.setOut(PrintStream(outFile))
    }

    @BeforeEach
    fun prepareScript() {
        script = scriptFile(n)
    }

    @Test
    fun `should print stdout and stderr to console when no redirect buffers given`() {
        // when
        shell {
            val process = systemProcess { cmd = "./${script.name}" }
            process()
        }

        // then
//        TODO: synchronize std and err
//        assertEquals(scriptOut(n), outFile.withoutLogs())
        val expected = scriptOut(n).lines().toHashSet()
        val actual = outFile.withoutLogs().lines().toHashSet()
        assertIterableEquals(expected, actual)

    }

//    @Test
//    fun `should print stdout to given buffer and stderr to console when only stdout buffer given`() {
//        // when
//        shell {
//            systemProcess {
//                cmd = "./${script.name}"
//            } pipe storeResult
//
//            commander.awaitAll()
//        }
//
//        // then
//        assertEquals(scriptStdOut(n), readResult())
//        assertEquals(scriptStdErr(n), outFile.withoutLogs())
//    }

//    @Test
//    fun `should print stdout to console and stderr to given buffer when only stderr buffer given`() {
//        // when
//        shell {
//            val script = systemProcess {
//                cmd = "./${script.name}"
//            } forkErr { it pipe storeResult }
//
//            val process = commander.createProcess(script)
//            commander.startProcess(process)
//
//            commander.awaitAll()
//        }
//
//        // then
//        assertEquals(scriptStdOut(n), outFile.withoutLogs())
//        assertEquals(scriptStdErr(n), readResult())
//    }
//
//    @Test
//    fun `should print stdout and stderr to given buffer when both buffers given`() {
//        // given
//        val stdBuilder = BytePacketBuilder()
//        val storeStd = { it: ByteReadPacket -> stdBuilder.writePacket(it) }
//
//        // when
//        shell {
//            val script = systemProcess {
//                cmd = "./${script.name}"
//            }
//
//            (script forkErr { it pipe storeResult }) pipe storeStd
//
//            commander.awaitAll()
//        }
//
//        // then
//        assertEquals("", outFile.withoutLogs())
//        assertEquals(scriptStdOut(n), stdBuilder.build().readText())
//        assertEquals(scriptStdErr(n), readResult())
//    }

    // TODO: split into two IO tests
//    @Test
//    fun `should consume long output`() {
//        // given
//        val n = 10_000
//        val scriptCode = scriptCode(n)
//        val stdBuilder = BytePacketBuilder()
//        val storeStd = { it: ByteReadPacket -> stdBuilder.writePacket(it) }
//
//        val scriptName = "script"
//        file(scriptName, scriptCode)
//
//        // when
//        shell {
//            val script = systemProcess {
//                cmd = "./$scriptName"
//            }
//
//            (script forkErr  { it pipe storeResult }) pipe storeStd
//
//            commander.awaitAll()
//        }
//
//        // then
//        assertEquals(scriptStdOut(n), stdBuilder.build().readText())
//        assertEquals(scriptStdErr(n), readResult())
//    }

    companion object {

        private lateinit var defaultOut: PrintStream

        @BeforeAll
        @JvmStatic
        fun storeSystemOut() {
            defaultOut = System.out
        }

        @AfterAll
        @JvmStatic
        fun resetSystemOut() {
            System.setOut(defaultOut)
        }
    }

}
