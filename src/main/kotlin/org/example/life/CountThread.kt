package org.example.life

class CountThread(
    private val matrix: List<List<CellCommon>>,
    private val config: Configuration,
    private val counter: ThreadedCounter
) : Thread() {

    override fun run() {
        while (true) {
            counter.barrier.await()
            doForCells { it.countNextState(config) } //расширение
            counter.barrier.await()
            doForCells { it.recalculateFields(config) } //сокращение часть 1
            counter.barrier.await()
            doForCells { it.updateToNextState() } //сокращение часть 2
            counter.barrier.await()
        }
    }

    private fun doForCells(action: (CellCommon) -> Unit) {
        var rowIndex = counter.nextCountNextStateRawIndex()
        while (rowIndex >= 0) {
            matrix[rowIndex].forEach { action(it) }
            rowIndex = counter.nextCountNextStateRawIndex()
        }
    }
}
