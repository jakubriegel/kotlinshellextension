package eu.jrie.jetbrains.kotlinshellextension

import eu.jrie.jetbrains.kotlinshellextension.processes.ProcessCommander
import eu.jrie.jetbrains.kotlinshellextension.shell.ShellScript
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.jetbrains.annotations.TestOnly
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.opentest4j.AssertionFailedError
import java.io.File

@ExperimentalCoroutinesApi
abstract class BaseIntegrationTest {

    protected val vPID = 1

    protected val command = "cmd"
    protected val argument = "arg"
    protected val arguments = listOf("arg1", "arg2", "arg3")

    protected val env1 = "var1" to "val1"
    protected val env2 = "var2" to "val2"
    protected val env3 = "var3" to "val3"
    protected val environment = mapOf(env1, env2, env3)


    protected val directoryPath = "$testDirPath/${this::class.simpleName}Dir"
    protected val directory = File(directoryPath)

    private lateinit var commander: ProcessCommander
    protected lateinit var scope: CoroutineScope
        private set

    @BeforeEach
    fun init() {
        directory.mkdirs()
    }

    @AfterEach
    fun cleanup() {
        directory.deleteRecursively()
    }

    fun file(name: String = "testfile", content: String = "") = File("$directoryPath/$name").also {
        it.writeText(content)
        it.createNewFile()
    }

    fun dir(name: String = "testdir") = File("$directoryPath/$name").also {
        it.mkdirs()
    }

    @TestOnly
    protected fun shell(
        env: Map<String, String>? = null,
        dir: File? = directory,
        script: ShellScript
    ) = runBlocking {
        eu.jrie.jetbrains.kotlinshellextension.shell.shell(env, dir, this) { script() }
    }

    companion object {

        protected val testDirectory = File(testDirPath)

        @BeforeAll
        @JvmStatic
        fun initTestClass() {
            testDirectory.deleteRecursively()
            testDirectory.mkdirs()
        }

        @AfterAll
        @JvmStatic
        fun cleanupTestClass() {
            testDirectory.deleteRecursively()
        }

        fun assertRegex(regex: Regex, value: String) {
            try {
                assertTrue(regex.containsMatchIn(value))
            } catch (e: AssertionFailedError) {
                throw AssertionFailedError("expected: ${regex.pattern} but was: $value")
            }
        }

        private val testDirPath: String
            get() = "${System.getProperty("user.dir")}/testdata"

        const val LOREM_IPSUM = "Lorem ipsum dolor sit amet,\n" +
                                "consectetur adipiscing elit.\n" +
                                "Etiam sed pharetra enim.\n" +
                                "Duis rhoncus purus sed lorem finibus,\n" +
                                "nec gravida.\n"
    }
}
