package org.example.life

import com.jakewharton.rxrelay2.Relay
import io.reactivex.Observable
import javafx.scene.image.Image
import org.example.ui.base.AppState

interface LifeMap {

    fun setOnEndListener(channelForEnd: Relay<Long>)
    fun setOnUpdateScreenListener(channelForImage: Relay<Image>)
    fun setIterations(i: Int)
    fun generate(configuration: Configuration)
    fun play()
    fun pause()
    fun step()
    fun stopCount()

    fun setStyle(style: AppState.Style)

    fun paused(): Boolean

    companion object {
        const val MINIMAL_RENDER_TIME: Long = 25
    }
}