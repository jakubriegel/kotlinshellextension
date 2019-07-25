package eu.jrie.jetbrains.kotlinshellextension.shell

import eu.jrie.jetbrains.kotlinshellextension.processes.configuration.KtsProcessConfiguration
import eu.jrie.jetbrains.kotlinshellextension.processes.configuration.ProcessConfiguration
import eu.jrie.jetbrains.kotlinshellextension.processes.configuration.SystemProcessConfiguration
import eu.jrie.jetbrains.kotlinshellextension.processes.process.ProcessBuilder
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
interface ShellProcess : ShellBase {
    fun systemProcess(config: SystemProcessConfiguration.() -> Unit) = SystemProcessConfiguration()
        .apply(config)
        .configureBuilder()

    fun ktsProcess(config: KtsProcessConfiguration.() -> Unit) = KtsProcessConfiguration()
        .apply(config)
        .configureBuilder()

    private fun ProcessConfiguration.configureBuilder(): ProcessBuilder {
        env(environment.plus(variables))
        dir(directory)
        return builder()
    }

    fun launchSystemProcess(config: SystemProcessConfiguration.() -> Unit) = launchProcess(systemProcess(config))

    fun launchKtsProcess(config: KtsProcessConfiguration.() -> Unit) = launchProcess(ktsProcess(config))

    private fun launchProcess(builder: ProcessBuilder) = process(builder).also { commander.startProcess(it) }

    private fun process(builder: ProcessBuilder) = commander.createProcess(builder)

    fun ps() = println(commander.status())

}