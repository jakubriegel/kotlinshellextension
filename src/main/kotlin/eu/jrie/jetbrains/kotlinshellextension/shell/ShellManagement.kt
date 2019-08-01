package eu.jrie.jetbrains.kotlinshellextension.shell

import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.io.File

@ExperimentalCoroutinesApi
interface ShellManagement : ShellBase {

    /**
     * Changes directory to user root
     */
    fun cd() = cd(env("HOME"))

    /**
     * Changes directory to its parent
     */
    fun cd(up: Up) = cd(directory.parentFile)

    /**
     * Changes directory to given [path]
     */
    fun cd(path: String) = cd(File(path))

    /**
     * Changes directory to given [dir]
     */
    fun cd(dir: File)

    /**
     * Adds new shell variable
     */
    fun variable(variable: Pair<String, String>)

    /**
     * Adds new environment variable
     */
    fun export(env: Pair<String, String>)

    /**
     * Removes shell or environmental variable matching given key
     */
    fun unset(key: String)

    /**
     * Retrieves system environment variables
     *
     * @see [set]
     * @see [systemEnv]
     */
    val env: ShellExecutable get() = exec { systemEnv.toEnvString() }

    /**
     * Retrieves system environment variable matching given key
     *
     * @see [env]
     * @see [systemEnv]
     */
    fun env(key: String) = System.getenv(key) ?: ""

    /**
     * Retrieves all environment variables
     */
    val set: ShellExecutable get() = exec { shellEnv.toEnvString() }

    /**
     * Retrieves all environment variables and returns them as a [Map]
     */
    val shellEnv: Map<String, String>
        get() = systemEnv.plus(environment).plus(variables)

    /**
     * Retrieves system environment variables and returns them as a [Map]
     *
     * @see [set]
     * @see [env]
     */
    val systemEnv: Map<String, String>
        get() = System.getenv().toMap()

    private fun Map<String, String>.toEnvString() = StringBuilder().let { b ->
        forEach { (k, v) -> b.append("$k=$v\n") }
        b.toString()
    }
}

object Up
typealias up = Up
