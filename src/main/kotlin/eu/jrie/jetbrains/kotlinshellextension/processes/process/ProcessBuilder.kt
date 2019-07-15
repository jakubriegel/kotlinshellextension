package eu.jrie.jetbrains.kotlinshellextension.processes.process

import eu.jrie.jetbrains.kotlinshellextension.processes.process.stream.NullProcessStream
import eu.jrie.jetbrains.kotlinshellextension.processes.process.stream.ProcessStream
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.io.File

abstract class ProcessBuilder  {

    protected var vPID: Int = -1

    var input: ProcessStream = NullProcessStream()
        protected set
    var stdout: ProcessStream = NullProcessStream()
        protected set
    var stderr: ProcessStream = NullProcessStream()
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
     * Will set given [File] as input of created [Process]
     *
     * @return this builder
     */
    fun followFile(file: File) = followIn(ProcessStream().fromFile(file))

    /**
     * Will redirect merged stdout and stderr of created [Process] to the new [ProcessStream]
     *
     * @return this builder
     */
    fun followMergedOut() = followMergedOut(ProcessStream())

    /**
     * Will redirect merged stdout and stderr of created [Process] to the given [ProcessStream]
     *
     * @return this builder
     */
    fun followMergedOut(stream: ProcessStream) = apply { stdout = stream }

    /**
     * Will redirect merged stdout and stderr of created [Process] to the given [File]
     *
     * @return this builder
     */
    @ExperimentalCoroutinesApi
    fun followMergedOut(file: File) = followMergedOut(ProcessStream().apply { invokeOnReady { subscribe(file) } } )

    /**
     * Will redirect separated stdout and stderr of created [Process] to 2 separated new [ProcessStream]s
     *
     * @return this builder
     */
    fun followOut() = apply {
        followStdOut()
        followStdErr()
    }

    /**
     * Will redirect separated stdout and stderr of created [Process] to given [ProcessStream]s
     *
     * @return this builder
     */
    fun followOut(stdStream: ProcessStream, errStream: ProcessStream) = apply {
        followStdOut(stdStream)
        followStdErr(errStream)
    }

    /**
     * Will redirect separated stdout and stderr of created [Process] to given [File]s
     *
     * @return this builder
     */
    @ExperimentalCoroutinesApi
    fun followOut(stdFile: File, errFile: File) = followOut(
        ProcessStream().apply { invokeOnReady { subscribe(stdFile) } },
        ProcessStream().apply { invokeOnReady { subscribe(errFile) } }
    )

    /**
     * Will redirect stdout of created [Process] to the new [ProcessStream]
     *
     * @return this builder
     */
    fun followStdOut() = followStdOut(ProcessStream())

    /**
     * Will redirect stdout of created [Process] to the given [ProcessStream]
     *
     * @return this builder
     */
    fun followStdOut(stream: ProcessStream) = apply { stdout = stream }

    /**
     * Will redirect stdout of created [Process] to the given [File]
     *
     * @return this builder
     */
    @ExperimentalCoroutinesApi
    fun followStdOut(file: File) = apply {
        followStdOut(ProcessStream().apply { invokeOnReady { subscribe(file) } })
    }

    /**
     * Will redirect stderr of created [Process] to the new [ProcessStream]
     *
     * @return this builder
     */
    fun followStdErr() = followStdErr(ProcessStream())

    /**
     * Will redirect stderr of created [Process] to the given [ProcessStream]
     *
     * @return this builder
     */
    fun followStdErr(stream: ProcessStream) = apply { stderr = stream }

    /**
     * Will redirect stderr of created [Process] to the given [File]
     *
     * @return this builder
     */
    @ExperimentalCoroutinesApi
    fun followStdErr(file: File) = apply {
        followStdErr(ProcessStream().apply { invokeOnReady { subscribe(file) } })
    }

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
