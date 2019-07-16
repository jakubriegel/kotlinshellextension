package eu.jrie.jetbrains.kotlinshellextension

import eu.jrie.jetbrains.kotlinshellextension.Shell.Companion.logger
import eu.jrie.jetbrains.kotlinshellextension.processes.ProcessCommander
import eu.jrie.jetbrains.kotlinshellextension.processes.configuration.KtsProcessConfiguration
import eu.jrie.jetbrains.kotlinshellextension.processes.configuration.ProcessConfiguration
import eu.jrie.jetbrains.kotlinshellextension.processes.configuration.SystemProcessConfiguration
import eu.jrie.jetbrains.kotlinshellextension.processes.process.ProcessBuilder
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory

fun shell(script: suspend Shell.() -> Unit) {
    runBlocking {
        Shell(ProcessCommander(this)).script()
        logger.debug("script end")
    }
    logger.debug("shell end")
}

class Shell constructor (
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
