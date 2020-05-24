package org.example.ui.base

import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.stage.Stage
import javafx.stage.WindowEvent
import org.example.App
import org.example.alsoPrintDebug

abstract class UiController : DisposablesProvider by AppDisposables() {

    var stage: Stage? = null
        set(value) {
            field = value
            value?.setOnCloseRequest {
                close()
                parentController?.childClosed(value)
            }
        }

    private fun childClosed(child: Stage) {
        childrenStages.remove(child)
    }

    private var parentController: UiController? = null

    private val childrenStages: MutableList<Stage> = mutableListOf()

    abstract fun onViewCreated()
    abstract fun onClose()

    open fun onGlobalAction(it: GlobalAction) {
        if (it is GlobalAction.NewConfigurationChosen)
            stage?.closeWithCallBack()
    }

    fun initialize() {
        this::class.java.canonicalName.alsoPrintDebug("initialized")
        App.globalActions.subscribe { onGlobalAction(it) }.bind()
        onViewCreated()
    }

    protected fun createChildStage(layoutName: String, title: String) {
        require(layoutName.isNotBlank())
        require(layoutName.first() != '/')

        val loader = FXMLLoader(javaClass.getResource("/${layoutName}"))
        val stage = Stage()
        stage.title = title
        stage.scene = Scene(loader.load())
        stage.isResizable = false
        stage.show()

        childrenStages.add(stage)
        loader.getController<UiController>().stage = stage
        loader.getController<UiController>().parentController = this
    }

    private fun close() {
        onClose()
        childrenStages.toList().forEach { it.closeWithCallBack() }
        this::class.java.canonicalName.alsoPrintDebug("closed")
        clear()
    }
}

fun Stage.closeWithCallBack() {
    fireEvent(WindowEvent(this, WindowEvent.WINDOW_CLOSE_REQUEST))
}