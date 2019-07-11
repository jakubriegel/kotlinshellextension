package eu.jrie.jetbrains.kotlinshellextension

import eu.jrie.jetbrains.kotlinshellextension.processes.ProcessCommander
import eu.jrie.jetbrains.kotlinshellextension.processes.process.ProcessBuilder
import kotlinx.coroutines.runBlocking

private lateinit var commander: ProcessCommander

fun shell(script: ProcessCommander.() -> Unit) = runBlocking {
    if (!::commander.isInitialized) commander = ProcessCommander(this)
    commander
        .apply(script)
        .awaitAll()
}

fun process(builder: ProcessBuilder) = commander.process(builder)

fun ps() = println(commander.status())
