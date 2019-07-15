package eu.jrie.jetbrains.kotlinshellextension

import eu.jrie.jetbrains.kotlinshellextension.processes.ProcessCommander
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import java.io.File

abstract class BaseIntegrationTest {

    protected val command = "cmd"
    protected val argument = "arg"
    protected val arguments = listOf("arg1", "arg2", "arg3")

    protected val env1 = "var1" to "val1"
    protected val env2 = "var2" to "val2"
    protected val env3 = "var3" to "val3"
    protected val environment = mapOf(env1, env2, env3)

    protected val testDirectory = File(testDirPath)
    protected val directory = "$testDirPath/${this::class.simpleName}Dir"
    protected val directoryFile = File(directory)

    private lateinit var commander: ProcessCommander

    @BeforeEach
    fun createDir() {
        testDirectory.mkdirs()
        directoryFile.mkdirs()
        print("")
    }

    @AfterEach
    fun cleanup() {
        testDirectory.deleteRecursively()
    }

    protected fun <T> runTest(test: () -> T) = runBlocking {
        commander = ProcessCommander(this)
        test()
    }

    companion object {

        private val testDirPath: String
            get() = "${System.getProperty("user.dir")}/testdata"

        private fun currentDir(): File {
            val path = System.getProperty("user.dir")
            return File(path)
        }
    }
}
