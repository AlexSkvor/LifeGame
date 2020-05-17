package org.example.life

import com.jakewharton.rxrelay2.Relay
import javafx.scene.image.Image
import javafx.scene.image.WritableImage
import javafx.scene.paint.Color

class CellMatrix<C : Cell>(
    private val width: Int,
    private val height: Int,
    create: (x: Int, y: Int) -> C
) : Thread(), LifeMap {

    private val matrix: List<List<C>>

    private lateinit var relay: Relay<Image>

    init {
        matrix = MutableList(height) { i ->
            MutableList(width) { j ->
                create(i, j)
            }
        }

        matrix.forEachIndexed { i, line ->
            line.forEachIndexed { j, cell ->
                cell.setNeighbours(
                    listOfNotNull(
                        getTopLeftNeighbour(i, j),
                        getTopCenterNeighbour(i, j),
                        getTopRightNeighbour(i, j),
                        getRightNeighbour(i, j),
                        getBottomRightNeighbour(i, j),
                        getBottomCenterNeighbour(i, j),
                        getBottomLeftNeighbour(i, j),
                        getLeftNeighbour(i, j)
                    )
                )
            }
        }
        isDaemon = true
    }

    override fun setOnUpdateScreenListener(relay: Relay<Image>) {
        this.relay = relay
    }

    override fun startWork() {
        start()
    }

    override fun run() {
        relay.accept(getBitmap())
        sleep(1500)
        doForeverWithSleepAndTimeAndRenderCheck {
            matrix.forEachCell { it.countNextState() }
            matrix.forEachCell { it.updateNextState() }
            relay.accept(getBitmap())
        }
    }

    private fun doForeverWithSleepAndTimeAndRenderCheck(action: () -> Unit) {
        var prevTime = System.currentTimeMillis()
        var shouldSleep = false
        while (true) {
            if (shouldSleep) sleep(LifeMap.MINIMAL_RENDER_TIME)

            action.invoke()

            val newTime = System.currentTimeMillis()
            val timePassed = newTime - prevTime
            prevTime = newTime

            println("Current time $newTime; Millis passed $timePassed")
            shouldSleep = if (shouldSleep) (timePassed < LifeMap.MINIMAL_RENDER_TIME * 2)
            else (timePassed < LifeMap.MINIMAL_RENDER_TIME)
        }
    }

    override fun getBitmap(): Image {
        val image = WritableImage(width, height)
        val writer = image.pixelWriter

        matrix.forEachCellIndexed { i, j, cell ->
            writer.setColor(j, i, if (cell.alive) Color.GREEN else Color.WHITE)
        }

        return image
    }

    private fun getTopLeftNeighbour(i: Int, j: Int): Cell? =
        if (i == 0 || j == 0) null
        else matrix[i - 1][j - 1]

    private fun getTopCenterNeighbour(i: Int, j: Int): Cell? =
        if (i == 0) null
        else matrix[i - 1][j]

    private fun getTopRightNeighbour(i: Int, j: Int): Cell? =
        if (i == 0 || j == width - 1) null
        else matrix[i - 1][j + 1]


    private fun getBottomLeftNeighbour(i: Int, j: Int): Cell? =
        if (i == height - 1 || j == 0) null
        else matrix[i + 1][j - 1]

    private fun getBottomCenterNeighbour(i: Int, j: Int): Cell? =
        if (i == height - 1) null
        else matrix[i + 1][j]

    private fun getBottomRightNeighbour(i: Int, j: Int): Cell? =
        if (i == height - 1 || j == width - 1) null
        else matrix[i + 1][j + 1]


    private fun getLeftNeighbour(i: Int, j: Int): Cell? =
        if (j == 0) null
        else matrix[i][j - 1]

    private fun getRightNeighbour(i: Int, j: Int): Cell? =
        if (j == width - 1) null
        else matrix[i][j + 1]

    private fun List<List<C>>.forEachCell(action: (C) -> Unit) {
        forEach { line ->
            line.forEach {
                action(it)
            }
        }
    }

    private fun List<List<C>>.forEachCellIndexed(action: (Int, Int, C) -> Unit) {
        forEachIndexed { i, line ->
            line.forEachIndexed { j, cell ->
                action(i, j, cell)
            }
        }
    }
}