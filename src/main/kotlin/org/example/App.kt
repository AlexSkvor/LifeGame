package org.example

import com.jakewharton.rxrelay2.BehaviorRelay
import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.stage.Stage
import org.example.life.CellMatrix
import org.example.life.LifeMap
import org.example.ui.base.AppState
import org.example.ui.base.GlobalAction
import org.example.ui.base.UiController

class App : Application() {

    companion object {
        private val relay = BehaviorRelay.create<GlobalAction>()
        val globalActions = relay.hide().share()
        fun pushAction(action: GlobalAction) = relay.accept(action)
        val state = AppState()

        val lifeMap: LifeMap by lazy { CellMatrix() }
    }

    override fun start(primaryStage: Stage) {
        val loader = FXMLLoader(javaClass.getResource("/main_screen.fxml"))
        openEmptyWindowWithTitle(loader, primaryStage)
        loader.getController<UiController>().stage = primaryStage
    }

    private fun openEmptyWindowWithTitle(loader: FXMLLoader, stage: Stage) {
        val root: Parent = loader.load()
        stage.title = "Выбор конфигурации"
        stage.scene = Scene(root)
        stage.isResizable = false
        stage.show()
    }
}