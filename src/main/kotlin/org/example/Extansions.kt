package org.example

import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import org.example.life.Configuration
import kotlin.random.Random

inline fun <reified T> T.alsoPrintDebug(msg: String) =
    also { println("$msg...$this") }

inline fun <reified T> T?.onNull(default: T): T = this ?: default

fun doNothing() = Unit

inline fun <reified T> Observable<T>.subscribeOnIo(): Observable<T> = this.subscribeOn(Schedulers.io())

inline fun <reified T> List<T>.randomOrder() = this.sortedBy { Random.nextInt() }

inline fun <reified T> Set<T>.randomOrder() = this.sortedBy { Random.nextInt() }

inline fun <reified T> MutableList<T>.dropAfter(index: Int) {
    for (i in size - 1 downTo index) removeAt(i)
}
