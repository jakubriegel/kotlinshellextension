package eu.jrie.jetbrains.kotlinshellextension.processes.process

import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.subjects.PublishSubject
import org.zeroturnaround.exec.stream.LogOutputStream

class ProcessOutputStream : LogOutputStream() {

    private val subject = PublishSubject.create<String>()
    private val tap = subject.replay().autoConnect()

    init {
        tap.subscribe()
    }

    override fun processLine(line: String?) {
        if (line != null) addToSubject(line)
    }

    private fun addToSubject(line: String) = subject.onNext("$line\n")

    fun subscribe(onLine: (String) -> Unit) {
        // TODO: handle errors and end
        tap.subscribeBy(
            onNext = onLine,
            onError = { println("error obs $it") },
            onComplete = { println("Done!") }
        )
    }
}
