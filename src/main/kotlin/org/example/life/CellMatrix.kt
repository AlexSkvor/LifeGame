package org.example.life

import com.jakewharton.rxrelay2.Relay
import io.reactivex.Observable
import javafx.scene.image.Image
import javafx.scene.image.WritableImage
import org.example.alsoPrintDebug
import org.example.life.LifeMap.Companion.MINIMAL_RENDER_TIME
import org.example.ui.base.AppState
import java.util.concurrent.TimeUnit

class CellMatrix : Thread(), LifeMap {

    private lateinit var matrix: List<List<CellCommon>>
    private lateinit var config: Configuration

    private val width
        get() = config.width

    private val height
        get() = config.height

    private lateinit var imageChannel: Relay<Image>
    override fun setOnUpdateScreenListener(channelForImage: Relay<Image>) {
        imageChannel = channelForImage
    }

    override val iterations: Observable<Long>
        get() = Observable.interval(35, TimeUnit.MILLISECONDS)

    private lateinit var timeChannel: Relay<Long>
    override fun setOnEndListener(channelForEnd: Relay<Long>) {
        timeChannel = channelForEnd
    }

    init {
        isDaemon = true
        start()
    }

    private var style = AppState.Style.WATCH

    @Synchronized
    override fun setStyle(style: AppState.Style) {
        this.style = style
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

        threadedCounter = ThreadedCounter(config = config, matrix = matrix)

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
        canStep = false
    }

    override fun step() {
        canStep = true
    }

    private lateinit var threadedCounter: ThreadedCounter

    override fun run() {
        while (!canStart) sleep(100)
        imageChannel.accept(getBitmap())

        while (true) {
            while (!canContinue && !canStep) sleep(100)
            when (style) {
                AppState.Style.WATCH -> onWatchStyleAction()
                AppState.Style.TIME -> thousandInvocations()
            }
        }
    }

    private fun onWatchStyleAction() {
        var prevTime = System.currentTimeMillis()
        var shouldSleep = false

        while (true) {
            while (!canContinue && !canStep) sleep(100)
            canStep = false

            if (shouldSleep) sleep(MINIMAL_RENDER_TIME)

            threadedCounter.step()
            imageChannel.accept(getBitmap())

            val newTime = System.currentTimeMillis()
            val timePassed = newTime - prevTime
            prevTime = newTime

            println("Millis for last iteration $timePassed")
            shouldSleep = if (shouldSleep) (timePassed < MINIMAL_RENDER_TIME * 2)
            else (timePassed < MINIMAL_RENDER_TIME)
        }
    }

    private fun thousandInvocations() {
        var prevTime = System.currentTimeMillis()
        val superStartTime = System.currentTimeMillis()

        imageChannel.accept(getBitmap())

        for (i in 1..1000) {//TODO change from user!
            if (i % 10 == 0) i.alsoPrintDebug("step")
            threadedCounter.step()

            if (System.currentTimeMillis() - prevTime > 1000L) {
                imageChannel.accept(getBitmap())
                prevTime = System.currentTimeMillis()
            }
        }

        val totalTime = System.currentTimeMillis() - superStartTime
        println("TOTAL TIME $totalTime")
        pause()
        return timeChannel.accept(totalTime)
    }

    private fun getBitmap(): Image {
        val image = WritableImage(width, height)
        val writer = image.pixelWriter

        matrix.forEachIndexed { i, line ->
            line.forEachIndexed { j, cell ->
                writer.setColor(j, i, cell.color())
            }
        }

        return image
    }
}