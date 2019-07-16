package eu.jrie.jetbrains.kotlinshellextension.processes.process.stream

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.jetbrains.annotations.TestOnly
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

open class ProcessStream @TestOnly internal constructor(
//    protected val scope: CoroutineScope
    private val channel: Channel<Byte>
) : ProcessInputStream, ProcessOutputStream{

    constructor() : this(Channel(CHANNEL_BUFFER_SIZE))

    private lateinit var scope: CoroutineScope

    private val vPIDs = mutableListOf<Int>()

    private var onReady: ProcessStream.() -> Unit = {}

    val name: String
        get() = "[${this::class.simpleName} ${vPIDs.joinToString()}]"

    fun initialize(vPID: Int, scope: CoroutineScope) {
        vPIDs.add(vPID)
        if (!this::scope.isInitialized) this.scope = scope
        onReady()
    }

    fun invokeOnReady(action: ProcessStream.() -> Unit) {
        onReady = action
    }

    fun close() {
        channel.close()
        logger.debug("closed $name")
    }

    override fun write(line: String) = write(line.toByteArray())

    override fun writeAsLine(data: String) = writeAsLine(data.toByteArray())

    override fun write(line: ByteArray) = runBlocking (scope.coroutineContext) {
        line.forEach { write(it) }
    }

    override fun fromFile(file: File) = apply {
        invokeOnReady {
            write(file.readText())
            close()
        }
    }

    override fun writeAsLine(data: ByteArray) = write(data.plus(LINE_END.toByte()))

    override fun writeNewLine() = writeAsLine(ByteArray(0))

    override suspend fun write(b: Byte) = channel.send(b)

    override fun writeBlocking(b: Byte) = channel.sendBlocking(b)

    override fun read() = runBlocking (scope.coroutineContext) {
        channel.receive()
    }

    @ExperimentalCoroutinesApi
    override fun subscribe(onNext: (Byte) -> Unit) = subscribe(onNext, {})

    @ExperimentalCoroutinesApi
    override fun subscribe(onNext: (Byte) -> Unit, afterLast: () -> Unit): Job {
        val job = scope.launch { consume(onNext, afterLast) }
        logger.debug("subscribed to $name")
        return job
    }

    @ExperimentalCoroutinesApi
    override fun subscribe(file: File) = subscribe { file.bufferedWriter().write(it.toInt()) }

    @ExperimentalCoroutinesApi
    private suspend fun consume(onNext: (Byte) -> Unit, afterLast: () -> Unit) {
        channel.consumeEach { onNext(it) }
        logger.debug("consumed all of $name")
        afterLast()
    }

    companion object {
        const val CHANNEL_BUFFER_SIZE = 512
        const val LINE_END = '\n'

        @JvmStatic
        private val logger: Logger = LoggerFactory.getLogger(ProcessStream::class.java)
    }
}
