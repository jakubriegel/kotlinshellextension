package eu.jrie.jetbrains.kotlinshellextension.processes.process

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.io.File

abstract class ProcessBuilder {

    protected var vPID: Int = -1

    var directory = currentDir()
        protected set

    protected val environment = mutableMapOf<String, String>()

    @ExperimentalCoroutinesApi
    var stdinBuffer: ProcessIOBuffer? = null
        protected set

    @ExperimentalCoroutinesApi
    var stdoutBuffer: ProcessIOBuffer? = null
        protected set

    @ExperimentalCoroutinesApi
    var stderrBuffer: ProcessIOBuffer? = null
        protected set

    protected lateinit var scope: CoroutineScope

    /**
     * Sets virtual PID of created process to given value
     *
     * @return this builder
     */
    internal fun withVirtualPID(vPID: Int) = apply { this.vPID = vPID }

    /**
     * Sets [buffer] as a source of [Process.stdin]
     *
     * @return this builder
     */
    @ExperimentalCoroutinesApi
    internal fun withStdinBuffer(buffer: ProcessIOBuffer) = apply {
        stdinBuffer = buffer
    }

    /**
     * Sets [buffer] as a destination of [Process.stdout]
     *
     * @return this builder
     */
    @ExperimentalCoroutinesApi
    internal fun withStdoutBuffer(buffer: ProcessIOBuffer) = apply {
        stdoutBuffer = buffer
    }

    /**
     * Sets [buffer] as a destination of [Process.stderr]
     *
     * @return this builder
     */
    @ExperimentalCoroutinesApi
    internal fun withStderrBuffer(buffer: ProcessIOBuffer) = apply {
        stderrBuffer = buffer
    }

    /**
     * Replaces current environment with given variables
     *
     * @return this builder
     */
    internal fun withEnv(env: Map<String, String>) = apply {
        environment.clear()
        environment.putAll(env)
    }

    /**
     * Sets execution directory
     *
     * @param dir directory to execute the [Process] fromBuffer
     * @return this builder
     */
    internal fun withDir(dir: File) = apply {
        if (!dir.isDirectory) throw Exception("Process must be executed fromBuffer directory")
        directory = dir
    }

    /**
     * Sets coroutine scope of created [Process] to given scope
     *
     * @return this builder
     */
    internal fun withScope(scope: CoroutineScope) = apply { this.scope = scope }

    /**
     * Builds a [Process] fromBuffer this builder
     *
     * @return new running [Process]
     */
    @ExperimentalCoroutinesApi
    internal abstract fun build(): Process

    companion object {
        /**
         * Returns current directory as a [File]
         */
        private fun currentDir(): File {
            val path = System.getProperty("user.dir")
            return File(path)
        }
    }
}
