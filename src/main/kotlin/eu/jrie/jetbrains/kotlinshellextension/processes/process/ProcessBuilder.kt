package eu.jrie.jetbrains.kotlinshellextension.processes.process

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ReceiveChannel
import java.io.File

abstract class ProcessBuilder  {

    protected var vPID: Int = -1

    var input: ReceiveChannel<Byte>? = null
        protected set

    var directory = currentDir()
        protected set

    protected val environment = mutableMapOf<String, String>()
    fun environment() = environment.toMap()

    protected lateinit var scope: CoroutineScope

    /**
     * Sets virtual PID of created process to given value
     *
     * @return this builder
     */
    internal fun withVirtualPID(vPID: Int) = apply { this.vPID = vPID }

    /**
     * Will redirect the input of created process to the given [ProcessStream]
     *
     * @return this builder
     */
    internal fun followIn(source: ReceiveChannel<Byte>) = apply { input = source }

    /**
     * Will set given [File] as input of created [Process]
     *
     * @return this builder
     */
    internal fun followFile(file: File): Nothing = TODO()// followIn(ProcessStream().fromFile(file))

    /**
     * Adds new variable to the environment.
     * If the key was already present it overrides its value
     *
     * @return this builder
     */
    fun addEnv(env: Pair<String, String>) = apply {
        environment.put(env.first, env.second)
    }

    /**
     * Adds all given variables to the environment.
     * If a key was already present it overrides its value
     *
     * @return this builder
     */
    fun addEnv(env: Map<String, String>) = apply {
        environment.putAll(env)
    }

    /**
     * Replaces current environment with given variable
     *
     * @return this builder
     */
    fun withEnv(env: Pair<String, String>) = apply {
        environment.clear()
        addEnv(env)
    }

    /**
     * Replaces current environment with given variables
     *
     * @return this builder
     */
    fun withEnv(env: Map<String, String>) = apply {
        environment.clear()
        addEnv(env)
    }

    /**
     * Sets execution directory
     *
     * @param path absolute path to desired directory
     * @return this builder
     */
    fun withDir(path: String) = withDir(File(path))

    /**
     * Sets execution directory
     *
     * @param dir directory to execute the [Process] from
     * @return this builder
     */
    fun withDir(dir: File) = apply {
        if (!dir.isDirectory) throw Exception("Process must be executed from directory")
        directory = dir
    }

    /**
     * Sets coroutine scope of created [Process] to given scope
     *
     * @return this builder
     */
    internal fun withScope(scope: CoroutineScope) = apply { this.scope = scope }

    /**
     * Builds a [Process] from this builder
     *
     * @return new running [Process]
     */
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
