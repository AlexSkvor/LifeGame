package org.example.ui.species_screen

import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import io.reactivex.Observable.merge
import io.reactivex.functions.BiFunction
import org.example.App
import org.example.onNull
import org.example.ui.base.AppDisposables
import org.example.ui.base.DisposablesProvider

class SpeciesScreenCombiner(
    private val view: SpeciesScreenView
) : DisposablesProvider by AppDisposables() {

    private lateinit var lastState: SpeciesScreenViewState
    private val startRelay = PublishRelay.create<Unit>()

    private val reducer = BiFunction { oldState: SpeciesScreenViewState, it: SpeciesScreenPartialState ->
        when (it) {
            is SpeciesScreenPartialState.NewConfig -> oldState.copy(
                config = it.config,
                currentSpecies = it.config.species.firstOrNull { it.id == oldState.currentSpecies.id }
                    .onNull(it.config.species.first())
            )
            is SpeciesScreenPartialState.SpeciesChosen -> oldState.copy(currentSpecies = it.species)
            is SpeciesScreenPartialState.NewSpeciesCreated -> SpeciesScreenViewState(
                it.config,
                it.config.species.firstOrNull { s -> s.id == it.speciesId }.onNull(it.config.species.first())
            )
        }
    }

    private val loader = SpeciesLoader()

    init {
        val initialState = SpeciesScreenViewState(loader.getConfig())

        getActions().share().scan(initialState, reducer).distinctUntilChanged()
            .doOnNext { lastState = it }
            .subscribe { view.render(it) }.bind()
        startRelay.accept(Unit)
    }

    private fun getActions(): Observable<SpeciesScreenPartialState> {

        val chooseAction = view.speciesChosenIntent()
            .filter { !App.state.configurationInChangeProcess }
            .map { lastState.config.species.first { s -> s.name == it } }
            .map { SpeciesScreenPartialState.SpeciesChosen(it) }

        val renameAction = view.renameSpeciesIntent()
            .filter { !App.state.configurationInChangeProcess }
            .filter { !lastState.config.species.map { s -> s.name }.contains(it) }
            .flatMap { loader.renameSpecies(lastState.currentSpecies.id, it) }
            .map { SpeciesScreenPartialState.NewConfig(it) }

        val createAction = view.createSpeciesIntent()
            .filter {
                !App.state.configurationInChangeProcess &&
                        !lastState.config.species.map { s -> s.name }.contains(it) &&
                        lastState.config.species.size < App.MAX_SPECIES
            }
            .flatMap { loader.createSpecies(it) }
            .map { SpeciesScreenPartialState.NewSpeciesCreated(it, it.species.size - 1) }

        val deleteSpeciesAction = view.deleteSpeciesIntent()
            .filter {
                !App.state.configurationInChangeProcess &&
                        lastState.config.species.size != 1
            }.flatMap { loader.deleteSpecies(lastState.currentSpecies.name) }
            .map { SpeciesScreenPartialState.NewConfig(it) }

        val changeColorAction = view.changeColorSpeciesIntent()
            .filter { !App.state.configurationInChangeProcess }
            .flatMap { loader.changeColor(lastState.currentSpecies.id, it) }
            .map { SpeciesScreenPartialState.NewConfig(it) }

        val newDroppedSeedsChanceAction = view.newAlwaysDroppedSeedsChanceIntent()
            .filter { !App.state.configurationInChangeProcess }
            .flatMap { loader.changeDroppedChance(lastState.currentSpecies.id, it) }
            .map { SpeciesScreenPartialState.NewConfig(it) }

        val mainFiledAction = view.newMainFieldMultiplierDropAndWasteIntent()
            .filter { !App.state.configurationInChangeProcess }
            .flatMap { loader.changeMainFieldMultiplier(lastState.currentSpecies.id, it) }
            .map { SpeciesScreenPartialState.NewConfig(it) }

        val mineralNeedAction = view.newMineralNeedIntent()
            .filter { !App.state.configurationInChangeProcess }
            .flatMap { loader.changeMineralNeed(lastState.currentSpecies.id, it.first, it.second) }
            .map { SpeciesScreenPartialState.NewConfig(it) }

        val mineralDropAction = view.newMineralDropIntent()
            .filter { !App.state.configurationInChangeProcess }
            .flatMap { loader.changeMineralDrop(lastState.currentSpecies.id, it.first, it.second) }
            .map { SpeciesScreenPartialState.NewConfig(it) }


        val list = listOf(
            chooseAction, renameAction, createAction, deleteSpeciesAction, changeColorAction,
            newDroppedSeedsChanceAction, mainFiledAction, mineralNeedAction, mineralDropAction
        )
        return merge(list)
    }
}