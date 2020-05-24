package org.example.ui.minerals_main_screen

import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import io.reactivex.Observable.merge
import io.reactivex.functions.BiFunction
import org.example.App
import org.example.doNothing
import org.example.ui.base.AppDisposables
import org.example.ui.base.DisposablesProvider
import org.example.ui.main_screen.ConfigurationsLoader

class MineralsMainScreenCombiner(
    private val view: MineralsMainScreenView,
    private val configLoader: ConfigurationsLoader = ConfigurationsLoader(),
    private val mineralsLoader: MineralsLoader = MineralsLoader(configLoader)
) : DisposablesProvider by AppDisposables() {

    private lateinit var lastState: MineralsMainScreenViewState

    private val startRelay = PublishRelay.create<Unit>()

    private val reducer = BiFunction { oldState: MineralsMainScreenViewState, it: MineralsMainScreenPartialState ->
        when (it) {
            is MineralsMainScreenPartialState.NewConfigurationState -> oldState.copy(config = it.config)
        }
    }

    init {
        val initialState = MineralsMainScreenViewState(configLoader.getConfigByName(App.state.currentConfigurationName))
        val actions = getActions().share()
        subscribeActions(actions)

        actions.scan(initialState, reducer).distinctUntilChanged()
            .doOnNext { lastState = it }
            .subscribe { view.render(it) }.bind()
        startRelay.accept(Unit)
    }

    private fun subscribeActions(actions: Observable<MineralsMainScreenPartialState>) {
        actions.subscribe {
            when (it) {
                is MineralsMainScreenPartialState.NewConfigurationState -> doNothing()
            }
        }.bind()
    }

    private fun getActions(): Observable<MineralsMainScreenPartialState> {

        val renameAction = view.renameMineralIntent()
            .filter { !App.state.configurationInChangeProcess }
            .flatMap { mineralsLoader.renameMineral(oldName = it.first, newName = it.second) }
            .map { MineralsMainScreenPartialState.NewConfigurationState(it) }

        val deleteAction = view.deleteMineralIntent()
            .filter { !App.state.configurationInChangeProcess && lastState.config.minerals.size != 1 }
            .flatMap { mineralsLoader.deleteMineral(name = it) }
            .map { MineralsMainScreenPartialState.NewConfigurationState(it) }

        val createAction = view.createMineralIntent()
            .filter { !App.state.configurationInChangeProcess }
            .flatMap { mineralsLoader.createMineral(name = it) }
            .map { MineralsMainScreenPartialState.NewConfigurationState(it) }

        val list = listOf(renameAction, deleteAction, createAction)
        return merge(list)
    }
}