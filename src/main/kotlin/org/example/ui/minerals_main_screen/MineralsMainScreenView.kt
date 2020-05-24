package org.example.ui.minerals_main_screen

import io.reactivex.Observable

interface MineralsMainScreenView {

    fun deleteMineralIntent(): Observable<String>
    fun createMineralIntent(): Observable<String>
    fun renameMineralIntent(): Observable<Pair<String, String>>

    fun render(state: MineralsMainScreenViewState)

}