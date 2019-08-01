package eu.jrie.jetbrains.kotlinshellextension.shell.piping.from

import eu.jrie.jetbrains.kotlinshellextension.processes.process.ProcessChannel
import eu.jrie.jetbrains.kotlinshellextension.shell.piping.PipingBaseIntegrationTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.io.core.BytePacketBuilder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File
import java.io.OutputStream

@ExperimentalCoroutinesApi
class PipingFromFileIntegrationTest : PipingBaseIntegrationTest() {

    private lateinit var file: File

    @BeforeEach
    fun createFile() {
        file = file("content", content)
    }

    @Test
    fun `should start pipeline from file to process`() {
        // when
        shell {
            pipeline { file pipe "cat".process() pipe storeResult }
        }

        // then
        assertEquals("$content\n", readResult())
    }

    @Test
    fun `should start pipeline from file to lambda`() {
        // when
        shell {
            pipeline { file pipe storeResult }
        }

        // then
        assertContent()
    }

    @Test
    fun `should start pipeline from file to channel`() {
        // given
        val channel: ProcessChannel = Channel(16)

        // when
        shell {
            pipeline { file pipe channel }
        }

        // then
        assertContent(channel.read())
    }

    @Test
    fun `should start pipeline from file to packet builder`() {
        // given
        val builder = BytePacketBuilder()

        // when
        shell {
            pipeline { file pipe builder }
        }

        // then
        assertContent(builder.build().readText())
    }

    @Test
    fun `should start pipeline from file to stream`() {
        // given
        val result = mutableListOf<Char>()
        val outStream = object : OutputStream() {
            override fun write(b: Int) { result.add(b.toChar()) }
        }

        // when
        shell {
            pipeline { file pipe outStream }
        }

        // then
        assertContent(result.joinToString(separator = ""))
    }

    @Test
    fun `should start pipeline from file to file`() {
        // given
        val endFile = file()

        // when
        shell {
            pipeline { file pipe endFile }
        }

        // then
        assertContent(endFile.readText())
    }

    @Test
    fun `should start pipeline from file to file append`() {
        // given
        val fileContent = "def"
        val endFile = file(content = fileContent)

        // when
        shell {
            pipeline { file pipeAppend endFile }
        }

        // then
        assertEquals(fileContent.plus(content), endFile.readText())
    }


    @Test
    fun `should start pipeline from file to string builder`() {
        // given
        val builder = StringBuilder()

        // when
        shell {
            pipeline { file pipe builder }
        }

        // then
        assertContent(builder.toString())
    }

    @Test
    fun `should start pipeline from file with java like api`() {
        // when
        shell {
            from(file.inputStream())
                .throughLambda { storeResult(it) }
                .await()
        }

        // then
        assertContent()
    }
}
