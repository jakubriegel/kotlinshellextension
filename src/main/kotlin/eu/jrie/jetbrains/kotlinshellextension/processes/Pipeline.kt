package eu.jrie.jetbrains.kotlinshellextension.processes

import eu.jrie.jetbrains.kotlinshellextension.processes.process.Process
import eu.jrie.jetbrains.kotlinshellextension.processes.process.ProcessBuilder
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import java.io.File

class Pipeline private constructor (
//    private val scope: CoroutineScope
    private val commander: ProcessCommander
) {

    val processLine = mutableListOf<Process>()

    internal fun toProcess(to: ProcessBuilder) = apply {
        processLine.add(
            commander.process(
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

    fun await() = runBlocking (commander.scope.coroutineContext) {
        processLine.forEach { it.await().join() }
    }

    companion object {
        internal fun from(start: ProcessBuilder, commander: ProcessCommander): Pipeline {
            start.followStdOut()

            val process = commander.process(start).apply { start() }
            val pipeline = Pipeline(commander)
            pipeline.processLine.add(process)
            return pipeline
        }

        internal fun fromFile(path: String, to: ProcessBuilder, commander: ProcessCommander) = fromFile(File(path), to, commander)

        internal fun fromFile(file: File, to: ProcessBuilder, commander: ProcessCommander) = from(to.followFile(file), commander)
    }

}
