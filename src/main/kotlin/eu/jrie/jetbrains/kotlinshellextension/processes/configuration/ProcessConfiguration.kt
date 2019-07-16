package eu.jrie.jetbrains.kotlinshellextension.processes.configuration

import eu.jrie.jetbrains.kotlinshellextension.processes.process.ProcessBuilder
import eu.jrie.jetbrains.kotlinshellextension.processes.process.stream.ProcessStream
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.io.File

abstract class ProcessConfiguration {
    val environment = mutableMapOf<String, String>()

    fun env(env: Pair<String, String>) {
        environment[env.first] = env.second
    }

    fun env(env: Map<String, String>) {
        environment.putAll(env)
    }

    fun env(config: EnvironmentConfiguration.() -> Unit) {
        environment.putAll(
            EnvironmentConfiguration().apply(config).environment
        )
    }

    var configureInput: ProcessBuilder.() -> Unit = {}
        private set

    fun input(config: InputConfiguration.() -> Unit) {
        configureInput = InputConfiguration().apply(config).configure
    }

    var configureOutput: ProcessBuilder.() -> Unit = {}
        private set

    fun output(config: OutputConfiguration.() -> Unit) {
        configureOutput = OutputConfiguration().apply(config).configure
    }

    var configureDirectory: ProcessBuilder.() -> Unit = {}
        private set

    fun dir(dir: String) {
        configureDirectory = { withDir(dir) }
    }

    fun dir(dir: File) {
        configureDirectory = { withDir(dir) }
    }

    /**
     * Converts this configuration to [ProcessBuilder]
     */
    fun builder() = createBuilder().configure()

    /**
     * Creates [ProcessBuilder] and applies custom configurations on it
     */
    protected abstract fun createBuilder(): ProcessBuilder

    /**
     * Contains configurations common for all builders
     */
    private fun ProcessBuilder.configure() = apply {
        withEnv(environment)
        configureInput()
        configureOutput()
        configureDirectory()
    }

}

class EnvironmentConfiguration {
    val environment = mutableMapOf<String, String>()
    infix fun String.to(value: String) = environment.putIfAbsent(this, value)
}

abstract class InOutConfiguration {
    var configure: ProcessBuilder.() -> Unit = {}
        protected set

    protected fun config(action: ProcessBuilder.() -> Unit) {
        configure = action
    }
}

class InputConfiguration : InOutConfiguration() {
    fun follow() = config { followIn() }

    fun from(stream: ProcessStream) = config { followIn(stream) }

    fun from(file: File) = config { followFile(file) }

    fun set(input: String) = config {
        followIn(ProcessStream().apply {
            invokeOnReady {
                write(input)
                close()
            }
        })
    }

    fun set(input: ByteArray) = config {
        followIn(ProcessStream().apply {
            invokeOnReady {
                write(input)
                close()
            }
        })
    }

    fun set(input: Byte) = config {
        followIn(ProcessStream().apply {
            invokeOnReady {
                writeBlocking(input)
                close()
            }
        })
    }
}

class OutputConfiguration : InOutConfiguration() {
    fun follow() = config { followOut() }

    fun followTo(std: ProcessStream, err: ProcessStream) = config { followOut(std, err) }

    @ExperimentalCoroutinesApi
    fun followTo(stdFile: File, errFile: File) = config { followOut(stdFile, errFile) }

    fun followMerged() = config { followMergedOut() }

    fun followMergedTo(out: ProcessStream) = config { followMergedOut(out) }

    @ExperimentalCoroutinesApi
    fun followMergedTo(file: File) = config { followMergedOut(file) }

    fun followStd(): ProcessStream {
        val stream = ProcessStream()
        config { followStdOut(stream) }
        return stream
    }

    fun followStdTo(std: ProcessStream) = config { followStdOut(std) }

    @ExperimentalCoroutinesApi
    fun followStdTo(file: File) = config { followStdOut(file) }

    fun followErr(): ProcessStream {
        val stream = ProcessStream()
        config { followStdErr(stream) }
        return stream
    }

    fun followErrTo(err: ProcessStream) = config { followStdErr(err) }

    @ExperimentalCoroutinesApi
    fun followErrTo(file: File) = config { followStdErr(file) }

    @ExperimentalCoroutinesApi
    infix fun ProcessStream.andDo(subscribe: (Byte) -> Unit) = invokeOnReady { subscribe(subscribe) }
}
