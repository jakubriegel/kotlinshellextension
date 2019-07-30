@file:Suppress("EXPERIMENTAL_API_USAGE")

package eu.jrie.jetbrains.kotlinshellextension.shell.piping

import eu.jrie.jetbrains.kotlinshellextension.processes.pipeline.Pipeline
import eu.jrie.jetbrains.kotlinshellextension.shell.ExecutionMode
import eu.jrie.jetbrains.kotlinshellextension.shell.piping.from.ShellPipingFrom
import kotlinx.coroutines.ExperimentalCoroutinesApi

typealias PipeConfig =  suspend ShellPiping.() -> Pipeline

@ExperimentalCoroutinesApi
interface ShellPiping : ShellPipingFrom, ShellPipingThrough, ShellPipingTo {

    /**
     * List of all pipelines in this shell
     */
    val pipelines: List<Pipeline>

    /**
     * Creates and executes new [Pipeline] specified by DSL [pipeConfig]
     * Part of piping DSL
     */
    suspend fun pipeline(mode: ExecutionMode = ExecutionMode.ATTACHED, pipeConfig: PipeConfig) {
        when (mode) {
            ExecutionMode.ATTACHED -> pipeConfig().apply { if (!ended) toDefaultEndChannel(stdout) }
            ExecutionMode.DETACHED -> detach(pipeConfig)
            ExecutionMode.DAEMON -> TODO("implement daemon pipelines")
        }
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
