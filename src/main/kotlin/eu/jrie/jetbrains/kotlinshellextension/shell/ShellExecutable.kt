package eu.jrie.jetbrains.kotlinshellextension.shell

import eu.jrie.jetbrains.kotlinshellextension.processes.execution.Executable
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.io.core.BytePacketBuilder
import kotlinx.io.core.writeText

@ExperimentalCoroutinesApi
class ShellExecutable constructor(
    private val shell: Shell,
    private val block: Shell.() -> String
) : Executable(shell) {

    override suspend fun exec() = with(context) {
        stdout.send(
            BytePacketBuilder().let {
                it.writeText(shell.block())
                it.build()
            }
        )
    }
}
