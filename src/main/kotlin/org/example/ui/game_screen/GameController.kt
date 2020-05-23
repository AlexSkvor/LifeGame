package org.example.ui.game_screen

import com.github.thomasnield.rxkotlinfx.observeOnFx
import com.jakewharton.rxrelay2.PublishRelay
import javafx.fxml.FXML
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.image.PixelReader
import javafx.scene.image.WritableImage
import org.example.App
import org.example.alsoPrintDebug
import org.example.life.Configuration
import org.example.ui.base.UiController
import org.example.ui.main_screen.ConfigurationsLoader

class GameController : UiController() {

    @FXML
    private lateinit var mapView: ImageView

    private lateinit var config: Configuration

    private val imageChannel = PublishRelay.create<Image>()

    override fun onViewCreated() {
        @Suppress("ControlFlowWithEmptyBody")
        while (!App.lifeMap.paused());

        config = ConfigurationsLoader().getConfigByName(App.state.currentConfigurationName)
        App.lifeMap.setOnUpdateScreenListener(imageChannel)
        imageChannel.hide()
            .observeOnFx()
            .subscribe { updateImage(it) }.bind()
        App.lifeMap.generate(config)
        App.lifeMap.play()
    }

    override fun onClose() {
        App.lifeMap.pause()
    }

    private fun updateImage(image: Image) {
        val neededHeight = mapView.fitHeight
        val neededWidth = mapView.fitWidth

        val scaleHeight = neededHeight / image.height
        val scaleWidth = neededWidth / image.width

        mapView.image = resample(image, scaleWidth.toInt(), scaleHeight.toInt())
    }

    private fun resample(input: Image, scaleFactorWeight: Int, heightScaleFactor: Int): Image? {
        val weight = input.width.toInt()
        val height = input.height.toInt()
        val output = WritableImage(
            weight * scaleFactorWeight,
            height * heightScaleFactor
        )
        val reader: PixelReader = input.pixelReader
        val writer = output.pixelWriter
        for (y in 0 until height) {
            for (x in 0 until weight) {
                val argb = reader.getArgb(x, y)
                for (dy in 0 until heightScaleFactor) {
                    for (dx in 0 until scaleFactorWeight) {
                        writer.setArgb(x * scaleFactorWeight + dx, y * heightScaleFactor + dy, argb)
                    }
                }
            }
        }
        return output
    }

}