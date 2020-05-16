package org.example

import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.image.Image
import javafx.stage.Stage

class App : Application() {

    override fun start(primaryStage: Stage) {
        val loader = FXMLLoader(javaClass.getResource("/sample.fxml"))
        openEmptyWindowWitTitle(loader, primaryStage)
        setIcon(primaryStage)
        primaryStage.setOnCloseRequest {
            val controller = loader.getController<Controller>()
        }
    }


    private fun openEmptyWindowWitTitle(loader: FXMLLoader, stage: Stage) {
        val root: Parent = loader.load()
        stage.maxHeight = 700.0
        stage.minHeight = 700.0
        stage.height = 700.0
        stage.minWidth = 900.0
        stage.maxWidth = 900.0
        stage.width = 900.0
        stage.title = "Игра Жизнь"
        stage.scene = Scene(root)
        stage.show()
    }

    private fun setIcon(stage: Stage) {
        //val iconStream = javaClass.getResourceAsStream("/icon.png")
        //val image = Image(iconStream)
        //stage.icons.add(image)
    }
}