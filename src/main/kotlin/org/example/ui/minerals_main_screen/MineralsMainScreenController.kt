package org.example.ui.minerals_main_screen

import com.github.thomasnield.rxkotlinfx.itemSelections
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.ListView
import javafx.scene.control.TextFormatter
import javafx.scene.control.TextInputDialog
import org.example.life.Mineral
import org.example.ui.base.UiController
import java.util.function.UnaryOperator

class MineralsMainScreenController : UiController(), MineralsMainScreenView {

    @FXML
    private lateinit var mineralsListView: ListView<String>

    @FXML
    private lateinit var createNewMineralButton: Button

    private lateinit var combiner: MineralsMainScreenCombiner

    override fun onViewCreated() {
        combiner = MineralsMainScreenCombiner(this)
        mineralsListView.items = observableMinerals
        mineralsListView.itemSelections
            .subscribe {
                Platform.runLater { dialogRenamingOrDeleting(it) }
            }.bind()
    }

    override fun onClose() {
        if (::combiner.isInitialized) combiner.clear()
    }

    private val deleteMineralRelay = PublishRelay.create<String>()
    override fun deleteMineralIntent(): Observable<String> = deleteMineralRelay.hide()

    private val createMineralRelay = PublishRelay.create<String>()
    override fun createMineralIntent(): Observable<String> = createMineralRelay.hide()

    private val renameMineralRelay = PublishRelay.create<Pair<String, String>>()
    override fun renameMineralIntent(): Observable<Pair<String, String>> = renameMineralRelay.hide()

    override fun render(state: MineralsMainScreenViewState) {
        createNewMineralButton.setOnMouseClicked { dialogForAdding(state.config.minerals.size) }
        renderMineralsList(state.config.minerals)
    }

    private fun dialogForAdding(exists: Int) {
        val dialog = TextInputDialog("Ресурс${exists + 1}")
        dialog.title = "Создание ресурса"
        dialog.headerText = "Задайте имя ресурса (нельзя использовать слово \"Удалить\")"
        dialog.contentText = "Имя ресурса"
        dialog.editor.textFormatter = TextFormatter<TextFormatter.Change>(UnaryOperator { change ->
            if (change.text.matches("^[а-яА-ЯёЁa-zA-Z0-9]*\$".toRegex())) change
            else null
        })
        dialog.showAndWait().ifPresent {
            if (it.isNotBlank() && it.toLowerCase() != "удалить")
                createMineralRelay.accept(it)
        }
    }

    private fun dialogRenamingOrDeleting(mineralName: String) {
        val dialog = TextInputDialog(mineralName)
        dialog.title = "Изменение ресурса"
        dialog.headerText = "Задайте имя ресурса (или \"Удалить\" для удаления)"
        dialog.contentText = "Имя ресурса"
        dialog.editor.textFormatter = TextFormatter<TextFormatter.Change>(UnaryOperator { change ->
            if (change.text.matches("^[а-яА-ЯёЁa-zA-Z0-9\\s]*\$".toRegex())) change
            else null
        })
        dialog.showAndWait().ifPresent {
            if (it.isNotBlank()) {
                if (it.toLowerCase() == "удалить") deleteMineralRelay.accept(mineralName)
                else renameMineralRelay.accept(mineralName to it)
            }
        }
    }

    private val observableMinerals = FXCollections.observableArrayList<String>()
    private fun renderMineralsList(list: List<Mineral>) {
        if (observableMinerals.toList() != list.sortedBy { it.id }.map { it.name }) {
            observableMinerals.clear()
            observableMinerals.addAll(list.sortedBy { it.id }.map { it.name })
        }
    }
}