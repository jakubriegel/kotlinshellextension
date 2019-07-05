package eu.jrie.jetbrains.kotlinshellextension.processes.process

class ProcessConfiguration {

    var command: String = ""
    private set
    var arguments: List<String> = emptyList()
    private set
    fun cmd(cmd: String) { command = cmd }
    fun cmd(config: ProcessConfiguration.() -> Unit) = config()
    infix fun String.withArgs(args: List<String>) {
        command = this
        arguments = args
    }
    infix fun String.withArg(arg: String) {
        command = this
        arguments = listOf(arg)
    }

    val environment = mutableMapOf<String, String>()
    fun env(config: EnvironmentConfiguration.() -> Unit) {
        environment.putAll(
            EnvironmentConfiguration().apply { config() }
                .environment
        )
    }

    var inputSource: ProcessInputStream? = null
    private set
    fun input(source: ProcessInputStream) { inputSource = source }

    var redirectOutput: Process.() -> Unit = {}
    private set
    fun output(config: OutputConfiguration.() -> Unit) {
        redirectOutput = OutputConfiguration()
            .apply { config() }
            .outputDestination
    }


}

class EnvironmentConfiguration {
    val environment = mutableMapOf<String, String>()
    infix fun String.to(value: String) = environment.putIfAbsent(this, value)
}

class OutputConfiguration {

    var outputDestination: Process.() -> Unit = {}
    private set

    fun mergeOutTo(destination: ProcessOutputStream) {
        outputDestination = { followMergedOut(destination) }
    }

    fun redirectOutTo(stdDestination: ProcessOutputStream, errDestination: ProcessOutputStream) {
        outputDestination = { followOut(stdDestination, errDestination) }
    }

    fun redirectStdOutTo(destination: ProcessOutputStream) {
        outputDestination = { followStdOut(destination) }
    }

    fun redirectStdErrTo(destination: ProcessOutputStream) {
        outputDestination = { followStdErr(destination) }
    }

}
