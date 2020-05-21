package org.example.life

import com.jakewharton.rxrelay2.Relay
import javafx.scene.image.Image

interface LifeMap {

    fun setOnUpdateScreenListener(channelForImage: Relay<Image>)
    fun generate(configuration: Configuration)
    fun play()
    fun pause()
    fun step()

    companion object {
        const val MINIMAL_RENDER_TIME: Long = 25
    }
}