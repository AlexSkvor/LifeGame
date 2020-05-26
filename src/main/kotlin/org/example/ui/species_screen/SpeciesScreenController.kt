package org.example.ui.species_screen

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
import javafx.scene.paint.Color
import org.example.alsoPrintDebug
import org.example.clicks
import org.example.life.Mineral
import org.example.ui.base.UiController
import java.util.function.UnaryOperator

class SpeciesScreenController : UiController(), SpeciesScreenView {

    @FXML
    private lateinit var createNewSpeciesButton: Button

    @FXML
    private lateinit var chosenSpeciesName: Button

    @FXML
    private lateinit var deleteSpecies: Button

    @FXML
    private lateinit var colorButton: Button

    @FXML
    private lateinit var alwaysDroppedSeedsChanceButton: Button

    @FXML
    private lateinit var mainFieldMultiplierDropAndWasteButton: Button

    @FXML
    private lateinit var levelsButton: Button //TODO

    @FXML
    private lateinit var speciesListView: ListView<String>
    private val observableSpecies = FXCollections.observableArrayList<String>()

    @FXML
    private lateinit var needMineralsListView: ListView<UiMineral>
    private val observableNeedMinerals = FXCollections.observableArrayList<UiMineral>()

    @FXML
    private lateinit var dropMineralsListView: ListView<UiMineral>
    private val observableDropMinerals = FXCollections.observableArrayList<UiMineral>()

    private lateinit var combiner: SpeciesScreenCombiner

    private val createSpeciesRelay = PublishRelay.create<String>()
    override fun createSpeciesIntent(): Observable<String> = createSpeciesRelay.hide()

    override fun deleteSpeciesIntent(): Observable<Unit> = deleteSpecies.clicks()

    override fun speciesChosenIntent(): Observable<String> = speciesListView.itemSelections

    private val renameRelay = PublishRelay.create<String>()
    override fun renameSpeciesIntent(): Observable<String> = renameRelay.hide()

    private val colorRelay = PublishRelay.create<Color>()
    override fun changeColorSpeciesIntent(): Observable<Color> = colorRelay.hide()

    private val alwaysDroppedSeedsChanceRelay = PublishRelay.create<Int>()
    override fun newAlwaysDroppedSeedsChanceIntent(): Observable<Int> = alwaysDroppedSeedsChanceRelay.hide()

    private val mainFieldMultiplierDropAndWasteRelay = PublishRelay.create<Int>()
    override fun newMainFieldMultiplierDropAndWasteIntent(): Observable<Int> =
        mainFieldMultiplierDropAndWasteRelay.hide()

    private val newMineralNeedRelay = PublishRelay.create<Pair<Int, Int>>()
    override fun newMineralNeedIntent(): Observable<Pair<Int, Int>> = newMineralNeedRelay.hide()

    private val newMineralDropRelay = PublishRelay.create<Pair<Int, Int>>()
    override fun newMineralDropIntent(): Observable<Pair<Int, Int>> = newMineralDropRelay.hide()

    override fun onViewCreated() {
        combiner = SpeciesScreenCombiner(this)
        speciesListView.items = observableSpecies
        needMineralsListView.items = observableNeedMinerals
        dropMineralsListView.items = observableDropMinerals

        createNewSpeciesButton.setOnMouseClicked {
            dialog(
                startValue = "Новая Форма Жизни",
                title = "Создание формы Жизни",
                hint = "Имя для новой Жизни",
                regexp = "^[а-яА-ЯёЁa-zA-Z0-9]*\$".toRegex()
            ) {
                if (it.isNotBlank()) createSpeciesRelay.accept(it)
            }
        }

        needMineralsListView.itemSelections
            .subscribe {
                Platform.runLater {
                    dialogChangeMineralValue(it, "Базовое потребление ресурса ${it.name}") { value ->
                        newMineralNeedRelay.accept(it.id to value)
                    }
                }
            }.bind()

        dropMineralsListView.itemSelections
            .subscribe {
                Platform.runLater {
                    dialogChangeMineralValue(it, "Базовая выработка ресурса ${it.name}") { value ->
                        newMineralDropRelay.accept(it.id to value)
                    }
                }
            }.bind()
    }

    private fun dialogChangeMineralValue(uiMineral: UiMineral, hint: String, onChange: (Int) -> Unit) {
        val dialog = TextInputDialog(uiMineral.number.toString())
        dialog.title = "Изменение ресурса"
        dialog.headerText = hint
        dialog.contentText = "Количество ресурса"
        dialog.editor.textFormatter = TextFormatter<TextFormatter.Change>(UnaryOperator { change ->
            if (change.text.matches("^[0-9]*\$".toRegex())) change
            else null
        })
        dialog.showAndWait().ifPresent { str ->
            str.toIntOrNull()?.let { onChange(it) }
        }
    }

    override fun onClose() {
        if (::combiner.isInitialized) combiner.clear()
    }

    override fun render(state: SpeciesScreenViewState) {

        chosenSpeciesName.text = "Название: " + state.currentSpecies.name
        chosenSpeciesName.setOnMouseClicked {
            dialog(
                startValue = state.currentSpecies.name,
                title = "Смена имени формы Жизни",
                hint = "Задайте имя",
                regexp = "^[а-яА-ЯёЁa-zA-Z0-9]*\$".toRegex()
            ) {
                if (it.isNotBlank()) renameRelay.accept(it)
            }
        }


        colorButton.text = "Цвет: " + state.currentSpecies.color.stringColor()
        colorButton.style = "-fx-font: 14 arial; -fx-base: ${state.currentSpecies.color.stringColor()};"
        colorButton.setOnMouseClicked {
            dialog(
                startValue = state.currentSpecies.color.stringColor(),
                title = "Смена цвета формы Жизни",
                hint = "Задайте цвет",
                regexp = "^[a-fA-F0-9#]*\$".toRegex()
            ) {
                it.toColor()?.let { color -> colorRelay.accept(color) }
            }
        }

        alwaysDroppedSeedsChanceButton.text = "Шанс выпадения семени: " +
                state.currentSpecies.alwaysDroppedSeedsChance
        alwaysDroppedSeedsChanceButton.setOnMouseClicked {
            dialog(
                startValue = state.currentSpecies.alwaysDroppedSeedsChance.toString(),
                title = "Смена базового шанса выпадения семени",
                hint = "Задайте шанс",
                regexp = "^[0-9]*\$".toRegex()
            ) {
                it.toIntOrNull()?.let { chance ->
                    if (chance in 0..100)
                        alwaysDroppedSeedsChanceRelay.accept(chance)
                }
            }
        }

        mainFieldMultiplierDropAndWasteButton.text = "Множитель своей клетки: " +
                state.currentSpecies.mainFieldMultiplierDropAndWaste
        mainFieldMultiplierDropAndWasteButton.setOnMouseClicked {
            dialog(
                startValue = state.currentSpecies.mainFieldMultiplierDropAndWaste.toString(),
                title = "Смена множителя своей клетки",
                hint = "Задайте множитель",
                regexp = "^[0-9]*\$".toRegex()
            ) {
                it.toIntOrNull()?.let { multiplier ->
                    if (multiplier in 0..1000000)
                        mainFieldMultiplierDropAndWasteRelay.accept(multiplier)
                }
            }
        }

        renderNeedsList(state.currentSpecies.needMineralsForStayAlive.toUiMineralsList(state.config.minerals))
        renderDropList(state.currentSpecies.alwaysMineralsDropped.toUiMineralsList(state.config.minerals))
        renderSpeciesList(state.config.species.map { it.name })
    }

    private fun renderSpeciesList(species: List<String>) {
        if (observableSpecies.toList() != species) {
            observableSpecies.clear()
            observableSpecies.addAll(species)
        }
    }

    private fun renderNeedsList(needs: List<UiMineral>) {
        if (observableNeedMinerals.toList() != needs) {
            observableNeedMinerals.clear()
            observableNeedMinerals.addAll(needs)
        }
    }

    private fun renderDropList(drops: List<UiMineral>) {
        if (observableDropMinerals.toList() != drops) {
            observableDropMinerals.clear()
            observableDropMinerals.addAll(drops)
        }
    }

    private fun String.toColor(): Color? {
        if (length != 7 || first() != '#') return null
        val r = (substring(1, 3).toIntOrNull(16)?.toDouble()?.div(255)) ?: return null
        val g = (substring(3, 5).toIntOrNull(16)?.toDouble()?.div(255)) ?: return null
        val b = (substring(5, 7).toIntOrNull(16)?.toDouble()?.div(255)) ?: return null
        return Color.color(r, g, b)
    }

    private fun Color.stringColor(): String {
        val r = (red * 255).toInt().toString(16).leadingNullIfNeeded()
        val g = (green * 255).toInt().toString(16).leadingNullIfNeeded()
        val b = (blue * 255).toInt().toString(16).leadingNullIfNeeded()
        return "#$r$g$b"
    }

    private fun String.leadingNullIfNeeded() = if (length == 1) "0$this" else this

    private fun dialog(startValue: String, title: String, hint: String, regexp: Regex, onChange: (String) -> Unit) {
        val dialog = TextInputDialog(startValue)
        dialog.title = title
        dialog.contentText = hint
        dialog.editor.textFormatter = TextFormatter<TextFormatter.Change>(UnaryOperator { change ->
            if (change.text.matches(regexp)) change
            else null
        })
        dialog.showAndWait().ifPresent { onChange(it) }
    }

    private fun Map<Int, Int>.toUiMineralsList(minerals: List<Mineral>): List<UiMineral> = toList()
        .map { UiMineral(minerals[it.first].name, it.first, it.second) }.sortedBy { it.id }

    private data class UiMineral(
        val name: String,
        val id: Int,
        val number: Int
    ) {
        override fun toString(): String = "$name $number"
    }
}