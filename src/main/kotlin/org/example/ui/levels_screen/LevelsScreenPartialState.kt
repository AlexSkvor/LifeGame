package org.example.ui.levels_screen

import org.example.life.Configuration

sealed class LevelsScreenPartialState {
    class Config(val config: Configuration): LevelsScreenPartialState()
    class LevelChosen(val level: Int): LevelsScreenPartialState()
}