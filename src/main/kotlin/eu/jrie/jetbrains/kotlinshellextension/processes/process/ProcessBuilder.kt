package eu.jrie.jetbrains.kotlinshellextension.processes.process

import eu.jrie.jetbrains.kotlinshellextension.processes.process.stream.NullProcessStream
import eu.jrie.jetbrains.kotlinshellextension.processes.process.stream.ProcessStream
import kotlinx.coroutines.CoroutineScope
import java.io.File

abstract class ProcessBuilder  {

    protected var vPID: Int = -1

    protected var input: ProcessStream = NullProcessStream()
    protected var stdout: ProcessStream = NullProcessStream()
    protected var stderr: ProcessStream = NullProcessStream()

    protected var directory = currentDir()

    protected val environment = mutableMapOf<String, String>()

    protected lateinit var scope: CoroutineScope

    /**
     * Sets virtual PID of created process to given value
     *
     * @return this builder
     */
    internal fun withVirtualPID(vPID: Int) = apply { this.vPID = vPID }

    /**
     * Will redirect the input of created process to the new [ProcessStream]
     *
     * @return this builder
     */
    fun followIn() = followIn(ProcessStream())

    /**
     * Will redirect the input of created process to the given [ProcessStream]
     *
     * @return this builder
     */
    fun followIn(source: ProcessStream) = apply { input = source }

    /**
     * Will redirect merged stdout and stderr of created process to the new [ProcessStream]
     *
     * @return this builder
     */
    fun followMergedOut() = followMergedOut(ProcessStream())

    /**
     * Will redirect merged stdout and stderr of created process to the given [ProcessStream]
     *
     * @return this builder
     */
    fun followMergedOut(destination: ProcessStream) = apply { stdout = destination }

    /**
     * Will redirect merged stdout and stderr of created process to 2 separated new [ProcessStream]s
     *
     * @return this builder
     */
    fun followOut() = apply {
        followStdOut()
        followStdErr()
    }

    /**
     * Will redirect merged stdout and stderr of created process to given [ProcessStream]s
     *
     * @return this builder
     */
    fun followOut(stdDestination: ProcessStream, errDestination: ProcessStream) = apply {
        followStdOut(stdDestination)
        followStdErr(errDestination)
    }

    /**
     * Will redirect stdout of created process to the new [ProcessStream]
     *
     * @return this builder
     */
    fun followStdOut() = followStdOut(ProcessStream())

    /**
     * Will redirect stdout of created process to the given [ProcessStream]
     *
     * @return this builder
     */
    fun followStdOut(destination: ProcessStream) = apply { stdout = destination }

    /**
     * Will redirect stderr of created process to the new [ProcessStream]
     *
     * @return this builder
     */
    fun followStdErr() = followStdErr(ProcessStream())

    /**
     * Will redirect stderr of created process to the given [ProcessStream]
     *
     * @return this builder
     */
    fun followStdErr(destination: ProcessStream) = apply { stderr = destination }

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
     * @param dir directory to execute the process from
     * @return this builder
     */
    fun withDir(dir: File) = apply {
        if (!dir.isDirectory) throw Exception("Process must be executed from directory")
        directory = dir
    }

    /**
     * Sets coroutine scope of created process to given scope
     *
     * @return this builder
     */
    internal fun withScope(scope: CoroutineScope) = apply { this.scope = scope }

    /**
     * Builds a process from this builder
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
