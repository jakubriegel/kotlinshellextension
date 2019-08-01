@file:Suppress("EXPERIMENTAL_API_USAGE")

package eu.jrie.jetbrains.kotlinshellextension.shell

import eu.jrie.jetbrains.kotlinshellextension.processes.ProcessCommander
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.coroutineScope
import java.io.File

typealias ShellScript = suspend Shell.() -> Unit

@ExperimentalCoroutinesApi
suspend fun shell(
    env: Map<String, String>? = null,
    dir: File? = null,
    script: ShellScript
) = coroutineScope { shell(env, dir, this, script) }

@ExperimentalCoroutinesApi
suspend fun shell(
    env: Map<String, String>? = null,
    dir: File? = null,
    scope: CoroutineScope,
    script: ShellScript
) {
    shell(env, dir, ProcessCommander(scope), script)
    Shell.logger.debug("shell end")
}

@ExperimentalCoroutinesApi
private suspend fun shell(
    env: Map<String, String>? = null,
    dir: File? = null,
    commander: ProcessCommander,
    script: ShellScript
) {
    Shell.build(env, dir, commander)
        .apply { script() }
        .finalize()
    Shell.logger.debug("script end")
}