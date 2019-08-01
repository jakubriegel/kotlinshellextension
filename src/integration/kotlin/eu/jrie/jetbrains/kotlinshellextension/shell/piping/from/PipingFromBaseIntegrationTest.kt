package eu.jrie.jetbrains.kotlinshellextension.shell.piping.from

import eu.jrie.jetbrains.kotlinshellextension.processes.process.ProcessChannel
import eu.jrie.jetbrains.kotlinshellextension.shell.ProcessBaseIntegrationTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions

@ExperimentalCoroutinesApi
abstract class PipingFromBaseIntegrationTest : ProcessBaseIntegrationTest() {
    protected val content = "abc"

    protected fun ProcessChannel.read() = runBlocking {
        StringBuilder().let { b ->
            this@read.consumeEach { b.append(it.readText()) }
            b.toString()
        }
    }

    protected fun assertContent() = assertContent(readResult())

    protected open fun assertContent(result: String) {
        Assertions.assertEquals(content, result)
    }
}
