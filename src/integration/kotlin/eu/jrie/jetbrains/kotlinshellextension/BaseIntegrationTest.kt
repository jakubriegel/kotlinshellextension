package eu.jrie.jetbrains.kotlinshellextension

import eu.jrie.jetbrains.kotlinshellextension.processes.ProcessCommander
import kotlinx.coroutines.runBlocking

abstract class BaseIntegrationTest {

    val COMMAND = "cmd"
    val ARGUMENT = "arg"
    val ARGUMENTS = listOf("arg1", "arg2", "arg3")

    val ENV_VAR_1 = "var1" to "val1"
    val ENV_VAR_2 = "var2" to "val2"
    val ENV_VAR_3 = "var3" to "val3"
    val ENVIRONMENT = mapOf(ENV_VAR_1, ENV_VAR_2, ENV_VAR_3)

    internal lateinit var commander: ProcessCommander

    internal fun <T> runTest(test: () -> T) = runBlocking {
        commander = ProcessCommander(this)
        test()
    }
}