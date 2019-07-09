package eu.jrie.jetbrains.kotlinshellextension.processes.process.stream

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.jetbrains.annotations.TestOnly

class ProcessOutputStream @TestOnly internal constructor(
    scope: CoroutineScope,
    channel: Channel<Byte>
) : ProcessStream(scope, channel){

    constructor(scope: CoroutineScope) : this(scope, Channel<Byte>(CHANNEL_BUFFER_SIZE))

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
        subscribe(onNext, {})
    }

    @ExperimentalCoroutinesApi
    suspend fun subscribe(onNext: (Byte) -> Unit, afterLast: () -> Unit) {
        scope.launch { consume(onNext, afterLast) }
        logger.debug("subscribed to $name")
    }

    @ExperimentalCoroutinesApi
    private suspend fun consume(onNext: (Byte) -> Unit, afterLast: () -> Unit) {
        channel.consumeEach { onNext(it) }
        logger.debug("consumed all of $name")
        afterLast()
    }

}
