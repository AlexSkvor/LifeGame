package org.example.life

import com.jakewharton.rxrelay2.Relay
import javafx.scene.image.Image
import javafx.scene.image.WritableImage
import org.example.alsoPrintDebug
import org.example.life.LifeMap.Companion.MINIMAL_RENDER_TIME

class CellMatrix() : Thread(), LifeMap {

    private lateinit var matrix: List<List<CellCommon>>
    private lateinit var config: Configuration

    private val width
        get() = config.width

    private val height
        get() = config.height

    lateinit var imageChannel: Relay<Image>
    override fun setOnUpdateScreenListener(channelForImage: Relay<Image>) {
        imageChannel = channelForImage
    }

    init {
        isDaemon = true
        start()
    }

    override fun generate(configuration: Configuration) {
        canContinue = false
        canStep = false
        canStart = false
        config = configuration
        matrix = MutableList(height) {
            MutableList(width) {
                create(config)
            }
        }

        matrix.forEachIndexed { i, line ->
            line.forEachIndexed { j, cell ->
                if (cell is CellCommon.Cell)
                    cell.setNeighbours(matrix.getNeighbours(height, width, i, j))
            }
        }
        canStart = true
    }

    private var canStart: Boolean = false
        @Synchronized set
        @Synchronized get

    private var canContinue: Boolean = false
        @Synchronized set
        @Synchronized get

    private var canStep: Boolean = false
        @Synchronized set
        @Synchronized get

    override fun play() {
        canContinue = true
    }

    override fun paused(): Boolean {
        return !(canContinue || canStep)
    }

    override fun pause() {
        canContinue = false
    }

    override fun step() {
        canStep = true
    }

    override fun run() {
        while (!canStart) sleep(100)
        imageChannel.accept(getBitmap())
        sleep(500)
        doForeverWithSleepAndTimeAndRenderCheck { //TODO parallel Impl
            matrix.forEachCell { it.countNextState(config) } // TODO config from each thread
            matrix.forEachCell { it.recalculateFields(config) }
            matrix.forEachCell { it.updateToNextState() }
            imageChannel.accept(getBitmap())
        }
    }

    private fun doForeverWithSleepAndTimeAndRenderCheck(action: () -> Unit) {
        var prevTime = System.currentTimeMillis()
        var shouldSleep = false
        while (true) {//TODO count steps, every species number, every mineral number
            while (!canContinue && !canStep) sleep(100)
            canStep = false

            if (shouldSleep) sleep(MINIMAL_RENDER_TIME)

            action.invoke()

            val newTime = System.currentTimeMillis()
            val timePassed = newTime - prevTime
            prevTime = newTime

            println("Current time $newTime; Millis passed $timePassed")
            shouldSleep = if (shouldSleep) (timePassed < MINIMAL_RENDER_TIME * 2)
            else (timePassed < MINIMAL_RENDER_TIME)
        }
    }

    private fun getBitmap(): Image {//TODO parallelImp
        val image = WritableImage(width, height)
        val writer = image.pixelWriter

        matrix.forEachCellIndexed { i, j, cell ->
            writer.setColor(j, i, cell.color(config)) //TODO mode species or minerals, config from each thread
        }

        return image
    }
}

private fun List<List<CellCommon>>.forEachCell(action: (CellCommon) -> Unit) {
    forEach { line ->
        line.forEach {
            action(it)
        }
    }
}

private fun List<List<CellCommon>>.forEachCellIndexed(action: (Int, Int, CellCommon) -> Unit) {
    forEachIndexed { i, line ->
        line.forEachIndexed { j, cell ->
            action(i, j, cell)
        }
    }
}