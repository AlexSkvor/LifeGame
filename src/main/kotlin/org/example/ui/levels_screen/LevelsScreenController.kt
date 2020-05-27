package org.example.ui.levels_screen

import com.github.thomasnield.rxkotlinfx.itemSelections
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.ListView
import javafx.scene.control.TextFormatter
import javafx.scene.control.TextInputDialog
import org.example.clicks
import org.example.filterDigits
import org.example.onNull
import org.example.ui.base.UiController
import org.example.ui.common.UiMineral
import org.example.ui.common.dialogChangeMineralValue
import org.example.ui.common.toUiMineralsList
import java.util.function.UnaryOperator

class LevelsScreenController : UiController(), LevelsScreenView {

    @FXML
    private lateinit var createNewLevelButton: Button

    @FXML
    private lateinit var deleteLevelButton: Button

    @FXML
    private lateinit var additionalSeeds: Button

    @FXML
    private lateinit var levelsListView: ListView<String>
    private val observableLevels = FXCollections.observableArrayList<String>()

    @FXML
    private lateinit var forUpdateListView: ListView<UiMineral>
    private val observableForUpdate = FXCollections.observableArrayList<UiMineral>()

    @FXML
    private lateinit var neededMineralsListView: ListView<UiMineral>
    private val observableNeed = FXCollections.observableArrayList<UiMineral>()

    @FXML
    private lateinit var dropMineralsListView: ListView<UiMineral>
    private val observableDrop = FXCollections.observableArrayList<UiMineral>()

    override fun addLevelIntent(): Observable<Unit> = createNewLevelButton.clicks()

    override fun deleteLevelIntent(): Observable<Unit> = deleteLevelButton.clicks()

    override fun selectLevelIntent(): Observable<Int> = levelsListView.itemSelections
        .map { it.filterDigits().toIntOrNull().onNull(0) }

    private val changeSeedsDropChanceRelay = PublishRelay.create<Int>()
    override fun changeSeedsDropChanceIntent(): Observable<Int> = changeSeedsDropChanceRelay.hide()

    private val newMineralNeedRelay = PublishRelay.create<Pair<Int, Int>>()
    override fun newMineralNeedIntent(): Observable<Pair<Int, Int>> = newMineralNeedRelay.hide()

    private val newMineralDropRelay = PublishRelay.create<Pair<Int, Int>>()
    override fun newMineralDropIntent(): Observable<Pair<Int, Int>> = newMineralDropRelay.hide()

    private val newMineralForUpdateRelay = PublishRelay.create<Pair<Int, Int>>()
    override fun newMineralForUpdateIntent(): Observable<Pair<Int, Int>> = newMineralForUpdateRelay.hide()

    private lateinit var combiner: LevelsScreenCombiner

    override fun onViewCreated() {
        combiner = LevelsScreenCombiner(this)
        levelsListView.items = observableLevels
        forUpdateListView.items = observableForUpdate
        neededMineralsListView.items = observableNeed
        dropMineralsListView.items = observableDrop

        forUpdateListView.itemSelections
            .subscribe {
                Platform.runLater {
                    dialogChangeMineralValue(it, "Потребление ресурса ${it.name} для повышения уровня") { value ->
                        newMineralForUpdateRelay.accept(it.id to value)
                    }
                }
            }.bind()

        neededMineralsListView.itemSelections
            .subscribe {
                Platform.runLater {
                    dialogChangeMineralValue(it, "Потребление ресурса ${it.name}") { value ->
                        newMineralNeedRelay.accept(it.id to value)
                    }
                }
            }.bind()

        dropMineralsListView.itemSelections
            .subscribe {
                Platform.runLater {
                    dialogChangeMineralValue(it, "Выработка ресурса ${it.name}") { value ->
                        newMineralDropRelay.accept(it.id to value)
                    }
                }
            }.bind()
    }

    override fun onClose() {
        if (::combiner.isInitialized) combiner.clear()
    }

    override fun render(state: LevelScreenViewState) {
        additionalSeeds.text = "Доп. шанс семян: ${state.currentLevel.additionalSeedsDroppedChance}"
        additionalSeeds.setOnMouseClicked {
            prepareDialogAdditionalSeeds(state.currentLevel.additionalSeedsDroppedChance)
        }

        observableLevels.renderList(state.levels.mapIndexed { i, _ -> "Уровень $i" })
        observableForUpdate.renderList(state.currentLevel.neededMineralsForUpgradeToNext.toUiMineralsList(state.config.minerals))
        observableDrop.renderList(state.currentLevel.additionalMineralsDropped.toUiMineralsList(state.config.minerals))
        observableNeed.renderList(state.currentLevel.additionalMineralsForStayAlive.toUiMineralsList(state.config.minerals))
    }

    private inline fun <reified T> ObservableList<T>.renderList(newList: List<T>) {
        if (toList() != newList) {
            clear()
            addAll(newList)
        }
    }

    private fun prepareDialogAdditionalSeeds(startValue: Int) {
        val dialog = TextInputDialog(startValue.toString())
        dialog.title = "Задайте шанс"
        dialog.contentText = "Дополнительный шанс выпадения семени на этом уровне: "
        dialog.editor.textFormatter = TextFormatter<TextFormatter.Change>(UnaryOperator { change ->
            if (change.text.matches("^[0-9]*\$".toRegex())) change
            else null
        })
        dialog.showAndWait().ifPresent { str ->
            str.toIntOrNull()?.let {
                if (it in 0..100) changeSeedsDropChanceRelay.accept(it)
            }
        }
    }
}