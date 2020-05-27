package org.example.ui.levels_screen

import io.reactivex.Observable
import io.reactivex.Observable.merge
import io.reactivex.functions.BiFunction
import org.example.App
import org.example.life.Configuration
import org.example.onNull
import org.example.ui.base.AppDisposables
import org.example.ui.base.DisposablesProvider

class LevelsScreenCombiner(
    private val view: LevelsScreenView
) : DisposablesProvider by AppDisposables() {

    private lateinit var lastState: LevelScreenViewState

    private val reducer = BiFunction { oldState: LevelScreenViewState, it: LevelsScreenPartialState ->
        when (it) {
            is LevelsScreenPartialState.Config -> fromOldStateAndConfig(oldState, it.config)
            is LevelsScreenPartialState.LevelChosen -> oldState.copy(currentLevelNumber = it.level)
        }
    }

    private fun fromOldStateAndConfig(oldState: LevelScreenViewState?, config: Configuration) =
        LevelScreenViewState(
            config = config,
            levels = config.species.first { s -> s.id == App.state.currentSpeciesId }.levels,
            currentLevelNumber = oldState?.currentLevelNumber.onNull(0)
        )

    private val loader = LevelsLoader()

    init {
        val initialState = fromOldStateAndConfig(null, loader.getConfig())

        getActions().share().scan(initialState, reducer).distinctUntilChanged()
            .doOnNext { lastState = it }
            .subscribe { view.render(it) }.bind()
    }

    private fun getActions(): Observable<LevelsScreenPartialState> {

        val chosenLevelAction = view.selectLevelIntent()
            .map { LevelsScreenPartialState.LevelChosen(it) }

        val addLevelAction = view.addLevelIntent()
            .map { loader.addLevel() }
            .map { LevelsScreenPartialState.Config(it) }

        val deleteLevelAction = view.deleteLevelIntent()
            .map { loader.deleteLevel(lastState.currentLevelNumber) }
            .map { LevelsScreenPartialState.Config(it) }

        val dropSeedsChanceAction = view.changeSeedsDropChanceIntent()
            .map { loader.changeSeedsDropChance(lastState.currentLevelNumber, it) }
            .map { LevelsScreenPartialState.Config(it) }

        val mineralNeedAction = view.newMineralNeedIntent()
            .map { loader.changeMineralNeed(lastState.currentLevelNumber, it.first, it.second) }
            .map { LevelsScreenPartialState.Config(it) }

        val mineralDropAction = view.newMineralDropIntent()
            .map { loader.changeMineralDrop(lastState.currentLevelNumber, it.first, it.second) }
            .map { LevelsScreenPartialState.Config(it) }

        val mineralUpdateAction = view.newMineralForUpdateIntent()
            .map { loader.changeMineralUpdate(lastState.currentLevelNumber, it.first, it.second) }
            .map { LevelsScreenPartialState.Config(it) }

        val list = listOf(
            chosenLevelAction, addLevelAction, deleteLevelAction, dropSeedsChanceAction, mineralNeedAction,
            mineralDropAction, mineralUpdateAction
        )
        return merge(list)
    }
}