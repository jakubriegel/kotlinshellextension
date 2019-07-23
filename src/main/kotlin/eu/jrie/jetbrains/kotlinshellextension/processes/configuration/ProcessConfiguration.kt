package eu.jrie.jetbrains.kotlinshellextension.processes.configuration

import eu.jrie.jetbrains.kotlinshellextension.processes.process.ProcessBuilder
import java.io.File

abstract class ProcessConfiguration {
    val environment = mutableMapOf<String, String>()

    @Deprecated("dev", ReplaceWith(""))
    fun env(env: Pair<String, String>) {
        environment[env.first] = env.second
    }

    fun env(env: Map<String, String>) = apply {
        environment.putAll(env)
    }

    @Deprecated("dev", ReplaceWith(""))
    fun env(config: EnvironmentConfiguration.() -> Unit) {
        environment.putAll(
            EnvironmentConfiguration().apply(config).environment
        )
    }

    var configureDirectory: ProcessBuilder.() -> Unit = {}
        private set
    @Deprecated("dev", ReplaceWith(""))
    fun dir(dir: String) {
        configureDirectory = { withDir(dir) }
    }

    fun dir(dir: File) = apply {
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
        configureDirectory()
    }

}

class EnvironmentConfiguration {
    val environment = mutableMapOf<String, String>()
    infix fun String.to(value: String) = environment.putIfAbsent(this, value)
}
