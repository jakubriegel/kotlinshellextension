package eu.jrie.jetbrains.kotlinshellextension.processes

import eu.jrie.jetbrains.kotlinshellextension.process
import eu.jrie.jetbrains.kotlinshellextension.processes.process.Process
import eu.jrie.jetbrains.kotlinshellextension.processes.process.ProcessBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import java.io.File

class Pipeline private constructor (
    private val scope: CoroutineScope
) {

    val processLine = mutableListOf<Process>()

    internal fun toProcess(to: ProcessBuilder) = apply {
        processLine.add(
            process(
                to
                    .followIn(processLine.last().stdout)
                    .followStdOut()
            ).apply { start() }
        )
    }
    
    @ExperimentalCoroutinesApi
    internal fun toFile(to: File) = appendFile(
        to.apply { delete() }
    )

    @ExperimentalCoroutinesApi
    internal fun appendFile(to: File) = apply {
        processLine.last().stdout.subscribe {
            to.appendBytes(ByteArray(1) { _ -> it })
        }
    }

    fun await() = runBlocking (scope.coroutineContext) {
        processLine.forEach { it.await().join() }
    }

    companion object {
        internal fun from(start: ProcessBuilder): Pipeline {
            start.followStdOut()

            val process = process(start).apply { start() }
            val pipeline = Pipeline(process.scope)
            pipeline.processLine.add(process)
            return pipeline
        }

        internal fun fromFile(path: String, to: ProcessBuilder) = fromFile(File(path), to)

        internal fun fromFile(file: File, to: ProcessBuilder) = from(to.apply { followFile(file) })
    }

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

// start

infix fun ProcessBuilder.pipe(to: ProcessBuilder) = Pipeline.from(this) pipe to

@ExperimentalCoroutinesApi
infix fun ProcessBuilder.pipe(to: (Byte) -> Unit) = Pipeline.from(this) pipe to

@ExperimentalCoroutinesApi
@Suppress("UNUSED_PARAMETER")
infix fun ProcessBuilder.pipe(to: Print) = pipe { print(it.toChar()) }

infix fun File.pipe(to: ProcessBuilder) = Pipeline.fromFile(this, to)

// pipe

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
