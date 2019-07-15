package eu.jrie.jetbrains.kotlinshellextension

import eu.jrie.jetbrains.kotlinshellextension.processes.ProcessCommander
import eu.jrie.jetbrains.kotlinshellextension.processes.configuration.KtsProcessConfiguration
import eu.jrie.jetbrains.kotlinshellextension.processes.configuration.ProcessConfiguration
import eu.jrie.jetbrains.kotlinshellextension.processes.configuration.SystemProcessConfiguration
import eu.jrie.jetbrains.kotlinshellextension.processes.process.ProcessBuilder
import kotlinx.coroutines.runBlocking

private lateinit var commander: ProcessCommander

fun shell(script: ProcessCommander.() -> Unit) = runBlocking {
    if (!::commander.isInitialized) commander = ProcessCommander(this)
    commander
        .apply(script)
        .awaitAll()
}

fun systemProcess(config: SystemProcessConfiguration.() -> Unit) = SystemProcessConfiguration().apply(config).builder()

fun launchSystemProcess(config: SystemProcessConfiguration.() -> Unit) = process(SystemProcessConfiguration().apply(config))

fun launchKtsProcess(config: KtsProcessConfiguration.() -> Unit) = process(KtsProcessConfiguration().apply(config))

fun process(config: ProcessConfiguration) = process(config.builder())

fun process(builder: ProcessBuilder) = commander.process(builder)

fun ps() = println(commander.status())
