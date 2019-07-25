package eu.jrie.jetbrains.kotlinshellextension.shell

import eu.jrie.jetbrains.kotlinshellextension.processes.ProcessCommander
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.io.File

@ExperimentalCoroutinesApi
interface ShellBase {
    /**
     * Environment of this shell.
     * These variables are being inherited to sub shells.
     *
     * @see [variables]
     */
    val environment: Map<String, String>

    /**
     * Variables of this shell.
     * These variables are not being inherited to sub shells.
     *
     * @see [environment]
     */
    val variables: Map<String, String>

    /**
     * Current directory of this shell
     */
    val directory: File

    val commander: ProcessCommander
}
