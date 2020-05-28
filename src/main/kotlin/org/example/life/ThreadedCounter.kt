package org.example.life

import java.util.concurrent.CyclicBarrier

class ThreadedCounter(
    private val matrix: List<List<CellCommon>>,
    private val config: Configuration
) {

    private var countNextCounter: Int = -1
        @Synchronized get
        @Synchronized set

    @Synchronized
    fun nextCountNextStateRawIndex(): Int {
        return if (countNextCounter > 0) {
            countNextCounter -= 1
            countNextCounter
        } else -1
    }

    val barrier: CyclicBarrier = CyclicBarrier(config.threadsNum + 1) {
        countNextCounter = config.height
    }

    private val threads: List<CountThread> = List(config.threadsNum) {
        CountThread(matrix, config.copy(), this)
    }

    init {
        threads.forEach {
            it.isDaemon = true
            it.start()
        }
    }

    fun step() {
        barrier.await()//start
        barrier.await()//counted next state
        barrier.await()//fields recalculated
        barrier.await()//state updated
    }

}