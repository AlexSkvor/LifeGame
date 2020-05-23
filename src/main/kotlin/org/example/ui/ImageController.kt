package org.example.ui

import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.stage.Stage
import org.example.alsoPrintDebug


class TestController {

    fun initialize() {
        alsoPrintDebug("BBBBBBBBBBBBB")
        Window2()
    }
}

class Window2{
    init {
        val loader = FXMLLoader(javaClass.getResource("/game_screen.fxml"))
        val stage = Stage()
        stage.title = "Игра \"Жизнь\""
        stage.scene = Scene(loader.load())
        stage.show()
    }
}