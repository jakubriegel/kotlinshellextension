package eu.jrie.jetbrains.kotlinshellextension.processes.process

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.jetbrains.annotations.TestOnly

open class ProcessOutputStream @TestOnly internal constructor(
    private val scope: CoroutineScope,
    private val channel: Channel<Byte>
) {

    constructor(scope: CoroutineScope) : this(scope, Channel<Byte>(CHANNEL_BUFFER))

    fun send(line: String) = runBlocking (scope.coroutineContext) {
        line.plus(LINE_END)
            .map { it.toByte() }
            .forEach { send(it) }
    }

    suspend fun send(b: Byte) {
        channel.send(b)
    }

    @ExperimentalCoroutinesApi
    suspend fun subscribe(onNext: (Byte) -> Unit) = scope.launch {
        channel.consumeEach { onNext(it) }
    }

    fun close() {
        channel.close()
    }

    companion object {
        const val CHANNEL_BUFFER = 512
        private const val LINE_END = '\n'
    }
}
