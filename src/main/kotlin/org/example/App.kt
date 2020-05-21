package org.example

import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.stage.Stage

class App : Application() {

    override fun start(primaryStage: Stage) {
        val loader = FXMLLoader(javaClass.getResource("/sample.fxml"))
        openEmptyWindowWithTitle(loader, primaryStage)
        primaryStage.setOnCloseRequest {
            val controller = loader.getController<Controller>()
            controller.clear()
        }
    }

    private fun openEmptyWindowWithTitle(loader: FXMLLoader, stage: Stage) {
        val root: Parent = loader.load()
        stage.title = "Игра Жизнь"
        stage.scene = Scene(root)
        stage.show()
    }
}