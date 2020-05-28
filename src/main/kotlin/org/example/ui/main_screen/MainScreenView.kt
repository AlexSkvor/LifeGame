package org.example.ui.main_screen

import io.reactivex.Observable

interface MainScreenView {

    fun selectConfigIntent(): Observable<String>
    fun deleteCurrentConfigIntent(): Observable<Unit>

    fun newConfigNameIntent(): Observable<String>
    fun newStonePercentIntent(): Observable<Int>
    fun newSpeciesPercentIntent(): Observable<Int>
    fun newNeededMineralsMultiplierIntent(): Observable<Int>
    fun newOnDeathMultiplierIntent(): Observable<Int>
    fun newStartMineralsIntent(): Observable<Int>
    fun newSeedsLifeTimeIntent(): Observable<Int>
    fun newSizeIntent(): Observable<Int>

    fun newThreadsIntent(): Observable<Int>

    fun createNewConfigIntent(): Observable<String>

    fun render(state: MainScreenViewState)
}