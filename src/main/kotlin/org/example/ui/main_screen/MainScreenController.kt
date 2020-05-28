package org.example.ui.main_screen

import com.github.thomasnield.rxkotlinfx.itemSelections
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import javafx.collections.FXCollections
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.ListView
import javafx.scene.control.TextFormatter
import javafx.scene.control.TextInputDialog
import org.example.App
import org.example.clicks
import org.example.doNothing
import org.example.life.Configuration
import org.example.ui.base.AppState
import org.example.ui.base.GlobalAction
import org.example.ui.base.UiController
import java.util.function.UnaryOperator

class MainScreenController : UiController(), MainScreenView {

    @FXML
    private lateinit var configurationsListView: ListView<String>

    @FXML
    private lateinit var chosenConfigurationName: Button

    @FXML
    private lateinit var deleteConfiguration: Button

    @FXML
    private lateinit var stonePercentButton: Button

    @FXML
    private lateinit var threadsNumButton: Button

    @FXML
    private lateinit var speciesPercentButton: Button

    @FXML
    private lateinit var neededMineralsMultiplierButton: Button

    @FXML
    private lateinit var onDeathMultiplierButton: Button

    @FXML
    private lateinit var startMineralsButton: Button

    @FXML
    private lateinit var seedsLifeTimeButton: Button

    @FXML
    private lateinit var mapSizeButton: Button

    @FXML
    private lateinit var createNewButton: Button

    @FXML
    private lateinit var mineralsButton: Button

    @FXML
    private lateinit var speciesButton: Button

    @FXML
    private lateinit var startButton: Button

    @FXML
    private lateinit var timeButton: Button

    private lateinit var combiner: MainScreenCombiner

    override fun onViewCreated() {
        timeButton.text = "ЗАМЕРЫ"
        combiner = MainScreenCombiner(this)
        configurationsListView.items = observableConfigurations
    }

    override fun onClose() {
        if (::combiner.isInitialized) combiner.clear()
    }

    override fun onGlobalAction(it: GlobalAction) = doNothing()

    private val threadsRelay = PublishRelay.create<Int>()
    override fun newThreadsIntent(): Observable<Int> = threadsRelay.hide()

    override fun selectConfigIntent(): Observable<String> = configurationsListView.itemSelections

    override fun deleteCurrentConfigIntent(): Observable<Unit> = deleteConfiguration.clicks()

    private val newNameRelay = PublishRelay.create<String>()
    override fun newConfigNameIntent(): Observable<String> = newNameRelay.hide()

    private val stoneRelay = PublishRelay.create<Int>()
    override fun newStonePercentIntent(): Observable<Int> = stoneRelay.hide()

    private val speciesRelay = PublishRelay.create<Int>()
    override fun newSpeciesPercentIntent(): Observable<Int> = speciesRelay.hide()

    private val neededMineralsMultiplierRelay = PublishRelay.create<Int>()
    override fun newNeededMineralsMultiplierIntent(): Observable<Int> = neededMineralsMultiplierRelay.hide()

    private val newOnDeathMultiplierRelay = PublishRelay.create<Int>()
    override fun newOnDeathMultiplierIntent(): Observable<Int> = newOnDeathMultiplierRelay.hide()

    private val newStartMineralsRelay = PublishRelay.create<Int>()
    override fun newStartMineralsIntent(): Observable<Int> = newStartMineralsRelay.hide()

    private val newSeedsLifeTimeRelay = PublishRelay.create<Int>()
    override fun newSeedsLifeTimeIntent(): Observable<Int> = newSeedsLifeTimeRelay.hide()

    private val newSizeRelay = PublishRelay.create<Int>()
    override fun newSizeIntent(): Observable<Int> = newSizeRelay.hide()

    private val createNewConfigRelay = PublishRelay.create<String>()
    override fun createNewConfigIntent(): Observable<String> = createNewConfigRelay.hide()

    override fun render(state: MainScreenViewState) {
        renderConfigList(state.configsList, state.chosenConfiguration)

        mineralsButton.setOnMouseClicked {
            createChildStage("minerals_main_screen.fxml", "Минералы")
        }

        speciesButton.setOnMouseClicked {
            createChildStage("species_screen.fxml", "Формы Жизни")
        }

        startButton.setOnMouseClicked {
            App.state.gameStyle = AppState.Style.WATCH
            createChildStage("game_screen.fxml", "Игра \"Жизнь\"")
        }

        timeButton.setOnMouseClicked {
            openEditDialog("Количество итераций", "", "100", "^[0-9]*\$".toRegex()) {
                it.toIntOrNull()?.let { iters ->
                    if (iters > 0) {
                        App.state.iterations = iters
                        App.state.gameStyle = AppState.Style.TIME
                        createChildStage("game_screen.fxml", "Игра \"Жизнь\"")
                    }
                }
            }
        }

        state.chosenConfiguration.let {

            renderTextButton(
                threadsNumButton,
                "Использовать потоков:",
                "",
                "[0-9]*".toRegex(),
                it.threadsNum.toString()
            ) { newValue ->
                newValue.toIntOrNull()?.let { num ->
                    if (num in 1..32) threadsRelay.accept(num)
                }
            }

            renderTextButton(
                stonePercentButton,
                "Шанс появления камня на клетке при старте:",
                "%",
                "[0-9]*".toRegex(),
                it.stonePercent.toString()
            ) { newValue ->
                newValue.toIntOrNull()?.let { num ->
                    if (num in 0..100) stoneRelay.accept(num)
                }
            }

            renderTextButton(
                speciesPercentButton,
                "Шанс появления семени на клетке при старте:",
                "%",
                "[0-9]*".toRegex(),
                it.speciesPercent.toString()
            ) { newValue ->
                newValue.toIntOrNull()?.let { num ->
                    if (num in 1..100) speciesRelay.accept(num)
                }
            }

            renderTextButton(
                onDeathMultiplierButton, "Множитель при смерти:", "", "[0-9]*".toRegex(),
                it.onDeathMultiplier.toString()
            ) { newValue ->
                newValue.toIntOrNull()?.let { num ->
                    if (num in 1..10000) newOnDeathMultiplierRelay.accept(num)
                }
            }

            renderTextButton(
                startMineralsButton, "Стартовые ресурсы:", "шт", "[0-9]*".toRegex(),
                it.startMinerals.toString()
            ) { newValue ->
                newValue.toIntOrNull()?.let { num ->
                    if (num in 1..1000000) newStartMineralsRelay.accept(num)
                }
            }

            renderTextButton(
                seedsLifeTimeButton, "Продолжительность жизни семян:", "ходов", "[0-9]*".toRegex(),
                it.seedsLifeTime.toString()
            ) { newValue ->
                newValue.toIntOrNull()?.let { num ->
                    if (num in 0..1000000) newSeedsLifeTimeRelay.accept(num)
                }
            }

            renderTextButton(
                mapSizeButton, "Размер карты", "% от макс", "[0-9]*".toRegex(),
                it.mapSize.toString()
            ) { newValue ->
                newValue.toIntOrNull()?.let { num ->
                    if (num in 1..100) newSizeRelay.accept(num)
                }
            }

            renderTextButton(
                neededMineralsMultiplierButton,
                "Коэффициент достаточности:",
                "",
                "[0-9]*".toRegex(),
                it.neededMineralsMultiplier.toString()
            ) { newValue ->
                newValue.toIntOrNull()?.let { num ->
                    if (num in 0..10000) neededMineralsMultiplierRelay.accept(num)
                }
            }

            renderTextButton(
                chosenConfigurationName, "Конфигурация:", "", "^[а-яА-ЯёЁa-zA-Z0-9]*\$".toRegex(),
                it.fileName
            ) { newName ->
                if (newName.isNotBlank()) newNameRelay.accept(newName)
            }
        }

        createNewButton.setOnMouseClicked {
            openEditDialog("Имя новой конфигурации", "", "", "^[а-яА-ЯёЁa-zA-Z0-9]*\$".toRegex()) {
                if (it.isNotBlank() && !state.configsList.map { config -> config.fileName }.contains(it))
                    createNewConfigRelay.accept(it)
            }
        }
    }

    private fun renderTextButton(
        view: Button, hint: String, secondHint: String, regexp: Regex, value: String,
        onChange: (String) -> Unit
    ) {
        view.text = if (secondHint.isBlank()) "$hint $value"
        else "$hint $value ($secondHint)"
        view.setOnMouseClicked {
            openEditDialog(hint, secondHint, value, regexp, onChange)
        }
    }

    private fun openEditDialog(
        hint: String, secondHint: String, startValue: String, regexp: Regex, onChange: (String) -> Unit
    ) {
        val dialog = TextInputDialog(startValue)
        dialog.title = "Изменение значения"
        dialog.headerText = null
        dialog.contentText = if (secondHint.isNotBlank()) "$hint, $secondHint" else hint
        dialog.editor.textFormatter = TextFormatter<TextFormatter.Change>(UnaryOperator { change ->
            if (change.text.matches(regexp)) change
            else null
        })
        dialog.showAndWait().ifPresent { onChange(it) }
    }


    private val observableConfigurations = FXCollections.observableArrayList<String>()
    private fun renderConfigList(list: List<Configuration>, current: Configuration?) {
        if (observableConfigurations.toList() != list.map { it.fileName }.sortedBy { it }) {
            observableConfigurations.clear()
            observableConfigurations.addAll(list.map { it.fileName }.sortedBy { it })
        }
        current?.let {
            if (configurationsListView.selectionModel.selectedItem != it.fileName)
                configurationsListView.selectionModel.select(it.fileName)
        }
    }
}