package eu.jrie.jetbrains.kotlinshellextension.processes.process.stream

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import org.jetbrains.annotations.TestOnly
import org.slf4j.Logger
import org.slf4j.LoggerFactory

abstract class ProcessStream @TestOnly internal constructor(
    protected val scope: CoroutineScope,
    protected val channel: Channel<Byte>
) {
    var vPID = -1
        internal set

    val name: String
        get() = "${this::class.simpleName}_$vPID"

    fun close() {
        channel.close()
        onClose()
        logger.debug("closed $name")
    }

    protected open fun onClose() {}

    companion object {
        const val CHANNEL_BUFFER_SIZE = 512
        const val LINE_END = '\n'

        @JvmStatic
        protected val logger: Logger = LoggerFactory.getLogger(ProcessStream::class.java)
    }
}
