package org.example.life

data class SpeciesInstance(
    val speciesId: Int,
    var state: State = State.DEAD,
    var nextState: State = State.DEAD,
    var currentLevelNumber: Int = 0
) {
    enum class State { ALIVE, LEVEL_UP, DEAD }
}