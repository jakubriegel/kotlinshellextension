package eu.jrie.jetbrains.kotlinshellextension.shell.piping

import eu.jrie.jetbrains.kotlinshellextension.processes.execution.ProcessExecutable
import eu.jrie.jetbrains.kotlinshellextension.processes.pipeline.Pipeline
import eu.jrie.jetbrains.kotlinshellextension.processes.pipeline.PipelineContextLambda
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
interface ShellPipingThrough : ShellPipingTo {
    /**
     * Adds [process] to this pipeline.
     * Part of piping DSL
     *
     * @return this [Pipeline]
     */
    @ExperimentalCoroutinesApi
    infix fun Pipeline.pipe(process: ProcessExecutable) = throughProcess(process)

    /**
     * Adds [lambda] to this pipeline.
     * Part of piping DSL
     *
     * @return this [Pipeline]
     */
    @ExperimentalCoroutinesApi
    infix fun Pipeline.pipe(lambda: PipelineContextLambda) = throughLambda(lambda = lambda)
}
