package org.example

import com.github.thomasnield.rxkotlinfx.observeOnFx
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import javafx.fxml.FXML
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.image.PixelReader
import javafx.scene.image.WritableImage
import org.example.configuration_tests.ConfigTest
import org.example.life.CellMatrix
import org.example.life.LifeMap

class Controller {

    @FXML
    private lateinit var mapView: ImageView

    private val imageChannel = PublishRelay.create<Image>()

    fun initialize() {
        ConfigTest().printTestConfig()
        val config = ConfigTest().getTestConfig()
        val map: LifeMap = CellMatrix()

        map.setOnUpdateScreenListener(imageChannel)
        imageChannel.hide()
            .observeOnFx()
            .subscribe { updateImage(it) }.bind()

        map.generate(config)
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

    private fun Disposable.bind() {
        compositeDisposable.add(this)
    }

    fun clear() {
        compositeDisposable.clear()
    }

    private val compositeDisposable = CompositeDisposable()
}