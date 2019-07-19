package eu.jrie.jetbrains.kotlinshellextension.shell

import eu.jrie.jetbrains.kotlinshellextension.BaseIntegrationTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.io.core.BytePacketBuilder
import kotlinx.io.core.ByteReadPacket
import org.junit.jupiter.api.BeforeEach

@ExperimentalCoroutinesApi
abstract class ProcessBaseIntegrationTest : BaseIntegrationTest() {
    private lateinit var resultBuilder: BytePacketBuilder

    protected val storeResult = { it: ByteReadPacket -> resultBuilder.writePacket(it) }

    @BeforeEach
    fun initResultBuilder() {
        resultBuilder = BytePacketBuilder()
    }

    protected fun readResult() = resultBuilder.build().readText()

    protected var printScriptMessage = "hello"
    protected val printScriptErrorMessage: String
            get() = "${printScriptMessage}_error"

    protected fun printScript(n: Int) =
        "for (( i = 0; i < $n; ++i )); do\n" +
        "    echo $printScriptMessage\n" +
        "    >&2 echo $printScriptErrorMessage\n" +
        "done\n"

    protected fun printScriptStdOut(n: Int) = StringBuilder().let { b ->
        repeat(n) { b.append("$printScriptMessage\n") }
        b.toString()
    }

    protected fun printScriptStdErr(n: Int) = StringBuilder().let { b ->
        repeat(n) { b.append("$printScriptErrorMessage\n") }
        b.toString()
    }

    protected fun printScriptOut(n: Int) = StringBuilder().let { b ->
        repeat(n) {
            b.append("$printScriptMessage\n")
            b.append("$printScriptErrorMessage\n")
        }
        b.toString()
    }
}
