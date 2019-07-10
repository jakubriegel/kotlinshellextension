package eu.jrie.jetbrains.kotlinshellextension.testutils

import eu.jrie.jetbrains.kotlinshellextension.processes.process.ProcessConfiguration
import eu.jrie.jetbrains.kotlinshellextension.processes.process.stream.ProcessInputStream
import eu.jrie.jetbrains.kotlinshellextension.processes.process.stream.ProcessOutputStream
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.CoroutineScope

object TestDataFactory {
    // constants
    const val VIRTUAL_PID = 1

    const val PROCESS_COMMAND = "some_cmd"
    val PROCESS_ARGS = listOf("arg1", "arg2", "arg3")

    val ENV_VAR_1 = "env1" to "value1"
    val ENV_VAR_2 = "env2" to "value2"
    val ENV_VAR_3 = "env3" to "value3"
    val ENVIRONMENT = mapOf(ENV_VAR_1, ENV_VAR_2, ENV_VAR_3)

    const val SYSTEM_PID: Long = 1001

    // mocks and spies
    val SCOPE_MOCK = mockk<CoroutineScope>()
    fun processInputStreamSpy() = spyk(ProcessInputStream(SCOPE_MOCK))
    fun processOutputStreamSpy() = spyk(ProcessOutputStream(SCOPE_MOCK))

    // other data
    fun processConfigFunction(): ProcessConfiguration.() -> Unit {
        return {
            cmd {
                PROCESS_COMMAND withArgs PROCESS_ARGS
            }
        }
    }
}