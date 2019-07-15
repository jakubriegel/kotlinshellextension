package eu.jrie.jetbrains.kotlinshellextension.shell

import eu.jrie.jetbrains.kotlinshellextension.BaseIntegrationTest
import eu.jrie.jetbrains.kotlinshellextension.launchSystemProcess
import eu.jrie.jetbrains.kotlinshellextension.shell
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.io.core.BytePacketBuilder
import kotlinx.io.streams.readerUTF8
import org.junit.jupiter.api.Test

class ProcessIntegrationTest : BaseIntegrationTest() {

    @Test
    @ExperimentalCoroutinesApi
    fun `should execute "ls -l"`() {
        // given
        val result = BytePacketBuilder()

        file("file1")
        file("file2")
        dir()

        val dirRegex = Regex("drw.+testdir\n")
        val fileRegex = Regex("-rw.+file[0-9]\n")

        // when
        shell {
            launchSystemProcess {
                cmd {
                    "ls" withArg "-l"
                }
                output {
                    followStd() andDo { result.writeByte(it) }
                }
                dir(directory)
            }
        }

        // then
        val output = result.build().readerUTF8().readText()
        print(output)
        println()
        assertRegex(dirRegex, output)
        assertRegex(fileRegex, output)
    }

}
