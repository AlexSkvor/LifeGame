package org.example.ui.main_screen

import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import io.reactivex.Observable.merge
import io.reactivex.functions.BiFunction
import org.example.App
import org.example.doNothing
import org.example.ui.base.AppDisposables
import org.example.ui.base.DisposablesProvider

class MainScreenCombiner(
    private val view: MainScreenView,
    private val configLoader: ConfigurationsLoader = ConfigurationsLoader()
) : DisposablesProvider by AppDisposables() {

    private var lastState = MainScreenViewState()

    private val reducer = BiFunction { oldState: MainScreenViewState, it: MainScreenPartialState ->
        when (it) {
            is MainScreenPartialState.ConfigsList -> oldState.copy(
                configsList = it.list,
                chosenConfiguration = it.list.first(),
                changedChosenConfiguration = it.list.first(),
                changeStarted = false
            )
            is MainScreenPartialState.ConfigChosen -> oldState.copy(
                chosenConfiguration = it.config,
                changedChosenConfiguration = it.config.copy()
            )
            MainScreenPartialState.StartEdit -> oldState.copy(changeStarted = true)
            is MainScreenPartialState.ConfigsListAfterSaving -> MainScreenViewState(
                configsList = it.list, //TODO save chosen! or renew after creating!
                chosenConfiguration = it.list.first(),
                changedChosenConfiguration = it.list.first(),
                changeStarted = false
            )
            MainScreenPartialState.StopEdit -> oldState.copy(
                changedChosenConfiguration = oldState.chosenConfiguration,
                changeStarted = false
            )
            is MainScreenPartialState.ConfigsListAfterDeleting -> oldState.copy(
                configsList = it.list,
                chosenConfiguration = it.list.first(),
                changedChosenConfiguration = it.list.first(),
                changeStarted = false
            )
            is MainScreenPartialState.NewConfigName -> oldState.copy(
                changedChosenConfiguration = oldState.changedChosenConfiguration?.copy(
                    fileName = it.name
                )
            )
            is MainScreenPartialState.NewStonePercent -> oldState.copy(
                changedChosenConfiguration = oldState.changedChosenConfiguration?.copy(
                    stonePercent = it.percent
                )
            )
            is MainScreenPartialState.NewSpeciesPercent -> oldState.copy(
                changedChosenConfiguration = oldState.changedChosenConfiguration?.copy(
                    speciesPercent = it.percent
                )
            )
            is MainScreenPartialState.NewNeededMineralsMultiplier -> oldState.copy(
                changedChosenConfiguration = oldState.changedChosenConfiguration?.copy(
                    neededMineralsMultiplier = it.multiplier
                )
            )
            is MainScreenPartialState.NewOnDeathMultiplier -> oldState.copy(
                changedChosenConfiguration = oldState.changedChosenConfiguration?.copy(
                    onDeathMultiplier = it.multiplier
                )
            )
            is MainScreenPartialState.NewStartMinerals -> oldState.copy(
                changedChosenConfiguration = oldState.changedChosenConfiguration?.copy(
                    startMinerals = it.minerals
                )
            )
            is MainScreenPartialState.NewSeedsLifeTime -> oldState.copy(
                changedChosenConfiguration = oldState.changedChosenConfiguration?.copy(
                    seedsLifeTime = it.time
                )
            )
            is MainScreenPartialState.NewMapSize -> oldState.copy(
                changedChosenConfiguration = oldState.changedChosenConfiguration?.copy(
                    width = 12 * it.size,
                    height = 8 * it.size
                )
            )
        }
    }

    private val startRelay = PublishRelay.create<Unit>()

    init {
        val initialState = MainScreenViewState()
        val actions = getActions().share()
        subscribeActions(actions)

        actions.scan(initialState, reducer).distinctUntilChanged()
            .doOnNext { lastState = it }
            .subscribe { view.render(it) }.bind()
        startRelay.accept(Unit)
    }

    private fun subscribeActions(actions: Observable<MainScreenPartialState>) {
        actions.subscribe {
            when (it) {
                is MainScreenPartialState.ConfigsList -> doNothing()
                is MainScreenPartialState.ConfigChosen -> doNothing()
                is MainScreenPartialState.ConfigsListAfterSaving -> App.state.configurationInChangeProcess = false
                MainScreenPartialState.StopEdit -> App.state.configurationInChangeProcess = false
                MainScreenPartialState.StartEdit -> App.state.configurationInChangeProcess = true
                is MainScreenPartialState.ConfigsListAfterDeleting -> doNothing()
                is MainScreenPartialState.NewConfigName -> doNothing()
                is MainScreenPartialState.NewStonePercent -> doNothing()
                is MainScreenPartialState.NewSpeciesPercent -> doNothing()
                is MainScreenPartialState.NewNeededMineralsMultiplier -> doNothing()
                is MainScreenPartialState.NewOnDeathMultiplier -> doNothing()
                is MainScreenPartialState.NewStartMinerals -> doNothing()
                is MainScreenPartialState.NewSeedsLifeTime -> doNothing()
                is MainScreenPartialState.NewMapSize -> doNothing()
            }
        }.bind()
    }

    private fun getActions(): Observable<MainScreenPartialState> {

        val initialLoadAction = startRelay.hide()
            .flatMap { configLoader.getConfigsList() }
            .map { MainScreenPartialState.ConfigsList(it) }

        val selectAction = view.selectConfigIntent()
            .filter { !lastState.changeStarted }
            .map { lastState.configsList.first { item -> item.fileName.substringBeforeLast('.') == it } }
            .map { MainScreenPartialState.ConfigChosen(it) }

        val startEditCurrentConfigAction = view.startEditCurrentConfigIntent()
            .filter { !App.state.configurationInChangeProcess }
            .map { MainScreenPartialState.StartEdit }

        val saveCurrentConfigAction = view.saveCurrentConfigIntent()
            .filter { lastState.changeStarted }
            .switchMap { configLoader.saveConfig(lastState.chosenConfiguration?.fileName, lastState.changedChosenConfiguration) }
            .map { MainScreenPartialState.ConfigsListAfterSaving(it) }

        val cancelChangingAction = view.cancelChangingIntent()
            .filter { lastState.changeStarted }
            .map { MainScreenPartialState.StopEdit }

        val deleteAction = view.deleteCurrentConfigIntent()
            .filter { !lastState.changeStarted && lastState.configsList.size != 1 && !App.state.configurationInChangeProcess }
            .switchMap { configLoader.deleteConfig(lastState.chosenConfiguration) }
            .map { MainScreenPartialState.ConfigsListAfterDeleting(it) }

        val newConfigNameAction = view.newConfigNameIntent()
            .filter { lastState.changeStarted }
            .map { MainScreenPartialState.NewConfigName(it) }

        val newStonePercentAction = view.newStonePercentIntent()
            .filter { lastState.changeStarted }
            .map { MainScreenPartialState.NewStonePercent(it) }

        val newSpeciesPercentAction = view.newSpeciesPercentIntent()
            .filter { lastState.changeStarted }
            .map { MainScreenPartialState.NewSpeciesPercent(it) }

        val newNeededMineralsMultiplierAction = view.newNeededMineralsMultiplierIntent()
            .filter { lastState.changeStarted }
            .map { MainScreenPartialState.NewNeededMineralsMultiplier(it) }

        val newOnDeathMultiplierAction = view.newOnDeathMultiplierIntent()
            .filter { lastState.changeStarted }
            .map { MainScreenPartialState.NewOnDeathMultiplier(it) }

        val newStartMineralsAction = view.newStartMineralsIntent()
            .filter { lastState.changeStarted }
            .map { MainScreenPartialState.NewStartMinerals(it) }

        val newSeedsLifeTimeAction = view.newSeedsLifeTimeIntent()
            .filter { lastState.changeStarted }
            .map { MainScreenPartialState.NewSeedsLifeTime(it) }

        val newSizeAction = view.newSizeIntent()
            .filter { lastState.changeStarted }
            .map { MainScreenPartialState.NewMapSize(it) }

        val createNewConfigAction = view.createNewConfigIntent()
            .filter { !lastState.changeStarted && !App.state.configurationInChangeProcess }
            .flatMap { configLoader.createNewConfig(it) }
            .map { MainScreenPartialState.ConfigsListAfterSaving(it) }

        val list = listOf(
            initialLoadAction,
            selectAction,
            startEditCurrentConfigAction,
            saveCurrentConfigAction,
            cancelChangingAction,
            deleteAction,
            newConfigNameAction,
            newStonePercentAction,
            newSpeciesPercentAction,
            newNeededMineralsMultiplierAction,
            newOnDeathMultiplierAction,
            newStartMineralsAction,
            newSeedsLifeTimeAction,
            newSizeAction,
            createNewConfigAction
        )
        return merge(list)
    }

}