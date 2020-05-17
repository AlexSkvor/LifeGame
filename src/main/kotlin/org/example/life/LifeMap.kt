package org.example.life

import com.jakewharton.rxrelay2.Relay
import javafx.scene.image.Image

interface LifeMap {

    fun setOnUpdateScreenListener(channelForImage: Relay<Image>)
    fun startWork()
    fun getBitmap(): Image

    companion object {
        val MINIMAL_RENDER_TIME: Long
            get() = 30
    }
}