package org.example.ui.levels_screen

import org.example.life.Configuration
import org.example.life.Level

data class LevelScreenViewState(
    val config: Configuration,
    val levels: List<Level>,
    var currentLevelNumber: Int
) {
    val currentLevel: Level
        get() = levels.getOrNull(currentLevelNumber) ?: levels.first().also { currentLevelNumber = 0 }

}