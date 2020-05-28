package org.example.ui.main_screen

import io.reactivex.Observable
import io.reactivex.Observable.merge
import io.reactivex.functions.BiFunction
import org.example.App
import org.example.doNothing
import org.example.onNull
import org.example.ui.base.AppDisposables
import org.example.ui.base.DisposablesProvider

class MainScreenCombiner(
    private val view: MainScreenView,
    private val configLoader: ConfigurationsLoader = ConfigurationsLoader()
) : DisposablesProvider by AppDisposables() {

    private lateinit var lastState: MainScreenViewState

    private val reducer = BiFunction { oldState: MainScreenViewState, it: MainScreenPartialState ->
        when (it) {
            is MainScreenPartialState.ConfigsList -> oldState.copy(
                configsList = it.list,
                chosenConfiguration = it.list.firstOrNull { c -> c.fileName == oldState.chosenConfiguration.fileName }
                    .onNull(it.list.first())
            )
            is MainScreenPartialState.ConfigChosen -> oldState.copy(
                chosenConfiguration = it.config
            )
        }
    }

    init {
        val initialState = MainScreenViewState(configLoader.getConfigsListBlocking())
        val actions = getActions().share()
        subscribeActions(actions)

        actions.scan(initialState, reducer).distinctUntilChanged()
            .doOnNext { lastState = it }
            .subscribe { view.render(it) }.bind()
    }

    private fun subscribeActions(actions: Observable<MainScreenPartialState>) {
        actions.subscribe {
            when (it) {
                is MainScreenPartialState.ConfigsList -> doNothing()
                is MainScreenPartialState.ConfigChosen -> App.state.currentConfigurationName = it.config.fileName
            }
        }.bind()
    }

    private fun getActions(): Observable<MainScreenPartialState> {

        val selectAction = view.selectConfigIntent()
            .map { lastState.configsList.first { item -> item.fileName == it } }
            .map { MainScreenPartialState.ConfigChosen(it) }

        val deleteAction = view.deleteCurrentConfigIntent()
            .filter { lastState.configsList.size != 1 }
            .switchMap { configLoader.deleteConfig(lastState.chosenConfiguration) }
            .map { MainScreenPartialState.ConfigsList(it) }

        val newConfigNameAction = view.newConfigNameIntent()
            .flatMap { newName -> configLoader.changeAttr { it.copy(fileName = newName) } }
            .map { MainScreenPartialState.ConfigsList(it) }

        val newStonePercentAction = view.newStonePercentIntent()
            .flatMap { newValue -> configLoader.changeAttr { it.copy(stonePercent = newValue) } }
            .map { MainScreenPartialState.ConfigsList(it) }

        val newSpeciesPercentAction = view.newSpeciesPercentIntent()
            .flatMap { newValue -> configLoader.changeAttr { it.copy(speciesPercent = newValue) } }
            .map { MainScreenPartialState.ConfigsList(it) }

        val newNeededMineralsMultiplierAction = view.newNeededMineralsMultiplierIntent()
            .flatMap { newValue -> configLoader.changeAttr { it.copy(neededMineralsMultiplier = newValue) } }
            .map { MainScreenPartialState.ConfigsList(it) }

        val newOnDeathMultiplierAction = view.newOnDeathMultiplierIntent()
            .flatMap { newValue -> configLoader.changeAttr { it.copy(onDeathMultiplier = newValue) } }
            .map { MainScreenPartialState.ConfigsList(it) }

        val newStartMineralsAction = view.newStartMineralsIntent()
            .flatMap { newValue -> configLoader.changeAttr { it.copy(startMinerals = newValue) } }
            .map { MainScreenPartialState.ConfigsList(it) }

        val newSeedsLifeTimeAction = view.newSeedsLifeTimeIntent()
            .flatMap { newValue -> configLoader.changeAttr { it.copy(seedsLifeTime = newValue) } }
            .map { MainScreenPartialState.ConfigsList(it) }

        val newSizeAction = view.newSizeIntent()
            .flatMap { newValue -> configLoader.changeAttr { it.newWithSize(newValue) } }
            .map { MainScreenPartialState.ConfigsList(it) }

        val createNewConfigAction = view.createNewConfigIntent()
            .flatMap { configLoader.createNewConfig(it) }
            .map { MainScreenPartialState.ConfigsList(it) }

        val newThreadsNumAction = view.newThreadsIntent()
            .flatMap { newValue -> configLoader.changeAttr { it.copy(threadsNum = newValue) } }
            .map { MainScreenPartialState.ConfigsList(it) }

        val list = listOf(
            selectAction,
            deleteAction,
            newConfigNameAction,
            newStonePercentAction,
            newSpeciesPercentAction,
            newNeededMineralsMultiplierAction,
            newOnDeathMultiplierAction,
            newStartMineralsAction,
            newSeedsLifeTimeAction,
            newSizeAction,
            createNewConfigAction,
            newThreadsNumAction
        )
        return merge(list)
    }

}