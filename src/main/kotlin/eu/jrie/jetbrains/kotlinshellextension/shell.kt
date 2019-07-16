package eu.jrie.jetbrains.kotlinshellextension

import eu.jrie.jetbrains.kotlinshellextension.processes.Pipeline
import eu.jrie.jetbrains.kotlinshellextension.processes.ProcessCommander
import eu.jrie.jetbrains.kotlinshellextension.processes.configuration.KtsProcessConfiguration
import eu.jrie.jetbrains.kotlinshellextension.processes.configuration.ProcessConfiguration
import eu.jrie.jetbrains.kotlinshellextension.processes.configuration.SystemProcessConfiguration
import eu.jrie.jetbrains.kotlinshellextension.processes.process.ProcessBuilder
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import java.io.File

fun shell(script: Shell.() -> Unit) {
    runBlocking {
        Shell(ProcessCommander(this)).apply(script)
    }
}

class Shell constructor (
    private val commander: ProcessCommander
) {
    fun systemProcess(config: SystemProcessConfiguration.() -> Unit) = SystemProcessConfiguration().apply(config).builder()

    fun launchSystemProcess(config: SystemProcessConfiguration.() -> Unit) = process(
        SystemProcessConfiguration().apply(config)
    ).apply { start() }

    fun launchKtsProcess(config: KtsProcessConfiguration.() -> Unit) = process(KtsProcessConfiguration().apply(config))

    fun process(config: ProcessConfiguration) = process(config.builder())

    fun process(builder: ProcessBuilder) = commander.process(builder)

    fun ps() = println(commander.status())

    infix fun ProcessBuilder.pipe(to: ProcessBuilder) = Pipeline.from(this, commander) pipe to

    @ExperimentalCoroutinesApi
    infix fun ProcessBuilder.pipe(to: (Byte) -> Unit) = Pipeline.from(this, commander) pipe to

    @ExperimentalCoroutinesApi
    @Suppress("UNUSED_PARAMETER")
    infix fun ProcessBuilder.pipe(to: Print) = pipe { print(it.toChar()) }

    infix fun File.pipe(to: ProcessBuilder) = Pipeline.fromFile(this, to, commander)

    infix fun Pipeline.pipe(to: ProcessBuilder) = toProcess(to)

    @ExperimentalCoroutinesApi
    infix fun Pipeline.pipe(to: (Byte) -> Unit) = apply {
        processLine.last().stdout.subscribe(to)
        // TODO: implement KtsProcess
    }

    @ExperimentalCoroutinesApi
    @Suppress("UNUSED_PARAMETER")
    infix fun Pipeline.pipe(to: Print) = pipe { print(it.toChar()) }

    @ExperimentalCoroutinesApi
    infix fun Pipeline.pipe(to: File) = toFile(to)

    @ExperimentalCoroutinesApi
    infix fun Pipeline.append(to: File) = appendFile(to)
}

/**
 * Keyword for piping stdout to console. Should be used with alias [print]
 *
 * @see Pipeline
 */
object Print

/**
 * Alias keyword for piping stdout to console
 *
 * sample: `p1 pipe p2 pipe print`
 *
 * @see Pipeline
 */
typealias print = Print
