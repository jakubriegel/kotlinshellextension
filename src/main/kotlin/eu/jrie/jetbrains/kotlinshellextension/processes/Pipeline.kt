package eu.jrie.jetbrains.kotlinshellextension.processes

import eu.jrie.jetbrains.kotlinshellextension.process
import eu.jrie.jetbrains.kotlinshellextension.processes.process.Process
import eu.jrie.jetbrains.kotlinshellextension.processes.process.ProcessBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking

object Print
typealias print = Print

class Pipeline private constructor (
    private val scope: CoroutineScope
) {

    val processLine = mutableListOf<Process>()

    companion object {
        fun from(start: ProcessBuilder): Pipeline {
            start.followStdOut()

            val process = process(start).apply { start() }
            val pipeline = Pipeline(process.scope)
            pipeline.processLine.add(process)
            return pipeline
        }
    }

    fun await() = runBlocking (scope.coroutineContext) {
        processLine.forEach { it.await().join() }
    }

}

infix fun ProcessBuilder.pipe(sink: ProcessBuilder) = Pipeline.from(this) pipe sink

infix fun Pipeline.pipe(to: ProcessBuilder) = apply {
    processLine.add(
        process(
            to
                .followIn(processLine.last().stdout)
                .followStdOut()
        ).apply { start() }
    )
}

@ExperimentalCoroutinesApi
infix fun Pipeline.pipe(to: (Byte) -> Unit) = apply {
    processLine.last().stdout.subscribe(to)
}

@ExperimentalCoroutinesApi
@Suppress("UNUSED_PARAMETER")
infix fun Pipeline.pipe(to: Print) = pipe { print(it.toChar()) }
