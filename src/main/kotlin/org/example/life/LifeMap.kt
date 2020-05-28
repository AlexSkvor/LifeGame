package org.example.life

import com.jakewharton.rxrelay2.Relay
import io.reactivex.Observable
import javafx.scene.image.Image
import org.example.ui.base.AppState

interface LifeMap {

    fun setOnEndListener(channelForEnd: Relay<Long>)
    fun setOnUpdateScreenListener(channelForImage: Relay<Image>)
    fun generate(configuration: Configuration)
    fun play()
    fun pause()
    fun step()

    val iterations: Observable<Long>

    fun setStyle(style: AppState.Style)

    fun paused(): Boolean

    companion object {
        const val MINIMAL_RENDER_TIME: Long = 25
    }
}