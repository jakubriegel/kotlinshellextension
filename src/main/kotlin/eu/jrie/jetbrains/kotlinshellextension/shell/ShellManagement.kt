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
     */
    fun env() //= printEnv(systemEnv())
    = StringBuilder().let { b ->
        systemEnv().forEach { (k, v) -> b.append("$k=$v\n") }
        b.toString()
    }

    /**
     * Retrieves system environment variable matching given key
     *
     * @see [env]
     */
    fun env(key: String) = System.getenv(key) ?: ""

    /**
     * Retrieves all environment variables
     */
    fun set() = printEnv(systemEnv().plus(environment).plus(variables))

    private fun systemEnv() = System.getenv().toMap()

    private fun printEnv(env: Map<String, String>) {
        env.forEach { (k, v) -> println("$k=$v") }
    }
}

object Up
typealias up = Up

object Old
typealias old = Old
