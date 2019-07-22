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

@ExperimentalCoroutinesApi
suspend fun shell(script: suspend Shell.() -> Unit) {
    coroutineScope {
        shell(this, script)
    }
    logger.debug("shell end")
}

@ExperimentalCoroutinesApi
suspend fun shell(scope: CoroutineScope, script: suspend Shell.() -> Unit) {
    scope.launch {
        val commander = ProcessCommander(this)
        Shell(commander).script()
        logger.debug("script end")
    }
}

@ExperimentalCoroutinesApi
open class Shell constructor (
    val commander: ProcessCommander
) : ShellPiping(commander) {
    fun systemProcess(config: SystemProcessConfiguration.() -> Unit) = SystemProcessConfiguration().apply(config).builder()

    fun launchSystemProcess(config: SystemProcessConfiguration.() -> Unit) = process(
        SystemProcessConfiguration().apply(config)
    ).apply { start() }

    fun launchKtsProcess(config: KtsProcessConfiguration.() -> Unit) = process(KtsProcessConfiguration().apply(config))

    fun process(config: ProcessConfiguration) = process(config.builder())

    fun process(builder: ProcessBuilder) = commander.process(builder)

    fun ps() = println(commander.status())

    companion object {
        internal val logger = LoggerFactory.getLogger(Shell::class.java)
    }
}
