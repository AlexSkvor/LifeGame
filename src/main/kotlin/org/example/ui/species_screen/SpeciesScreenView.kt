package org.example.ui.species_screen

import io.reactivex.Observable
import javafx.scene.paint.Color

interface SpeciesScreenView {

    fun createSpeciesIntent(): Observable<String>
    fun deleteSpeciesIntent(): Observable<Unit>

    fun speciesChosenIntent(): Observable<String>
    fun renameSpeciesIntent(): Observable<String>
    fun changeColorSpeciesIntent(): Observable<Color>
    fun newAlwaysDroppedSeedsChanceIntent(): Observable<Int>
    fun newMainFieldMultiplierDropAndWasteIntent(): Observable<Int>

    fun newMineralNeedIntent(): Observable<Pair<Int, Int>>
    fun newMineralDropIntent(): Observable<Pair<Int, Int>>

    fun render(state: SpeciesScreenViewState)
}