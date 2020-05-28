package org.example.ui.game_screen

import com.github.thomasnield.rxkotlinfx.observeOnFx
import com.jakewharton.rxrelay2.PublishRelay
import javafx.fxml.FXML
import javafx.scene.control.Alert
import javafx.scene.control.Button
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.image.PixelReader
import javafx.scene.image.WritableImage
import javafx.scene.text.Text
import org.example.App
import org.example.life.Configuration
import org.example.ui.base.AppState
import org.example.ui.base.UiController
import org.example.ui.base.closeWithCallBack
import org.example.ui.main_screen.ConfigurationsLoader

class GameController : UiController() {

    @FXML
    private lateinit var mapView: ImageView

    @FXML
    private lateinit var playButton: Button

    @FXML
    private lateinit var pauseButton: Button

    @FXML
    private lateinit var stepButton: Button

    @FXML
    private lateinit var timeInfo: Text

    private lateinit var config: Configuration

    private val imageChannel = PublishRelay.create<Image>()
    private val timeRelay = PublishRelay.create<Long>()

    override fun onViewCreated() {
        @Suppress("ControlFlowWithEmptyBody")
        while (!App.lifeMap.paused());

        timeInfo.text = "Экран открыт в режиме замера времени!\n" +
                "Пожалуйста, не закрывайте экран до \n" +
                " окончания расчетов\n" +
                "Будет подсчитано ${App.state.iterations} итераций\n\n" +
                "В целях экономии процессорного времени,\n" +
                " изображение на экране будет обновляться\n" +
                " не чаще 1 раза в секунду\n\n" +
                "После завершения работы экран предоставит\n" +
                " результаты!"

        config = ConfigurationsLoader().getConfigByName(App.state.currentConfigurationName)
        imageChannel.hide()
            .observeOnFx()
            .subscribe { updateImage(it) }.bind()
        timeRelay.hide()
            .observeOnFx()
            .subscribe { timePassed(it) }.bind()
        App.lifeMap.setOnUpdateScreenListener(imageChannel)
        App.lifeMap.setOnEndListener(timeRelay)

        App.lifeMap.setStyle(App.state.gameStyle)
        App.lifeMap.generate(config)

        when (App.state.gameStyle) {
            AppState.Style.WATCH -> {
                App.lifeMap.step()
                playButton.setOnMouseClicked { App.lifeMap.play() }
                pauseButton.setOnMouseClicked { App.lifeMap.pause() }
                stepButton.setOnMouseClicked {
                    if (App.lifeMap.paused())
                        App.lifeMap.step()
                }
                timeInfo.isVisible = false
            }
            AppState.Style.TIME -> {
                App.lifeMap.setIterations(App.state.iterations)
                App.lifeMap.play()
                playButton.isVisible = false
                pauseButton.isVisible = false
                stepButton.isVisible = false
                timeInfo.isVisible = true
            }
        }
    }

    override fun onClose() {
        App.lifeMap.stopCount()
    }

    private fun timePassed(time: Long) {
        val alert = Alert(Alert.AlertType.INFORMATION)
        alert.title = "Итоги замеров ${App.state.iterations} итераций"
        alert.contentText = "На карте размером ${config.width}*${config.height}\n" +
                "Вычисления при ${config.threadsNum} потоках\n" +
                "Заняли $time миллисекунд!"
        alert.setOnCloseRequest {
            stage?.closeWithCallBack()
        }
        alert.showAndWait()
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