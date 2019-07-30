@file:Suppress("EXPERIMENTAL_API_USAGE")

package eu.jrie.jetbrains.kotlinshellextension.shell.piping

import eu.jrie.jetbrains.kotlinshellextension.processes.pipeline.Pipeline
import eu.jrie.jetbrains.kotlinshellextension.shell.piping.from.ShellPipingFrom
import kotlinx.coroutines.ExperimentalCoroutinesApi

typealias PipeConfig =  suspend ShellPiping.() -> Pipeline

@ExperimentalCoroutinesApi
interface ShellPiping : ShellPipingFrom,
    ShellPipingThrough,
    ShellPipingTo {

    val pipelines: List<Pipeline>

    /**
     * Creates and executes new [Pipeline] specified by DSL [pipeConfig]
     * Part of piping DSL
     */
    suspend fun pipeline(pipeConfig: PipeConfig) {
        pipeConfig() pipe stdout await all
    }

    /**
     * Creates new [Pipeline] specified by DSL [pipeConfig] and executes it as detached job.
     * Part of piping DSL
     */
    suspend fun detach(pipeConfig: PipeConfig)

    /**
     * Awaits this [Pipeline]
     * Part of piping DSL
     *
     * @see Pipeline.await
     * @return this [Pipeline]
     */
    @Suppress("UNUSED_PARAMETER")
    @ExperimentalCoroutinesApi
    suspend infix fun Pipeline.await(all: All) = await()
}

/**
 * Object for [all] alias
 */
object All
/**
 * Alias to be used in piping DSL with [Pipeline.await]
 *
 * Ex: `p1 pipe p2 await all`
 *
 * @see ShellPiping
 * @see Pipeline
 */
typealias all = All
