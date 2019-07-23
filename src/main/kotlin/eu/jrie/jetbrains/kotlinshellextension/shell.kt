@file:Suppress("EXPERIMENTAL_API_USAGE")

package eu.jrie.jetbrains.kotlinshellextension

import eu.jrie.jetbrains.kotlinshellextension.Shell.Companion.logger
import eu.jrie.jetbrains.kotlinshellextension.processes.ProcessCommander
import eu.jrie.jetbrains.kotlinshellextension.processes.configuration.KtsProcessConfiguration
import eu.jrie.jetbrains.kotlinshellextension.processes.configuration.ProcessConfiguration
import eu.jrie.jetbrains.kotlinshellextension.processes.configuration.SystemProcessConfiguration
import eu.jrie.jetbrains.kotlinshellextension.processes.process.ProcessBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
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
    scope.launch { shell(env, dir, ProcessCommander(scope), script) }
    logger.debug("shell end")
}

@ExperimentalCoroutinesApi
suspend fun shell(
    env: Map<String, String>? = null,
    dir: File? = null,
    commander: ProcessCommander,
    script: ShellScript
) {
    Shell
        .build(env, dir, commander)
        .script()
    logger.debug("script end")
}

@ExperimentalCoroutinesApi
open class Shell private constructor (
    val environment: Map<String, String>,
    val directory: File,
    val commander: ProcessCommander
) : ShellPiping(commander) {

    fun systemProcess(config: SystemProcessConfiguration.() -> Unit) = SystemProcessConfiguration()
        .apply(config)
        .configureBuilder()

    fun ktsProcess(config: KtsProcessConfiguration.() -> Unit) = KtsProcessConfiguration()
        .apply(config)
        .configureBuilder()

    private fun ProcessConfiguration.configureBuilder(): ProcessBuilder {
        env(environment)
        dir(directory)
        return builder()
    }

    fun launchSystemProcess(config: SystemProcessConfiguration.() -> Unit) = launchProcess(systemProcess(config))

    fun launchKtsProcess(config: KtsProcessConfiguration.() -> Unit) = launchProcess(ktsProcess(config))

    private fun launchProcess(builder: ProcessBuilder) = process(builder).also { commander.startProcess(it) }

    private fun process(builder: ProcessBuilder) = commander.createProcess(builder)

    fun ps() = println(commander.status())

    suspend fun shell(
        env: Map<String, String> = emptyMap(),
        dir: File = directory,
        script: ShellScript
    ) = shell(env, dir, commander, script)

    companion object {

        fun build(env: Map<String, String>?, dir: File?, commander: ProcessCommander) = Shell(
            env ?: emptyMap(),
            assertDir(dir ?: currentDir()),
            commander
        )

        internal val logger = LoggerFactory.getLogger(Shell::class.java)

        private fun currentDir(): File {
            val path = System.getProperty("user.dir")
            return File(path)
        }

        private fun assertDir(dir: File) = dir.also { assert(it.isDirectory) }
    }
}
