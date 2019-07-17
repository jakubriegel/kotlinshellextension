package eu.jrie.jetbrains.kotlinshellextension.shell

import eu.jrie.jetbrains.kotlinshellextension.BaseIntegrationTest
import eu.jrie.jetbrains.kotlinshellextension.processes.process.Process
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.consumeEach
import kotlinx.io.core.BytePacketBuilder
import kotlinx.io.streams.readerUTF8
import org.junit.jupiter.api.BeforeEach

abstract class ProcessBaseIntegrationTest : BaseIntegrationTest() {
    private lateinit var resultBuilder: BytePacketBuilder

    protected val storeResult = { it: Byte -> resultBuilder.writeByte(it) }

    @ExperimentalCoroutinesApi
    protected suspend fun consumeResult(process: Process) {
        process.stdoutChannel.consumeEach(storeResult)
    }

    @BeforeEach
    fun initResultBuilder() {
        resultBuilder = BytePacketBuilder()
    }

    protected fun readResult() = resultBuilder.build().readerUTF8().readText()
}
