package eu.jrie.jetbrains.kotlinshellextension.shell

import eu.jrie.jetbrains.kotlinshellextension.processes.ProcessCommander
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.slf4j.LoggerFactory
import java.io.File
import java.io.InputStream
import java.io.OutputStream

@ExperimentalCoroutinesApi
suspend fun shell(
    env: Map<String, String>? = null,
    dir: File? = null,
    script: ShellScript
) = coroutineScope { shell(env, dir, this, script) }

@ExperimentalCoroutinesApi
suspend fun shell(
    env: Map<String, String>? = null,
    dir: File? = null,
    scope: CoroutineScope,
    script: ShellScript
) {
    scope.launch { shell(env, dir, ProcessCommander(scope), script) }
    logger.debug("shell end")
}

@ExperimentalCoroutinesApi
suspend fun shell(
    env: Map<String, String>? = null,
    dir: File? = null,
    commander: ProcessCommander,
    script: ShellScript
) {
    Shell
        .build(env, dir, commander)
        .script()
    logger.debug("script end")
}

@ExperimentalCoroutinesApi
open class Shell private constructor (
    environment: Map<String, String>,
    variables: Map<String, String>,
    directory: File,
    override val commander: ProcessCommander
) : ShellPiping, ShellProcess, ShellManagement, ExecutionContext() {

    override var environment: Map<String, String> = environment
        protected set

    override var variables: Map<String, String> = variables
        protected set

    override var directory: File = directory
        protected set

    override fun cd(dir: File) {
        directory = assertDir(dir).canonicalFile
    }

    override fun variable(variable: Pair<String, String>) {
        variables = variables.plus(variable)
    }

    override fun export(env: Pair<String, String>) {
        environment = environment.plus(env)
    }

    override fun unset(key: String) {
        variables = variables.without(key)
        environment = environment.without(key)
    }

    private fun Map<String, String>.without(key: String) = toMutableMap()
        .apply { remove(key) }
        .toMap()

    suspend fun shell(
        vars: Map<String, String> = emptyMap(),
        dir: File = directory,
        script: suspend Shell.() -> Unit
    ) = Shell(environment, vars, dir, commander).script()

    companion object {

        internal fun build(env: Map<String, String>?, dir: File?, commander: ProcessCommander) =
            Shell(
                env ?: emptyMap(),
                emptyMap(),
                assertDir(dir?.canonicalFile ?: currentDir()),
                commander
            )

        private fun currentDir(): File {
            val path = System.getProperty("user.dir")
            return File(path)
        }

        private fun assertDir(dir: File) = dir.also { assert(it.isDirectory) }

        internal val logger = LoggerFactory.getLogger(Shell::class.java)
    }
}
