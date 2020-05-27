package org.example.ui.levels_screen

import io.reactivex.Observable

interface LevelsScreenView {

    fun addLevelIntent(): Observable<Unit>
    fun deleteLevelIntent(): Observable<Unit>

    fun selectLevelIntent(): Observable<Int>

    fun changeSeedsDropChanceIntent(): Observable<Int>

    fun newMineralNeedIntent(): Observable<Pair<Int, Int>>
    fun newMineralDropIntent(): Observable<Pair<Int, Int>>
    fun newMineralForUpdateIntent(): Observable<Pair<Int, Int>>

    fun render(state: LevelScreenViewState)
}